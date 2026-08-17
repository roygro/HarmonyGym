package com.example.harmonyGymBack.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaypalService {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1. Obtener el access token
    private String getAccessToken() {
        String credentials = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encoded);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/v1/oauth2/token", request, Map.class
        );

        return (String) response.getBody().get("access_token");
    }

    // 2. Crear una orden de pago
    public Map<String, Object> crearOrden(String monto, String descripcion) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> amount = new HashMap<>();
        amount.put("currency_code", "MXN");
        amount.put("value", monto);

        Map<String, Object> purchaseUnit = new HashMap<>();
        purchaseUnit.put("amount", amount);
        purchaseUnit.put("description", descripcion);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("intent", "CAPTURE");
        requestBody.put("purchase_units", List.of(purchaseUnit));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/v2/checkout/orders", request, Map.class
        );

        return response.getBody();
    }

    // 3. Capturar (confirmar) el pago
    public Map<String, Object> capturarOrden(String orderId) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/v2/checkout/orders/" + orderId + "/capture", request, Map.class
        );

        return response.getBody();
    }
}