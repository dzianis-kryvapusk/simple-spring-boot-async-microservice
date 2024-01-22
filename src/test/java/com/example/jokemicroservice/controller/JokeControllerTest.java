package com.example.jokemicroservice.controller;

import java.util.List;

import com.example.jokemicroservice.config.ThirdPartyApiJokeProperties;
import com.example.jokemicroservice.dto.JokeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.once;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JokeControllerTest {
    private static ClientAndServer mockServer;
    @Autowired
    private ThirdPartyApiJokeProperties jokeApiProperties;
    @Autowired
    private MockMvc api;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void startServer() {
        mockServer = startClientAndServer(1080);
    }

    @AfterAll
    static void stopServer() {
        mockServer.stop();
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_getJokes_successful() throws Exception {
        try (var mockServerClient = new MockServerClient("localhost", 1080)) {
            for (int i = 1; i <= 5; i++) {
                JokeDto joke = buildJoke(i);
                //TODO fix mock server returning only the first joke
                mockServerClient.when(request().withMethod(HttpMethod.GET.name()).withPath(jokeApiProperties.getPath()), once())
                        .respond(response().withStatusCode(HttpStatusCode.OK_200.code())
                                         .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                                         .withBody(objectMapper.writeValueAsString(joke)));
            }

            List<JokeDto> response = (List<JokeDto>) api
                    .perform(get("/jokes")
                                     .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getAsyncResult();
            assertNotNull(response);
            assertEquals(5, response.size());
        }
    }

    @Test
    void test_getJokes_tooManyJokesRequested() throws Exception {
        api.perform(get("/jokes")
                            .queryParam("count", "101")
                            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void test_getJokes_negativeNumberJokesRequested() throws Exception {
        api.perform(get("/jokes")
                            .queryParam("count", "-1")
                            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private JokeDto buildJoke(int id) {
        JokeDto joke = new JokeDto();
        joke.setId(id);
        joke.setSetup("test setup " + id);
        joke.setPunchline("test punchline " + id);
        return joke;
    }
}