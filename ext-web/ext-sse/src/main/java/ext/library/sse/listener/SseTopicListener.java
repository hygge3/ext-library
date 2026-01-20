package ext.library.sse.listener;

import ext.library.sse.manager.SseEmitterManager;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.ObjectUtil;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;

import jakarta.annotation.Resource;

/**
 * SSE 主题订阅监听器
 */
@Order(-1)
public class SseTopicListener implements ApplicationRunner {

    @Resource
    private SseEmitterManager sseEmitterManager;

    /**
     * 在 Spring Boot 应用程序启动时初始化 SSE 主题订阅监听器
     *
     * @param args 应用程序参数
     *
     * @throws Exception 初始化过程中可能抛出的异常
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        sseEmitterManager.subscribeMessage((message) -> {
            Logs.info(EmojiSymbol.SSE, "SSE 主题订阅收到消息，session keys:{},message:{}", message.getUserIds(), message.getMessage());
            // 如果 key 不为空就按照 key 发消息 如果为空就群发
            if (ObjectUtil.isNotEmpty(message.getUserIds())) {
                message.getUserIds().forEach(key -> sseEmitterManager.sendMessage(key, message.getMessage()));
            } else {
                sseEmitterManager.sendMessage(message.getMessage());
            }
        });
        Logs.info(EmojiSymbol.SSE, "初始化 SSE 主题订阅监听器成功");
    }

}
