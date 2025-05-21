package com.OHYS.Destiny2Tools.Handler;

import com.OHYS.Destiny2Tools.config.CommandContext;
// 1. 修改CommandHandler为函数式接口
@FunctionalInterface
public interface CommandHandler {
    /**
     * 处理命令
     * @param context 包含所有执行上下文
     * @throws Exception 处理过程中可能出现的异常
     */
    void handle(CommandContext context) throws Exception;
}
