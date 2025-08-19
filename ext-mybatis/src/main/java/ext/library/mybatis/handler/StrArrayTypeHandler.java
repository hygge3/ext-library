package ext.library.mybatis.handler;

import com.google.common.base.Splitter;
import ext.library.tool.constant.Symbol;
import ext.library.tool.util.StringUtil;
import io.github.linpeilie.utils.ArrayUtil;
import lombok.SneakyThrows;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import jakarta.annotation.Nonnull;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mybatis 字符数组，字符串互转
 * <p>
 * MappedJdbcTypes 数据库中的数据类型 MappedTypes java 中的的数据类型
 */
@MappedTypes(value = {String[].class})
@MappedJdbcTypes(value = JdbcType.VARCHAR)
public class StrArrayTypeHandler extends BaseTypeHandler<String[]> {

    @Override
    public void setNonNullParameter(@Nonnull PreparedStatement ps, int i, String[] parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, ArrayUtil.join(parameter, Symbol.COMMA));
    }

    @Override
    @SneakyThrows
    public String[] getNullableResult(@Nonnull ResultSet rs, String columnName) {
        String reString = rs.getString(columnName);
        return toStrArray(reString);
    }

    @Override
    @SneakyThrows
    public String[] getNullableResult(@Nonnull ResultSet rs, int columnIndex) {
        String reString = rs.getString(columnIndex);
        return toStrArray(reString);
    }

    @Override
    @SneakyThrows
    public String[] getNullableResult(@Nonnull CallableStatement cs, int columnIndex) {
        String reString = cs.getString(columnIndex);
        return toStrArray(reString);
    }

    private String[] toStrArray(String str) {
        if (StringUtil.isBlank(str)) {
            return new String[0];
        }
        return Splitter.on(Symbol.COMMA).omitEmptyStrings().trimResults().splitToList(str).toArray(String[]::new);
    }
}