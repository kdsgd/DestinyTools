package com.OHYS.Destiny2Tools.service;

import com.alibaba.fastjson.JSONObject;
/** 实现 Bungie API 调用**/

public interface BungieService {

    /**获取玩家基础信息（示例）**/
    JSONObject getPlayerProfile(String bungieName) throws Exception;

    /**
     * 获取周报信息
     * @param playerIdentifier 玩家ID或名称
     * @return 周报数据
     */
    JSONObject getWeeklyReport(String playerIdentifier) throws Exception;
    // 其他 API 方法（如获取装备、活动等）

}