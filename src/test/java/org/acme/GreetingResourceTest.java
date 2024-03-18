package org.acme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;

@QuarkusTest
class GreetingResourceTest {

    @Test
    @TestSecurity(user = "testUser", roles = {"admin", "user"})
    void bruteForceContextNotActiveException() throws Exception {
        //increase  these values to load (and chance to "provoke" the issue)
        int numberOfRequest = 100;
        int numberOfParallelRequests = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfParallelRequests);

        CountDownLatch countDownLatch = new CountDownLatch(numberOfRequest);
        for (int i = 0; i < numberOfRequest; i++) {
            executorService.execute(() -> {
                try {
                    given()
                            .when().get("/hello")
                            .then()
                            .statusCode(200)
                            .body(is("Hello testUser. Greetings from RESTEasy Reactive"));
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
        executorService.shutdown();
    }

}