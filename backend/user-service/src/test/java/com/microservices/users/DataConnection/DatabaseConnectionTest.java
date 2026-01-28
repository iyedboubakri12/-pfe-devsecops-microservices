package com.microservices.users.DataConnection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
// 1. On utilise le profil 'test' (celui avec H2)
@ActiveProfiles("test")
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDatabaseConnection() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn).isNotNull();
            assertThat(conn.isValid(1)).isTrue();

            // 2. On vérifie que la connexion est bien de type H2 (pour le CI/CD)
            // car PostgreSQL n'est pas disponible dans le Runner
            assertThat(conn.getMetaData().getURL()).contains("h2");
            System.out.println("✅ Connexion réussie à la base de données de test : " + conn.getMetaData().getURL());
        }
    }
}