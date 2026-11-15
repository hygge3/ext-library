package ext.library.example.domain;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableId;
import java.time.LocalDateTime;
import lombok.Data;

/** 用户表，主键由 xbatis 的 UUID_V7 生成器填。 */
@Data
@Table
public class AppUser {

    @TableId(value = IdAutoType.GENERATOR, generator = Generators.UUID_V7)
    private String id;

    private String email;

    private String nickname;

    private String passwordHash;

    private String role;

    private LocalDateTime createdAt;
}
