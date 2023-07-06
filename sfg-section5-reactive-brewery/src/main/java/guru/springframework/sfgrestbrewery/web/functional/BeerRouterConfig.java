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

    public static final String API_V_2_BEER_BEER_ID = "/api/v2/beer/{beerId}";
    public static final String API_V_2_BEER_UPC_UPC = "/api/v2/beerUpc/{upc}";
    public static final String API_POST_BEER = "/api/v2/beer";

    @Bean
    public RouterFunction<ServerResponse> beerRouterV2 (BeerHandler2 beerHandler2) {
        return route ()
                .GET (API_V_2_BEER_BEER_ID, accept (MediaType.APPLICATION_JSON), beerHandler2::getBeerById)
                .GET (API_V_2_BEER_UPC_UPC, accept (MediaType.APPLICATION_JSON), beerHandler2::getBeerByUpc)
                .POST (API_POST_BEER, accept (MediaType.APPLICATION_JSON), beerHandler2::saveNewBeer)
                .build ();
    }
}
