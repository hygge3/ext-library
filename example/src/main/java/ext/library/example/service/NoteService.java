package ext.library.example.service;

import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import ext.library.audit.OperationLog;
import ext.library.audit.OperationType;
import ext.library.cache.annotation.Cache;
import ext.library.cache.strategy.CacheStrategy;
import ext.library.example.domain.Note;
import ext.library.example.mapper.NoteMapper;
import ext.library.example.support.NoteBroadcaster;
import ext.library.idempotent.Idempotent;
import ext.library.lock.RedisLock;
import ext.library.servlet.response.PageQuery;
import ext.library.servlet.response.PageResult;
import ext.library.tool.exception.BizException;
import ext.library.tool.runtime.Logs;
import ext.library.tool.trace.TraceContext;
import ext.library.tool.util.StringUtil;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 笔记读写。
 * <ul>
 *   <li>ext-cache：详情走 {@code @Cache}，改完按 cacheName + key 精确 evict</li>
 *   <li>ext-lock：同一篇笔记的保存串行，避免两次并发写互相覆盖</li>
 *   <li>ext-idempotent：新建按钮重复点只落一条</li>
 *   <li>ext-audit：删除记审计</li>
 *   <li>ext-core：变更通过 {@code PubSub} SPI 广播，多实例一起收到</li>
 *   <li>ext-trace：把当前 traceId 写进行里，页面上能看到这条笔记是哪次请求写的</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class NoteService {

    private static final String MODULE = "NOTE";

    private static final String CACHE_NAME = "note";

    private static final String LIST_CACHE = "note-list";

    private final NoteMapper noteMapper;

    private final CacheStrategy cache;

    private final NoteBroadcaster broadcaster;

    /** 列表进缓存 30 秒，写操作精确失效；关键词先过 StringUtil 清洗，避免通配符注入。 */
    public PageResult<Note> page(String owner, String keyword, PageQuery query) {
        String safe = StringUtil.cleanText(keyword);
        String cacheKey = owner + ":" + query.getPage() + ":" + query.getSize() + ":" + safe;
        PageResult<Note> cached = cache.get(LIST_CACHE, cacheKey, PageResult.class);
        if (cached != null) {
            return cached;
        }

        // 关键词只拼一次：空串时不加 nested，避免生成空的括号条件
        QueryChain<Note> chain = QueryChain.of(noteMapper).eq(Note::getOwnerEmail, owner);
        if (StringUtil.isNotEmpty(safe)) {
            chain.andNested(n -> n.like(Note::getTitle, safe).or().like(Note::getContent, safe));
        }
        Pager<Note> pager = chain.orderByDesc(Note::getUpdatedAt)
                .paging(Pager.of((int) query.getPage(), (int) query.getSize()));

        PageResult<Note> result = new PageResult<>(pager.getNumber(), pager.getSize(),
                pager.getTotal().longValue(), pager.getTotalPage().longValue(), pager.getResults());
        cache.put(LIST_CACHE, cacheKey, result, Duration.ofSeconds(30));
        return result;
    }

    @Cache(cacheName = CACHE_NAME, key = "#id", timeout = 60)
    public Note get(String id) {
        return find(id);
    }

    public Note find(String id) {
        return noteMapper.getOptionalById(id).orElseThrow(() -> new BizException(404, "笔记不存在"));
    }

    @Idempotent(uniqueExpression = "'note:create:' + #note.ownerEmail + ':' + #note.title", duration = 30,
            message = "这篇笔记刚刚提交过")
    public Note create(Note note) {
        note.setTraceId(TraceContext.currentOrGenerate());
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.save(note);
        invalidate(note);
        Logs.info(MODULE, "新建笔记：id={} owner={}", note.getId(), note.getOwnerEmail());
        return note;
    }

    @RedisLock(key = "'note:edit:' + #note.id", waitTime = 3)
    @OperationLog(module = "note", type = OperationType.UPDATE, desc = "保存笔记")
    public Note update(Note note) {
        Note exists = requireOwner(note.getId(), note.getOwnerEmail());
        exists.setTitle(note.getTitle());
        exists.setContent(note.getContent());
        exists.setTags(note.getTags());
        exists.setTraceId(TraceContext.currentOrGenerate());
        exists.setUpdatedAt(LocalDateTime.now());
        noteMapper.update(exists);
        invalidate(exists);
        // 同一账号的其它标签页会收到这条 SSE
        broadcaster.publish(exists.getOwnerEmail(), exists.getTitle(), "已更新");
        return exists;
    }

    @OperationLog(module = "note", type = OperationType.DELETE, desc = "删除笔记", recordResult = false)
    public void delete(String id, String owner) {
        Note note = requireOwner(id, owner);
        noteMapper.deleteById(id);
        invalidate(note);
        broadcaster.publish(owner, note.getTitle(), "已删除");
    }

    public List<Note> latest(String owner, int limit) {
        return QueryChain.of(noteMapper)
                .eq(Note::getOwnerEmail, owner)
                .orderByDesc(Note::getUpdatedAt)
                .limit(limit)
                .list();
    }

    public Note requireOwner(String id, String owner) {
        Note note = find(id);
        if (!note.getOwnerEmail().equals(owner)) {
            throw new BizException(403, "无权访问该笔记");
        }
        return note;
    }

    private void invalidate(Note note) {
        cache.distributed().evict(CACHE_NAME, note.getId());
        cache.clear(LIST_CACHE);
    }
}
