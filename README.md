 一个基于 Spring Boot + NapCat + Shiro + WebSocket + Bungie API 的 QQ 群机器人插件系统，功能上实现了通过群消息指令 /d2 player 玩家名#编号 查询《命运2（Destiny 2）》玩家的角色信息，并在群中回复查询结果。
一、技术栈与框架解析
1. Spring Boot
● 用于搭建后端服务，管理项目依赖和配置。
● 管理类的注入（如 @Component 和构造器注入）。
2. NapCat + Shiro 插件框架
● NapCat 是一个基于 QQ 官方客户端的机器人框架，提供对 QQ 消息事件的监听。
● Shiro 是 NapCat 的插件系统，提供事件钩子如 onGroupMessage、onPrivateMessage 等。
3. WebSocket 通信
● NapCat 与你的 Java 程序通过 WebSocket 通信，推送 QQ 消息事件给你的程序，程序响应后也通过 WebSocket 向 NapCat 发消息。
● 非 HTTP 模式，更实时、不依赖 HTTP 端口监听，适合桌面型客户端开发。
4. CommandContext / NapCatCommandContext
● 封装了指令解析逻辑，例如获取玩家名参数、回复消息。
● 解耦了底层 Shiro 的事件结构与业务逻辑，让逻辑更清晰、更易测试与复用。
5. Bungie API 调用
● 通过封装的 BungieService，使用 Bungie 提供的 REST API 获取《命运2》玩家数据。
● 使用 FastJSON 处理 API 响应数据。

二、代码执行流程（onGroupMessage）
1. 接收到群消息事件 GroupMessageEvent。
2. 判断消息是否为：
  ○ hello：简单回复。
  ○ /d2 player：进入 Destiny 2 玩家信息处理流程。
3. 解析玩家名参数并校验格式（必须符合 xxx#1234）。
4. 调用 BungieService.getPlayerProfile() 获取数据。
5. 将返回的 JSON 格式化为易读文字，展示光等、职业、最后上线等角色信息。
6. 使用 bot.sendGroupMsg 回复信息至群。

三、WebSocket 的作用
NapCat 客户端通过 WebSocket 向你的后端（Shiro 插件）发送如下事件：
● 私聊消息
● 群聊消息
● 通知（如入群、退群等）
你的机器人通过同样的 WebSocket 连接回发处理结果（如 bot.sendGroupMsg()）。
相较 HTTP 接口：
● WebSocket 具有实时性（事件推送）
● 更适合长期运行的客户端插件
● 适合 NapCat 的插件化架构

四、模拟面试问题 & 答案
Q1: 简述你这个 QQ 机器人系统的主要技术架构和工作流程。
答：
我使用 Spring Boot 作为基础架构，NapCat（内置 LiteLoader QQ 客户端） + Shiro 插件系统接收 QQ 消息，使用 WebSocket 实时通讯。收到 /d2 player 玩家名#编号 指令后，使用自定义命令上下文类 NapCatCommandContext 解析命令参数，调用 Bungie 官方 API 获取 Destiny 2 玩家角色信息，并使用 bot.sendGroupMsg() 将数据格式化后回复至 QQ 群。

Q2: 为什么需要自定义 NapCatCommandContext，而不是直接使用原始 event？
答：
自定义 NapCatCommandContext 是为了抽象和统一命令处理逻辑，使得业务代码不依赖具体框架细节，便于扩展、测试、维护。例如，getFirstArg()、hasArgs()、reply() 等方法屏蔽了底层 Shiro 的 event 数据结构。

Q3: WebSocket 与 HTTP 在 QQ 机器人开发中的优劣比较？
答：
WebSocket 是长连接，消息推送实时，适合事件驱动架构如 NapCat。HTTP 是短连接，处理延迟高、不适合持续监听。WebSocket 能减少轮询带来的资源消耗，且无需本地 HTTP 服务监听端口，更适合桌面机器人部署。

Q4: 如何确保 Bungie ID 的格式正确？
答：
我定义了正则表达式 ^[\\p{L}\\p{N}_\\s]+#\\d{4}$，匹配“玩家名#四位数字”的格式。解析命令时先判断参数数量，再使用正则校验格式，否则抛出异常并提示用户正确格式。

Q5: 如果 Bungie API 响应结构变了，会影响哪部分逻辑？如何优化？
答：
响应结构变动将影响 buildResponseString() 方法中 JSON 的字段读取逻辑。可以通过引入 POJO + FastJSON 的 @JSONField 做对象映射，替代硬编码的 getJSONObject("characters") 等方式，提高健壮性和可维护性。

五、建议补充点
1. 参数解析建议更灵活：
  ○ 当前 args 只能解析 /d2 player 玩家#1234。
  ○ 可改为正则提取玩家 ID，从整个消息中提取第一个符合格式的。
2. 错误日志记录：
  ○ 当前异常只 reply 给用户，建议加日志输出，方便排查。
3. 玩家名大小写敏感问题：
  ○ Bungie 可能对大小写敏感，建议在提交请求前处理。
4. 缓存机制（可选）：
  ○ 为减少频繁请求 API，可对相同玩家名进行缓存（例如本地缓存 5 分钟）。
  
