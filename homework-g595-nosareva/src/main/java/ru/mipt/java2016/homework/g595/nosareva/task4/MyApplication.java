package ru.mipt.java2016.homework.g595.nosareva.task4;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.mipt.java2016.homework.base.task1.Calculator;
import ru.mipt.java2016.homework.g595.nosareva.task1.CalculatorAlpha;
import ru.mipt.java2016.homework.g595.nosareva.task1.CalculatorInterface;
import ru.mipt.java2016.homework.g595.nosareva.task1.WCalculator;

/**
 * curl http://localhost:9001/eval \
 *     -X POST \
 *     -H "Content-Type: text/plain" \
 *     -H "Authorization: Basic $(echo -n "username:password" | base64)" \
 *     --data-raw "44*3+2"
 */

@Configuration
@SpringBootApplication
public class MyApplication extends SpringBootServletInitializer {

    @Bean
    public CalculatorInterface calculator() {
        return WCalculator.INSTANCE;
    }

    @Bean
    public EmbeddedServletContainerCustomizer customizer(
            @Value("${ru.mipt.java2016.homework.g595.nosareva.task4.httpPort:9001}") int port) {
        return container -> container.setPort(port);
    }

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}

// curl http://localhost:9001/eval -X POST -H "Content-Type: text/plain" -H "Authorization: Basic $(echo -n "username:password" | base64)" --data-raw "44*3+2"