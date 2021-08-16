package springone.messageboardservice;

import org.hibernate.dialect.PostgreSQL9Dialect;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = "spring.sql.init.mode=always", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessageIntegrationTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Container
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres");

    @DynamicPropertySource
    static void registerPostgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.jpa.database-platform", PostgreSQL9Dialect.class::getName);
        registry.add("spring.datasource.url", () -> db.getJdbcUrl());
        registry.add("spring.datasource.username", () -> db.getUsername());
        registry.add("spring.datasource.password", () -> db.getPassword());
    }

    @Test
    void getMessagesSucceeds() {
        // Dynamically create type for List<Message> - required to properly
        // parse the response for a composite class in an HTTP exchange
        var ptr = new ParameterizedTypeReference<List<Message>>() {
        };
        var exchange =
                this.testRestTemplate.exchange("/message", HttpMethod.GET, null, ptr);

        List<Message> response = exchange.getBody();
        Message message = response.get(0);
        assertThat(message.getUsername()).isEqualTo("Cora");
        assertThat(message.getText()).isEqualTo("Welcome to everyone!");
    }

}
