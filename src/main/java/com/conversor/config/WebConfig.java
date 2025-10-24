package com.conversor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração web da aplicação.
 *
 * Define configurações de CORS, recursos estáticos e outras configurações web.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configura CORS para permitir requisições de diferentes origens.
     *
     * @param registry Registro de configurações CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /**
     * Configura handlers para recursos estáticos.
     *
     * @param registry Registro de handlers de recursos
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
