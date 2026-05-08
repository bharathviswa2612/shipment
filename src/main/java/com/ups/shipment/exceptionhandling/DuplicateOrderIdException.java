package com.ups.shipment.exceptionhandling;

public class DuplicateOrderIdException extends RuntimeException {
    public DuplicateOrderIdException(String orderId) {
        super("Shipment with orderId " + orderId + " already exists");
    }
}
