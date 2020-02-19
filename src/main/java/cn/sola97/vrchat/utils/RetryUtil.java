package cn.sola97.vrchat.utils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class RetryUtil {

    public static <R> CompletableFuture<R> retry(Supplier<CompletableFuture<R>> supplier, int maxRetries) {
        CompletableFuture<R> f = supplier.get();
        for (int i = 0; i < maxRetries; i++) {
            f = f.thenApply(CompletableFuture::completedFuture)
                    .exceptionally(t -> {
                        System.out.println("retry for: " + t.getMessage());
                        return supplier.get();
                    })
                    .thenCompose(Function.identity());
        }
        return f;
    }
}
