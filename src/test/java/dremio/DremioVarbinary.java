package dremio;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DremioVarbinary {
  private Connection connect() throws SQLException {
    String jdbcUrl = System.getenv("JDBC_URL");
    assertNotNull("must specify JDBC_URL", jdbcUrl);
    String accessToken = System.getenv("ACCESS_TOKEN");
    assertNotNull("must specify ACCESS_TOKEN", accessToken);
    return DriverManager.getConnection(jdbcUrl, "$token", accessToken);
  }

  @Before
  public void setup() throws SQLException {
    try (Connection conn = connect();
         Statement stmt = conn.createStatement()) {
      stmt.executeUpdate("CREATE TABLE $scratch.test_varbinary AS (SELECT 1 UNION ALL SELECT 1)");
    }
  }

  @After
  public void teardown() throws SQLException {
    try (Connection conn = connect();
         Statement stmt = conn.createStatement()) {
      stmt.executeUpdate("DROP TABLE IF EXISTS $scratch.test_varbinary");
    }
  }

  @Test
  public void testVarbinary() throws SQLException, IOException {
    String query = "SELECT cast('abc' as varbinary) FROM $scratch.test_varbinary";
    try (Connection conn = connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        try (InputStream is = rs.getBinaryStream(1)) {
          assertEquals(read(is), "abc");
        }
      }
    }
  }

  private String read(InputStream is) throws IOException {
    try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      return r.lines().collect(Collectors.joining("\n"));
    }
  }
}
