package ext.library.example.domain;

import cn.xbatis.core.incrementer.Generators;
import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableId;
import java.time.LocalDateTime;
import lombok.Data;

/** 笔记附件元数据，文件本体在 ext-oss。 */
@Data
@Table
public class NoteAttachment {

    @TableId(value = IdAutoType.GENERATOR, generator = Generators.UUID_V7)
    private String id;

    private String noteId;

    private String fileName;

    private String objectKey;

    private Long size;

    private String contentType;

    private LocalDateTime createdAt;
}
