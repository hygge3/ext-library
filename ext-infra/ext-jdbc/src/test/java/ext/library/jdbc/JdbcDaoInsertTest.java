package ext.library.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("通用 DAO 插入")
class JdbcDaoInsertTest {

    /**
     * 带自增主键的实体
     */
    public static class Demo {

        private Long id;

        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private JdbcClient jdbcClient;

    private JdbcDao<Demo> jdbcDao;

    @BeforeEach
    void setUp() {
        jdbcClient = mock(JdbcClient.class);
        JdbcClient.StatementSpec statementSpec = mock(JdbcClient.StatementSpec.class, RETURNS_SELF);
        when(jdbcClient.sql(anyString())).thenReturn(statementSpec);
        when(statementSpec.params(anyList())).thenReturn(statementSpec);
        when(statementSpec.update()).thenReturn(1);
        jdbcDao = new JdbcDao<>(jdbcClient, new EntityMetaData<>(Demo.class, "demo", "id", new String[]{"id", "name"}));
    }

    @Test
    @DisplayName("主键为空时不进 INSERT，交给数据库自增")
    void omitsNullId() {
        Demo demo = new Demo();
        demo.setName("a");

        assertThat(jdbcDao.insert(demo)).isEqualTo(1);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcClient).sql(sql.capture());
        assertThat(sql.getValue()).isEqualTo("INSERT INTO demo (name) VALUES (?)");
    }

    @Test
    @DisplayName("主键有值时正常写入")
    void keepsProvidedId() {
        Demo demo = new Demo();
        demo.setId(7L);
        demo.setName("a");

        jdbcDao.insert(demo);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcClient).sql(sql.capture());
        assertThat(sql.getValue()).isEqualTo("INSERT INTO demo (id, name) VALUES (?, ?)");
    }

}
