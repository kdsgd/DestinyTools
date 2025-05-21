package com.OHYS.Destiny2Tools.listener;

import com.OHYS.Destiny2Tools.Handler.CommandHandler;
import com.OHYS.Destiny2Tools.Handler.CommandMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.OHYS.Destiny2Tools.config.CommandContext;
import com.OHYS.Destiny2Tools.config.ResponseGenerator;
import love.forte.simboot.annotation.Listener;
import love.forte.simbot.event.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


// 主监听器类（指令路由）
@Component
public class DestinyCommandListener {
    private final Map<String, CommandHandler> commandHandlers;
    private final ResponseGenerator responseGenerator = new ResponseGenerator();
    private static final Logger logger = LoggerFactory.getLogger(DestinyCommandListener.class);



    @Autowired
    public DestinyCommandListener(List<CommandHandler> handlers) {
        this.commandHandlers = new HashMap<>();

        // 自动注册所有带@CommandMapping的处理器
        for (CommandHandler handler : handlers) {
            CommandMapping mapping = handler.getClass().getAnnotation(CommandMapping.class);
            if (mapping != null) {
                // 注册主命令
                commandHandlers.put(mapping.value(), handler);
                // 注册别名
                for (String alias : mapping.aliases()) {
                    commandHandlers.put(alias, handler);
                }
            }
        }
    }


    // 在监听器中创建上下文
    @Listener
    public void onGroupMessage(GroupMessageEvent event) {
        // 1. 验证消息格式
        String rawMessage = event.getMessageContent().getPlainText().trim();
        if (!rawMessage.startsWith("/d2 ")) {
            return; // 忽略非/d2开头的消息
        }

        // 2. 提取指令和参数
        String[] parts = rawMessage.substring(4).split("\\s+", 2);
        if (parts.length == 0) {
            event.replyAsync("⚠ 指令格式错误，请输入：/d2 [指令] [参数]");
            return;
        }

        // 3. 创建上下文
        CommandContext context = new CommandContext(event, parts);


        // 4.  获取并执行处理器
        String command = context.getCommand().toLowerCase();
        CommandHandler handler = commandHandlers.get(command);

        if (handler != null) {
            try {
                handler.handle(context);
            } catch (Exception e) {
                context.replyWithMention("指令执行出错: " + e.getMessage());
            }
        } else {
            context.reply("未知指令，可用指令: " + String.join(", ", commandHandlers.keySet()));
        }
    }

    private String getAvailableCommands() {
        return """
        ⚡ 可用指令：
        • /d2 player [名称#1234] - 查询玩家
        • /d2 weekly [名称] - 周报查询
        • /d2 help - 显示帮助""";
    }
}