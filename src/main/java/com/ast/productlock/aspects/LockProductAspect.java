package com.ast.productlock.aspects;

import com.ast.productlock.lock.LockService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Aspect
@RequiredArgsConstructor
public class LockProductAspect {

    private final LockService lockService;

    @Before("@annotation(LockedProduct)")
    public void checkLocks(JoinPoint joinPoint) {
        Map<String, Object> parametersMap = getParametersMap(joinPoint);
        UUID productId = (UUID) parametersMap.get("productId");

        if (lockService.isLocked(productId)) {
            throw productLockedError(productId);
        }
    }

    private Map<String, Object> getParametersMap(JoinPoint joinPoint) {
        Object[] parameterValues = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();

        return IntStream.of(0, parameterNames.length - 1)
                .boxed()
                .collect(Collectors.toMap(
                        i -> parameterNames[i],
                        i -> parameterValues[i])
                );
    }

    private RuntimeException productLockedError(UUID productId) {
        return new RuntimeException("Product %s is locked".formatted(productId));
    }
}
