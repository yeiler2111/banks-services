package com.nexusbank.account.domain.model;

/** Estado operativo de la cuenta. Una cuenta LOCKED no admite débitos ni créditos. */
public enum AccountStatus {
    ACTIVE,
    LOCKED
}
