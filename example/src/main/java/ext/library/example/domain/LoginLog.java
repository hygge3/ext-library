package ext.library.example.domain;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableId;
import java.time.LocalDateTime;
import lombok.Data;

/** 登录日志，UA 解析结果由 ext-useragent 填。 */
@Data
@Table
public class LoginLog {

    @TableId(value = IdAutoType.GENERATOR, generator = Generators.UUID_V7)
    private String id;

    private String email;

    private String ip;

    private String userAgent;

    private String browser;

    private String os;

    private String device;

    private Boolean success;

    private LocalDateTime createdAt;
}
