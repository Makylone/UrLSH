package com.Makylone.UrLSH.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.Makylone.UrLSH.dto.ShortenResponse;
import com.Makylone.UrLSH.entity.UrlMapping;
import com.Makylone.UrLSH.exception.UrlExpiredException;
import com.Makylone.UrLSH.repository.UrlMappingRepository;
import com.Makylone.UrLSH.util.Base62Encoder;

import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;

@Service
public class UrlShorterService {

    private final UrlMappingRepository urlRepository;
    private final Base62Encoder base62encoder;

    public UrlShorterService(UrlMappingRepository repository, Base62Encoder encoder){
        this.base62encoder = encoder;
        this.urlRepository = repository;
    }

    @Transactional
    public ShortenResponse shortenURL(String originalUrl, @Nullable LocalDateTime expireAt){
        // 1. Save "empty" to get an ID
        UrlMapping urlMapping = new UrlMapping(originalUrl);
        if(expireAt != null){
            urlMapping.setExpireAt(expireAt.plusMonths(1));
        }
        UrlMapping savedUrl = urlRepository.save(urlMapping);

        // 2. Get the id to conver to shortcode
        String shortCode = base62encoder.encode(savedUrl.getId());

        // 3. Update the record 
        savedUrl.setShortCode(shortCode);
        urlRepository.saveAndFlush(savedUrl);
        return new ShortenResponse(shortCode, expireAt);
    }

    public String getOriginalUrl(String shortcode) {
        
        long id = base62encoder.decode(shortcode);

        UrlMapping mapping = urlRepository.findById(id).orElseThrow(() -> new NoSuchElementException());

        if(mapping.getExpireAt() != null && mapping.getExpireAt().isBefore(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))){
            throw new UrlExpiredException("The requested URL has expired");
        }

        return mapping.getOriginalUrl();
    }


}
