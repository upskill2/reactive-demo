package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@WebMvcTest (BeerController.class)
class BeerControllerWebFluxTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    BeerService beerService;

    BeerDto validBeer;

    @BeforeEach
    void setUp () {
        validBeer = BeerDto.builder ()
                .beerName ("Test Beer")
                .beerStyle ("PALE_ALE")
                .upc (BeerLoader.BEER_2_UPC)
                .build ();
    }

    @Test
    void getBeerById () {
        UUID beerId = UUID.randomUUID ();
        given (beerService.getById (any (), any ())).willReturn (validBeer);

        webTestClient.get ()
                .uri ("/api/v1/beer/" + beerId)
                .accept (MediaType.APPLICATION_JSON)
                .exchange ()
                .expectStatus ().isOk ()
                .expectBody (BeerDto.class)
                .value (beerDto -> beerDto.getBeerName (), equalTo (validBeer.getBeerName ()));

    }
}