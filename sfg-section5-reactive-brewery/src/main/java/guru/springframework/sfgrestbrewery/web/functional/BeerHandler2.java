package guru.springframework.sfgrestbrewery.web.functional;

import guru.springframework.sfgrestbrewery.services.BeerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerHandler2 {

    private final BeerService beerService;

    public Mono<ServerResponse> getBeerById (ServerRequest serverRequest) {
        Integer beerId = Integer.valueOf (serverRequest.pathVariable ("beerId"));
        Boolean showInventoryOnHand = Boolean.valueOf (serverRequest.queryParam ("showInventoryOnHand").orElse ("false"));


        return beerService.getById (beerId, showInventoryOnHand)
                .flatMap (beerDto -> {
                    return ServerResponse.ok ()
                            .contentType (MediaType.APPLICATION_JSON)
                            .bodyValue (beerDto);
                }).switchIfEmpty (ServerResponse.notFound ().build ());
    }

}
