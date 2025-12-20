package com.Makylone.UrLSH.dto;

import java.time.LocalDateTime;

public record ShortenResponse(String shortUrl, LocalDateTime expireAt) {
    
}
