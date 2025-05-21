package com.OHYS.Destiny2Tools.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BungieServiceImplTest {

	@Mock
	private CloseableHttpClient httpClient;

	@Mock
	private CloseableHttpResponse response;

	@Mock
	private StatusLine statusLine;

	@Mock
	private HttpEntity entity;

	@InjectMocks
	private BungieServiceImpl bungieService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(bungieService, "apiKey", "test-key");
		ReflectionTestUtils.setField(bungieService, "baseUrl", "https://test.url");
	}

	@Test
	void testGetPlayerProfile_Success() throws Exception {
		// 配置 Mock 行为
		when(httpClient.execute(any(HttpGet.class))).thenReturn(response);
		when(response.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(200);
		when(response.getEntity()).thenReturn(entity);
		//这表示模拟的 HTTP 响应返回了一个 只有空 Response 字段的 JSON，所以实现类解析后自然返回 {"Response":{}}。
		when(entity.getContent()).thenReturn(new ByteArrayInputStream("{\"Response\":{}}".getBytes()));

		// 执行测试
		JSONObject result = bungieService.getPlayerProfile("XYZ#1782");
		System.out.println(result);
		// 验证
		assertNotNull(result);
		verify(httpClient).execute(any(HttpGet.class));
	}
}