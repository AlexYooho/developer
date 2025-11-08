package com.developer.rpc.client;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Supplier;

public class RpcExecutor {

    public static <T> T execute(Supplier<T> call){
        try {
            SecurityContextHolder.clearContext();
            return call.get();
        }finally {
            SecurityContextHolder.clearContext();
        }
    }

}
