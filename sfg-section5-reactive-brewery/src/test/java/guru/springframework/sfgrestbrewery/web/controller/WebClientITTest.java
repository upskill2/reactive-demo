package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * Created by jt on 3/7/21.
 */

@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class WebClientITTest {

    public static final String BASE_URL = "http://localhost:8080";

    WebClient webClient;

    @BeforeEach
    void setUp () {
        webClient = WebClient.builder ()
                .baseUrl (BASE_URL)
                .clientConnector (new ReactorClientHttpConnector (HttpClient.create ().wiretap (true)))
                .build ();
    }

    @Test
    void testListBeersByNameAndStyle () throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch (1);

        Mono<BeerPagedList> beerPagedListMono = webClient.get ().uri (uriBuilder -> {
                    return uriBuilder.path ("/api/v1/beer")
                            .queryParam ("beerName", "Mango Bobs")
                            .queryParam ("beerStyle", "ALE")
                            .build ();
                })
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ().bodyToMono (BeerPagedList.class);


        beerPagedListMono.publishOn (Schedulers.parallel ()).subscribe (beerPagedList -> {

            beerPagedList.getContent ().forEach (beerDto -> System.out.println (beerDto.toString ()));

            countDownLatch.countDown ();
        });


        countDownLatch.await (1000, TimeUnit.MILLISECONDS);

        if(beerPagedListMono.block ().getContent ().size ()>0){
            assertEquals (beerPagedListMono.block ().getContent ().get (0).getBeerStyle (), "ALE");
        }

        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }
    @Test
    void testUpdateBeerBadRequest () throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch (2);

        BeerDto updatedPayload = BeerDto.builder ()
                .beerName ("JTsUpdate")
                .upc ("123456789012")
                .price (new BigDecimal ("12.99"))
                .build ();

        webClient.put ().uri ("/api/v1/beer" + 200)
                .contentType (MediaType.APPLICATION_JSON)
                .body (BodyInserters.fromValue (updatedPayload))
                .retrieve ().toBodilessEntity ()
                .subscribe (voidResponseEntity -> {
                        }, throwable -> {
                            if (throwable.getClass ().getName ().equals ("org.springframework.web.reactive.function.client" +
                                    ".WebClientResponseException$NotFound")) {
                                WebClientResponseException exception = (WebClientResponseException) throwable;
                                if (exception.getStatusCode () == HttpStatus.NOT_FOUND) {
                                }
                                countDownLatch.countDown ();
                            }
                        }
                );
        countDownLatch.countDown ();

        countDownLatch.await (1000, TimeUnit.MILLISECONDS);
        assertThat (countDownLatch.getCount ()).isZero ();
    }
    @Test
    void testDeleteBeer() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3);

        webClient.get ().uri ("/api/v1/beer/")
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ()
                .bodyToMono (BeerPagedList.class)
                .publishOn (Schedulers.parallel ())
                .subscribe (pageList ->{
                    countDownLatch.countDown ();

                    BeerDto beerDto = pageList.getContent ().get (0);

                    webClient.delete ().uri ("/api/v1/beer/" + beerDto.getId ())
                            .accept (MediaType.APPLICATION_JSON)
                            .retrieve ()
                            .toBodilessEntity ()
                            .flatMap (voidResponseEntity -> {
                                countDownLatch.countDown ();

                                return webClient.get ().uri ("/api/v1/beer/" + beerDto.getId ())
                                        .accept (MediaType.APPLICATION_JSON)
                                        .retrieve ()
                                        .bodyToMono (BeerDto.class);
                            })
                            .subscribe (saveDto ->{},throwable -> {
                                countDownLatch.countDown ();
                            });
                });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);

    }

    @Test
    void testListBeersPageSize5() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerPagedList> beerPagedListMono = webClient.get().uri(uriBuilder -> {
                    return uriBuilder.path("/api/v1/beer").queryParam("pageSize", "5").build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerPagedList.class);

        beerPagedListMono.publishOn(Schedulers.parallel()).subscribe(beerPagedList -> {

            beerPagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void testGetBeerById () throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch (1);

        Mono<BeerDto> beerDtoMono = webClient.get ().uri ("/api/v1/beer/1")
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ().bodyToMono (BeerDto.class);

        beerDtoMono.subscribe (beerDto -> {
            assertThat (beerDto).isNotNull ();
            assertThat (beerDto.getBeerName ()).isNotBlank ();
            countDownLatch.countDown ();
        });

        countDownLatch.await (1000, TimeUnit.MILLISECONDS);
        assertThat (countDownLatch.getCount ()).isEqualTo (0);
    }

    @Test
    void testGetBeerByUpc () throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch (1);

        Mono<BeerDto> beerDtoMono = webClient.get ().uri ("/api/v1/beerUpc/" + BeerLoader.BEER_1_UPC)
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ().bodyToMono (BeerDto.class);

        beerDtoMono.subscribe (beerDto -> {
            assertThat (beerDto).isNotNull ();
            assertThat (beerDto.getBeerName ()).isNotBlank ();
            countDownLatch.countDown ();
        });

        countDownLatch.await (1000, TimeUnit.MILLISECONDS);
        assertThat (countDownLatch.getCount ()).isEqualTo (0);
    }

    @Test
    void testListBeers () throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch (1);

        Mono<BeerPagedList> beerPagedListMono = webClient.get ().uri ("/api/v1/beer")
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ().bodyToMono (BeerPagedList.class);


//        BeerPagedList pagedList = beerPagedListMono.block();
//        pagedList.getContent().forEach(beerDto -> System.out.println(beerDto.toString()));
        beerPagedListMono.publishOn (Schedulers.parallel ()).subscribe (beerPagedList -> {

            beerPagedList.getContent ().forEach (beerDto -> System.out.println (beerDto.toString ()));

            countDownLatch.countDown ();
        });

        countDownLatch.await (1000, TimeUnit.MILLISECONDS);
    }

    @Test
    void testListBeersByBeerName () throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch (1);

        Mono<BeerPagedList> beerPagedListMono = webClient.get ().uri (uriBuilder -> {
                    return uriBuilder.path ("/api/v1/beer")
                            .queryParam ("beerName", "Mango Bobs")
                            .build ();
                })
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ().bodyToMono (BeerPagedList.class);


        beerPagedListMono.publishOn (Schedulers.parallel ()).subscribe (beerPagedList -> {

            beerPagedList.getContent ().forEach (beerDto -> System.out.println (beerDto.toString ()));

            countDownLatch.countDown ();
        });

        assertEquals (beerPagedListMono.block ().getContent ().get (0).getBeerName (), "Mango Bobs");

        countDownLatch.await (1000, TimeUnit.MILLISECONDS);
    }



    @Test
    void testSaveBeer () throws InterruptedException {

        BeerDto beerDto = BeerDto.builder ()
                .beerName ("New Beer")
                .beerStyle ("ALE")
                .build ();


        CountDownLatch countDownLatch = new CountDownLatch (2);

        Mono<BeerPagedList> beerResponseMono = webClient.post ().uri ("/api/v1/beer")
                // .body (Mono.just (beerDto), BeerDto.class)
                .body (BodyInserters.fromValue (beerDto))
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ()
                .bodyToMono (BeerPagedList.class);

        beerResponseMono.publishOn (Schedulers.parallel ())
                .doOnError (throwable -> {
                    log.error ("Error Occurred");
                    countDownLatch.countDown ();
                })
                .subscribe (responseEntity -> {
                    responseEntity.getContent ().forEach (beerDto1 -> System.out.println (beerDto1.toString ()));
                    countDownLatch.countDown ();
                });

        WebClient.ResponseSpec responseSpec = webClient.post ().uri ("/api/v1/beer")
                // .body (Mono.just (beerDto), BeerDto.class)
                .body (BodyInserters.fromValue (beerDto))
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ();

        Assertions.assertEquals (HttpStatus.CREATED, responseSpec.toBodilessEntity ().block ().getStatusCode ());
        countDownLatch.await (1000, TimeUnit.MILLISECONDS);
    }

    @Test
    void testSaveBeerBadRequest () throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch (1);

        BeerDto beerDto = BeerDto.builder ()
                .price (new BigDecimal ("12.99"))
                .build ();


        Mono<ResponseEntity<Void>> beerResponseMono = webClient.post ().uri ("/api/v1/beer")
                .contentType (MediaType.APPLICATION_JSON)
                .body (BodyInserters.fromValue (beerDto))
                .retrieve ().toBodilessEntity ();


        beerResponseMono.publishOn (Schedulers.parallel ())
                .doOnError (throwable -> {
                    log.error ("Error Occurred");
                    countDownLatch.countDown ();
                })
                .subscribe (responseEntity -> {
                });


        countDownLatch.await (1000, TimeUnit.MILLISECONDS);
        assertThat (countDownLatch.getCount ()).isZero ();

    }

    @Test
    void testUpdateBeer () throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch (3);

        webClient.get ().uri ("/api/v1/beer")
                // .body (Mono.just (beerDto), BeerDto.class)
                .accept (MediaType.APPLICATION_JSON)
                .retrieve ()
                .bodyToMono (BeerPagedList.class)
                .publishOn (Schedulers.single ())
                .subscribe (pageList -> {
                    countDownLatch.countDown ();

                    //get existing beer
                    BeerDto beerDto = pageList.getContent ().get (0);

                    BeerDto updatedBeer = BeerDto.builder ()
                            .beerName ("New New Beer")
                            .beerStyle (beerDto.getBeerStyle ())
                            .upc (beerDto.getUpc ())
                            .price (beerDto.getPrice ())
                            .build ();

                    //update beer
                    webClient.put ().uri ("/api/v1/beer/" + beerDto.getId ())
                            .contentType (MediaType.APPLICATION_JSON)
                            .body (BodyInserters.fromValue (updatedBeer))
                            .retrieve ().toBodilessEntity ()
                            .flatMap (responseEntity -> {
                                //get and verify updated beer
                                countDownLatch.countDown ();
                                return webClient.get ().uri ("/api/v1/beer/" + beerDto.getId ())
                                        .accept (MediaType.APPLICATION_JSON)
                                        .retrieve ().bodyToMono (BeerDto.class);
                            }).subscribe (saveDto -> {
                                assertThat (saveDto.getBeerName ()).isEqualTo ("New New Beer");
                                countDownLatch.countDown ();
                            });
                });
        countDownLatch.await (1000, TimeUnit.MILLISECONDS);
        assertThat (countDownLatch.getCount ()).isZero ();
    }

}
