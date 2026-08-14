package org.tajiro.market.service;

import org.springframework.stereotype.Component;
import org.tajiro.market.domain.MarketComparableTransactionVO;
import org.tajiro.market.domain.MarketEvaluationResult;
import org.tajiro.market.domain.MarketEvaluationStatus;
import org.tajiro.market.domain.MarketPropertyVO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class MarketEvaluationCalculator {

    private static final int MINIMUM_TRANSACTION_COUNT = 3;
    private static final BigDecimal LEVEL_ONE_AREA_RANGE = BigDecimal.valueOf(5);
    private static final BigDecimal LEVEL_TWO_AREA_RANGE = BigDecimal.valueOf(10);
    private static final Pattern UMD_PATTERN =
            Pattern.compile("(?:^|\\s)([^\\s]+(?:동|읍|면))(?:\\s|$)");

    public Optional<MarketEvaluationResult> calculate(
            MarketPropertyVO property,
            BigDecimal listingPrice,
            List<MarketComparableTransactionVO> transactions,
            LocalDate referenceDate) {
        if (property == null
                || property.getAreaM2() == null
                || listingPrice == null
                || listingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        LocalDate twelveMonthsAgo = referenceDate.minusMonths(12);
        LocalDate twentyFourMonthsAgo = referenceDate.minusMonths(24);
        Predicate<MarketComparableTransactionVO> sameBuilding =
                transaction -> sameBuilding(property, transaction);
        Predicate<MarketComparableTransactionVO> sameLegalDong =
                transaction -> sameLegalDong(property, transaction);

        Optional<MarketEvaluationResult> levelOne = resultFor(
                property,
                listingPrice,
                transactions,
                transaction -> sameBuilding.test(transaction)
                        && onOrAfter(transaction, twelveMonthsAgo)
                        && withinArea(property, transaction, LEVEL_ONE_AREA_RANGE),
                1);
        if (levelOne.isPresent()) {
            return levelOne;
        }

        Optional<MarketEvaluationResult> levelTwo = resultFor(
                property,
                listingPrice,
                transactions,
                transaction -> sameBuilding.test(transaction)
                        && onOrAfter(transaction, twentyFourMonthsAgo)
                        && withinArea(property, transaction, LEVEL_TWO_AREA_RANGE),
                2);
        if (levelTwo.isPresent()) {
            return levelTwo;
        }

        return resultFor(
                property,
                listingPrice,
                transactions,
                transaction -> sameLegalDong.test(transaction)
                        && onOrAfter(transaction, twentyFourMonthsAgo)
                        && withinArea(property, transaction, LEVEL_TWO_AREA_RANGE),
                3);
    }

    private Optional<MarketEvaluationResult> resultFor(
            MarketPropertyVO property,
            BigDecimal listingPrice,
            List<MarketComparableTransactionVO> transactions,
            Predicate<MarketComparableTransactionVO> predicate,
            int basisLevel) {
        List<BigDecimal> prices = transactions.stream()
                .filter(predicate)
                .map(MarketComparableTransactionVO::getConvertedPrice)
                .filter(price -> price != null && price.compareTo(BigDecimal.ZERO) > 0)
                .sorted()
                .collect(Collectors.toList());

        if (prices.size() < MINIMUM_TRANSACTION_COUNT) {
            return Optional.empty();
        }

        BigDecimal median = median(prices);
        BigDecimal evaluationScore = listingPrice.subtract(median)
                .divide(median, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        return Optional.of(MarketEvaluationResult.builder()
                .propertyId(property.getId())
                .status(MarketEvaluationStatus.CALCULATED)
                .evaluationScore(evaluationScore)
                .referencePrice(median.setScale(2, RoundingMode.HALF_UP))
                .transactionCount(prices.size())
                .basisLevel(basisLevel)
                .preserveExistingScore(false)
                .build());
    }

    BigDecimal median(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("중앙값을 계산할 값이 없습니다.");
        }
        List<BigDecimal> sorted = new ArrayList<>(values);
        sorted.sort(Comparator.naturalOrder());
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 1) {
            return sorted.get(middle);
        }
        return sorted.get(middle - 1)
                .add(sorted.get(middle))
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private boolean onOrAfter(
            MarketComparableTransactionVO transaction,
            LocalDate boundary) {
        return transaction.getDealDate() != null
                && !transaction.getDealDate().isBefore(boundary);
    }

    private boolean withinArea(
            MarketPropertyVO property,
            MarketComparableTransactionVO transaction,
            BigDecimal allowedDifference) {
        BigDecimal transactionArea = transaction.comparableArea();
        return transactionArea != null
                && property.getAreaM2().subtract(transactionArea).abs()
                .compareTo(allowedDifference) <= 0;
    }

    private boolean sameBuilding(
            MarketPropertyVO property,
            MarketComparableTransactionVO transaction) {
        if (!sameLegalDong(property, transaction)) {
            return false;
        }

        String propertyJibun = normalizeJibun(jibunFromPnu(property.getPnu()));
        String transactionJibun = normalizeJibun(transaction.getJibun());
        if (!propertyJibun.isEmpty()
                && !transactionJibun.isEmpty()
                && propertyJibun.equals(transactionJibun)) {
            return true;
        }

        String propertyName = normalizeBuildingName(property.getBuildingName());
        String transactionName = normalizeBuildingName(transaction.getBuildingName());
        return !propertyName.isEmpty()
                && !transactionName.isEmpty()
                && propertyName.equals(transactionName);
    }

    private boolean sameLegalDong(
            MarketPropertyVO property,
            MarketComparableTransactionVO transaction) {
        String propertyCode = legalDongCodeFromPnu(property.getPnu());
        String transactionCode = normalize(transaction.getLegalDongCode());
        if (!propertyCode.isEmpty() && !transactionCode.isEmpty()) {
            return propertyCode.equals(transactionCode);
        }

        String propertyUmd = normalize(extractUmd(firstNonBlank(
                property.getJibunAddress(),
                property.getAddress(),
                property.getRoadAddress())));
        String transactionUmd = normalize(transaction.getUmdName());
        return !propertyUmd.isEmpty()
                && !transactionUmd.isEmpty()
                && propertyUmd.equals(transactionUmd);
    }

    private String legalDongCodeFromPnu(String pnu) {
        String digits = digitsOnly(pnu);
        return digits.length() == 19 ? digits.substring(5, 10) : "";
    }

    private String jibunFromPnu(String pnu) {
        String digits = digitsOnly(pnu);
        if (digits.length() != 19) {
            return "";
        }
        boolean mountain = digits.charAt(10) == '2';
        int main = Integer.parseInt(digits.substring(11, 15));
        int sub = Integer.parseInt(digits.substring(15, 19));
        return (mountain ? "산" : "") + main + (sub == 0 ? "" : "-" + sub);
    }

    private String extractUmd(String address) {
        if (address == null) {
            return "";
        }
        Matcher matcher = UMD_PATTERN.matcher(address + " ");
        return matcher.find() ? matcher.group(1) : "";
    }

    private String normalizeBuildingName(String value) {
        return normalize(value)
                .replace("아파트", "")
                .replace("오피스텔", "");
    }

    private String normalizeJibun(String value) {
        if (value == null) {
            return "";
        }
        String compact = value.trim().replace(" ", "");
        boolean mountain = compact.startsWith("산");
        compact = compact.replaceFirst("^산", "");
        String[] parts = compact.split("-", -1);
        try {
            int main = Integer.parseInt(parts[0]);
            int sub = parts.length > 1 && !parts[1].isEmpty()
                    ? Integer.parseInt(parts[1])
                    : 0;
            return (mountain ? "산" : "") + main + (sub == 0 ? "" : "-" + sub);
        } catch (NumberFormatException e) {
            return normalize(value);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^0-9a-z가-힣]", "");
    }

    private String digitsOnly(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return "";
    }
}
