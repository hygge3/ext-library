package ext.library.sse.listener;

import ext.library.sse.manager.SseConnectionManager;
import ext.library.sse.manager.SseMessagePublisher;
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
    private SseConnectionManager connectionManager;

    @Resource
    private SseMessagePublisher messagePublisher;

    /**
     * 在 Spring Boot 应用程序启动时初始化 SSE 主题订阅监听器
     *
     * @param args 应用程序参数
     */
    @Override
    public void run(ApplicationArguments args) {
        messagePublisher.subscribe(message -> {
            Logs.info(EmojiSymbol.SSE, "SSE 收到订阅消息，userIds:{}, message:{}", message.getUserIds(), message.getMessage());
            // 如果 userIds 不为空就按用户发消息，否则群发
            if (ObjectUtil.isNotEmpty(message.getUserIds())) {
                message.getUserIds().forEach(userId -> connectionManager.sendMessage(userId, message.getMessage()));
            } else {
                connectionManager.sendMessageToAll(message.getMessage());
            }
        });
        Logs.info(EmojiSymbol.SSE, "初始化 SSE 主题订阅监听器成功");
    }

}
