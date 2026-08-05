package com.demo.my_backend_java.service;

import org.springframework.stereotype.Service;

import io.github.bucket4j.distributed.proxy.ProxyManager;

@Service
public class RateLimitingService {
    private static final int REQUEST_PER_MINUTE = 10;

    private ProxyManager<String> proxyManager;

    public RateLimitingService(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }
}
