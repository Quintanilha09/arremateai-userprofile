package com.arremateai.userprofile.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * Filtro que valida o header X-Gateway-Auth em todas as requisições para /api/**
 * (exceto /api/internal/**, que tem sua própria autenticação via X-Internal-Api-Key).
 *
 * Segurança: comparação de segredo em tempo constante para prevenir timing attacks.
 */
@Component
public class GatewayAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewayAuthFilter.class);

    @Value("${app.gateway.secret:}")
    private String gatewaySecret;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Endpoints internos têm sua própria autenticação
        // Actuator /health é público
        return path.startsWith("/api/internal/") || path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        if (gatewaySecret == null || gatewaySecret.isBlank()) {
            log.warn("GATEWAY_SHARED_SECRET não configurado — rejeitando todas as requisições");
            writeError(response, 503, "Serviço indisponível — configuração ausente");
            return;
        }

        String receivedSecret = request.getHeader("X-Gateway-Auth");

        if (receivedSecret == null || !constantTimeEquals(gatewaySecret, receivedSecret)) {
            log.warn("Requisição rejeitada — X-Gateway-Auth inválido ou ausente para: {}", request.getServletPath());
            writeError(response, 401, "Acesso não autorizado");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Comparação em tempo constante para prevenir timing attacks.
     */
    private boolean constantTimeEquals(String expected, String received) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] receivedBytes = received.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, receivedBytes);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
