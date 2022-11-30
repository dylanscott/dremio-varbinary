This repository contains a minimal reproduction of what appears to be a bug with the handling of `varbinary` columns in the Dremio JDBC driver.

### Setup

The reproduction consists of a [single test file](https://github.com/dylanscott/dremio-varbinary/blob/main/src/test/java/dremio/DremioVarbinary.java) with no dependencies other than the Dremio JDBC driver. The easiest way to run the test is with the Gradle build included.

**Note**: On macOS this project only seems to work on Java 8 - I get an SSL error on newer versions of Java - but I suspect this is OS-specific since we use this driver in production on a newer Java version.

To run the test you need to point it at a Dremio instance using the `JDBC_URL` and `ACCESS_TOKEN` environment variables. If running with gradle you can set those values here:

https://github.com/dylanscott/dremio-varbinary/blob/6c5a50d105061be947580694bf6613a911f2d4a1/build.gradle#L20-L21

### Details

When I run `./gradlew test --info` I get the following error:

```
dremio.DremioVarbinary > testVarbinary FAILED
    java.lang.IndexOutOfBoundsException: index (9) must not be greater than size (6)
        at cdjd.org.apache.arrow.util.Preconditions.checkPositionIndex(Preconditions.java:1236)
        at cdjd.org.apache.arrow.util.Preconditions.checkPositionIndex(Preconditions.java:1218)
        at cdjd.org.apache.arrow.memory.ArrowBuf.slice(ArrowBuf.java:199)
        at cdjd.com.dremio.exec.vector.accessor.VarBinaryAccessor.getStream(VarBinaryAccessor.java:71)
        at cdjd.com.dremio.exec.vector.accessor.BoundCheckingAccessor.getStream(BoundCheckingAccessor.java:114)
        at com.dremio.jdbc.impl.TypeConvertingSqlAccessor.getStream(TypeConvertingSqlAccessor.java:844)
        at com.dremio.jdbc.impl.SqlAccessorWrapper.getBinaryStream(SqlAccessorWrapper.java:173)
        at cdjd.org.apache.calcite.avatica.AvaticaResultSet.getBinaryStream(AvaticaResultSet.java:302)
        at com.dremio.jdbc.impl.DremioResultSetImpl.getBinaryStream(DremioResultSetImpl.java:258)
        at dremio.DremioVarbinary.testVarbinary(DremioVarbinary.java:40)
```

The issue seems to stem from reading varbinary data from more than one row of results. If you change the `setup()` function to only initialize the `$scratch.test_varbinary` table with a single row, it will not fail. It also seems to depend on running against a table even though it doesn't depend on the data in the table, as you can see from the `testVarbinaryAlternate` case which runs over an identical sub-query instead of `$scratch.test_varbinary` but succeeds.