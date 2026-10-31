package ext.library.jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("实体行映射")
class EntityRowMapperTest {

    /**
     * 枚举列
     */
    enum Status {
        ON, OFF
    }

    /**
     * 带时间与枚举列的实体
     */
    public static class Demo {

        private Long id;

        private LocalDateTime createTime;

        private Status status;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }
    }

    @Test
    @DisplayName("timestamp 列映射到 LocalDateTime、字符串列映射到枚举")
    void mapsTemporalAndEnumColumns() throws Exception {
        LocalDateTime now = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(3);
        when(metaData.getColumnLabel(1)).thenReturn("id");
        when(metaData.getColumnLabel(2)).thenReturn("create_time");
        when(metaData.getColumnLabel(3)).thenReturn("status");
        when(resultSet.getObject(1)).thenReturn(1L);
        when(resultSet.getObject(2)).thenReturn(Timestamp.valueOf(now));
        when(resultSet.getObject(3)).thenReturn("ON");

        Demo demo = new EntityRowMapper<>(Demo.class).mapRow(resultSet, 0);

        assertThat(demo.getId()).isEqualTo(1L);
        assertThat(demo.getCreateTime()).isEqualTo(now);
        assertThat(demo.getStatus()).isEqualTo(Status.ON);
    }

}
