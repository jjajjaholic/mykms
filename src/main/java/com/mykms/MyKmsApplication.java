package com.mykms;

import com.mykms.service.ShutdownService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * MyKMS - 대칭키 관리 시스템
 * Key Management System for Symmetric Keys
 */
@Slf4j
@SpringBootApplication
public class MyKmsApplication {

    public static void main(String[] args) {
        printBanner();

        // Thread의 uncaught exception handler 설정
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            log.error("=".repeat(70));
            log.error("Uncaught exception in thread: {}", thread.getName(), throwable);
            log.error("=".repeat(70));
        });

        SpringApplication.run(MyKmsApplication.class, args);
    }

    /**
     * 애플리케이션 시작 시 Shutdown Hook 등록
     */
    @Bean
    public CommandLineRunner registerShutdownHook(ShutdownService shutdownService) {
        return args -> {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutdown Hook 실행");
                shutdownService.saveDbInfoOnShutdown();
            }, "shutdown-hook-thread"));

            log.info("Shutdown Hook이 등록되었습니다.");
        };
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
