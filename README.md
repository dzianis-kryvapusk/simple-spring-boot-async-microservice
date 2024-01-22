# Task

Here, you can find an example of a simple async microservice on java with an integration with an external service.
WebFlux is not used here purposely. The aim is to have an example of pure HttpClient.
inspired by [https://habr.com/ru/articles/784754/](https://habr.com/ru/articles/784754/)

Create a microservice to show jokes.
Technologies: Java, Spring Boot.

API description:

Call GET /jokes with parameter: count: number of jokes (from 1 to 100), 5 by default. Throw an error "No more than 100 jokes can be requested at once" if requested more than 100 jokes.

Third-party API service to retrieve jokes: [https://official-joke-api.appspot.com/random_joke](https://official-joke-api.appspot.com/random_joke). It's allowed to use only this URI (one joke per request).

If more than 1 joke is requested, they must be requested in a batch by 10 jokes in parallel (т.е. 10 jokes in parallel, than more 10 jokes in parallel and so on).

Code must be covered by tests.