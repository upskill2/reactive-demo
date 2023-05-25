package netflux.controllers;

import lombok.RequiredArgsConstructor;
import netflux.domain.Movie;
import netflux.domain.MovieEvent;
import netflux.services.MovieService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;

@RestController
@RequestMapping ("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping ("/{id}")
    Mono<Movie> getMovieById (@PathVariable String id) {
        return movieService.getMovieById (id);
    }

    @GetMapping
    Flux<Movie> getAllMovies () {
        return movieService.getAllMovies ();
    }


   @GetMapping (value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<MovieEvent>  streamMovieEvents (@PathVariable String id) {
        return movieService.streamMovieEvents (id);
    }

}
