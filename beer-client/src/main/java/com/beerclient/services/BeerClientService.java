package com.beerclient.services;

import domain.Beer;
import domain.BeerPagedList;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BeerClientService {

    Mono<Beer> getBeerById (UUID beerId, boolean showInventoryOnHand);

    Mono<BeerPagedList> getAllBeers (Integer pageNumber, Integer pageSize, String beerName, String beerStyle, PageRequest pageRequest,
                                     Boolean showInventoryOnHand);

    Mono<ResponseEntity<Void>> createBeer (Beer beerDto);

    Mono<ResponseEntity<Void>> updateBeer (UUID beerId, Beer beerDto);

    Mono<ResponseEntity<Void>> deleteBeerById (UUID beerId);

    Mono<Beer> getBeerByUpc (String upc);
}
