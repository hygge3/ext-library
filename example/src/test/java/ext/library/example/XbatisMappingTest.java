package ext.library.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.xbatis.core.mybatis.mapper.context.Pager;
import cn.xbatis.core.sql.executor.chain.QueryChain;
import ext.library.example.domain.AppUser;
import ext.library.example.domain.Note;
import ext.library.example.domain.NoteShare;
import ext.library.example.mapper.AppUserMapper;
import ext.library.example.mapper.NoteMapper;
import ext.library.example.mapper.NoteShareMapper;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * 用 H2 跑 xbatis 的真实 SQL：验证实体与表的字段映射、主键生成、条件链与分页。
 * <p>
 * 只装载 MyBatis 与 mapper，不启动整个应用（那需要 Redis 和 PostgreSQL）。
 */
@SpringJUnitConfig({XbatisMappingTest.DbConfig.class, MybatisAutoConfiguration.class})
class XbatisMappingTest {

    @Autowired
    AppUserMapper userMapper;

    @Autowired
    NoteMapper noteMapper;

    @Autowired
    NoteShareMapper shareMapper;

    @Test
    void insertFillsUuidV7AndMapsSnakeCaseColumns() {
        AppUser user = new AppUser();
        user.setEmail("a@b.c");
        user.setNickname("小明");
        user.setPasswordHash("{noop}x");
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());

        userMapper.save(user);

        assertNotNull(user.getId(), "主键应由 UUID_V7 生成器填回实体");
        AppUser loaded = userMapper.getOptionalById(user.getId()).orElseThrow();
        assertEquals("a@b.c", loaded.getEmail());
        assertEquals("小明", loaded.getNickname());
        assertEquals("USER", loaded.getRole(), "camelCase 字段应映射到 snake_case 列");
    }

    @Test
    void queryChainFiltersPagesAndDeletes() {
        for (int i = 0; i < 3; i++) {
            Note note = new Note();
            note.setOwnerEmail("pager@example.com");
            note.setTitle("笔记" + i);
            note.setContent("正文" + i);
            note.setUpdatedAt(LocalDateTime.now().plusMinutes(i));
            note.setCreatedAt(LocalDateTime.now());
            noteMapper.save(note);
        }

        Pager<Note> firstPage = QueryChain.of(noteMapper)
                .eq(Note::getOwnerEmail, "pager@example.com")
                .orderByDesc(Note::getUpdatedAt)
                .paging(Pager.of(1, 2));
        assertEquals(3L, firstPage.getTotal().longValue());
        assertEquals(2, firstPage.getResults().size());
        assertEquals("笔记2", firstPage.getResults().get(0).getTitle(), "应按 updatedAt 倒序");

        List<Note> liked = QueryChain.of(noteMapper)
                .eq(Note::getOwnerEmail, "pager@example.com")
                .like(Note::getTitle, "笔记1")
                .list();
        assertEquals(1, liked.size());

        Note target = liked.get(0);
        target.setTitle("改过的标题");
        noteMapper.update(target);
        assertEquals("改过的标题", noteMapper.getOptionalById(target.getId()).orElseThrow().getTitle());

        noteMapper.deleteById(target.getId());
        assertFalse(noteMapper.getOptionalById(target.getId()).isPresent());
    }

    @Test
    void deleteWithWhereClauseRemovesExpiredShares() {
        NoteShare expired = share("expired", LocalDateTime.now().minusDays(1));
        NoteShare alive = share("alive", LocalDateTime.now().plusDays(1));

        int removed = shareMapper.delete(where -> where.lt(NoteShare::getExpiresAt, LocalDateTime.now()));

        assertEquals(1, removed);
        assertTrue(shareMapper.getOptionalById(expired.getToken()).isEmpty(), "过期分享应被条件删除");
        assertTrue(shareMapper.getOptionalById(alive.getToken()).isPresent());
    }

    private NoteShare share(String token, LocalDateTime expiresAt) {
        NoteShare share = new NoteShare();
        share.setToken(token);
        share.setNoteId("note-1");
        share.setExpiresAt(expiresAt);
        share.setCreatedAt(LocalDateTime.now());
        shareMapper.save(share);
        return share;
    }

    /** DataSource 必须独立成类并排在前面：MybatisAutoConfiguration 的 @ConditionalOnSingleCandidate(DataSource) 在解析时就要能匹配到。 */
    @Configuration
    @MapperScan("ext.library.example.mapper")
    static class DbConfig {

        @Bean
        DataSource dataSource() throws SQLException {
            DataSource dataSource = DataSourceBuilder.create()
                    .url("jdbc:h2:mem:xbatis;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
                    .driverClassName("org.h2.Driver")
                    .username("sa")
                    .build();
            try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE app_user (id VARCHAR(36) PRIMARY KEY, email VARCHAR(128) NOT NULL,
                            nickname VARCHAR(64) NOT NULL, password_hash VARCHAR(100), role VARCHAR(16) NOT NULL,
                            created_at TIMESTAMP NOT NULL)
                        """);
                statement.execute("""
                        CREATE TABLE note (id VARCHAR(36) PRIMARY KEY, owner_email VARCHAR(128) NOT NULL,
                            title VARCHAR(120) NOT NULL, content TEXT NOT NULL, tags VARCHAR(200),
                            trace_id VARCHAR(32), created_at TIMESTAMP NOT NULL, updated_at TIMESTAMP NOT NULL)
                        """);
                statement.execute("""
                        CREATE TABLE note_share (token VARCHAR(32) PRIMARY KEY, note_id VARCHAR(32) NOT NULL,
                            expires_at TIMESTAMP NOT NULL, created_at TIMESTAMP NOT NULL)
                        """);
            }
            return dataSource;
        }
    }
}
