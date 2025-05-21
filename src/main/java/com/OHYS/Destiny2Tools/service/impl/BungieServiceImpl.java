package com.OHYS.Destiny2Tools.service.impl;

import com.OHYS.Destiny2Tools.Exception.BungieApiException;
import com.OHYS.Destiny2Tools.service.BungieService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import io.micrometer.common.util.StringUtils;
import lombok.Setter;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

@Setter
@Service
public class BungieServiceImpl implements BungieService {

    private static final Logger logger = LoggerFactory.getLogger(BungieServiceImpl.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 3000;

    //setter注入
    private CloseableHttpClient httpClient;



    @Value("${bungie.api-key}")
    private String apiKey;

    @Value("${bungie.base-url}")
    private String baseUrl;


    @Autowired
    public BungieServiceImpl(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    //获取玩家基本信息


    public JSONObject getPlayerProfile(String bungieName) throws Exception {
        // 1. 解析名称格式
        String[] parts = bungieName.split("#");
        if (parts.length != 2) {
            throw new IllegalArgumentException("格式必须为 名称#1234");
        }

        // 2. 获取玩家的 membershipId（Destiny 会员 ID）
        JSONObject userInfo = searchByBungieName(parts[0], parts[1]);
        String membershipId = userInfo.getString("membershipId");
        int membershipType = userInfo.getInteger("membershipType");

        // 3. 调用原有方法
        // 参数校验
        if (!membershipId.matches("\\d+") || membershipId.length() < 10) {
            throw new IllegalArgumentException("membershipId 必须是长数字字符串");
        }
        if (StringUtils.isBlank(membershipId)) {
            throw new IllegalArgumentException("membershipId不能为空");
        }
        if (membershipType < 1 || membershipType > 5) {
            throw new IllegalArgumentException("membershipType 必须为 1(Xbox), 2(PSN), 3(Steam), 4(Blizzard), 或 5(Stadia)");
        }

        logger.debug("请求玩家资料: membershipType={}, membershipId={}", membershipType, membershipId);

        String url = String.format("%s/Destiny2/%d/Profile/%s/?components=Characters", baseUrl, membershipType, membershipId);
        HttpGet request = new HttpGet(url);
        request.setHeader("X-API-Key", apiKey);

        int retryCount = 0;
        while (true) {
            // 直接使用注入的 httpClient，而不是新建
            try (CloseableHttpResponse response = httpClient.execute(request)) {

                int statusCode = response.getStatusLine().getStatusCode();
                String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode != HttpStatus.SC_OK) {
                    throw new BungieApiException(
                            String.format("Bungie API请求失败[%d]: %s", statusCode, json),
                            statusCode
                    );
                }

                JSONObject result = JSON.parseObject(json);
                if (!result.containsKey("Response")) {
                    throw new BungieApiException("API返回格式异常，缺少Response字段");
                }

                return result;

            } catch (SocketTimeoutException | ConnectException e) {
                if (retryCount >= MAX_RETRIES) {
                    throw new BungieApiException("重试次数超过限制", -1, e);  // 明确指定状态码-1
                }
                retryCount++;
                logger.warn("Bungie API请求失败，正在重试({}/{}): {}", retryCount, MAX_RETRIES, e.getMessage());
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BungieApiException("重试被中断", ie);
                }
            } catch (JSONException e) {
                throw new BungieApiException("API返回数据解析失败", -1, e);
            } catch (Exception e) {
                throw new BungieApiException("调用Bungie API发生未知错误", -1, e);
            }
        }

    }

    @Override
    public JSONObject getWeeklyReport(String playerIdentifier) throws Exception {
        return null;
    }


    //获取玩家的 membershipId（Destiny 会员 ID）
    public JSONObject searchByBungieName(String displayName, String discriminator) throws Exception {
        // 1. 验证名称格式
        if (StringUtils.isBlank(displayName)){
            throw new IllegalArgumentException("显示名不能为空");
        }
        if (!discriminator.matches("\\d{4}")) {
            throw new IllegalArgumentException("后缀必须是4位数字");
        }



        // 2. 调用搜索API POST https://www.bungie.net/Platform/Destiny2/SearchDestinyPlayerByBungieName/{membershipType}/
        //  构建 POST 请求
        HttpPost request = new HttpPost(
                "https://www.bungie.net/Platform/Destiny2/SearchDestinyPlayerByBungieName/" + 3 + "/"
        );

        // 设置 Headers 和 JSON 请求体
        try{
            int displayNameCode = Integer.parseInt(discriminator);
            request.setHeader("X-API-Key", apiKey);
            request.setHeader("Content-Type", "application/json");
            String jsonBody = String.format("{\"displayName\":\"%s\",\"displayNameCode\":%d}", displayName, displayNameCode);
            request.setEntity(new StringEntity(jsonBody, "UTF-8"));
        }catch(Exception e){
            logger.warn("玩家后缀转换错误！");
        }


        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String json = EntityUtils.toString(response.getEntity());
            JSONObject result = JSON.parseObject(json);

            // 3. 处理响应
            Object response1 = result.get("Response");
            if (!result.containsKey("Response")&& response1 != null) {
                throw new BungieApiException("未找到玩家");
            }

            return result.getJSONArray("Response").getJSONObject(0);
        }
    }


}