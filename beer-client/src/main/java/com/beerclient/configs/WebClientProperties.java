package com.beerclient.configs;

import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

public class WebClientProperties {

    public static final String BEER_URL = "http://localhost:8080/";
    public static final String BEER_V1_PATH = "api/v1/beer";
    public static final String BEER_V1_PATH_GET_BY_ID = "api/v1/beer/{uuid}";
    public static final String BEER_V1_UPC_PATH = "api/v1/beerUpc/{upc}";
}
