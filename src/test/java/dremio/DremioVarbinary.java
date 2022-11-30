package dremio;

import org.junit.AfterClass;
import org.junit.BeforeClass;
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
  @BeforeClass
  public static void setup() throws SQLException {
    try (Connection conn = connect();
         Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(
          "CREATE TABLE IF NOT EXISTS $scratch.test_varbinary " +
              "AS (SELECT 1 UNION ALL SELECT 1)");
    }
  }

  @Test
  public void testVarbinary() throws SQLException, IOException {
    try (Connection conn = connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(
             "SELECT cast('abc' as varbinary) FROM $scratch.test_varbinary")) {
      while (rs.next()) {
        try (InputStream is = rs.getBinaryStream(1)) {
          assertEquals(read(is), "abc");
        }
      }
    }
  }

  @Test
  public void testVarbinaryAlternate() throws SQLException, IOException {
    try (Connection conn = connect();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(
             "SELECT cast('abc' as varbinary) FROM (SELECT 1 UNION ALL SELECT 1)")) {
      while (rs.next()) {
        try (InputStream is = rs.getBinaryStream(1)) {
          assertEquals(read(is), "abc");
        }
      }
    }
  }

  @AfterClass
  public static void teardown() throws SQLException {
    try (Connection conn = connect();
         Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(
          "DROP TABLE IF EXISTS $scratch.test_varbinary");
    }
  }


  private static Connection connect() throws SQLException {
    String jdbcUrl = System.getenv("JDBC_URL");
    assertNotNull("must specify JDBC_URL", jdbcUrl);
    String accessToken = System.getenv("ACCESS_TOKEN");
    assertNotNull("must specify ACCESS_TOKEN", accessToken);
    return DriverManager.getConnection(jdbcUrl, "$token", accessToken);
  }


  private static String read(InputStream is) throws IOException {
    try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
      return r.lines().collect(Collectors.joining("\n"));
    }
  }
}
