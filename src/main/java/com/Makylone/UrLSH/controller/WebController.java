package com.Makylone.UrLSH.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.Makylone.UrLSH.dto.ShortenResponse;
import com.Makylone.UrLSH.service.UrlShorterService;

@Controller
public class WebController {

    private final UrlShorterService urlShorterService;
    private final String baseUrl;

    public WebController(UrlShorterService service, @Value("${app.base-url}") String burl){
        this.urlShorterService = service;
        this.baseUrl = burl;
    }

    @GetMapping("/")
    public String showForm(){
        return "index";
    }

    @PostMapping("/")
    public String ShortenUrl(@RequestParam String originalUrl, Model model){
        LocalDateTime expireAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS).plusMonths(2);
        ShortenResponse response = urlShorterService.shortenURL(originalUrl, expireAt);

        String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        
        String fullUrl = cleanBaseUrl + "/api/v1/shorten/" + response.shortUrl();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        String formattedDate = response.expireAt().format(formatter);

        model.addAttribute("shortUrl", fullUrl);
        model.addAttribute("expirationDate", formattedDate);
        
        return "index";
    }
}
