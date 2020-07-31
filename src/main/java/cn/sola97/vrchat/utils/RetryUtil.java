package cn.sola97.vrchat.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class RetryUtil {
    private static final Logger logger = LoggerFactory.getLogger(RetryUtil.class);
    public static <R> CompletableFuture<R> retry(Supplier<CompletableFuture<R>> supplier, int maxRetries) {
        CompletableFuture<R> f = supplier.get();
        for (int i = 0; i < maxRetries; i++) {
            f = f.thenApply(CompletableFuture::completedFuture)
                    .exceptionally(t -> {
                        logger.error("RetryUtil:", t);
                        return supplier.get();
                    })
                    .thenCompose(Function.identity());
        }
        return f;
    }
}
