package org.tajiro.market.client;

public class MarketApiException extends RuntimeException {
    //실거래가 API 호출/파싱 중 문제 발생 예외 처리
    public MarketApiException(String message) {
        super(message);
    }

    public MarketApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
