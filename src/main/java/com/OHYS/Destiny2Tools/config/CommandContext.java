package com.OHYS.Destiny2Tools.config;


import lombok.Getter;
import love.forte.simbot.ID;
import love.forte.simbot.message.Text;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.event.GroupMessageEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.At;

import java.util.Arrays;

//命令上下文对象
public class CommandContext {

    private static final Logger logger = LoggerFactory.getLogger(CommandContext.class);

    // region 核心访问方法
    // 获取原始事件
    @Getter
    private final GroupMessageEvent event;
    private final String[] rawArgs;

    public CommandContext(@NotNull GroupMessageEvent event, @NotNull String[] rawArgs) {
        this.event = event;
        this.rawArgs = rawArgs;
        Bot bot = event.getBot();
    }

    // 新增回复方法
    public void reply(String message) {
        event.replyAsync(message); // 使用异步回复
    }

    // 带标记的回复（如@发送者）
    // 带@回复的方法
    public void replyWithMention(String message) {
        try {
            // 获取发送者ID并转换为 long 类型
            String authorIdStr = String.valueOf(event.getAuthor().getId());
            long authorIdLong = Long.parseLong(authorIdStr);

            // ✅ 使用 $ 创建 ID 实例
            ID authorId = ID.$(authorIdLong);

            // 创建 At 消息元素
            At atUser = new At(authorId);

            // 构造消息：@用户 + 文本
            var msg = Messages.toMessages(
                    atUser,
                    Text.of(" " + message)
            );

            // 异步回复
            event.replyAsync(msg);
        } catch (Exception e) {
            logger.error("@发送者失败"+e);
        }
    }

    public String getCommand() {
        return rawArgs.length > 0 ? rawArgs[0] : "";
    }

    public String[] getArgs() {
        return rawArgs.length > 1 ? Arrays.copyOfRange(rawArgs, 1, rawArgs.length) : new String[0];
    }

    /**
     * 检查是否包含参数
     */
    public boolean hasArgs() {
        return getArgs().length > 0;
    }

    /**
     * 获取第一个参数（指令后的第一个参数）
     * @return 第一个参数，若无参数则返回空字符串
     */
    public String getFirstArg() {
        return getArgs().length > 0 ? getArgs()[0] : "";
    }

    /**
     * 带默认值的获取方法
     * @param defaultValue 当无参数时返回的默认值
     * @return 第一个参数或默认值
     */
    public String getFirstArg(String defaultValue) {
        return getArgs().length > 0 ? getArgs()[0] : defaultValue;
    }


    // 其他实用方法...
}