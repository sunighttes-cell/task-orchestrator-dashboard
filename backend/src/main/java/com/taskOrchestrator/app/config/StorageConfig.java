package com.taskOrchestrator.app.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//spring checks for WebMvcConfigurer during startup
//and allows us to look into uploadpath instead of looking for a controller which is the default behavior
//Spring Boot starts -> Looks for WebMvcConfigurer -> Does anyone want to customize MVC? Yes -> Calls methods

@Configuration
public class StorageConfig implements WebMvcConfigurer {
    //ResourceHandlerRegistry maps URL path (HTTP) to Physical location (File System)
    private final StorageProperties storageProperties;

    public StorageConfig(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    //Spring Checks Any controller? No Any resource handler? Yes -> Look in uploads/ -> Find and Return image
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/avatars/");
    }
}