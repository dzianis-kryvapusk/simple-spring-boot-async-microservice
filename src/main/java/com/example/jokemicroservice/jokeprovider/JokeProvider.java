package com.example.jokemicroservice.jokeprovider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.jokemicroservice.dto.JokeDto;

public interface JokeProvider {
    CompletableFuture<List<JokeDto>> getJokesAsync(int number);
}
