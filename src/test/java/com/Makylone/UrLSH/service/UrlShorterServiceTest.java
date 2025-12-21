package com.Makylone.UrLSH.service;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.Makylone.UrLSH.dto.ShortenResponse;
import com.Makylone.UrLSH.entity.UrlMapping;
import com.Makylone.UrLSH.repository.UrlMappingRepository;
import com.Makylone.UrLSH.util.Base62Encoder;

@ExtendWith(MockitoExtension.class)
public class UrlShorterServiceTest {


    @Mock UrlMappingRepository urlMappingRepository;

    @Mock Base62Encoder encoder;

    @InjectMocks UrlShorterService urlShorterService;

    @Test
    void shouldGenerateShortcode_WhenUrlIsNew(){
        // -- A. GIVEN --
        String url = "http://google.com";
        UrlMapping savedWithId = new UrlMapping(url);
        savedWithId.setId(1005L);
        when(urlMappingRepository.save(any(UrlMapping.class))).thenReturn(savedWithId);
        // -- B. WHEN -- 
        when(encoder.encode(1005L)).thenReturn("gd");

        ShortenResponse response =  urlShorterService.shortenURL(url);

         // -- C. THEN --
        assertThat(response.shortUrl()).isEqualTo("gd");

        verify(encoder).encode(1005L);

        verify(urlMappingRepository, atLeastOnce()).save(any(UrlMapping.class));
    }

    @Test
    void shouldRecoverOriginalUrl_WhenShortCodeIsDecoded(){
        String shortenCode = "1";
        long decodedCode = 100L;
        String originalUrl = "http://google.com";

        when(encoder.decode(shortenCode)).thenReturn(decodedCode);

        UrlMapping urlFound = new UrlMapping(originalUrl);
        urlFound.setId(decodedCode);

        when(urlMappingRepository.findById(decodedCode)).thenReturn(Optional.of(urlFound));

        String response = urlShorterService.getOriginalUrl("1");

        assertThat(response).isEqualTo(originalUrl);

    }

    @Test
    void shouldThrowException_WhenShortCodeUnknown(){
        String shortenCode = "4KGtFLV";
        long decodedCode = 99999;

        when(encoder.decode(shortenCode)).thenReturn(decodedCode);

        when(urlMappingRepository.findById(decodedCode)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            urlShorterService.getOriginalUrl(shortenCode);
        });
    }
}
