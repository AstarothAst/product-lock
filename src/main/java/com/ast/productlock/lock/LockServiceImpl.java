package com.ast.productlock.lock;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static java.util.Objects.isNull;

/**
 * Вместо этого сервиса должен быть репозиторий сохраняющий все в БД на случай работы в многоподовом режиме
 */
@Service
public class LockServiceImpl implements LockService {

    /**
     * Общий список локов, доступный из всех потоков
     */
    private final Map<UUID, LockEntity> locks = new ConcurrentHashMap<>();

    /**
     * Продукты заблокированные данным потоком
     */
    private final ThreadLocal<Set<UUID>> ownLocks = new ThreadLocal<>();

    /**
     * При работе с БД метод должен выполняться транзакционно и с оптимистической блокировкой
     * при помощи аннотации org.springframework.data.annotation.Version
     */
    public synchronized void tryLock(UUID productId) {
        if (ownLockAlreadyGet(productId)) {
            return;
        }

        if (!locks.containsKey(productId)) {
            addToOwnLocks(productId);
            LockEntity lockEntity = LockEntity.of(productId);
            locks.put(productId, lockEntity);
        } else {
            throw productLockedError(productId);
        }
    }

    @Override
    public synchronized void tryUnlock(UUID productId) {
        removeFromOwnLocks(productId);
        locks.remove(productId);
    }

    @Override
    public synchronized boolean isLocked(UUID productId) {
        return locks.containsKey(productId);
    }

    @Override
    public synchronized void refreshLock(UUID productId) {
        if (!locks.containsKey(productId)) {
            throw lockDoesNotExistError(productId);
        }

        if(!ownLockAlreadyGet(productId)){
            throw notOwnLockForRefreshError(productId);
        }

        LockEntity lockEntity = locks.get(productId);

        Duration lockDuration = lockEntity.getLockDuration();
        lockEntity.setLockEnd(Instant.now().plus(lockDuration));
        lockEntity.refreshInc();

        locks.put(lockEntity.getLockedProduct(), lockEntity);
    }

    private void addToOwnLocks(UUID productId) {
        if (isNull(ownLocks.get())) {
            ownLocks.set(new CopyOnWriteArraySet<>());
        }
        ownLocks.get().add(productId);
    }

    private void removeFromOwnLocks(UUID productId) {
        Optional.ofNullable(ownLocks.get()).ifPresent(uuids -> uuids.remove(productId));
    }

    private boolean ownLockAlreadyGet(UUID productId) {
        return Optional.ofNullable(ownLocks.get())
                .map(uuids -> uuids.contains(productId))
                .orElse(false);
    }

    private RuntimeException productLockedError(UUID productId) {
        return new RuntimeException("Product %s is locked".formatted(productId));
    }

    private RuntimeException lockDoesNotExistError(UUID productId) {
        return new RuntimeException("Lock for product %s does not exist".formatted(productId));
    }

    private RuntimeException notOwnLockForRefreshError(UUID productId) {
        return new RuntimeException("The lock owner for product %s is different thread".formatted(productId));
    }
}
