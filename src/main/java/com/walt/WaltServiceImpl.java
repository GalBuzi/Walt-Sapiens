package com.walt;

import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.model.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
public class WaltServiceImpl implements WaltService {

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) {

        // check for valid params
        if (customer == null || restaurant == null || deliveryTime == null){
            WaltApplication.getLog().error("ERROR: one or more parameters is null and therefore new delivery NOT created");
            WaltApplication.getLog().info("new delivery was NOT created");
            throw new RuntimeException("ERROR: one or more parameters is null and therefore new delivery NOT created");
        }

        //check customer and restaurant in same city
        if (!customer.getCity().getId().equals(restaurant.getCity().getId())){
            WaltApplication.getLog().error("ERROR: customer ordered from restaurant outside the city!");
            WaltApplication.getLog().info("customer ordered from restaurant outside the city hence new delivery was NOT created");
            throw new RuntimeException("ERROR: customer ordered from restaurant outside the city!");
        }

        // find available driver and then assign him to the new delivery
        try{
            Driver availableDriver = findAvailableDriver(restaurant, deliveryTime);
            if (availableDriver != null){
                Delivery newDelivery = new Delivery(availableDriver, restaurant, customer, deliveryTime);
                deliveryRepository.save(newDelivery);
                WaltApplication.getLog().info("new delivery was created successfully");
                return newDelivery;
            }
            else {
                throw new RuntimeException("ERROR: no available driver!");
            }
        }catch (RuntimeException e){
            e.printStackTrace();
            WaltApplication.getLog().error("ERROR: no available driver!");
            WaltApplication.getLog().info("new delivery was NOT created");
            return null;
        }
    }

    private Driver findAvailableDriver(Restaurant restaurant, Date deliveryTime) {

        // go over all drivers in the city and find for each one if he is available to take new delivery
        List<Driver> availableDriversQuery = deliveryRepository.findAvailableDrivers(restaurant.getCity(), deliveryTime);

        if (availableDriversQuery.isEmpty())
            return null;

        return availableDriversQuery.get(0);
    }

    @Override
    public List<DriverDistance> getDriverRankReport() {
        return deliveryRepository.findTotalDistancePerDriver();
    }

    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        return deliveryRepository.findTotalDistancePerDriverByCity(city);
    }
}
