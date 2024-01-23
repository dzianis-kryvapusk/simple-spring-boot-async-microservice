package com.example.jokemicroservice.jokeprovider;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import com.example.jokemicroservice.config.ThirdPartyApiJokeProperties;
import com.example.jokemicroservice.dto.JokeDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ThirdPartyJokeProvider implements JokeProvider {
    private final ThirdPartyApiJokeProperties jokeApiProperties;

    private final ThreadPoolTaskExecutor customTaskExecutor;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final HttpRequest request;

    public ThirdPartyJokeProvider(ThirdPartyApiJokeProperties properties, ThreadPoolTaskExecutor customTaskExecutor, ObjectMapper objectMapper) {
        this.jokeApiProperties = properties;
        this.customTaskExecutor = customTaskExecutor;
        this.objectMapper = objectMapper;
        this.request = createRequest();
        this.httpClient = HttpClient.newBuilder().executor(customTaskExecutor)
                .connectTimeout(Duration.of(properties.getTimeout(), ChronoUnit.SECONDS))
                .build();
    }

    private HttpRequest createRequest() {
        try {
            return HttpRequest.newBuilder()
                    .uri(new URI(jokeApiProperties.getHost() + jokeApiProperties.getPath()))
                    .timeout(Duration.of(jokeApiProperties.getTimeout(), ChronoUnit.SECONDS))
                    .version(HttpClient.Version.HTTP_1_1)
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Can't create request", e);
        }
    }

    @Override
    public CompletableFuture<List<JokeDto>> getJokesAsync(int number) {
        CompletableFuture<List<JokeDto>> baseFuture = CompletableFuture.completedFuture(new ArrayList<>(number));
        for (int i = 0; i < number; i++) {
            CompletableFuture<JokeDto> foundJoke = getJokeAsync();
//            try {
//                TimeUnit.MILLISECONDS.sleep(100);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            baseFuture = baseFuture.thenCombine(foundJoke, (acc, response) -> {
                acc.add(response);
                return acc;
            });
        }
        return baseFuture;
    }

    private CompletableFuture<JokeDto> getJokeAsync() {
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(response -> {
                    if (response.statusCode() == HttpStatus.OK.value()) {
                        return convertBody(response.body());
                    }
                    throw new ResponseStatusException(response.statusCode(), response.body(), null);
                }, customTaskExecutor);
    }

    private JokeDto convertBody(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>(){});
        } catch (IOException ioe) {
            throw new CompletionException(ioe);
        }
    }
}
