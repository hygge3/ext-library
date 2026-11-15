package ext.library.example.service;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import ext.library.example.domain.NoteAttachment;
import ext.library.example.mapper.NoteAttachmentMapper;
import ext.library.oss.OssTemplate;
import ext.library.oss.util.ImageUtil;
import ext.library.tool.exception.BizException;
import ext.library.tool.util.IdUtil;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 附件：文件进 ext-oss，元数据进 PostgreSQL；图片附件额外出一张 webp 缩略图。
 */
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final int THUMB_MAX_EDGE = 320;

    private final NoteAttachmentMapper attachmentMapper;

    private final OssTemplate oss;

    private final NoteService noteService;

    public NoteAttachment upload(String noteId, String owner, MultipartFile file) throws IOException {
        noteService.requireOwner(noteId, owner);
        if (file.isEmpty()) {
            throw new BizException(400, "文件为空");
        }
        String fileName = file.getOriginalFilename() == null ? "unnamed" : file.getOriginalFilename();
        String objectKey = "note/" + noteId + "/" + IdUtil.random(8) + "-" + sanitize(fileName);
        try (InputStream in = file.getInputStream()) {
            oss.put(objectKey, in, file.getSize(), file.getContentType());
        }

        NoteAttachment attachment = new NoteAttachment();
        attachment.setNoteId(noteId);
        attachment.setFileName(fileName);
        attachment.setObjectKey(objectKey);
        attachment.setSize(file.getSize());
        attachment.setContentType(file.getContentType());
        attachment.setCreatedAt(LocalDateTime.now());
        attachmentMapper.save(attachment);
        return attachment;
    }

    public List<NoteAttachment> list(String noteId, String owner) {
        noteService.requireOwner(noteId, owner);
        return QueryChain.of(attachmentMapper)
                .eq(NoteAttachment::getNoteId, noteId)
                .orderByAsc(NoteAttachment::getCreatedAt)
                .list();
    }

    public byte[] download(String attachmentId, String owner) {
        return oss.getBytes(meta(attachmentId, owner).getObjectKey());
    }

    public NoteAttachment meta(String attachmentId, String owner) {
        return requireOwned(attachmentId, owner);
    }

    public String presignUrl(String attachmentId, String owner) {
        return oss.presign(requireOwned(attachmentId, owner).getObjectKey());
    }

    /** 非图片返回 404，调用方按原图展示。 */
    public byte[] thumbnail(String attachmentId, String owner) {
        NoteAttachment attachment = requireOwned(attachmentId, owner);
        String contentType = attachment.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BizException(404, "不是图片附件");
        }
        return ImageUtil.thumbnail(oss.getBytes(attachment.getObjectKey()), THUMB_MAX_EDGE, "webp", 0.8f);
    }

    public void delete(String attachmentId, String owner) {
        NoteAttachment attachment = requireOwned(attachmentId, owner);
        oss.delete(attachment.getObjectKey());
        attachmentMapper.deleteById(attachment.getId());
    }

    private NoteAttachment requireOwned(String attachmentId, String owner) {
        NoteAttachment attachment = attachmentMapper.getOptionalById(attachmentId)
                .orElseThrow(() -> new BizException(404, "附件不存在"));
        noteService.requireOwner(attachment.getNoteId(), owner);
        return attachment;
    }

    private static String sanitize(String fileName) {
        return fileName.replaceAll("[^\\w.\\-]", "_");
    }
}
