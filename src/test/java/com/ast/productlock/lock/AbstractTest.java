package com.ast.productlock.lock;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractTest {

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
    }

    public void assertMessageContains(Throwable e, Object... patterns) {
        if (!isMessageContains(e, patterns)) {
            throw new RuntimeException("Test assertion error - patterns not found in error message: " + e.getMessage());
        }
    }

    public int randomInt() {
        return Math.abs(ThreadLocalRandom.current().nextInt());
    }

    public long randomLong() {
        return Math.abs(ThreadLocalRandom.current().nextLong());
    }

    public double randomDouble() {
        return Math.abs(ThreadLocalRandom.current().nextDouble());
    }

    public String randomString() {
        return randomString(10);
    }

    public String randomString(int length) {
        int leftLimit = 97; // 'a'
        int rightLimit = 122; //  'z'

        Random random = new Random();
        return random.ints(leftLimit, rightLimit + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public void sleep(Duration duration) {
        Awaitility.await().pollDelay(duration).until(() -> true);
    }

    private boolean isMessageContains(Throwable e, Object... patterns) {
        String message = e.getMessage().toLowerCase();
        return Arrays.stream(patterns)
                .map(pattern -> pattern.toString().toLowerCase())
                .allMatch(message::contains);
    }
}