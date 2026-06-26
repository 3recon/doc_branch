package com.docbranch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI docBranchOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DocBranch API")
                        .description("DocBranch MVP1 REST API")
                        .version("v1"));
    }
}
