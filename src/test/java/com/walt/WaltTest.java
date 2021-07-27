package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.assertj.core.util.Lists;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData(){

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);

        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa);

        createRestaurant(jerusalem, tlv);
    }

    private void createRestaurant(City jerusalem, City tlv) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("restaurant", tlv, "mexican restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);

        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata));
    }

    @Test
    public void testBasics(){

        assertEquals(((List<City>) cityRepository.findAll()).size(),4);
        assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
    }


    /**
     * check for invalid Customer and Restaurant params and return null
     */
    @Test
    public void nullCustomerAndRestaurantParamTest() throws RuntimeException{
        //check for invalid inputs
        try{
            waltService.createOrderAndAssignDriver(null,null,new Date());
        }
        catch (RuntimeException e){
            e.printStackTrace();
        }
    }

    /**
     * check for invalid Restaurant params and return null
     * @throws RuntimeException
     */
    @Test
    public void nullRestaurantParamTest() throws RuntimeException{
        //check for invalid inputs
        try{
        assertNull(waltService.createOrderAndAssignDriver(new Customer("Beethoven",  new City("Tel-Aviv"), "Ludwig van Beethoven"),null,new Date()));
        }
        catch (RuntimeException e){
            e.printStackTrace();
        }
    }


    /**
     * check that the restaurant and the customer are in same city(if true create order) else return null
     * for successful order created - check its a correct order.
     */
    @Test
    public void isCustomerAndRestaurantAreInTheSameCity(){
        Restaurant restaurant = restaurantRepository.findByName("vegan");
        Customer sameCity = customerRepository.findByName("Bach");
        Customer diffCity = customerRepository.findByName("Chopin");
        Delivery notCreated = waltService.createOrderAndAssignDriver(diffCity,restaurant,new Date());

        assertNull(notCreated);

        Delivery created = waltService.createOrderAndAssignDriver(sameCity, restaurant, new Date());
        assertNotNull(created);
        assertEquals(created.getCustomer().getName() , "Bach");
        assertEquals(created.getCustomer().getCity().getName() , "Tel-Aviv");
        assertEquals(created.getRestaurant().getName() , "vegan");
        assertEquals(created.getRestaurant().getCity().getName() , "Tel-Aviv");

    }

    /**
     * check that when there is no driver available then get catch the exception
     */
    @Test
    public void isAvailableDriverTest() throws RuntimeException{
        Restaurant restaurant = restaurantRepository.findByName("vegan");
        Customer bach = customerRepository.findByName("Bach");
        Customer beethoven = customerRepository.findByName("Beethoven");
        Customer rachmaninoff = customerRepository.findByName("Rachmaninoff");

        Date date = new Date();

        Delivery d1 = waltService.createOrderAndAssignDriver(bach,restaurant,date);
        Delivery d2 = waltService.createOrderAndAssignDriver(beethoven,restaurant,date);
        Delivery d3 = waltService.createOrderAndAssignDriver(rachmaninoff,restaurant,date);
        Delivery d4 = waltService.createOrderAndAssignDriver(rachmaninoff,restaurant,new Date());
        Delivery d5 = waltService.createOrderAndAssignDriver(rachmaninoff,restaurant,new Date());
        Delivery d6 = waltService.createOrderAndAssignDriver(beethoven,restaurant,new Date());


        try{
            waltService.createOrderAndAssignDriver(bach, restaurant, new Date());
        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }

    /**
     * test that drivers rank report is correct
     */
    @Test
    public void driverRankReportCheckValuesTest(){
        Customer customer= customerRepository.findByName("Beethoven");
        Restaurant restaurant=restaurantRepository.findByName("vegan");
        Delivery delivery1 = waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery3 = waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        assertEquals(waltService.getDriverRankReport().size(),3);
        assertEquals(waltService.getDriverRankReport().get(0).getDriver().getCity().getId(),restaurant.getCity().getId());
        assertTrue(waltService.getDriverRankReport().get(0).getDriver().getName().equals("Mary") ||
                waltService.getDriverRankReport().get(0).getDriver().getName().equals("Patricia") ||
                waltService.getDriverRankReport().get(0).getDriver().getName().equals("Daniel"));
        assertTrue(waltService.getDriverRankReport().get(0).getTotalDistance() < 20);
    }

    /**
     * test that drivers rank report is sorted as requested
     */
    @Test
    public void isDriverRankReportSortedTest(){
        Customer customer= customerRepository.findByName("Beethoven");
        Restaurant restaurant=restaurantRepository.findByName("vegan");
        Delivery delivery1 = waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery3 = waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        assertEquals(true, isReportSorted(waltService.getDriverRankReport()));
    }

    /**
     * get list of drivers and their distances and check that the list is sorted
     * @param report
     * @return
     */
    private boolean isReportSorted(List<DriverDistance> report){
        if (report.size() == 0 || report.size() == 1){
            return true;
        }

        for (int i = 1; i < report.size(); i++) {
            double prev = report.get(i-1).getTotalDistance();
            double next = report.get(i).getTotalDistance();
            if (next > prev){
                return false;
            }
        }

        return true;
    }


    /**
     * test that drivers rank report by city is correct
     */
    @Test
    public void driverRankReportByCityCheckValuesTest(){
        Customer customer= customerRepository.findByName("Beethoven");
        Restaurant restaurant=restaurantRepository.findByName("vegan");
        City city = cityRepository.findByName("Tel-Aviv");
        Delivery delivery1 = waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery3 = waltService.createOrderAndAssignDriver(customer,restaurant,new Date());

        List<DriverDistance>  list = waltService.getDriverRankReportByCity(city);
        assertEquals(list.size(),3);
        assertEquals(list.get(0).getDriver().getCity().getName(), "Tel-Aviv");
        assertTrue(list.get(0).getDriver().getName().equals("Mary") ||
                list.get(0).getDriver().getName().equals("Patricia") ||
                list.get(0).getDriver().getName().equals("Daniel"));

        assertTrue(list.get(0).getTotalDistance() < 20);
    }

    /**
     * check that the driver rank report by city is sorted
     */
    @Test
    public void isDriverRankReportByCitySorted(){
        Customer customer= customerRepository.findByName("Beethoven");
        Restaurant restaurant=restaurantRepository.findByName("vegan");
        City city = cityRepository.findByName("Tel-Aviv");
        Delivery delivery1 = waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer,restaurant,new Date());
        Delivery delivery3 = waltService.createOrderAndAssignDriver(customer,restaurant,new Date());

        List<DriverDistance>  list = waltService.getDriverRankReportByCity(city);
        assertEquals(true, isReportSorted(list));

    }

}
