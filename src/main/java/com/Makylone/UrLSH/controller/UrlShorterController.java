package com.Makylone.UrLSH.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.Makylone.UrLSH.dto.ShortenRequest;
import com.Makylone.UrLSH.dto.ShortenResponse;
import com.Makylone.UrLSH.service.UrlShorterService;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/shorten")
public class UrlShorterController {
    
    private final UrlShorterService urlShorterService;

    // Store the bucket per ip to avoid global rate limit
    private final Map<String, Bucket> ipCache = new ConcurrentHashMap<>();

    public UrlShorterController(UrlShorterService urlShorterService) {
        this.urlShorterService = urlShorterService;
    }

    private Bucket createBucket(long tokens, long tokenRegenerated, Duration periodOfRegeneration){
        return Bucket.builder()
                     .addLimit(limit -> limit.capacity(tokens).refillGreedy(tokenRegenerated, periodOfRegeneration))
                     .build();
    }

    // Helper to find the bucket with a given ip address
    private Bucket resolveBucket(String ip, long tokens, long tokenRegenerated, Duration periodOfRegeneration){
        // Check if there is already the ip in the HashMap, if not, it calls the createBucket method
        return ipCache.computeIfAbsent(ip, i -> createBucket(tokens, tokenRegenerated, periodOfRegeneration));
    }

    @PostMapping
    public ResponseEntity<Object> shorten(@RequestBody @Valid ShortenRequest request, HttpServletRequest servletRequest) {
        String ip = servletRequest.getRemoteAddr();

        // Limit of 30 request per minutes, then regenerate 10 token every minutes
        Bucket bucket = resolveBucket(ip, 30, 10, Duration.ofMinutes(1));

        if(bucket.tryConsume(1)){
            LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS).plusMonths(2);
            // Delegates the work to the service
            ShortenResponse response = urlShorterService.shortenURL(request.originalUrl(), currentTime);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("shortCode", response.shortUrl());
            body.put("expireAt", response.expireAt());
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        }
        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "\"Rate limit exceeded. Try again later.\"");
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode, HttpServletRequest servletRequest){
        String ip = servletRequest.getRemoteAddr();

        Bucket bucket = resolveBucket(ip, 120, 60, Duration.ofMinutes(1));
        // W`ill throw 404 or 500 if not foud
        if(bucket.tryConsume(1)){
            String originalUrl = urlShorterService.getOriginalUrl(shortCode);

            return ResponseEntity.status(HttpStatus.FOUND).header("Location", originalUrl).build();
        }

        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "\"Rate limit exceeded. Try again later.\"");
    }
}
