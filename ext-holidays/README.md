# ext-holidays

`ext-holidays` 用来判断日期是否工作日，支持 2019 年起至 2025
年中国法定节假日，以国务院发布的公告为准，随时调整及增加；http://www.gov.cn/zfwj/bgtfd.htm
或 http://www.gov.cn/zhengce/xxgkzl.htm

## 使用

### maven

```xml

<dependency>
    <groupId>ext.library</groupId>
    <artifactId>ext-holidays</artifactId>
    <version>${version}</version>
</dependency>
```

### gradle

```groovy
compile("ext.library:ext-holidays:${version}")
```

### 注入 bean

```java

@Autowired
private HolidaysApi holidaysApi;
```

### 接口使用

```java
/**
 * 获取日期类型
 *
 * @param localDate LocalDate
 * @return DaysType
 */
DaysType getDaysType(LocalDate localDate);

/**
 * 获取日期类型
 *
 * @param localDateTime LocalDateTime
 * @return DaysType
 */
DaysType getDaysType(LocalDateTime localDateTime);

/**
 * 获取日期类型
 *
 * @param date Date
 * @return DaysType
 */
DaysType getDaysType(Date date);

/**
 * 判断是否工作日
 *
 * @param localDate LocalDate
 * @return 是否工作日
 */
boolean isWeekdays(LocalDate localDate);

/**
 * 判断是否工作日
 *
 * @param localDateTime LocalDateTime
 * @return 是否工作日
 */
boolean isWeekdays(LocalDateTime localDateTime);

/**
 * 判断是否工作日
 *
 * @param date Date
 * @return 是否工作日
 */
boolean isWeekdays(Date date);
```
