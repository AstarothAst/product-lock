package com.ast.productlock.lock;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
@Setter
public class LockEntity {

    public static Duration DEFAULT_LOCK_DURATION = Duration.ofSeconds(1);

    public static LockEntity of(UUID lockedProduct) {
        Instant now = Instant.now();

        return LockEntity.builder()
                .lockedProduct(lockedProduct)
                .lockStart(now)
                .lockDuration(DEFAULT_LOCK_DURATION)
                .lockEnd(now.plus(DEFAULT_LOCK_DURATION))
                .refreshCount(0)
                .build();
    }

    private UUID lockedProduct;
    private Instant lockStart;
    private Duration lockDuration;
    private int refreshCount;
    private Instant lockEnd;

    public void refreshInc(){
        refreshCount++;
    }
}