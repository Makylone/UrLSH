package com.Makylone.UrLSH.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Makylone.UrLSH.dto.ShortenRequest;
import com.Makylone.UrLSH.dto.ShortenResponse;
import com.Makylone.UrLSH.service.UrlShorterService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/shorten")
public class UrlShorterController {
    
    
    private final UrlShorterService urlShorterService;

    public UrlShorterController(UrlShorterService urlShorterService) {
        this.urlShorterService = urlShorterService;
    }

    @PostMapping
    public ShortenResponse shorten(@RequestBody @Valid ShortenRequest request) {
        // Delegates the work to the service
        return urlShorterService.shortenURL(request.originalUrl());
    }

}
