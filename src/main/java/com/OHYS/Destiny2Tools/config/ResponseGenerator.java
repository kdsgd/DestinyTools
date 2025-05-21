package com.OHYS.Destiny2Tools.config;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class ResponseGenerator {
    public String generateHelp() {
        return """
            ╔════ Destiny 2 指令帮助 ════
            ║ /d2 player [名称#1234]  查询玩家
            ║ /d2 weekly [名称]      周报信息
            ║ /d2 raid [名称]       副本进度
            ║ /d2 gear [名称]       装备检查
            ╚═══════════════════════""";
    }

    public String generatePlayerResponse(JSONObject data) {
        // 实现玩家信息格式化
        return "玩家信息响应...";
    }

    public String generateWeeklyReport(JSONObject data) {
        // 实现周报格式化
        return "周报信息响应...";
    }
}