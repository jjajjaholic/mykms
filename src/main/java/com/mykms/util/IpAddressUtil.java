package com.mykms.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP 주소 유틸리티
 * CIDR 표기법 지원
 */
public class IpAddressUtil {

    /**
     * IP 주소가 CIDR 범위에 포함되는지 확인
     * @param ipAddress 확인할 IP 주소
     * @param cidr CIDR 표기법 (예: "192.168.1.0/24")
     * @return 포함 여부
     */
    public static boolean isIpInRange(String ipAddress, String cidr) {
        try {
            // CIDR이 아닌 단일 IP인 경우
            if (!cidr.contains("/")) {
                return ipAddress.equals(cidr);
            }

            String[] parts = cidr.split("/");
            String network = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            InetAddress targetAddress = InetAddress.getByName(ipAddress);
            InetAddress networkAddress = InetAddress.getByName(network);

            byte[] targetBytes = targetAddress.getAddress();
            byte[] networkBytes = networkAddress.getAddress();

            // IPv4만 지원
            if (targetBytes.length != 4 || networkBytes.length != 4) {
                return false;
            }

            // 네트워크 마스크 생성
            int mask = 0xFFFFFFFF << (32 - prefixLength);

            // IP를 int로 변환
            int targetInt = byteArrayToInt(targetBytes);
            int networkInt = byteArrayToInt(networkBytes);

            // 네트워크 부분 비교
            return (targetInt & mask) == (networkInt & mask);

        } catch (UnknownHostException | NumberFormatException e) {
            return false;
        }
    }

    /**
     * byte 배열을 int로 변환
     */
    private static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value |= (bytes[i] & 0xFF) << (24 - 8 * i);
        }
        return value;
    }

    /**
     * 요청에서 실제 클라이언트 IP 추출
     * X-Forwarded-For, X-Real-IP 헤더 지원
     */
    public static String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For에 여러 IP가 있을 경우 첫 번째 IP 사용
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
