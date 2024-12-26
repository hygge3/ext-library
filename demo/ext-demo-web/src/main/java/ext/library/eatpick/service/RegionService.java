package ext.library.eatpick.service;

import java.util.List;

import com.mybatisflex.core.query.QueryWrapper;
import ext.library.eatpick.mapper.AreaMapper;
import ext.library.eatpick.mapper.CityMapper;
import ext.library.eatpick.mapper.ProvinceMapper;
import ext.library.eatpick.pojo.Area;
import ext.library.eatpick.pojo.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ext.library.eatpick.entity.table.AreaTableDef.AREA;
import static ext.library.eatpick.entity.table.CityTableDef.CITY;
import static ext.library.eatpick.entity.table.ProvinceTableDef.PROVINCE;


@Service
@RequiredArgsConstructor
public class RegionService {

	private final ProvinceMapper provinceMapper;

	private final CityMapper cityMapper;

	private final AreaMapper areaMapper;

	/**
	 * 获取所有省份的信息
	 * @return 返回包含所有省份信息的列表
	 */
	public List<Region> provinces() {
		QueryWrapper query = QueryWrapper.create();
		return provinceMapper.selectListByQueryAs(query, Region.class);
	}

	/**
	 * 根据省份代码查询该省份下的所有城市。
	 * @param provinceCode 省份的唯一标识代码。
	 * @return 返回一个包含该省份下所有城市信息的列表。
	 */
	public List<Region> cities(String provinceCode) {
		QueryWrapper query = QueryWrapper.create()
			.select(CITY.CODE, CITY.NAME)
			.where(CITY.PROVINCE_CODE.eq(provinceCode));
		return cityMapper.selectListByQueryAs(query, Region.class);
	}

	/**
	 * 根据城市代码查询区域信息
	 * @param cityCode 城市代码，用于过滤区域信息
	 * @return 返回包含指定城市代码的区域信息的列表
	 */
	public List<Region> areas(String cityCode) {
		QueryWrapper query = QueryWrapper.create()
			.select(AREA.CODE, AREA.NAME)
			.where(AREA.CITY_CODE.eq(cityCode));

		return areaMapper.selectListByQueryAs(query, Region.class);
	}

	/**
	 * 根据区域代码查询区域详情。
	 * <p>
	 * 该方法通过查询区域代码对应的区域名称、城市代码、城市名称、省份代码和省份名称，从区域表、城市表和省份表中获取详细的区域信息。
	 * 使用左连接确保即使某些区域没有对应的城市或省份信息，也能返回完整的区域详情。
	 * @param areaCode 区域代码，用于查询特定区域的详细信息。
	 * @return 返回一个包含区域详情的列表。每个区域的详情包括区域代码、区域名称、城市代码、城市名称、省份代码和省份名称。
	 */
	public Area areaDetail(String areaCode) {
		// 创建查询包装器，用于构建 SQL 查询语句
		QueryWrapper query = QueryWrapper.create()
			// 选择需要查询的字段，包括区域代码、区域名称、城市代码、城市名称、省份代码和省份名称
			.select(AREA.CODE.as("areaCode"), AREA.NAME.as("areaName"), AREA.CITY_CODE,
					CITY.NAME.as("cityName"), AREA.PROVINCE_CODE, PROVINCE.NAME.as("provinceName"))
			// 从区域表开始查询
			.from(AREA)
			// 左连接城市表，以便获取区域对应的城市信息
			.leftJoin(CITY)
			.on(AREA.CITY_CODE.eq(CITY.CODE))
			// 左连接省份表，以便获取区域对应的省份信息
			.leftJoin(PROVINCE)
			.on(AREA.PROVINCE_CODE.eq(PROVINCE.CODE))
			.where(AREA.CODE.eq(areaCode));

		// 使用查询包装器执行查询，并将结果映射为 AreaDTO 对象列表返回
		return areaMapper.selectOneByQueryAs(query, Area.class);
	}

}
