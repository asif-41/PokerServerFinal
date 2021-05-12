package com.example.PokerServer.db;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class JpaConfig {
    @Bean
    public DB auditorAware() {
        return new DB();
    }
}
