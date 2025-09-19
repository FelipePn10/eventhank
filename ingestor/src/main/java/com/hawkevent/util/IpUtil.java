package com.hawkevent.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

// IP real do client
public class IpUtil {

    /*
    * @param exchange ServerWebExchange contém informações da requisição
    * @return String endereço Ip do cliente
    * */
    public static String resolveClientIp(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();

        // X-Forwarded-For (padrão)
        String clientIp = extractIpFromHeader(request, "X_Forwarded-For");
        if (isValidIp(clientIp)) {
            return clientIp;
        }

        // Cabeçalhos comuns de proxy
        String[] proxyHeaders = {
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "X-Real-IP"
        };
        for (String header : proxyHeaders) {
            clientIp = extractIpFromHeader(request, header);
            if (isValidIp(clientIp)) {
                return clientIp;
            }
        }

        return getRemoteAddress(request);
    }

    /*
    * Extrai e valida o IP de um cabeçalho
    * @param request Request HTTP
    * @param headerName Nome do cabeçalho a ser verificado
    * @return IP extraído ou null se inválido
    * */
    private  static String extractIpFromHeader(ServerHttpRequest request, String headerName) {
        String headerValue = request.getHeaders().getFirst(headerName);
        if (!StringUtils.hasText(headerValue) || "unknown".equalsIgnoreCase(headerValue)) {
            return null;
        }

        String[] ips = headerValue.split(",");
        String clientIp = ips[0].trim();
        return isValidIp(clientIp) ? clientIp : null;
    }

    /*
    * @param ip Endereço Ip a ser validado
    * @return true se o IP for válido
    * */
    private static boolean isValidIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }

        return !"unknown".equalsIgnoreCase(ip) &&
                !ip.startsWith("127.") &&
                !ip.startsWith("0.") &&
                !ip.startsWith("10.") &&
                !ip.startsWith("172.16.") &&
                !ip.startsWith("192.168.") &&
                !"::1".equals(ip) &&
                !"0:0:0:0:0:0:0:1".equals(ip);
    }

    /*
    * Obtém o endereço remoto direto da request como falllback
    * @param request Request HTTP
    * @return Endereço Ip remoto ou localhost como último fallback
    * */
    private static String getRemoteAddress(ServerHttpRequest request) {
        return Optional.ofNullable(request.getRemoteAddress())
                .map(address -> address.getAddress().getHostAddress())
                .orElse("127.0.0.1"); // padrão
    }
}
