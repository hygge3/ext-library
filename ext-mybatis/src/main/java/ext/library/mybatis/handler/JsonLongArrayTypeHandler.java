package ext.library.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.base.Splitter;
import ext.library.tool.$;
import ext.library.tool.constant.Symbol;
import io.github.linpeilie.utils.ArrayUtil;
import lombok.SneakyThrows;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Mybatis 数组，符串互转
 * <p>
 * MappedJdbcTypes 数据库中的数据类型 MappedTypes java 中的的数据类型
 */
@MappedTypes(value = {Long[].class})
@MappedJdbcTypes(value = JdbcType.VARCHAR)
public class JsonLongArrayTypeHandler extends BaseTypeHandler<Long[]> {

    @Override
    public void setNonNullParameter(@NotNull PreparedStatement ps, int i, Long[] parameter, JdbcType jdbcType)
        throws SQLException {
        ps.setString(i, ArrayUtil.join(parameter, Symbol.COMMA));
    }

    @Override
    @SneakyThrows
    public Long[] getNullableResult(@NotNull ResultSet rs, String columnName) {
        String reString = rs.getString(columnName);
        return toLongArray(reString);
    }

    @Override
    @SneakyThrows
    public Long[] getNullableResult(@NotNull ResultSet rs, int columnIndex) {
        String reString = rs.getString(columnIndex);
        return toLongArray(reString);
    }

    @Override
    @SneakyThrows
    public Long[] getNullableResult(@NotNull CallableStatement cs, int columnIndex) {
        String reString = cs.getString(columnIndex);
        return toLongArray(reString);
    }
    @NotNull
    private Long[] toLongArray(String str) {
        return Splitter.on(Symbol.COMMA).omitEmptyStrings().trimResults().splitToList(str).stream().map(
            $::toLong
        ).toArray(Long[]::new);
    }

}
