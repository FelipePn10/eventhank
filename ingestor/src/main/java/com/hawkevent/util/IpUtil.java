package com.hawkevent.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Utility class to resolve the real client IP address from a ServerWebExchange.
 * Handles common proxy headers and validates against private/local IPs.
 */
public class IpUtil {

    // Regex for valid IPv4: matches 0-255.0-255.0-255.0-255
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    // Regex for valid IPv6 (simplified, not exhaustive but covers common cases)
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^(?:[0-9a-fA-F]{1,4}:){1,7}:$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$|^(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}$|^(?:[0-9a-fA-F]{1,4}:){1,3}(?::[0-9a-fA-F]{1,4}){1,4}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}$|^[0-9a-fA-F]{1,4}:(?:(?::[0-9a-fA-F]{1,4}){1,6})$|" +
                    "^:(?:(?::[0-9a-fA-F]{1,4}){1,7}|:)$");

    /**
     * Resolves the client IP from the request, checking proxy headers first.
     *
     * @param exchange the ServerWebExchange containing request info
     * @return the client IP as a String
     */
    public static String resolveClientIp(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();

        // Standard X-Forwarded-For header
        String clientIp = extractIpFromHeader(request, "X-Forwarded-For");
        if (isValidIp(clientIp)) {
            return clientIp;
        }

        // Common proxy headers
        String[] proxyHeaders = {
                "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "X-Real-IP"
        };
        for (String header : proxyHeaders) {
            clientIp = extractIpFromHeader(request, header);
            if (isValidIp(clientIp)) {
                return clientIp;
            }
        }

        // Fallback to remote address
        return getRemoteAddress(request);
    }

    /**
     * Extracts the first valid IP from a header value.
     *
     * @param request    the HTTP request
     * @param headerName the header name to check
     * @return the extracted IP or null if invalid
     */
    private static String extractIpFromHeader(ServerHttpRequest request, String headerName) {
        String headerValue = request.getHeaders().getFirst(headerName);
        if (!StringUtils.hasText(headerValue) || "unknown".equalsIgnoreCase(headerValue)) {
            return null;
        }

        String[] ips = headerValue.split(",");
        for (String ip : ips) {
            String trimmedIp = ip.trim();
            if (isValidIp(trimmedIp)) {
                return trimmedIp;
            }
        }
        return null;
    }

    /**
     * Validates if the IP is a valid public address (not private, local, or unknown).
     *
     * @param ip the IP address to validate
     * @return true if valid
     */
    private static boolean isValidIp(String ip) {
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            return false;
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            if (inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress() || inetAddress.isAnyLocalAddress()) {
                return false;
            }

            // Additional regex check for format
            if (inetAddress instanceof java.net.Inet4Address) {
                return IPV4_PATTERN.matcher(ip).matches();
            } else if (inetAddress instanceof java.net.Inet6Address) {
                return IPV6_PATTERN.matcher(ip).matches();
            }
        } catch (UnknownHostException e) {
            // Invalid IP format
            // Optional: Log.error("Invalid IP: " + ip, e);
        }
        return false;
    }

    /**
     * Gets the remote address as a fallback.
     *
     * @param request the HTTP request
     * @return the remote IP or "127.0.0.1" as default
     */
    private static String getRemoteAddress(ServerHttpRequest request) {
        return Optional.ofNullable(request.getRemoteAddress())
                .map(address -> address.getAddress().getHostAddress())
                .orElse("127.0.0.1");
    }
}