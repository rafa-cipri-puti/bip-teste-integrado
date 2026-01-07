package com.example.ejb.exception;

public class InvalidTransferException extends BusinessException {
    public InvalidTransferException(String message) {
        super(message);
    }
}
