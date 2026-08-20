package org.tajiro.market.client;

import org.tajiro.market.domain.ActualTransactionVO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class TransactionFingerprint {

    private TransactionFingerprint() {
    }

    static String create(ActualTransactionVO transaction) {
        String canonical = String.join("|",
                value(transaction.getSourceApi()),
                value(transaction.getSggCode()),
                value(transaction.getLegalDongCode()),
                value(transaction.getUmdName()),
                value(transaction.getJibun()),
                value(transaction.getBuildingName()),
                value(transaction.getSourceBuildingCode()),
                value(transaction.getBuildingDong()),
                value(transaction.getHouseType()),
                value(transaction.getExclusiveAreaM2()),
                value(transaction.getTotalFloorAreaM2()),
                value(transaction.getLandAreaM2()),
                value(transaction.getFloor()),
                value(transaction.getBuildYear()),
                value(transaction.getDealDate()),
                value(transaction.getDealAmount()),
                value(transaction.getDeposit()),
                value(transaction.getMonthlyRent()));
        return sha256(canonical);
    }

    static String withOccurrence(String baseFingerprint, int occurrence) {
        if (occurrence < 1) {
            throw new IllegalArgumentException("거래 발생 순번은 1 이상이어야 합니다.");
        }
        return sha256(baseFingerprint + "|occurrence=" + occurrence);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte digestByte : digest) {
                result.append(String.format("%02x", digestByte));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    private static String value(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
