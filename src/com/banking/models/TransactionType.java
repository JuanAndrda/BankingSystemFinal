package com.banking.models;

public enum TransactionType {
    /** Money deposited into an account */
    DEPOSIT,

    /** Money withdrawn from an account */
    WITHDRAW,

    /** Money transferred between accounts */
    TRANSFER
}
