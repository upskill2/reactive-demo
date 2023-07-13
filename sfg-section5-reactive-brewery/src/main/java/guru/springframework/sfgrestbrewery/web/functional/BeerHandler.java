package guru.springframework.sfgrestbrewery.web.functional;

import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.controller.NotFoundException;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;


@Slf4j
@Component
@RequiredArgsConstructor
public class BeerHandler {

    private final BeerService beerService;
    private final Validator validator;



    public Mono<ServerResponse> deleteBeer (ServerRequest serverRequest) {
        return beerService.reactiveDeleteById(Integer.valueOf (serverRequest.pathVariable ("beerId")))
                .flatMap (voidMono -> ServerResponse.ok ().build ())
                .onErrorResume (exception -> exception instanceof NotFoundException, exception -> ServerResponse.notFound ().build ());
    }

    public Mono<ServerResponse> updateBeer (ServerRequest serverRequest) {
        return serverRequest.bodyToMono (BeerDto.class)
                .doOnNext (this::validateBeer)
                .flatMap (beerDto -> {
                    return beerService.updateBeer (Integer.valueOf (serverRequest.pathVariable ("beerId")), beerDto);
                })
                .flatMap (savebeerDto -> {
                    if (savebeerDto.getId () != null) {
                        log.info ("Update Beer with ID: {}", savebeerDto.getId ());
                        return ServerResponse.noContent ().build ();
                    } else {
                        log.info ("Beer with ID: {} NOT FOUND", serverRequest.pathVariable ("beerId"));
                        return ServerResponse.notFound ().build ();
                    }
                });
    }

    private void validateBeer (BeerDto beerDto) {
        Errors errors = new BeanPropertyBindingResult (beerDto, "beerDto");
        validator.validate (beerDto, errors);

        if (errors.hasErrors ()) {
            throw new ServerWebInputException (errors.toString ());
        }

    }

    public Mono<ServerResponse> saveNewBeer (ServerRequest serverRequest) {

        return beerService.saveNewBeerMono (serverRequest.bodyToMono (BeerDto.class).doOnNext (this::validateBeer))
                .flatMap (beerDto -> {
                    return ServerResponse.ok ()
                            .header ("Location", "/api/v2/beer/" + beerDto.getId ())
                            .build ();
                });
    }

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

    public Mono<ServerResponse> getBeerByUpc (final ServerRequest serverRequest) {

        return beerService.getByUpc (serverRequest.pathVariable ("upc"))
                .flatMap (beerDto -> {
                    return ServerResponse.ok ()
                            .contentType (MediaType.APPLICATION_JSON)
                            .bodyValue (beerDto);
                }).switchIfEmpty (ServerResponse.notFound ().build ());
    }
}
