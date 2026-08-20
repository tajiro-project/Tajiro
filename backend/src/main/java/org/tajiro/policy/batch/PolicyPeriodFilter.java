package org.tajiro.policy.batch;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PolicyPeriodFilter {

    private static final Pattern PERIOD_PATTERN =
            Pattern.compile("(\\d{8})\\s*~\\s*(\\d{8})");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");


    public boolean isActive(
            String aplyYmd,
            LocalDate today
    ) {

        /*
         * 신청기간이 없으면
         * 상시/별도문의 정책일 수 있으므로 살려둔다.
         */
        if (!hasText(aplyYmd)) {
            return true;
        }

        Matcher matcher =
                PERIOD_PATTERN.matcher(aplyYmd);

        boolean foundRange = false;

        while (matcher.find()) {

            foundRange = true;

            try {

                LocalDate endDate =
                        LocalDate.parse(
                                matcher.group(2),
                                DATE_FORMATTER
                        );

                /*
                 * 종료일 당일까지 유효
                 */
                if (!endDate.isBefore(today)) {
                    return true;
                }

            } catch (DateTimeParseException e) {

                /*
                 * 형식 이상하다고 정책을 날려버리면 위험하므로
                 * 일단 유지
                 */
                return true;
            }
        }

        /*
         * 날짜 범위를 찾았는데
         * 전부 종료되었으면 false
         *
         * 날짜 형식 자체가 없는 텍스트 값이면 true
         */
        return !foundRange;
    }


    private static boolean hasText(String value) {

        return value != null
                && !value.trim().isEmpty();
    }
}