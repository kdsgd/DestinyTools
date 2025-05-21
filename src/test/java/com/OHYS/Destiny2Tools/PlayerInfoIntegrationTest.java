package com.OHYS.Destiny2Tools;

import com.OHYS.Destiny2Tools.Handler.impl.PlayerInfoHandler;
import com.OHYS.Destiny2Tools.listener.DestinyCommandListener;
import com.OHYS.Destiny2Tools.service.BungieService;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.event.GroupMessageEvent;
import love.forte.simbot.message.PlainText;
import love.forte.simbot.message.ReceivedMessageContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import love.forte.simbot.definition.Member;
import love.forte.simbot.ID;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PlayerInfoIntegrationTest {

    @Autowired
    private BungieService bungieService;

    @Autowired
    private PlayerInfoHandler playerInfoHandler;

    @Mock
    private GroupMessageEvent mockEvent;

    @Mock
    private Bot mockBot;

    @Mock
    ID mockId;

    @Mock
    private Member mockMember;  // 使用 Member 替代 Author

    @Mock
    private ReceivedMessageContent mockMessageContent;

    @Mock
    private PlainText mockPlainText;

    @Captor
    private ArgumentCaptor<String> replyCaptor;

    private DestinyCommandListener listener;

    @BeforeEach
    void setUp() {
        listener = new DestinyCommandListener(List.of(playerInfoHandler));

        // 正确配置 mock 对象
        when(mockEvent.getBot()).thenReturn(mockBot);
        when(mockEvent.getAuthor()).thenReturn(mockMember);
        when(mockMember.getId()).thenReturn(mockId);
        // 配置消息内容mock
        when(mockEvent.getMessageContent()).thenReturn(mockMessageContent);
        when(mockMessageContent.getPlainText()).thenReturn(""); // 默认值，测试方法中会覆盖
    }

    @Test
    void testValidPlayerQuery() {
        // 设置测试消息
        String testPlayerName = "娃哈哈店长#7697";
        String inputMessage = "/d2 player " + testPlayerName;

        when(mockMessageContent.getPlainText()).thenReturn(inputMessage);
        when(mockMessageContent.getPlainText()).thenReturn(inputMessage);

        // 执行测试
        listener.onGroupMessage(mockEvent);

        // 验证回复
        verify(mockEvent, atLeastOnce()).replyAsync(replyCaptor.capture());
        String reply = replyCaptor.getValue();

        assertNotNull(reply, "应该收到回复");
        assertTrue(reply.contains("角色ID"), "回复应包含玩家信息");
        System.out.println("API响应结果: " + reply);
    }

    @Test
    void testInvalidPlayerFormat() {
        String inputMessage = "/d2 player invalid_format";
        when(mockEvent.getMessageContent().getPlainText()).thenReturn(inputMessage);

        listener.onGroupMessage(mockEvent);

        verify(mockEvent).replyAsync(replyCaptor.capture());
        String reply = replyCaptor.getValue();

        assertTrue(reply.contains("格式必须为"), "应提示正确格式");
        System.out.println("格式错误测试结果: " + reply);
    }
}