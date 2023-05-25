package netflux.services;

import lombok.RequiredArgsConstructor;
import netflux.domain.Movie;
import netflux.domain.MovieEvent;
import netflux.repositories.MovieRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    @Override
    public Mono<Movie> getMovieById (final String id) {
        return movieRepository.findById (id);
    }

    @Override
    public Flux<Movie> getAllMovies () {
        return movieRepository.findAll ();

    }

    @Override
    public Flux<MovieEvent> streamMovieEvents (final String id) {
        return Flux.<MovieEvent>generate (movieEventSynchronousSink -> movieEventSynchronousSink.next (new MovieEvent (id, new Date ())))
                .delayElements (Duration.ofSeconds (1));
    }
}
