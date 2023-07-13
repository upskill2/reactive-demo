package guru.springframework.sfgrestbrewery.web.functional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;


import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;


@Configuration
public class BeerRouterConfig {

    public static final String API_V2_BEER_ID = "/api/v2/beer/{beerId}";
    public static final String API_V2_BEER_UPC = "/api/v2/beerUpc/{upc}";
    public static final String API_V2_POST_BEER = "/api/v2/beer";

    @Bean
    public RouterFunction<ServerResponse> beerRouterV2 (BeerHandler beerHandler) {
        return route ()
                .GET (API_V2_BEER_ID, accept (MediaType.APPLICATION_JSON), beerHandler::getBeerById)
                .GET (API_V2_BEER_UPC, accept (MediaType.APPLICATION_JSON), beerHandler::getBeerByUpc)
                .POST (API_V2_POST_BEER, accept (MediaType.APPLICATION_JSON), beerHandler::saveNewBeer)
                .PUT (API_V2_BEER_ID, accept (MediaType.APPLICATION_JSON), beerHandler::updateBeer)
                .build ();
    }


}
