package com.ups.shipment.exceptionhandling;

public class ShipmentNotFoundException extends RuntimeException {
    public ShipmentNotFoundException(String message) { super(message); }
}