package com.Makylone.UrLSH.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.Makylone.UrLSH.dto.ShortenResponse;
import com.Makylone.UrLSH.entity.UrlMapping;
import com.Makylone.UrLSH.repository.UrlMappingRepository;
import com.Makylone.UrLSH.util.Base62Encoder;

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
    public ShortenResponse shortenURL(String originalUrl){
        // 1. Save "empty" to get an ID
        UrlMapping urlMapping = new UrlMapping(originalUrl);
        UrlMapping savedUrl = urlRepository.save(urlMapping);

        // 2. Get the id to conver to shortcode
        String shortCode = base62encoder.encode(savedUrl.getId());

        // 3. Update the record 
        savedUrl.setShortCode(shortCode);
        urlRepository.saveAndFlush(savedUrl);
        return new ShortenResponse(shortCode, null);
    }

    public String getOriginalUrl(String shortcode) {
        
        long id = base62encoder.decode(shortcode);

        UrlMapping mapping = urlRepository.findById(id).orElseThrow(() -> new NoSuchElementException());

        return mapping.getOriginalUrl();
    }


}
