package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@WebFluxTest (BeerController.class)
class BeerControllerWebFluxTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    BeerService beerService;

    Mono<BeerDto> validBeer;

    Mono<List<BeerDto>> beerDtoList;

    BeerPagedList beerPagedList;

    @BeforeEach
    void setUp () {
        BeerDto beerDto = BeerDto.builder ()
                .beerName ("Test Beer")
                .beerStyle ("PALE_ALE")
                .upc (BeerLoader.BEER_2_UPC)
                .build ();
        validBeer = Mono.just (beerDto);

        beerDtoList = Mono.just (List.of (beerDto, beerDto));
        beerPagedList = new BeerPagedList (beerDtoList);

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
                .value (beerDto -> beerDto.getBeerName (), equalTo (validBeer.block ().getBeerName ()));

    }

    @Test
    void getListOfBeers () {

        given (beerService.listBeers (any (), any (), any (), any ())).willReturn (beerPagedList);

        webTestClient.get ()
                .uri ("/api/v1/beer")
                .accept (MediaType.APPLICATION_JSON)
                .exchange ()
                .expectStatus ().isOk ()
                .expectBody (BeerPagedList.class)
                .value (beerPagedList1 -> beerPagedList1.getContent ().get (0).getBeerName (), equalTo (validBeer.block ().getBeerName ()))
                .value (beerPagedList1 -> beerPagedList1.getSize (), equalTo (beerPagedList.getSize ()));
    }

    @Test
    void getBeerByUpc () {
        given (beerService.getByUpc (any ())).willReturn (validBeer);

        webTestClient.get ()
                .uri ("/api/v1/beerUpc/" + validBeer.block ().getUpc ())
                .accept (MediaType.APPLICATION_JSON)
                .exchange ()
                .expectStatus ().isOk ()
                .expectBody (BeerDto.class)
                .value (beerDto -> beerDto.getBeerName (), equalTo (validBeer.block ().getBeerName ()));
    }

}