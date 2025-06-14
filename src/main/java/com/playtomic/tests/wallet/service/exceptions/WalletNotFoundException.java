package com.playtomic.tests.wallet.service.exceptions;

public class WalletNotFoundException extends RuntimeException {

    public  WalletNotFoundException() {
        super();
    }

    public WalletNotFoundException(String message) {
        super(message);
    }

}
