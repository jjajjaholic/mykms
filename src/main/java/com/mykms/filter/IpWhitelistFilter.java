package com.mykms.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mykms.entity.IpWhitelistEntity;
import com.mykms.repository.IpWhitelistRepository;
import com.mykms.util.IpAddressUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IP 화이트리스트 필터
 * 모든 요청에 대해 IP 주소를 확인하고, 화이트리스트에 없으면 거부
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IpWhitelistFilter extends OncePerRequestFilter {

    private final IpWhitelistRepository ipWhitelistRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = IpAddressUtil.getClientIp(request);
        String requestUri = request.getRequestURI();

        log.info("Request from IP: {} to URI: {}", clientIp, requestUri);

        // 화이트리스트에서 활성화된 IP 목록 조회
        List<IpWhitelistEntity> whitelistEntries = ipWhitelistRepository.findByEnabledTrue();

        // 화이트리스트가 비어있으면 모든 IP 허용 (초기 설정 편의를 위해)
        if (whitelistEntries.isEmpty()) {
            log.warn("IP whitelist is empty. Allowing all IPs. Please configure IP whitelist for security.");
            filterChain.doFilter(request, response);
            return;
        }

        // IP가 화이트리스트에 있는지 확인
        boolean isAllowed = whitelistEntries.stream()
                .anyMatch(entry -> IpAddressUtil.isIpInRange(clientIp, entry.getIpAddress()));

        if (isAllowed) {
            log.info("IP {} is whitelisted. Access granted.", clientIp);
            filterChain.doFilter(request, response);
        } else {
            log.warn("IP {} is not whitelisted. Access denied.", clientIp);
            sendForbiddenResponse(response, clientIp);
        }
    }

    /**
     * 403 Forbidden 응답 전송
     */
    private void sendForbiddenResponse(HttpServletResponse response, String clientIp) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Forbidden");
        errorResponse.put("message", "Your IP address is not whitelisted");
        errorResponse.put("ip", clientIp);
        errorResponse.put("timestamp", System.currentTimeMillis());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
