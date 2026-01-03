package com.Makylone.UrLSH.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.Makylone.UrLSH.dto.ShortenRequest;
import com.Makylone.UrLSH.dto.ShortenResponse;
import com.Makylone.UrLSH.exception.UrlExpiredException;
import com.Makylone.UrLSH.service.UrlShorterService;



@WebMvcTest(UrlShorterController.class)
public class UrlShorterControllerTest {
    
    @Autowired
    public MockMvc mockMvc;

    @MockitoBean
    private UrlShorterService urlShorterService;

    @Test
    void shouldReturnShortUrl_WhenRequestIsValid() throws Exception {
        String url = "https://google.com";
        String shorterCode = "AbC12";
        LocalDateTime stableTime = LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);
        // Mock the service call
        when(urlShorterService.shortenURL(anyString(), any())).thenReturn(new ShortenResponse(shorterCode, stableTime));

        // Make the actual call to the API
        String requestBody = """
            {
                "originalUrl": "%s"
            }
            """.formatted(url);
        // Perform the POST request
        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl").value(shorterCode))
                .andExpect(jsonPath("$.expireAt").value(stableTime.toString()));
    }

    @Test
    void shouldReturnBadRequest_WhenOriginalUrlIsEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequest_WhenOriginalUrlIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\": \"not-a-valid-url\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRedirectToOriginal_WhenShortCodeExist() throws Exception {
        when(urlShorterService.getOriginalUrl("1")).thenReturn("https://google.com");
        mockMvc.perform(get("/api/v1/shorten/1"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://google.com"));
    }

    @Test
    void shouldReturnNotFound_WHenShortCodeDoesNotExist() throws Exception {
        when(urlShorterService.getOriginalUrl("99999999")).thenThrow(new NoSuchElementException("The shortcode does not exist"));
        mockMvc.perform(get("/api/v1/shorten/99999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnGone_WhenUrlIsExpire() throws Exception{
        String shortCode = "expired123";
        when(urlShorterService.getOriginalUrl(shortCode))
        .thenThrow(new UrlExpiredException("URL has expired"));

        // ACT & ASSERT
        mockMvc.perform(get("/api/v1/shorten/{shortCode}", shortCode))
                .andExpect(status().isGone());
    }

    @Test
    void shouldCreateShortUrl_WithExpirationDate() throws Exception {
        // ARRANGE
        String originalUrl = "https://example.com";
        // Create a stable future date for testing
        String futureDate = LocalDateTime.now().plusDays(5).truncatedTo(ChronoUnit.SECONDS).toString();
        
        ShortenRequest request = new ShortenRequest(originalUrl, LocalDateTime.parse(futureDate));
        
        // Mock service response
        when(urlShorterService.shortenURL(anyString(), any()))
            .thenReturn(new ShortenResponse("short123", LocalDateTime.parse(futureDate)));

        // ACT & ASSERT
        mockMvc.perform(post("/api/v1/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"originalUrl\":\"" + originalUrl + "\", \"expiresAt\":\"" + futureDate + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expireAt").value(futureDate));
    }
}
