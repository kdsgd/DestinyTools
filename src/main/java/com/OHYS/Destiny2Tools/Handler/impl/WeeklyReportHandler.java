package com.OHYS.Destiny2Tools.Handler.impl;


import com.OHYS.Destiny2Tools.config.CommandContext;
import com.OHYS.Destiny2Tools.config.ResponseGenerator;
import com.OHYS.Destiny2Tools.service.BungieService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// 周报处理器
@Component
public class WeeklyReportHandler {
    @Autowired
    private BungieService bungieService;
    @Autowired
    private ResponseGenerator responseGenerator;

    public void handle(CommandContext context) {
        try {
            String[] args = context.getArgs();
            String playerId = args.length > 0 ? args[0] : "";

            JSONObject weeklyData = bungieService.getWeeklyReport(playerId);
            context.reply(
                    responseGenerator.generateWeeklyReport(weeklyData)
            );
        } catch (Exception e) {
            context.reply("⚠ 周报获取失败: " + e.getMessage());
        }
    }
}