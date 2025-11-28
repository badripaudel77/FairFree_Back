package com.app.fairfree.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;

// Will be accessed as : http://localhost:8080/swagger-ui/index.html
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI fairFreeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FairFree Backend Server")
                        .description("Backend APIs for FairFree Application")
                        .contact(new Contact()
                                .name("FairFree Team")
                                .email("badripaudel77(at)gmail(dot)com")))
                .externalDocs(new ExternalDocumentation()
                        .description("FairFree Project Documentation")
                        .url("https://github.com/badripaudel77/FairFree_Back"));
    }
}
