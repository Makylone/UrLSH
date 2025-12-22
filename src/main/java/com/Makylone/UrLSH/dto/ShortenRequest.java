package com.Makylone.UrLSH.dto;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;

public record ShortenRequest(
    @NotBlank(message = "The url cannot be empty")
    @URL(message = "The url must be valid")
    String originalUrl,
    LocalDateTime expireAt
) {}
