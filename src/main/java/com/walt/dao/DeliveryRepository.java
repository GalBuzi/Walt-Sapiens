package com.walt.dao;

import com.walt.model.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DeliveryRepository extends CrudRepository<Delivery, Long> {

    List<Delivery> findDeliveryByDriver(Driver driver); // == select * from Delivery d where d.driver = driver

    List<Delivery> findAllDeliveryByDriverAndDeliveryTime( Driver driver, Date deliveryTime); // == select * from Delivery d where d.driver = driver and d.deliveryTime = delivery time

    @Query("select delivery.driver AS driver, sum(delivery.distance) AS totalDistance from Delivery delivery group by driver order by totalDistance desc")
    List<DriverDistance> findTotalDistancePerDriver();

    @Query("select delivery.driver AS driver, sum(delivery.distance) AS totalDistance from Delivery delivery where delivery.driver.city =:city group by driver order by totalDistance desc")
    List<DriverDistance> findTotalDistancePerDriverByCity(@Param("city")City city);

    @Query("select d.driver as driver, count(d.id) as numDelveries from Delivery d where d.driver.city =:city and d.deliveryTime <>:delTime group by driver order by numDelveries asc")
    List<Driver> findAvailableDrivers(@Param("city")City city, @Param("delTime") Date delTime);


}


