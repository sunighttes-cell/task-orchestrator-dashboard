//config binding
package com.taskOrchestrator.app.auth.infrastructure.jwt;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long expirationInMs;
}