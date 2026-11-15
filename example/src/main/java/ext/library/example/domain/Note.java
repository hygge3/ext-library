package ext.library.example.domain;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableId;
import java.time.LocalDateTime;
import lombok.Data;

/** 笔记，主键是 32 位十六进制 UUIDv7：按时间有序，URL 里直接出现也安全。 */
@Data
@Table
public class Note {

    @TableId(value = IdAutoType.GENERATOR, generator = Generators.UUID_V7)
    private String id;

    private String ownerEmail;

    private String title;

    private String content;

    private String tags;

    private String traceId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
