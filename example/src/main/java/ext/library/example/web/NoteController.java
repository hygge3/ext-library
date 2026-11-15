package ext.library.example.web;

import ext.library.example.domain.Note;
import ext.library.example.domain.NoteAttachment;
import ext.library.example.service.AttachmentService;
import ext.library.example.service.AuthService;
import ext.library.example.service.NoteService;
import ext.library.example.service.StatsService;
import ext.library.servlet.response.PageQuery;
import ext.library.servlet.response.PageResult;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 笔记读写与附件。
 */
@RestController
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    private final AttachmentService attachmentService;

    private final StatsService statsService;

    private final AuthService authService;

    @GetMapping("/api/notes")
    public PageResult<Note> page(@RequestParam(defaultValue = "1") long page,
                                 @RequestParam(defaultValue = "10") long size,
                                 @RequestParam(defaultValue = "") String keyword) {
        PageQuery query = new PageQuery();
        query.setPage(page);
        query.setSize(size);
        return noteService.page(owner(), keyword, query);
    }

    @GetMapping("/api/notes/latest")
    public List<Note> latest() {
        return noteService.latest(owner(), 5);
    }

    @PostMapping("/api/notes")
    public Note create(@RequestParam String title, @RequestParam(defaultValue = "") String content,
                       @RequestParam(defaultValue = "") String tags) {
        Note note = new Note();
        note.setOwnerEmail(owner());
        note.setTitle(title);
        note.setContent(content);
        note.setTags(tags);
        return noteService.create(note);
    }

    @PostMapping("/api/notes/{id}/update")
    public Note update(@PathVariable String id, @RequestParam String title,
                       @RequestParam(defaultValue = "") String content,
                       @RequestParam(defaultValue = "") String tags) {
        Note note = new Note();
        note.setId(id);
        note.setOwnerEmail(owner());
        note.setTitle(title);
        note.setContent(content);
        note.setTags(tags);
        return noteService.update(note);
    }

    @DeleteMapping("/api/notes/{id}")
    public Map<String, Object> delete(@PathVariable String id) {
        noteService.delete(id, owner());
        return Map.of("deleted", id);
    }

    /** 打开详情页时累加阅读量（PostgreSQL）并写最近浏览（Redis）。 */
    @PostMapping("/api/notes/{id}/view")
    public Map<String, Object> view(@PathVariable String id) {
        noteService.requireOwner(id, owner());
        statsService.touchRecent(owner(), id);
        return Map.of("views", statsService.incrementView(id));
    }

    @GetMapping("/api/notes/{id}/attachments")
    public List<NoteAttachment> attachments(@PathVariable String id) {
        return attachmentService.list(id, owner());
    }

    @PostMapping("/api/notes/{id}/attachments")
    public NoteAttachment upload(@PathVariable String id, @RequestParam("file") MultipartFile file)
            throws IOException {
        return attachmentService.upload(id, owner(), file);
    }

    @GetMapping("/api/attachments/{id}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String id) {
        NoteAttachment meta = attachmentService.meta(id, owner());
        byte[] bytes = attachmentService.download(id, owner());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''"
                        + URLEncoder.encode(meta.getFileName(), StandardCharsets.UTF_8))
                .contentType(meta.getContentType() == null
                        ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(meta.getContentType()))
                .body(new ByteArrayResource(bytes));
    }

    @GetMapping("/api/attachments/{id}/thumbnail")
    public ResponseEntity<ByteArrayResource> thumbnail(@PathVariable String id) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/webp"))
                .body(new ByteArrayResource(attachmentService.thumbnail(id, owner())));
    }

    @DeleteMapping("/api/attachments/{id}")
    public Map<String, Object> deleteAttachment(@PathVariable String id) {
        attachmentService.delete(id, owner());
        return Map.of("deleted", id);
    }

    private String owner() {
        return authService.currentUser().getEmail();
    }
}
