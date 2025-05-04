package com.ast.productlock.controller;

import com.ast.productlock.lock.LockService;
import com.ast.productlock.product.ProductService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    private final LockService lockService;
    private final ProductService productService;

    @GetMapping("/lock")
    public String lockProduct(UUID productId) {
        try {
            lockService.tryLock(productId);
            return "ok";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @GetMapping("/do")
    public String doSomething(UUID productId) {
        lockService.tryLock(productId);
        try {
            return productService.doSomething(productId, 123);
        } catch (Exception e) {
            return e.getMessage();
        } finally {
            lockService.tryUnlock(productId);
        }
    }
}
