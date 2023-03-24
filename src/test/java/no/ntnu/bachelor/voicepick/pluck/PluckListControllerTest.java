package no.ntnu.bachelor.voicepick.pluck;

import jakarta.transaction.Transactional;
import no.ntnu.bachelor.voicepick.dtos.AddLocationRequest;
import no.ntnu.bachelor.voicepick.dtos.AddProductRequest;
import no.ntnu.bachelor.voicepick.features.pluck.controllers.PluckListController;
import no.ntnu.bachelor.voicepick.features.pluck.dtos.CargoCarrierDto;
import no.ntnu.bachelor.voicepick.features.pluck.models.CargoCarrier;
import no.ntnu.bachelor.voicepick.features.pluck.models.PluckList;
import no.ntnu.bachelor.voicepick.features.pluck.services.CargoCarrierService;
import no.ntnu.bachelor.voicepick.features.pluck.services.PluckListLocationService;
import no.ntnu.bachelor.voicepick.models.ProductType;
import no.ntnu.bachelor.voicepick.models.Status;
import no.ntnu.bachelor.voicepick.services.ProductLocationService;
import no.ntnu.bachelor.voicepick.services.ProductService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class PluckListControllerTest {

  // TODO: Remove 'test method order' and tear down each test properly

  @Autowired
  private PluckListController pluckListController;
  @Autowired
  private ProductLocationService productLocationService;
  @Autowired
  private ProductService productService;

  @Autowired
  private PluckListLocationService pluckListLocationService;

  @Autowired
  private CargoCarrierService cargoCarrierService;

  /**
   * Tries to fetch a pluck list when there are no products store in the database
   */
  @Test
  @DisplayName("Try to get a pluck list when there are no products available")
  @Order(1)
  void getPluckListWithoutProducts() {
    var plucklist = this.pluckListController.getRandomPluckList(null);

    assertEquals(HttpStatus.NO_CONTENT, plucklist.getStatusCode());
  }

  @Test
  @DisplayName("Get a pluck list")
  @Order(2)
  void getPluckList() {
    // Setup
    this.productLocationService.addLocation(new AddLocationRequest("H201", 321));
    this.pluckListLocationService.addLocation(new AddLocationRequest("H344", 555));
    this.productService.addProduct(new AddProductRequest(
            "Q-Melk",
            "H201",
            1.75,
            1.75,
            50,
            ProductType.D_PAK,
            Status.READY
    ));

    // Execution
    var response = this.pluckListController.getRandomPluckList(null);
    var responseBody = response.getBody();

    // Validation
    assert responseBody != null;
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, ((PluckList) responseBody).getPlucks().size());
  }

  @Test
  @DisplayName("Update cargo carrier for pluck list")
  @Order(3)
  @Transactional
  void updateCargoCarrier() {
    // Setup
    var response = this.pluckListController.getRandomPluckList(null);
    var responseBody = response.getBody();

    PluckList pluckList = null;
    if (responseBody instanceof String) {
      fail();
    } else {
      pluckList = (PluckList) responseBody;
    }

    assert pluckList != null;

    this.cargoCarrierService.add(new CargoCarrier("Helpall", 1L, "one"));
    var cargoCarrierTypes = this.cargoCarrierService.findAll();

    assertEquals(1, cargoCarrierTypes.size());

    // Execution
    this.pluckListController.updateCargoCarrier(pluckList.getId(), new CargoCarrierDto(
            cargoCarrierTypes.get(0).getName(),
            cargoCarrierTypes.get(0).getIdentifier(),
            cargoCarrierTypes.get(0).getPhoneticIdentifier()
    ));

    var updatedPluckList = this.pluckListController.getPluckListById(pluckList.getId()).getBody();

    assert updatedPluckList != null;
    assertEquals("Helpall", updatedPluckList.getCargoCarrier().getName());
    assertEquals(1L, updatedPluckList.getCargoCarrier().getIdentifier());

  }
}
