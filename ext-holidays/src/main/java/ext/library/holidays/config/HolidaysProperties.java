package ext.library.holidays.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Holidays 配置类
 */
@ConfigurationProperties(HolidaysProperties.PREFIX)
public class HolidaysProperties {

    public static final String PREFIX = "ext.holidays";

    /**
     * 自行扩展的 json 文件路径
     */
    private List<ExtData> extData = new ArrayList<>();

    public List<ExtData> getExtData() {
        return extData;
    }

    public void setExtData(List<ExtData> extData) {
        this.extData = extData;
    }

    public static class ExtData {

        /**
         * 年份
         */
        private Integer year;

        /**
         * 数据目录
         */
        private String dataPath;

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public String getDataPath() {
            return dataPath;
        }

        public void setDataPath(String dataPath) {
            this.dataPath = dataPath;
        }
    }

}