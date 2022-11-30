package dremio.repro;

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

public class DremioTest {
  @Test
  public void testVarbinary() throws SQLException, IOException {
    String jdbcUrl = "jdbc:dremio:direct=sql.dremio.cloud:443;ssl=true;PROJECT_ID=d22f315e-3957-4454-a6af-fb69857f1c05;";
    String accessToken = System.getenv("ACCESS_TOKEN");
    String query = "SELECT cast('abc' as varbinary) ";
    try (Connection conn = DriverManager.getConnection(jdbcUrl, "$token", accessToken);
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
      while (rs.next()) {
        try (InputStream is = rs.getBinaryStream(1)) {
          System.out.println(read(is));
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
