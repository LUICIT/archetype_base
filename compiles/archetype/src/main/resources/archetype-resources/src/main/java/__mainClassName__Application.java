#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "${package}",
        "io.github.luicit.luisprojectscore"
})
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
        "${package}.domain.repository",
})
@EntityScan(basePackages = {
        "${package}.domain.entity",
        "io.github.luicit.luisprojectscore.domain.entity"
})
public class ${mainClassName}Application {

    public static void main(String[] args) {
        SpringApplication.run(${mainClassName}Application.class, args);
    }

}
