package com.Makylone.UrLSH.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.Makylone.UrLSH.dto.ShortenResponse;
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

        // Mock the service call
        when(urlShorterService.shortenURL(anyString())).thenReturn(new ShortenResponse(shorterCode, null));

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").value(shorterCode));
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
}
