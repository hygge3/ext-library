package ext.library.example.domain;

import cn.xbatis.db.IdAutoType;
import cn.xbatis.db.annotations.Table;
import cn.xbatis.db.annotations.TableId;
import java.time.LocalDateTime;
import lombok.Data;

/** 分享链接。主键 token 是 AES 密文，由 ShareService 签发，因此不用自增器。 */
@Data
@Table
public class NoteShare {

    @TableId(IdAutoType.NONE)
    private String token;

    private String noteId;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;
}
