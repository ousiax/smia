package com.optimagrowth.gateway.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Order(1)
@Component
public class TrackingFilter implements GlobalFilter {
    @Autowired
    private Tracer tracer;

    @Autowired
    FilterUtils filterUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
        if (isCorrelationIdPresent(requestHeaders)) {
            log.debug("tmx-correlation-id found in tracking filter: {}. ",
                    filterUtils.getCorrelationId(requestHeaders));
        } else {
            String correlationID = generateCorrelationId();
            exchange = filterUtils.setCorrelationId(exchange, correlationID);
            log.debug("tmx-correlation-id generated in tracking filter: {}.", correlationID);
        }

        log.debug("The authentication name from the token is : {}.", getUsername(requestHeaders));

        return chain.filter(exchange);
    }

    private boolean isCorrelationIdPresent(HttpHeaders requestHeaders) {
        return filterUtils.getCorrelationId(requestHeaders) != null;
    }

    private String generateCorrelationId() {
        // return java.util.UUID.randomUUID().toString();
        return tracer.currentSpan().context().traceId();
    }

    private String getUsername(HttpHeaders requestHeaders) {
        String username = "";
        if (filterUtils.getAuthToken(requestHeaders) != null) {
            String authToken = filterUtils.getAuthToken(requestHeaders)
                    .replace("Bearer ", "");
            try {
                JsonNode jsonNode = decodeJWT(authToken);
                username = jsonNode.get("preferred_username").asText();
            } catch (Exception e) {
                e.printStackTrace();
                log.debug(e.getMessage());
            }
        }
        return username;
    }

    private JsonNode decodeJWT(String jwt) throws JsonMappingException, JsonProcessingException {
        String[] tokens = jwt.split("\\.");
        String payload = new String(Base64.decodeBase64(tokens[1]));
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(payload);
    }

}
