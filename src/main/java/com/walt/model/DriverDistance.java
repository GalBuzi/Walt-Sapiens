package com.walt.model;

public interface DriverDistance {
    Driver getDriver();
//    Long getTotalDistance(); // error in definition of Type, in Delivery Object the distance is double
    Double getTotalDistance();
}
