package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.domain.Beer;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import guru.springframework.sfgrestbrewery.web.model.BeerStyleEnum;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static guru.springframework.sfgrestbrewery.web.model.BeerStyleEnum.PALE_ALE;
import static org.assertj.core.internal.bytebuddy.implementation.bytecode.constant.IntegerConstant.ONE;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;


@WebFluxTest (BeerController.class)
class BeerControllerWebFluxTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    ConnectionFactoryInitializer initializer;

    @MockBean
    BeerService beerService;

    Mono<BeerDto> validBeer;

    Mono<List<BeerDto>> beerDtoList;

    Mono<BeerPagedList> beerPagedList;

    BeerPagedList beerPaged;


    @BeforeEach
    void setUp () {
        BeerDto beerDto = BeerDto.builder ()
                .beerName ("Test Beer")
                .beerStyle ("PALE_ALE")
                .upc (BeerLoader.BEER_2_UPC)
                .build ();
        validBeer = Mono.just (beerDto);

        beerPaged = new BeerPagedList (List.of (beerDto, beerDto), PageRequest.of (1, 1), 2L);
        beerPagedList = Mono.just (beerPaged);

    }


    @Test
    void getListOfBeers () {

        given (beerService.listBeers (any (), any (), any (), any ())).willReturn ( beerPagedList);

        webTestClient.get ()
                .uri ("/api/v1/beer")
                .accept (MediaType.APPLICATION_JSON)
                .exchange ()
                .expectStatus ().isOk ()
                .expectBody (BeerPagedList.class)
                .value (beerPagedList1 -> beerPagedList1.getContent ().get (0).getBeerName (), equalTo ("Test Beer"))
           ;
    }

    @Test
    void getBeerById () {
        UUID beerId = UUID.randomUUID ();
        given (beerService.getById (any (), any ())).willReturn (validBeer);

        webTestClient.get ()
                .uri ("/api/v1/beer/" + 1)
                .accept (MediaType.APPLICATION_JSON)
                .exchange ()
                .expectStatus ().isOk ()
                .expectBody (BeerDto.class)
                .value (beerDto -> beerDto.getBeerName (), equalTo (validBeer.block ().getBeerName ()));

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