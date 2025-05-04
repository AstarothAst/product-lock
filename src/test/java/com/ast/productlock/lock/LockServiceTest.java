package com.ast.productlock.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LockServiceTest extends AbstractTest {

    private LockService lockService;

    @BeforeEach
    public void beforeEach() {
        this.lockService = new LockServiceImpl();
    }

    @Test
    @DisplayName("Простой лок с попыткой тут же взять его еще раз в том же потоке")
    public void test_10() {
        //given
        UUID productId = UUID.randomUUID();

        //then
        assertDoesNotThrow(() -> lockService.tryLock(productId));
        assertDoesNotThrow(() -> lockService.tryLock(productId));
    }

    @Test
    @DisplayName("Простой лок с попыткой взять его еще раз через таймаут")
    public void test_20() {
        //given
        UUID productId = UUID.randomUUID();

        //when
        lockService.tryLock(productId);
        sleep(LockEntity.DEFAULT_LOCK_DURATION);

        //then
        assertDoesNotThrow(() -> lockService.tryLock(productId));
    }

    @Test
    @DisplayName("Снятие лока в finally")
    public void test_30() {
        //given
        UUID productId = UUID.randomUUID();

        //when
        lockService.tryLock(productId);
        try {
            //do something with product
        } finally {
            lockService.tryUnlock(productId);
        }

        //then
        assertDoesNotThrow(() -> lockService.tryLock(productId));
    }

    @Test
    @DisplayName("Обновление лока в том же потоке")
    public void test_40() {
        //given
        UUID productId = UUID.randomUUID();

        //when
        lockService.tryLock(productId);
        sleep(LockEntity.DEFAULT_LOCK_DURATION);

        //then
        assertDoesNotThrow(() -> lockService.refreshLock(productId));
    }

    @Test
    @DisplayName("Попытка взять один и тот же лок из двух потоков")
    public void test_50() throws InterruptedException, TimeoutException {
        //given
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        UUID productId = UUID.randomUUID();

        Runnable task = () -> lockService.tryLock(productId);

        //when
        executor.submit(task);
        Future<?> future = executor.submit(task);

        Exception e = assertThrows(ExecutionException.class, future::get);

        //then
        assertMessageContains(e.getCause(), productId, "is locked");
    }

    @Test
    @DisplayName("Долгий рефреш лока")
    public void test_60() {
        //given
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        UUID productId = UUID.randomUUID();

        Runnable task = () -> {
            lockService.tryLock(productId);
            for (int i = 0; i < 5; i++) {
                lockService.refreshLock(productId);
            }
        };

        //when
        executor.submit(task);
        Future<?> future = executor.submit(task);

        sleep(Duration.ofSeconds(3));
        Exception e = assertThrows(ExecutionException.class, future::get);

        //then
        assertMessageContains(e.getCause(), productId, "is locked");
    }

    @Test
    @DisplayName("Рефреш не взятого лока")
    public void test_70() {
        //given
        UUID productId = UUID.randomUUID();

        //when
        RuntimeException e = assertThrows(RuntimeException.class, () -> lockService.refreshLock(productId));

        //then
        assertMessageContains(e, productId, "does not exist");
    }

    @Test
    @DisplayName("Рефреш лока взятого другим потоком")
    public void test_80() {
        //given
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        UUID productId = UUID.randomUUID();

        Runnable task1 = () -> lockService.tryLock(productId);
        Runnable task2 = () -> lockService.refreshLock(productId);

        //when
        executor.submit(task1);
        Future<?> future = executor.submit(task2);

        Exception e = assertThrows(ExecutionException.class, future::get);

        //then
        assertMessageContains(e.getCause(), productId, "different thread");
    }
}