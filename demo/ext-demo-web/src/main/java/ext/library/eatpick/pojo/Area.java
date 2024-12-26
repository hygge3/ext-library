package ext.library.eatpick.pojo;

import lombok.Getter;
import lombok.Setter;

/**
 * 区域数据
 */
@Getter
@Setter
public class Area {
    /*
      区级编码
     */
    private String areaCode;
    /*
      区级名称
     */
    private String areaName;
    /*
      市级编码
     */
    private String cityCode;
    /*
      市级名称
     */
    private String cityName;
    /*
      省级编码
     */
    private String provinceCode;
    /*
      省级名称
     */
    private String provinceName;
}
