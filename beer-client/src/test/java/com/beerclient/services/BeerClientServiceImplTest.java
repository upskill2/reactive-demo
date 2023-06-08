package com.beerclient.services;

import com.beerclient.configs.WebClientConfig;
import domain.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

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
        assertThat  (beerPagedList.getContent ().size ()).isGreaterThan (0);
        System.out.println (beerPagedList.toList ());

    }

    @Test
    void getBeersPageSize10 () {
        Mono<BeerPagedList> beerPagedListMono = beerClientService.getAllBeers (1, 10, null, null,
                null, null);

        BeerPagedList beerPagedList = beerPagedListMono.block ();
        assertNotNull (beerPagedList);
        assertThat  (beerPagedList.getContent ().size ()).isEqualTo (10);
        System.out.println (beerPagedList.toList ());

    }

    @Test
    void getBeersNoRecords () {
        Mono<BeerPagedList> beerPagedListMono = beerClientService.getAllBeers (10, 20, null, null,
                null, null);

        BeerPagedList beerPagedList = beerPagedListMono.block ();
        assertNotNull (beerPagedList);
        assertThat  (beerPagedList.getContent ().size ()).isEqualTo (0);
        System.out.println (beerPagedList.toList ());

    }

    @Test
    void getBeerById () {
    }

    @Test
    void getBeerByUpc () {
    }

    @Test
    void createBeer () {
    }

    @Test
    void updateBeer () {
    }

    @Test
    void deleteBeerById () {
    }
}