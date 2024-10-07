package com.developer.payment.client;

import com.developer.payment.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="developer-gateway",contextId = "developer-message",configuration = {FeignRequestInterceptor.class})
public interface MessageClient {




}
