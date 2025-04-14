package ext.library.mybatis.handler;

import jakarta.annotation.Nonnull;

import com.google.common.base.Splitter;
import ext.library.tool.$;
import ext.library.tool.constant.Symbol;
import io.github.linpeilie.utils.ArrayUtil;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.SneakyThrows;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * Mybatis Integer 数组，字符串互转
 * <p>
 * MappedJdbcTypes 数据库中的数据类型 MappedTypes java 中的的数据类型
 */
@MappedTypes(value = {Integer[].class})
@MappedJdbcTypes(value = JdbcType.VARCHAR)
public class IntArrayTypeHandler extends BaseTypeHandler<Integer[]> {

    @Override
    public void setNonNullParameter(@Nonnull PreparedStatement ps, int i, Integer[] parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, ArrayUtil.join(parameter, Symbol.COMMA));
    }

    @Override
    public Integer[] getNullableResult(@Nonnull ResultSet rs, String columnName) throws SQLException{
        String reString = rs.getString(columnName);
        return toIntArray(reString);
    }

    @Override
    @SneakyThrows
    public Integer[] getNullableResult(@Nonnull ResultSet rs, int columnIndex) {
        String reString = rs.getString(columnIndex);
        return toIntArray(reString);
    }

    @Override
    @SneakyThrows
    public Integer[] getNullableResult(@Nonnull CallableStatement cs, int columnIndex) {
        String reString = cs.getString(columnIndex);
        return toIntArray(reString);
    }

    private Integer[] toIntArray(String str) {
        if ($.isBlank(str)) {
            return new Integer[0];
        }
        return Splitter.on(Symbol.COMMA).omitEmptyStrings().trimResults().splitToList(str).stream().map(
                $::toInt
        ).toArray(Integer[]::new);
    }

}
