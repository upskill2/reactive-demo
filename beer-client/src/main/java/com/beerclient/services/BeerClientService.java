package com.beerclient.services;

import domain.Beer;
import domain.BeerPagedList;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BeerClientService {

    Mono<Beer> getBeerById (UUID beerId);

    Mono<BeerPagedList> getAllBeers (Integer pageNumber, Integer pageSize, String beerName, String beerStyle, PageRequest pageRequest,
                                     Boolean showInventoryOnHand);

    Mono<Beer> createBeer (Beer beerDto);

    Mono<Beer> updateBeer (Beer beerDto);

    Mono<Void> deleteBeerById (UUID beerId);

    Mono<Beer> getBeerByUpc (String upc);
}
