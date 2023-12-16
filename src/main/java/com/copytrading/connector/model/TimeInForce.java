package com.copytrading.connector.model;

public enum TimeInForce {
    GTC, // good till cancel
    IOC, // immediate or cancel
    FOK, // fill or kill
    GTX, // good till crossing (post only)
    GTD; // good till date
}
