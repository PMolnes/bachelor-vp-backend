package no.ntnu.bachelor.voicepick.pluck;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import no.ntnu.bachelor.voicepick.exceptions.EmptyListException;
import no.ntnu.bachelor.voicepick.features.authentication.models.User;
import no.ntnu.bachelor.voicepick.features.authentication.repositories.UserRepository;
import no.ntnu.bachelor.voicepick.features.pluck.models.CargoCarrier;
import no.ntnu.bachelor.voicepick.features.pluck.models.Pluck;
import no.ntnu.bachelor.voicepick.features.pluck.models.PluckList;
import no.ntnu.bachelor.voicepick.features.pluck.repositories.CargoCarrierRepository;
import no.ntnu.bachelor.voicepick.features.pluck.repositories.PluckListRepository;
import no.ntnu.bachelor.voicepick.features.pluck.services.PluckListService;
import no.ntnu.bachelor.voicepick.models.Location;
import no.ntnu.bachelor.voicepick.models.Product;
import no.ntnu.bachelor.voicepick.models.ProductType;
import no.ntnu.bachelor.voicepick.models.Status;
import no.ntnu.bachelor.voicepick.repositories.LocationRepository;
import no.ntnu.bachelor.voicepick.repositories.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PluckListTest {

    @Autowired
    private PluckListService pluckListService;
    @Autowired
    private PluckListRepository pluckListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CargoCarrierRepository cargoCarrierRepository;

    @AfterEach
    void teardown() {
        this.pluckListService.deleteAll();
        this.productRepository.deleteAll();
        this.locationRepository.deleteAll();
        this.userRepository.deleteAll();
        this.cargoCarrierRepository.deleteAll();
    }

    @Test
    @DisplayName("Create invalid pluck list")
    void invalidPluckList() {
        try {
            new PluckList("", "");
            new PluckList("test", "");
            new PluckList("", "test");
            new PluckList("", "", new User());
            new PluckList("", "test", new User());
            new PluckList("test", "", new User());
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Create valid pluck list")
    void validPluckList() {
        var pluckList1 = new PluckList("route", "destination");
        var pluckList2 = new PluckList("route", "destination", new User());

        assertEquals("route", pluckList1.getRoute());
        assertEquals("destination", pluckList1.getDestination());
        assertEquals("route", pluckList2.getRoute());
        assertEquals("destination", pluckList2.getDestination());
        assertNotNull(pluckList2.getUser());
    }

    @Test
    @DisplayName("Add pluck to pluck list")
    void addPluckToPluckList() {
        var pluckList = new PluckList("route", "destination");

        var pluck = new Pluck();
        pluckList.addPluck(pluck);

        assertEquals(1, pluckList.getPlucks().size());
        assertEquals(pluckList, pluck.getPluckList());
    }

    @Test
    @DisplayName("Remove pluck from pluck list")
    void removePluckFromPluckList() {
        var pluckList = new PluckList("route", "destination");
        var pluck = new Pluck();
        pluckList.addPluck(pluck);

        pluckList.removePluck(pluck);

        assertEquals(0, pluckList.getPlucks().size());
        assertNull(pluck.getPluckList());
    }

    @Test
    @DisplayName("Test clear pluck removes all relations")
    void clearPluck() {
        var pluckList = new PluckList();
        pluckList.setUser(new User());
        pluckList.setCargoCarrier(new CargoCarrier());
        pluckList.addPluck(new Pluck());

        pluckList.clear();

        assertNull(pluckList.getUser());
        assertNull(pluckList.getCargoCarrier());
        assertEquals(0, pluckList.getPlucks().size());
    }

    @Test
    @DisplayName("Test findById()")
    @Transactional
    void findById() {
        this.pluckListRepository.save(new PluckList());
        var plucks = this.pluckListRepository.findAll();

        var result = this.pluckListService.findById(plucks.get(0).getId());
        if (result.isEmpty()) {
            fail("Did not find pluck list with id 1");
        }
        assertTrue(true);
    }

    @Test
    @DisplayName("Generate random pluck list without user")
    void generatePluckWithoutUser() {
        try {
            this.pluckListService.generateRandomPluckList("9-ugjdafg");
            fail("No error was thrown when trying to generate random pluck without user");
        } catch (EntityNotFoundException e) {
            assertTrue(true);
        } catch (Exception e) {
            fail("An error was thrown, but not the right one: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Generate random pluck with user but without location")
    void generatePluckWithoutLocation() {
        var uid = "-2j3go4g5ha45qh";
        this.userRepository.save(new User(uid, "hans", "val", "hans@val.com"));

        try {
            this.pluckListService.generateRandomPluckList(uid);
            fail("No error was thrown when trying to generate random pluck without location");
        } catch (EmptyListException e) {
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Generate random pluck without available products")
    void generatePluckWithoutProducts() {
        var uid = "-2j3go4g5ha45qh";
        this.userRepository.save(new User(uid, "hans", "val", "hans@val.com"));

        this.locationRepository.save(new Location("H209", 123));

        try {
            this.pluckListService.generateRandomPluckList(uid);
            fail("No error was thrown when trying to generate random pluck without available products");
        } catch (EmptyListException e) {
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("Generate random pluck")
    @Transactional
    void generateRandomPluck() {
        var uid = "-2j3go4g5ha45qh";
        this.userRepository.save(new User(uid, "hans", "val", "hans@val.com"));
        var product = new Product("product1", 1.0, 1.0, 1, ProductType.D_PAK, Status.READY);
        var productLocation = new Location("H209", 123);
        productLocation.addEntity(product);
        this.locationRepository.save(productLocation);
        this.productRepository.save(product);
        var pluckListLocation = new Location("B842", 956);
        this.locationRepository.save(pluckListLocation);

        try {
            this.pluckListService.generateRandomPluckList(uid);
            assertEquals(1, this.pluckListRepository.findAll().size());
        } catch (Exception e) {
            fail("Error was thrown when pluck should have been generated!");
        }
    }

    @Test
    @DisplayName("Update cargo carrier")
    @Transactional
    void updateCargoCarrier() {
        var uid = "-2j3go4g5ha45qh";
        this.userRepository.save(new User(uid, "hans", "val", "hans@val.com"));

        var product = new Product("product1", 1.0, 1.0, 1, ProductType.D_PAK, Status.READY);
        var productLocation = new Location("H209", 123);
        productLocation.addEntity(product);
        this.locationRepository.save(productLocation);
        this.productRepository.save(product);
        var pluckListLocation = new Location("B842", 956);
        this.locationRepository.save(pluckListLocation);

        try {
            this.pluckListService.generateRandomPluckList(uid);
        } catch (Exception e) {
            fail("Exception thrown when pluck list should have been created");
        }

        this.cargoCarrierRepository.save(new CargoCarrier("cargocarrier", 1, "one"));

        var pluckLists = this.pluckListRepository.findAll();
        var cargoCarriers = this.cargoCarrierRepository.findAll();

        this.pluckListService.updateCargoCarrier(pluckLists.get(0).getId(), cargoCarriers.get(0).getIdentifier());

        var newPluckLists = this.pluckListRepository.findAll();

        assertNotNull(newPluckLists.get(0).getCargoCarrier());
    }

}