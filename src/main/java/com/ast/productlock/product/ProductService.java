package com.ast.productlock.product;

import com.ast.productlock.aspects.LockedProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    @LockedProduct
    public String doSomething(UUID productId, int fakeNum) {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String msg = "Do something with product " + productId;
        System.out.println(msg);
        return msg;
    }
}
