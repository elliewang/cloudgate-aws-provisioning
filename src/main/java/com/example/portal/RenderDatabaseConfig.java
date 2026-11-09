package com.example.portal;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Convert Render's postgresql:// private connection URL into a JDBC datasource. */
@Configuration
@Profile("render")
public class RenderDatabaseConfig {
  @Bean
  DataSource renderDataSource(@Value("${DATABASE_URL}") String databaseUrl) {
    URI uri = URI.create(databaseUrl);
    if (!"postgresql".equals(uri.getScheme()) && !"postgres".equals(uri.getScheme())) {
      throw new IllegalArgumentException("Expected a PostgreSQL DATABASE_URL");
    }
    String[] parts = uri.getRawUserInfo().split(":", 2);
    if (parts.length != 2) throw new IllegalArgumentException("Missing database credentials");
    String user = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
    String password = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
    String path = uri.getRawPath();
    String jdbc = "jdbc:postgresql://" + uri.getHost() + ":" + (uri.getPort() < 0 ? 5432 : uri.getPort()) + path;
    DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setDriverClassName("org.postgresql.Driver");
    ds.setUrl(jdbc);
    ds.setUsername(user);
    ds.setPassword(password);
    return ds;
  }
}
