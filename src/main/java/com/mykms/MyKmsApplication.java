package com.mykms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MyKMS - 대칭키 관리 시스템
 * Key Management System for Symmetric Keys
 */
@SpringBootApplication
public class MyKmsApplication {

    public static void main(String[] args) {
        printBanner();
        SpringApplication.run(MyKmsApplication.class, args);
    }

    private static void printBanner() {
        System.out.println("""
            ╔═══════════════════════════════════════════════════════════════╗
            ║                                                               ║
            ║   MyKMS - Key Management System                              ║
            ║                                                               ║
            ║   대칭키 관리 시스템이 시작됩니다...                            ║
            ║                                                               ║
            ╚═══════════════════════════════════════════════════════════════╝
            """);
    }
}
