package com.mykms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 설정
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MyKMS - Key Management System")
                        .version("1.0.0")
                        .description("""
                                대칭키(Symmetric Key)를 안전하게 관리하는 KMS(Key Management System)입니다.

                                ## 기능

                                - **인증**: Basic Auth를 사용한 API 인증
                                - **키 생성**: 새로운 대칭키 생성
                                - **키 저장**: 생성된 키를 암호화하여 안전하게 저장
                                - **키 요청**: 저장된 키 조회
                                - **키 업데이트**: 기존 키 정보 수정 및 로테이션
                                - **키 삭제**: 저장된 키 삭제

                                ## 인증

                                모든 API는 Basic Auth를 사용합니다. 요청 시 헤더에 인증 정보를 포함해야 합니다.

                                기본 인증 정보:
                                - Username: admin
                                - Password: admin123 (환경변수에서 변경 가능)

                                ## 보안

                                - 모든 키는 AES-256-GCM으로 암호화되어 저장됩니다
                                - 마스터 키는 PBKDF2를 사용하여 비밀번호로부터 유도됩니다
                                - 프로덕션 환경에서는 반드시 HTTPS를 사용하세요
                                """)
                        .contact(new Contact()
                                .name("MyKMS")
                                .url("https://github.com/yourusername/mykms"))
                        .license(new License()
                                .name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .components(new Components()
                        .addSecuritySchemes("basicAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")));
    }
}
