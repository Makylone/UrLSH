package com.Makylone.UrLSH.service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
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
import org.mockito.junit.jupiter.MockitoSettings; // Import this
import org.mockito.quality.Strictness; // Import this

import com.Makylone.UrLSH.dto.ShortenResponse;
import com.Makylone.UrLSH.entity.UrlMapping;
import com.Makylone.UrLSH.exception.UrlExpiredException;
import com.Makylone.UrLSH.repository.UrlMappingRepository;
import com.Makylone.UrLSH.util.Base62Encoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 👈 This fixes "UnnecessaryStubbing"
public class UrlShorterServiceTest {

    @Mock UrlMappingRepository urlMappingRepository;
    @Mock Base62Encoder encoder;

    @InjectMocks UrlShorterService urlShorterService;

    @Test
    void shouldGenerateShortcode_WhenUrlIsNew(){
        String url = "http://google.com";
        UrlMapping savedWithId = new UrlMapping(url);
        savedWithId.setId(1005L);
        when(urlMappingRepository.save(any(UrlMapping.class))).thenReturn(savedWithId);
        when(encoder.encode(1005L)).thenReturn("gd");

        ShortenResponse response =  urlShorterService.shortenURL(url, null);

        assertThat(response.shortUrl()).isEqualTo("gd");
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
        // Note: expireAt is null here, but our Service fix (Step 1) handles it now!

        when(urlMappingRepository.findById(decodedCode)).thenReturn(Optional.of(urlFound));

        String response = urlShorterService.getOriginalUrl("1");

        assertThat(response).isEqualTo(originalUrl);
    }

    @Test
    void shouldThrowException_WhenShortCodeUnknown(){
        String shortenCode = "4KGtFLV";
        long decodedCode = 99999L;

        when(encoder.decode(shortenCode)).thenReturn(decodedCode);
        when(urlMappingRepository.findById(decodedCode)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            urlShorterService.getOriginalUrl(shortenCode);
        });
    }

    @Test
    void shouldThrowException_WhenShortCodeIsExpire(){
        String shortenCode = "3H7";
        long decodedCode = 11L;

        when(encoder.decode(shortenCode)).thenReturn(decodedCode);

        UrlMapping expiredUrl = new UrlMapping();
        expiredUrl.setOriginalUrl("https://example.com");
        // Ensure this date is definitely in the past
        expiredUrl.setExpireAt(LocalDateTime.now().minusDays(1)); 

        when(urlMappingRepository.findById(decodedCode)).thenReturn(Optional.of(expiredUrl));

        assertThrows(UrlExpiredException.class, () -> {
            urlShorterService.getOriginalUrl(shortenCode);
        });
    }

    @Test
    void shouldReturnUrl_WhenNotExpire(){
        String shortenCode = "DWnK";
        long decodedCode = 13L;
        String originalLink = "https://example.com";

        when(encoder.decode(shortenCode)).thenReturn(decodedCode);

        UrlMapping validUrl = new UrlMapping();
        validUrl.setOriginalUrl(originalLink);
        // Ensure this date is definitely in the future
        validUrl.setExpireAt(LocalDateTime.now().plusDays(1));

        when(urlMappingRepository.findById(decodedCode)).thenReturn(Optional.of(validUrl));

        String result = urlShorterService.getOriginalUrl(shortenCode);

        assertEquals(originalLink, result);
    }
}