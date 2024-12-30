package ext.library.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * OkHttp logger, Slf4j and console log.
 */
@Slf4j
public enum HttpLogger implements HttpLoggingInterceptor.Logger {

    /**
     * http 日志：Slf4j
     */
    Slf4j() {
        @Override
        public void log(@NotNull @Nls String message) {
            log.info("[🔗] " + message);
        }
    },

    /**
     * http 日志：Console
     */
    Console() {
        @Override
        public void log(@NotNull @Nls String message) {
            // 统一添加前缀，方便在茫茫日志中查看
            System.out.println("[🔗] " + message);
        }
    }

}
