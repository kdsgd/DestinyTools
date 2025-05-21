package com.OHYS.Destiny2Tools.Handler.impl;

import com.OHYS.Destiny2Tools.Handler.CommandHandler;
import com.OHYS.Destiny2Tools.Handler.CommandMapping;
import com.OHYS.Destiny2Tools.config.CommandContext;
import com.OHYS.Destiny2Tools.service.BungieService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

//玩家信息处理器
@Component
@CommandMapping(value = "player", aliases = {"p", "查询玩家"})// 自定义注解
public class PlayerInfoHandler implements CommandHandler {

    private static final String BUNGIE_ID_REGEX = "^[\\p{L}\\p{N}_\\s]+#\\d{4}$";

    private final BungieService bungieService;

    // 通过构造器注入依赖
    public PlayerInfoHandler(BungieService bungieService) {
        this.bungieService = bungieService;
    }

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneId.of("UTC"));

    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");


    @Override
    public void handle(CommandContext context) {
        try {
            // 1. 参数提取与验证
            String bungieId = extractAndValidateBungieId(context);

            // 2. 调用不可修改的BungieService接口
            JSONObject playerData = bungieService.getPlayerProfile(bungieId);

            // 3. 响应处理
            processResponse(context, playerData);

        } catch (IllegalArgumentException e) {
            handleValidationError(context, e);
        } catch (Exception e) {
            handleApiError(context, e);
        }
    }

    private String extractAndValidateBungieId(CommandContext context) {
        if (!context.hasArgs()) {
            throw new IllegalArgumentException("需要提供玩家名称，格式：/d2 player 玩家名#1234");
        }

        String bungieId = context.getFirstArg();
        if (!bungieId.matches(BUNGIE_ID_REGEX)) {
            throw new IllegalArgumentException("Bungie ID格式必须为：玩家名#1234");
        }

        return bungieId;
    }

    private void processResponse(CommandContext context, JSONObject playerData) throws ParseException {
        if (!playerData.containsKey("Response")) {
            context.reply("⚠ API返回数据异常");
            return;
        }

        String response = buildResponseString(playerData);
        context.reply(response);
    }

    private String buildResponseString(JSONObject response) throws ParseException {
        if (response == null || !response.containsKey("Response")) {
            return "无效的API响应";
        }

//        JSONObject characters = Optional.ofNullable(response.getJSONObject("Response"))
//                .map(res -> res.getJSONObject("characters")).map(char -> char.getJSONObject("data")).orElse(null);

        JSONObject characters = response.getJSONObject("Response")
                .getJSONObject("characters")
                .getJSONObject("data");


        if (characters == null || characters.isEmpty()) {
            return "未找到角色信息。";
        }

        StringBuilder sb = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Set<String> characterIds = characters.keySet();
        for (String characterId : characterIds) {
            JSONObject character = characters.getJSONObject(characterId);

            // 职业类型映射
            String className = getClassTypeName(character.getIntValue("classType"));
            String raceName = getRaceTypeName(character.getIntValue("raceType"));

            // 获取属性值
            JSONObject stats = character.getJSONObject("stats");
            Map<String, Integer> attributes = Map.of(
                    "韧性", stats.getIntValue("392767087"),
                    "智慧", stats.getIntValue("1735777505"),
                    "纪律", stats.getIntValue("144602215"),
                    "敏捷", stats.getIntValue("1943323491"),
                    "恢复", stats.getIntValue("2996146975")
            );

            String isoDate = character.getString("dateLastPlayed");

            // 创建SimpleDateFormat对象并设置为UTC时区
            SimpleDateFormat sdfParse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdfParse.setTimeZone(TimeZone.getTimeZone("UTC"));


            // 解析时间字符串
            Date date = sdfParse.parse(isoDate);

            // 创建另一个SimpleDateFormat对象用于输出所需格式，并设置为目标时区（例如系统默认时区）
            SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdfFormat.setTimeZone(TimeZone.getDefault());

            // 格式化日期为所需格式
            String formattedTime = sdfFormat.format(date);

            // 构建角色信息
            sb.append(String.format(
                    "🛡️ 角色ID: %s%n" +
                            "├─ 职业: %s (%s)%n" +
                            "├─ 光等: %d%n" +
                            "├─ 属性: %s%n" +
                            "└─ 最后上线: %s%n",
                    characterId,
                    className, raceName,
                    character.getIntValue("light"),
                    formatAttributes(attributes),
                    formattedTime
            ));
        }

        return sb.toString();
    }

    private String formatDate(String isoDate) {
        return OUTPUT_FORMATTER.format(Instant.from(ISO_FORMATTER.parse(isoDate)));
    }

    // 辅助方法：职业类型转换
    private String getClassTypeName(int classType) {
        return switch(classType) {
            case 0 -> "泰坦";
            case 1 -> "猎人";
            case 2 -> "术士";
            default -> "未知";
        };
    }

    // 辅助方法：种族类型转换
    private String getRaceTypeName(int raceType) {
        return switch(raceType) {
            case 0 -> "人类";
            case 1 -> "觉醒者";
            case 2 -> "EXO";
            default -> "未知";
        };
    }

    // 辅助方法：格式化属性
    private String formatAttributes(Map<String, Integer> attributes) {
        return attributes.entrySet().stream()
                .map(entry -> entry.getKey() + entry.getValue())
                .collect(Collectors.joining(" "));
    }

    private void handleValidationError(CommandContext context, IllegalArgumentException e) {
        context.replyWithMention("⚠ " + e.getMessage() + "\n" +
                "示例：@小日向Bot /d2 player Guardian#1234");
    }

    private void handleApiError(CommandContext context, Exception e) {
        context.reply("🔍 查询失败，请检查ID是否正确或稍后重试");
        // 实际项目应记录日志
        // logger.error("Bungie API调用失败", e);
    }


}