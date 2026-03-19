package com.luis_r_aguilar.baseproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.luis_r_aguilar.baseproject",
        "com.luisraguilar.luisprojectscore"
})
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
        "com.luis_r_aguilar.baseproject.domain.repository",
        "com.luisraguilar.luisprojectscore.domain.repository"
})
@EntityScan(basePackages = {
        "com.luis_r_aguilar.baseproject.domain.entity",
        "com.luisraguilar.luisprojectscore.domain.entity"
})
public class BaseProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaseProjectApplication.class, args);
    }
}
