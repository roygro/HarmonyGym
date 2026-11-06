package com.example.harmonyGymBack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class PaypalService {

    private static final Logger log = LoggerFactory.getLogger(PaypalService.class);

    @Value("${paypal.client-id:dummy}")
    private String clientId;

    @Value("${paypal.client-secret:dummy}")
    private String clientSecret;

    @Value("${paypal.base-url:https://api-m.sandbox.paypal.com}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public PaypalService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    private String getAccessToken() {
        try {
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

            if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
                throw new RuntimeException("Respuesta inválida de PayPal al obtener access_token");
            }

            return (String) response.getBody().get("access_token");
        } catch (HttpStatusCodeException e) {
            log.error("Error HTTP al obtener token de PayPal: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error comunicando con PayPal: " + e.getStatusCode());
        } catch (RestClientException e) {
            log.error("Error de conexión con PayPal: {}", e.getMessage());
            throw new RuntimeException("No se pudo conectar con el servicio de PayPal: " + e.getMessage());
        }
    }

    public Map<String, Object> crearOrden(String monto, String descripcion) {
        try {
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

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new RuntimeException("PayPal devolvió una respuesta vacía al crear la orden.");
            }

            String orderId = (String) body.get("id");
            String approveUrl = null;

            Object linksObj = body.get("links");
            if (linksObj instanceof List<?> links) {
                for (Object item : links) {
                    if (item instanceof Map<?, ?> linkMap) {
                        if ("approve".equalsIgnoreCase(String.valueOf(linkMap.get("rel")))) {
                            approveUrl = (String) linkMap.get("href");
                            break;
                        }
                    }
                }
            }

            Map<String, Object> result = new HashMap<>(body);
            result.put("orderId", orderId);
            result.put("approveUrl", approveUrl);
            return result;

        } catch (HttpStatusCodeException e) {
            log.error("Error al crear orden en PayPal: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("PayPal rechazó la creación de orden: " + e.getStatusCode());
        } catch (RestClientException e) {
            log.error("Error de red con PayPal: {}", e.getMessage());
            throw new RuntimeException("Error de conexión al crear orden de PayPal: " + e.getMessage());
        }
    }

    public Map<String, Object> capturarOrden(String orderId) {
        try {
            String token = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/v2/checkout/orders/" + orderId + "/capture", request, Map.class
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyMap();

        } catch (HttpStatusCodeException e) {
            log.error("Error al capturar orden en PayPal: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error al capturar pago en PayPal: " + e.getStatusCode());
        } catch (RestClientException e) {
            log.error("Error de red al capturar PayPal: {}", e.getMessage());
            throw new RuntimeException("Error de conexión al capturar PayPal: " + e.getMessage());
        }
    }
}
