package ext.library.eatpick;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.sql.DataSource;

import jakarta.validation.constraints.NotNull;

import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.ColumnConfig;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.mybatisflex.codegen.dialect.IDialect;
import com.mybatisflex.codegen.dialect.JdbcTypeMapping;
import com.zaxxer.hikari.HikariDataSource;
import ext.library.mybatis.constant.DbField;
import ext.library.tool.$;

/**
 * 代码生成
 */
public class Codegen {
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/demo?zeroDateTimeBehavior=CONVERT_TO_NULL&rewriteBatchedStatements=true";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = null;

    private static final String BASE_PACKAGE = "ext.library.demo";
    private static final String SOURCE_DIR = "demo/ext-demo-web/src/main/java";
    private static final String[] TABLE_PREFIX = new String[]{"sys_", "data_", "region_"};
    private static final String[] GENERATE_TABLES = new String[]{"data_resource", "region_area", "region_city", "region_province", "sys_permission", "sys_role", "sys_role_permission", "sys_user", "sys_user_role"};
    private static final String[] UN_GENERATE_TABLES = null;

    public static void main(String[] args) {
        generate();
    }

    @NotNull
    private static DataSource getDataSource() {
        // 配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(DB_URL);
        dataSource.setUsername(DB_USERNAME);
        return dataSource;
    }

    /**
     * 生成
     */
    public static void generate() {
        // 配置数据源
        DataSource dataSource = getDataSource();

        // 创建配置内容
        GlobalConfig globalConfig = createGlobalConfig();

        // 通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig, IDialect.MYSQL);
        // 生成代码
        generator.generate();
    }

    @NotNull
    private static GlobalConfig createGlobalConfig() {
        // 创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        // 注释配置
        globalConfig.getJavadocConfig()
                // 作者
                .setAuthor("Auto Codegen By Ext");

        // 包配置
        globalConfig.getPackageConfig()
                // 根包名
                .setBasePackage(BASE_PACKAGE);
        globalConfig.setSourceDir(SOURCE_DIR);

        globalConfig.setLogicDeleteColumn(DbField.DELETE_TIME);
        globalConfig.setTablePrefix(TABLE_PREFIX);
        if ($.isNotEmpty(GENERATE_TABLES)) {
            globalConfig.setGenerateTable(GENERATE_TABLES);
        }
        if ($.isNotEmpty(UN_GENERATE_TABLES)) {
            globalConfig.setUnGenerateTable(UN_GENERATE_TABLES);
        }

        // 某个列的全局配置
        // 创建时间
        ColumnConfig createTimeConfig = new ColumnConfig();
        createTimeConfig.setColumnName(DbField.CREATE_TIME);
        createTimeConfig.setOnInsertValue("NOW()");
        globalConfig.setColumnConfig(createTimeConfig);
        // 更新时间
        ColumnConfig updateTimeConfig = new ColumnConfig();
        updateTimeConfig.setColumnName(DbField.UPDATE_TIME);
        createTimeConfig.setOnInsertValue("NOW()");
        updateTimeConfig.setOnUpdateValue("NOW()");
        globalConfig.setColumnConfig(updateTimeConfig);

        // Entity 生成配置
        globalConfig.enableEntity()
                // 是否覆盖之前生成的文件
                .setOverwriteEnable(true)
                // Entity 是否使用 Lombok 注解
                .setWithLombok(true)
                .setJdkVersion(21);

        globalConfig.enableTableDef().setOverwriteEnable(true);

        globalConfig.enableMapper().setMapperAnnotation(true);
        globalConfig.enableService();
        globalConfig.enableServiceImpl();
        globalConfig.enableController();

        // 默认类型映射
        JdbcTypeMapping.registerMapping(Timestamp.class, LocalDateTime.class);
        JdbcTypeMapping.registerMapping(Date.class, LocalDate.class);
        JdbcTypeMapping.registerMapping(Time.class, LocalTime.class);
        JdbcTypeMapping.registerMapping(BigInteger.class, Long.class);
        JdbcTypeMapping.registerMapping(Byte.class, Boolean.class);
        return globalConfig;
    }

}
