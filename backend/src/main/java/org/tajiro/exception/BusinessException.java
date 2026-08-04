package org.tajiro.exception;

import lombok.Getter;
import org.tajiro.common.api.ErrorCode;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode responseCode;

    public BusinessException(ErrorCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }
}