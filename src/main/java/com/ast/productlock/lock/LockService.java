package com.ast.productlock.lock;

import java.util.UUID;

public interface LockService {

    void tryLock(UUID productId);

    void tryUnlock(UUID productId);

    void refreshLock(UUID productId);

    boolean isLocked(UUID productId);
}
