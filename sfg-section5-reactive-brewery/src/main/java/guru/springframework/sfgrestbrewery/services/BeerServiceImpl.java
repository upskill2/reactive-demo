package guru.springframework.sfgrestbrewery.services;

import guru.springframework.sfgrestbrewery.domain.Beer;
import guru.springframework.sfgrestbrewery.repositories.BeerRepository;
import guru.springframework.sfgrestbrewery.web.mappers.BeerMapper;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import guru.springframework.sfgrestbrewery.web.model.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.empty;
import static org.springframework.data.relational.core.query.Query.query;

/**
 * Created by jt on 2019-04-20.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeerServiceImpl implements BeerService {
    private final BeerRepository beerRepository;
    private final BeerMapper beerMapper;
    private final R2dbcEntityTemplate template;

    @Cacheable (cacheNames = "beerListCache", condition = "#showInventoryOnHand == false ")
    @Override
    public Mono<BeerPagedList> listBeers (String beerName, BeerStyleEnum beerStyle, PageRequest pageRequest, Boolean showInventoryOnHand) {
        Query query = null;

        if (!StringUtils.isEmpty (beerName) && !StringUtils.isEmpty (beerStyle)) {
            //search both
            query = query (where ("beerName").is (beerName).and ("beerStyle").is (beerStyle));
        } else if (!StringUtils.isEmpty (beerName) && ObjectUtils.isEmpty (beerStyle)) {
            //search beer_service name
            query = query (where ("beerName").is (beerName));
        } else if (StringUtils.isEmpty (beerName) && !ObjectUtils.isEmpty (beerStyle)) {
            //search beer_service style
            query = query (where ("beerStyle").is (beerStyle));
        } else {
            query = empty ();
        }

        return template.select (Beer.class)
                .matching (query.with (pageRequest))
                .all ()
                .map (b -> beerMapper.beerToBeerDto (b))
                .collectList ()
                .map (beers -> {
                    return new BeerPagedList (beers,
                            PageRequest.of (pageRequest.getPageNumber (),
                                            pageRequest.getPageSize ()),
                                            beers.size ());
                });
    }

    @Cacheable (cacheNames = "beerCache", key = "#beerId", condition = "#showInventoryOnHand == false ")
    @Override
    public Mono<BeerDto> getById (Integer beerId, Boolean showInventoryOnHand) {
        if (showInventoryOnHand) {
            return
                    beerRepository.findById (beerId).map (beer -> beerMapper.beerToBeerDtoWithInventory (beer));
        } else {
            return
                    beerRepository.findById (beerId).map (beer -> beerMapper.beerToBeerDto (beer));
        }
    }

    @Override
    public Mono<BeerDto> saveNewBeer (BeerDto beerDto) {
        return beerRepository.save (beerMapper.beerDtoToBeer (beerDto)).map (beerMapper::beerToBeerDto);
    }

    @Override
    public Mono<BeerDto> updateBeer (Integer beerId, BeerDto beerDto) {
/*        Beer beer = beerRepository.findById(beerId).orElseThrow(NotFoundException::new);

        beer.setBeerName(beerDto.getBeerName());
        beer.setBeerStyle(BeerStyleEnum.PILSNER.valueOf(beerDto.getBeerStyle()));
        beer.setPrice(beerDto.getPrice());
        beer.setUpc(beerDto.getUpc());

        return beerMapper.beerToBeerDto(beerRepository.save(beer));*/
        return beerRepository.findById (beerId).map (beer -> {
            beer.setBeerName (beerDto.getBeerName ());
            beer.setBeerStyle (BeerStyleEnum.valueOf (beerDto.getBeerStyle ()));
            beer.setPrice (beerDto.getPrice ());
            beer.setUpc (beerDto.getUpc ());
            return beer;
        }).flatMap (beerRepository::save).map (beerMapper::beerToBeerDto);
    }

    @Cacheable (cacheNames = "beerUpcCache")
    @Override
    public Mono<BeerDto> getByUpc (String upc) {
        return beerRepository.findByUpc (upc).map (beerMapper::beerToBeerDto);
    }

    @Override
    public void deleteBeerById (Integer beerId) {
        beerRepository.deleteById (beerId);
    }
}
