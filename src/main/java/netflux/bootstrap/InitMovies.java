package netflux.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import netflux.domain.Movie;
import netflux.repositories.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Component
@Slf4j
public class InitMovies implements CommandLineRunner {
    private final MovieRepository movieRepository;

    @Override
    public void run (final String... args) throws Exception {
        movieRepository.deleteAll ()
                .thenMany (Flux.just ("Silence of the Lambdas", "AEon Flux", "Enter the Mono<Void>", "The Fluxxinator",
                        "Back to the Future", "Meet the Fluxes", "Lord of the Fluxes")
                        .map (title-> Movie.builder ().title (title).build ())
                        .flatMap (movieRepository::save)).subscribe (null, null,
                        ()-> movieRepository.findAll ().subscribe (movie-> log.info ("{}", movie)));
    }
}
