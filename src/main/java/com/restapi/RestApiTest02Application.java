package com.restapi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "com.restapi") 
public class RestApiTest02Application {
    
    private static final String DEFAULT_HOLIDAY_STATUS = "N";
    private static volatile String holiday = DEFAULT_HOLIDAY_STATUS;

    public static void main(String[] args) {
        SpringApplication.run(RestApiTest02Application.class, args);
    }

    public static String getHoliday() {
        return holiday;
    }

    public static void setHoliday(String holidayStatus) {
        holiday = holidayStatus;
    }

    // [디버깅용 로그] 서버 켜질 때 설정들이 잘 먹혔는지 검사
    @Bean
    public CommandLineRunner run(ApplicationContext ctx) {
        return args -> {
            System.out.println("=================================================");
            
            // 1. 보안 설정 파일 로딩 여부 확인
            boolean hasSecurityConfig = ctx.containsBean("securityConfig");
            System.out.println(">>>>> [CHECK 1] SecurityConfig Loaded? " + hasSecurityConfig);
            if (!hasSecurityConfig) {
                System.err.println("!!!!! 경고: SecurityConfig가 로드되지 않았습니다. 패키지 위치를 확인하세요 !!!!!");
            }

            // 2. API 컨트롤러 로딩 여부 확인
            boolean hasApiController = ctx.containsBean("apiController");
            System.out.println(">>>>> [CHECK 2] ApiController Loaded? " + hasApiController);

            System.out.println("====== [START] Registered URL Mappings (/api/reservation) ======");
            // 충돌 방지를 위해 빈 이름("requestMappingHandlerMapping")을 명시
            try {
                RequestMappingHandlerMapping mapping = ctx.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
                mapping.getHandlerMethods().forEach((key, value) -> {
                    if (key.toString().contains("/api/reservation")) {
                        System.out.println("Mapping: " + key + " -> " + value);
                    }
                });
            } catch (Exception e) {
                System.out.println("URL 매핑 정보를 가져오는 중 에러 발생 (무시 가능): " + e.getMessage());
            }
            System.out.println("====== [END] Registered URL Mappings ======");
            System.out.println("=================================================");
        };
    }
}