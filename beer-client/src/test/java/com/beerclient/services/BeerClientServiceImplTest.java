package com.beerclient.services;

import com.beerclient.configs.WebClientConfig;
import domain.Beer;
import domain.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BeerClientServiceImplTest {

    BeerClientServiceImpl beerClientService;

    @BeforeEach
    void setUp () {

        beerClientService = new BeerClientServiceImpl (new WebClientConfig ().getWebClient ());

    }

    @Test
    void getAllBeers () {
        Mono<BeerPagedList> beerPagedListMono = beerClientService.getAllBeers (null, null, null, null,
                null, null);

        BeerPagedList beerPagedList = beerPagedListMono.block ();
        assertNotNull (beerPagedList);
        assertThat (beerPagedList.getContent ().size ()).isGreaterThan (0);
        System.out.println (beerPagedList.toList ());

    }

    @Test
    void getBeersPageSize10 () {
        Mono<BeerPagedList> beerPagedListMono = beerClientService.getAllBeers (1, 10, null, null,
                null, null);

        BeerPagedList beerPagedList = beerPagedListMono.block ();
        assertNotNull (beerPagedList);
        assertThat (beerPagedList.getContent ().size ()).isEqualTo (10);
        System.out.println (beerPagedList.toList ());

    }

    @Test
    void getBeersNoRecords () {
        Mono<BeerPagedList> beerPagedListMono = beerClientService.getAllBeers (10, 20, null, null,
                null, null);

        BeerPagedList beerPagedList = beerPagedListMono.block ();
        assertNotNull (beerPagedList);
        assertThat (beerPagedList.getContent ().size ()).isEqualTo (0);
        System.out.println (beerPagedList.toList ());

    }

    @Test
    void getBeerByIdFunctional () throws InterruptedException {

        AtomicReference<String> beerName = new AtomicReference<> ();
        CountDownLatch countDownLatch = new CountDownLatch (1);

       beerClientService.getAllBeers (null, null, null, null,
                null, true)
                .map (beerPagedList -> beerPagedList.getContent ().get (0).getId ())
                .map (beerId -> beerClientService.getBeerById (beerId, true))
                .flatMap (beerMono -> beerMono)
                .subscribe (beer -> {
                    assertNotNull (beer);
                    beerName.set (beer.getBeerName ());
                    assertEquals (beer.getBeerName (), "Mango Bobs");
                    countDownLatch.countDown ();
                });

        countDownLatch.await ();
        assertEquals (beerName.get (), "Mango Bobs");
    }

    @Test
    void getBeerById () {

        UUID uiid = beerClientService.getAllBeers (null, null, null, null,
                null, true).block ().stream ().findFirst ().get ().getId ();

        Mono<Beer> beerMono = beerClientService.getBeerById (uiid, true);
        Beer beer = beerMono.block ();
        assertNotNull (beer);
        assertEquals (beer.getId (), uiid);
        System.out.println (beer);

    }

    @Test
    void getBeerByUpc () {

        List<String> ups = beerClientService.getAllBeers (null, null, null, null,
                null, true).block ().stream ().map (Beer::getUpc).toList ();
        String firstUps = ups.get (0);

        Mono<Beer> beerMono = beerClientService.getBeerByUpc (firstUps);
        Beer beer = beerMono.block ();
        assertNotNull (beer);
        assertEquals (beer.getUpc (), firstUps);
        System.out.println (beer);

    }

    @Test
    void createBeer () {

        Beer beer = Beer.builder ()
                .beerName ("New Beer")
                .beerStyle ("LAGER")
                .upc ("123456789012")
                .price (new BigDecimal ("12.99"))
                .build ();

        Mono<ResponseEntity<Void>> beerMono = beerClientService.createBeer (beer);
        ResponseEntity<Void> responseEntity = beerMono.block ();

        assertThat (responseEntity.getStatusCode ()).isEqualTo (HttpStatus.CREATED);

    }

    @Test
    void updateBeer () {
        UUID uiid = beerClientService.getAllBeers (null, null, null, null,
                null, true).block ().stream ().findFirst ().get ().getId ();

        Beer beer = Beer.builder ()
                .beerName ("New Beer")
                .beerStyle ("LAGER")
                .upc ("123456789012")
                .price (new BigDecimal ("12.99"))
                .build ();

        Mono<ResponseEntity<Void>> beerMono = beerClientService.updateBeer (uiid, beer);
        ResponseEntity<Void> responseEntity = beerMono.block ();

        assertThat (responseEntity.getStatusCode ()).isEqualTo (HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteBeerById () {
        UUID uiid = beerClientService.getAllBeers (null, null, null, null,
                null, true).block ().stream ().findFirst ().get ().getId ();

        Mono<ResponseEntity<Void>> beerMono = beerClientService.deleteBeerById (uiid);
        ResponseEntity<Void> responseEntity = beerMono.block ();

        assertThat (responseEntity.getStatusCode ()).isEqualTo (HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteBeerByIdNotFound () {
        Mono<ResponseEntity<Void>> beerMono = beerClientService.deleteBeerById (UUID.randomUUID ());


        assertThrows (WebClientResponseException.class, () -> {
            ResponseEntity<Void> responseEntity = beerMono.block ();
            assertThat (responseEntity.getStatusCode ()).isEqualTo (HttpStatus.NOT_FOUND);
        });
    }

    @Test
    void deleteBeerByIdNotFoundHandleException () {
        Mono<ResponseEntity<Void>> beerMono = beerClientService.deleteBeerById (UUID.randomUUID ());
        ResponseEntity<Void> responseEntity = beerMono.onErrorResume (throwable -> {
                    if (throwable instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) throwable;
                        return Mono.just (ResponseEntity.status (ex.getStatusCode ()).build ());
                    } else {
                        throw new RuntimeException (throwable);
                    }
                }
        ).block ();
        assertThat (responseEntity.getStatusCode ()).isEqualTo (HttpStatus.NOT_FOUND);
    }
}