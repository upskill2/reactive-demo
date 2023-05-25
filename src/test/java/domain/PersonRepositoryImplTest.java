package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Null;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersonRepositoryImplTest {
    PersonRepositoryImpl personRepository;

    @BeforeEach
    void setUp () {
        personRepository = new PersonRepositoryImpl ();

    }

    @Test
    void getByIdBlock () {
        Mono<Person> personMono = personRepository.findById (1);
        Person person = personMono.block ();

        System.out.println (person.toString ());
    }

    @Test
    void findByIdWithParam () {


        Mono<Person> personMono = personRepository.findById (1);
        personMono.subscribe (resultId -> assertEquals (1, resultId.getId ()));

        Mono<Person> personMono2 = personRepository.findById (4);

        StepVerifier.create (personMono2).expectNextCount (1).verifyComplete ();

        StepVerifier.create (personMono2)
                .expectNextMatches (person -> person.getId () == 4)
                .verifyComplete ();

        personMono2.subscribe (resultId -> assertEquals (4, resultId.getId ()));



        Mono<Person> personMono3 = personRepository.findById (9);
        StepVerifier.create (personMono3).expectNextCount (0).verifyComplete ();
        personMono3.subscribe (resultId -> assertEquals ("", resultId.getId ()));

        int idTest1 = personMono.block ().getId ();
        int idTest2 = personMono2.block ().getId ();

        int t = personMono.doOnNext (person-> {
            System.out.println (person.toString ());
        }).block ().getId ();

        System.out.println (idTest1);
        System.out.println (idTest2);

    }

    @Test
    void getByIdSubscriber () {
        Mono<Person> personMono = personRepository.findById (1);
        personMono.subscribe (person -> {
            System.out.println (person.toString ());
        });
    }

    @Test
    void getByIdMapFunction () {
        Mono<Person> personMono = personRepository.findById (1);
        personMono.map (person -> {
            System.out.println (person.toString ());
            return person.getFirstName ();
        }).subscribe (firstName -> {
            System.out.println ("From map: " + firstName);
        });

    }

    @Test
    void fluxTestBlockFirst () {

        Flux<Person> personFlux = personRepository.findAll ();
        Person person = personFlux.blockFirst ();

        System.out.println (person.toString ());
    }

    @Test
    void testFluxSubscribe () {
        Flux<Person> personFlux = personRepository.findAll ();

        StepVerifier.create (personFlux).expectNextCount (4).verifyComplete ();

        personFlux.subscribe (person -> {
            System.out.println (person.toString ());
        });
    }

    @Test
    void testFluxToListMono () {
        Flux<Person> personFlux = personRepository.findAll ();
        Mono<List<Person>> personListMono = personFlux.collectList ();

        personListMono.subscribe (list -> {
            list.forEach (person -> {
                System.out.println (person.toString ());
            });
        });
    }

    @Test
    void testFindPersonById () {
        Flux<Person> personFlux = personRepository.findAll ();
        final int id = 3;

        Mono<Person> personMono = personFlux.filter (person -> person.getId () == id).next ();
        personMono.subscribe (person -> System.out.println (person.toString ()));

    }

    @Test
    void testFindPersonByIdNotFound () {
        Flux<Person> personFlux = personRepository.findAll ();
        final int id = 5;

        Mono<Person> personMono = personFlux.filter (person -> person.getId () == id).next ();
        personMono.subscribe (person -> System.out.println (person.toString ()));

    }

    @Test
    void testFindPersonByIdNotFoundWithException () {
        Flux<Person> personFlux = personRepository.findAll ();
        final int id = 5;

        Mono<Person> personMono = personFlux.filter (person -> person.getId () == id).single ();
        personMono.doOnError (throwable -> System.out.println ("It went boom"))
                .onErrorReturn (Person.builder ()
                        .id (5)
                        .lastName ("H")
                        .lastName ("U")
                        .build ())
                .subscribe (person -> System.out.println (person.toString ()));

    }
}