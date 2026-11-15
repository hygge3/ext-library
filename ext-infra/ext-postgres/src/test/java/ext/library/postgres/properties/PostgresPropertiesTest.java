package ext.library.postgres.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PostgreSQL 配置属性测试")
class PostgresPropertiesTest {

    @Test
    void keepsBareNameWithoutSchema() {
        PostgresProperties properties = new PostgresProperties();

        assertEquals("pg_jobs", properties.qualify("pg_jobs"));
    }

    @Test
    void prefixesTableWithSchema() {
        PostgresProperties properties = new PostgresProperties();
        properties.setSchema("biz");

        assertEquals("biz.pg_jobs", properties.qualify("pg_jobs"));
    }

    @Test
    void rejectsIdentifiersThatCouldChangeTheStatement() {
        PostgresProperties properties = new PostgresProperties();

        assertThrows(IllegalArgumentException.class, () -> properties.setSchema("biz; DROP TABLE pg_jobs"));
        assertThrows(IllegalArgumentException.class, () -> properties.setSchema("a.b"));
    }
}
