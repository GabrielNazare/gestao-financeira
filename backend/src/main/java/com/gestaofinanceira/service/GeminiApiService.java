package com.gestaofinanceira.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiApiService {
    private static final Logger log = LoggerFactory.getLogger(GeminiApiService.class);
    private final String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiApiService(@Value("${gemini.api-key}") String apiKey, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public String generateContent(String prompt) {
        return generateContent(prompt, null, null);
    }

    public String generateContent(String prompt, String mimeType, String base64Data) {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("COLE_SUA_CHAVE")) {
            throw new RuntimeException("Chave de API do Gemini não configurada. Configure no application.properties.");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        List<Map<String, Object>> parts;

        if (base64Data != null && mimeType != null) {
            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mimeType", mimeType);
            inlineData.put("data", base64Data);

            Map<String, Object> filePart = new HashMap<>();
            filePart.put("inlineData", inlineData);

            parts = List.of(textPart, filePart);
        } else {
            parts = List.of(textPart);
        }

        Map<String, Object> contents = new HashMap<>();
        contents.put("parts", parts);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(contents));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        int maxRetries = 5;
        int attempt = 0;
        long delay = 3000;

        while (true) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE || e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    attempt++;
                    if (attempt >= maxRetries) {
                        String errorBody = e.getResponseBodyAsString();
                        log.error("Erro HTTP da API Gemini após " + maxRetries + " tentativas: {}", errorBody, e);
                        throw new RuntimeException("Servidores da IA muito ocupados no momento. Tente novamente em alguns instantes.");
                    }
                    log.warn("Servidor Gemini ocupado. Tentativa {}/{}. Aguardando {}ms...", attempt, maxRetries, delay);
                    try { Thread.sleep(delay); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    delay *= 2;
                } else {
                    String errorBody = e.getResponseBodyAsString();
                    log.error("Erro HTTP da API Gemini: {}", errorBody, e);
                    throw new RuntimeException("Erro do Gemini: " + errorBody);
                }
            } catch (Exception e) {
                log.error("Erro ao chamar API do Gemini Studio", e);
                throw new RuntimeException("Falha na comunicação com a IA via REST API.");
            }
        }
    }
}

