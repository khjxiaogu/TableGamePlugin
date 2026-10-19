# TableGames 平台中台（platform 包）接入文档

> 适用范围：本仓库 `com.khjxiaogu.TableGames.platform`（下称"中台层"）。
> 目标读者：需要把本桌游插件接入一个新聊天平台（QQ、Kook、Discord、TG……）的开发者，或需要在游戏逻辑里正确使用中台 API 的开发者。
> 依据代码：`src/main/java/com/khjxiaogu/TableGames/platform/**`，含 Mirai 与 SimpleRobot(Kook) 两套已有实现。

---

## 0. 一句话结论

中台层把"机器人平台"抽象成 **房间（AbstractRoom）/ 用户（AbstractUser）/ 消息（IMessage）/ 用户标识（UserIdentifier）/ 事件监听（DynamicListeners）** 五件事；
游戏层只依赖这五件事，永远不 `import net.mamoe.mirai.*` 或 `love.forte.simbot.*`。

接入一个新平台 = **实现 6 个接口 + 写一个事件入口 + 注册 id 反序列化器**，其余命令系统、权限系统、统计、序列化、持久化全部复用。

```
┌─────────────────────────────────────────────────────────┐
│ 游戏逻辑 game/**、统计 data/**、工具 utils/**            │
│   只依赖：AbstractRoom / AbstractUser / IMessage / ...   │
└─────────────────────────┬───────────────────────────────┘
                          │ 抽象接口
┌─────────────────────────▼───────────────────────────────┐
│ 中台层 platform/                                        │
│  AbstractRoom  AbstractUser  AbstractBotUser            │
│  UserIdentifier  message/**  DynamicListeners           │
│  GlobalMain（命令注册表 + 派发中枢 + 数据库初始化）      │
└─────────────────────────┬───────────────────────────────┘
        ┌─────────────────┴──────────────────┐
┌───────▼───────────┐              ┌────────▼──────────────┐
│ mirai/ (QQ)       │              │ simplerobot/ (Kook)   │
│ MiraiGroup        │              │ SBChannel             │
│ MiraiHumanUser    │              │ SBHumanUser           │
│ MiraiUserFriend   │              │ SBUserFriend          │
│ MiraiBotUser      │              │ SBBotUser             │
│ MiraiAdapter      │              │ KooKAdapter           │
│ MiraiMain(入口)   │              │ KookMain(入口,main)   │
└───────────────────┘              └───────────────────────┘
```

---

## 1. 包结构总览

| 位置 | 类 / 接口 | 职责 |
|---|---|---|
| `platform/` | `AbstractUser` | 单个用户（真人或机器人替身）的全部能力 |
| | `AbstractBotUser` | 继承 `AbstractUser`，机器人替身玩家，额外有 `BotUserLogic` |
| | `BotUserLogic` / `BotUser` | 机器人替身的"AI 大脑"回调接口 + 空实现基类 |
| | `AbstractRoom` | 群 / 频道 / 房间 |
| | `UserIdentifier` / `QQId` / `SBId` / `UserIdentifierSerializer` | 跨平台身份标识与序列化 |
| | `RoomMessageEvent` | 事件上下文（发送者 + 房间） |
| | `MessageListener` / `RoomMessageListener` | 两个函数式监听接口 |
| | `DynamicListeners` | 监听注册表 + 消息派发 |
| | `MsgType` / `Permission` | 消息类型（AT/PRIVATE/PUBLIC）、权限等级（USER/ADMIN/SYSTEM） |
| | `GlobalMain` | 中台总入口：`init()`、命令表、`firePublicCommand`、`defaultFirePrivate`、`makeGame` |
| | `UnifiedLogger` | 日志抽象，两个适配器各给一个实现 |
| | `UserFunction` | `AbstractUser` 的委托包装（实现 `equals`/`Serializable`，游戏内常用） |
| | `Platform` | **空接口，无任何引用，历史遗留**，不要实现它 |
| `platform/message/` | `IMessage` / `IMessageCompound` / `Text` / `At` / `Image` / `MessageCompound` / `PlatformMessage` | 统一消息模型 |
| `platform/mirai/` | `Mirai*` | QQ（mirai-console）适配器 |
| `platform/simplerobot/` | `SB*`、`KooKAdapter`、`KookAPI` | Kook（SimpleRobot）适配器 |
| `platform/Markov*`, `TMWK` | 马氏回声（闲聊）、TWMK wiki 查询 | 与平台无关的附属功能，入口层调用 |

---

## 2. 核心抽象逐个说明

### 2.1 UserIdentifier —— 身份标识

```java
public interface UserIdentifier extends Serializable {
    String getId();        // 平台原始 id（字符串形态）
    String serialize();    // 持久化形态，必须能被 UserIdentifierSerializer 反解
    boolean isActual();    // 是否为"真人"（机器人替身返回 false）
    static UserIdentifier parseUserIdentifier(String s); // = UserIdentifierSerializer.readOptional(s).orElse(null)
}
```

实现要求（踩过的坑都在这里）：

1. **`serialize()` 必须全局唯一且带平台前缀**。`GlobalMain.init()` 里先注册了 QQ 的解析器（`Long.parseLong`），如果新平台 id 也是纯数字会**被 QQ 抢走**。现有实现：`QQId.serialize() = "123456"`，`SBId.serialize() = "sb:841570688"`。
2. **必须做实例缓存（intern）**。`DynamicListeners.dispatch(uid,rid,...)` 里用 `e.from == rid`（**引用相等**）做房间过滤，`QQId.of(...)`、`SBId.of(...)` 内部都有 `Map` 缓存。新实现如果每次 `new`，房间级监听会静默失效。
3. **`equals`/`hashCode` 必须按 id 实现**，因为 `UserIdentifier` 被用作 `ConcurrentHashMap` 的 key（监听表、数据库表、积分表）。
4. **`serialize()` 会被当文件名用**：权限文件是 `<serializedId>.permission`（见 `GlobalMatcher.load`）。避免 `/ \ : * ? " < >` 之外还要注意 `split("\\.")`，所以**别再带 `.`**。
5. `isActual()` 用于区分真人/机器人替身，例如狼人杀机器人投票时排除非真人（`WereWolfBot` 用到）。
6. 存档里存的是 `serialize()` 字符串，**接入后不可随意改格式**，否则老存档里的人和积分全部失联。

反序列化注册（`UserIdentifierSerializer`）：

```java
UserIdentifierSerializer.addRawSerializer(s -> MyId.parse(s));       // 抛异常/返回 null 都会被吞掉
UserIdentifierSerializer.addSerializer(s -> Optional.ofNullable(x)); // 或显式 Optional
UserIdentifier u = UserIdentifierSerializer.read(str);               // 找不到会抛 NoSuchElementException
UserIdentifier u2 = UserIdentifier.parseUserIdentifier(str);         // 找不到返回 null
```

现有注册点在 `GlobalMain.init()`：

```java
UserIdentifierSerializer.addRawSerializer(e -> QQId.of(Long.parseLong(e)));
UserIdentifierSerializer.addRawSerializer(e -> SBId.load(e));   // "sb:" 前缀，非前缀返回 null
```

新平台两种接法：改 `GlobalMain.init()` 加一行；或在调用完 `init()` 后立刻 `addRawSerializer(...)`（`init` 有 `hasInited` 幂等保护，重复调用无副作用）。

### 2.2 消息模型（`platform/message`）

```java
public interface IMessage {
    default MessageCompound asMessage() { return new MessageCompound().append(this); }
}
public interface IMessageCompound extends List<IMessage>, IMessage {
    String getText();            // 纯文本视图：只有 Text 的内容参与拼接；At/Image 等非文本片段
                                 // 在 MessageCompound 中被替换为一个空格，在适配器实现中直接被忽略
    <T> T first(Class<T> cls);   // 取第一个指定类型元素
    void flatern();              // 把嵌套的 compound 摊平
}
```

| 类 | 说明 |
|---|---|
| `Text` | 纯文本片段 |
| `At` | `@某人`，持有 `UserIdentifier`；`new At(user)` 或 `new At(id)` |
| `Image` | 图片，持有 `byte[] data`（可 `new Image(RenderedImage)`） |
| `MessageCompound` | 有序片段容器，链式 `append(String)` / `append(IMessage)` |
| `PlatformMessage` | 平台原生消息的透传包装（例如 mirai 的 `MessageChain` 元素），渲染时原样交回平台 |

**发消息的惯用写法**：

```java
user.sendPrivate(new Text("你被投票出局").asMessage());
room.sendMessage(user.getAt().asMessage().append("请开始描述"));
user.sendPublic("结果：" + text);                  // 纯文本快捷方式
```

> ⚠️ **坑 1**：`MessageCompound.first()` 无匹配时返回 `null`，但两个适配器的 `IMessageCompound.first()` 实现（`MiraiMessageCompound`、`SBMessageCompound`）在无匹配时返回的是 `XxxPlatformMessage(null)`，调用方写成 `At at = msg.first(At.class);` 会 **ClassCastException** 而不是 `null`。自己实现时请务必返回 `null`，或者调用方用 `Utils.getPlainText(msg)` + `instanceof` 自行判断。
>
> ⚠️ **坑 2**：适配器的 `IMessageCompound`（如 `MiraiMessageCompound`）是对**平台原生消息对象的懒包装**，`flatern()` 是空实现，且包装内部的 `MessageChain`/`Messages` 只在当前事件内有效。**不要跨事件、跨线程延迟持有**，需要留存请先 `getText()` 或转成 `Text`/`Image`。

### 2.3 AbstractUser —— 用户

```java
public interface AbstractUser {
    // 三种发送语义
    void sendPrivate(String str);  void sendPrivate(IMessage msg);   // 私聊
    void sendPublic(String str);   void sendPublic(IMessage msg);    // 公开消息（现有实现会自动带 @）
    void sendForName(String str);  void sendForName(IMessage msg);   // 公开消息但带昵称前缀，不 @

    void tryAvailable();          // 检查能否私聊（未加好友时提示加好友）
    IMessage getAt();             // 生成 @ 自己的消息片段（机器人替身返回 Text("@名字")）
    String getMemberString();     // 展示用名（群名片优先）
    void setNameCard(String s);   String getNameCard();  // 群名片读写
    AbstractRoom getRoom();       // 所在房间（私聊场景可能为 null！）
    void tryMute(); void tryUnmute();  // 平台禁言
    UserIdentifier getId();  UserIdentifier getHostId();  // 用户 id / 机器人自身 id
    void bind(Object obj);  Object getRoleObject();       // 游戏侧挂载业务对象
    void setGame(Game g);                                 // 机器人替身关联游戏
    Permission getPermission();  boolean isFriend();

    // 监听（default 实现，依赖 getRoom() 非 null）
    default void registerListener(MessageListener msgc)          { DynamicListeners.registerListener(getId(), getRoom().getId(), msgc); }
    default void releaseListener()                               { DynamicListeners.releaseListener(getId()); }
    default void transferListener(AbstractUser another)          { DynamicListeners.transferListener(getId(), another); }

    // 可选钩子（默认空实现）
    default void receiveMessage(UserIdentifier id, String msg, boolean isPublic) {}  // 机器人替身收游戏消息
    default void tryMuteBackend() {}   default void tryUnmuteBackend() {}             // 后端（频道权限）禁言
    default void requestOperation(boolean isPublic, boolean isTemperal, String hint) {} // 请求该玩家行动（LLM 机器人用）

    void addRef();  void release();  boolean isReferred();   // 机器人替身引用计数
}
```

实现要点：

1. **`sendPublic` 建议带 @**：Mirai 实现是 `group.sendMessage(at + str)`，这样玩家知道在跟自己说话。`sendForName` 则用昵称前缀，适合"某人 已弃权"这种广播。
2. **发送失败要有兜底**：Mirai 实现遇到发送异常会 `sleep(3000)` 无限重试，公开失败则退回私聊。新实现至少不要抛出去炸掉游戏线程。
3. **`getRoom()` 在私聊用户上返回 `null`**（`MiraiUserFriend.getRoom()` / `SBUserFriend.getRoom()`）。因此这类实现**必须重写 `registerListener` / `releaseListener` / `transferListener` 为空实现**，否则 default 实现会 NPE。
4. `getHostId()` 返回**机器人自己的 id**（Mirai: `bot.getId()`；Kook: `bot.toMember().getId()`）。注意入口实际传给 `firePublicCommand` 的 `botid` 参数在两套现有实现里都是 `rid`（房间 id），所以 `GlobalMatcher` 的本地权限文件是按**房间 id** 命名的（`<roomId>.permission`）；`getHostId()` 目前主要出现在 `new MatchInfo(cmd, user)` 这类便捷构造里。
5. `getPermission()` 映射：群主 `SYSTEM`、管理员 `ADMIN`、普通成员 `USER`（Mirai 用 `MemberPermission`，Kook 用 `member.isOwner()/isAdmin()`）。
6. **序列化**：真人用户对象经常随 `Game` 一起被 Java 序列化落盘。做法是平台对象字段标 `transient`，实现 `writeObject` 写 id，`readObject` 用 id 重新取回平台对象。`MiraiHumanUser`、`MiraiGroup`、`SBHumanUser`、`SBChannel` 都是这个套路。
7. `addRef/release/isReferred` 是**机器人替身**的引用计数：`createBot()` 复用时判断 `!isReferred() && !GameUtils.hasMember(id)`。真人用户空实现即可。

### 2.4 AbstractBotUser / BotUserLogic —— 机器人替身玩家

用途：人数不够时用"机器人"凑桌。它不是真实平台账号，**不需要登录**，但有独立的 `UserIdentifier`（约定 `100 + 序号`），消息只在进程内流转 + 部分动作真的发到群里。

```java
public interface AbstractBotUser extends AbstractUser {
    // 让机器人"以玩家身份"说话：把消息派发给机器人自己的监听器（等价于玩家本人发了一条指令）
    default void sendAsBot(IMessageCompound msg, MsgType type) { DynamicListeners.dispatch(getId(), type, msg); }
    default void sendAsBot(String msg)       { DynamicListeners.dispatch(getId(), MsgType.PRIVATE, new Text(msg).asMessage()); }
    default void sendBotMessage(String msg)  { DynamicListeners.dispatch(getId(), MsgType.PUBLIC,  new Text(msg).asMessage()); } // 适配器重写以真正发群
    default void sendAtAsBot(String msg)     { /* 先发一条 @机器人 的公开消息，sleep 1s，再 dispatch AT */ }

    void onPublic(String msg);    // 由 sendPublic 触发，转给 logic
    void onPrivate(String msg);
    void setLogic(Class<? extends BotUserLogic> logicType);
    boolean hasLogic();
}
```

`BotUserLogic`（`Serializable`）是 AI 侧回调：

```java
void onPublic(String msg);
void onPrivate(String msg);
void receivedGameMessage(UserIdentifier sender, String msg, boolean isPublic);
void requestOperation(boolean isPublic, boolean isTemperal, String temperalHint);
```

- 通过 `setLogic(Class)` → `Utils.createLogic(...)` **反射查找 `(AbstractUser, Game)` 两参构造器**实例化。写新的 bot 逻辑类必须提供该构造器，否则 `RuntimeException`。
- 游戏侧调用 `abstractBotUser.requestOperation(...)` 表示"轮到你行动"；LLM 机器人（`LLMBot`）就是靠这个钩子生成发言/操作的。
- 实现要求：`getId()` 返回稳定的假 id；`getAt()` 返回 `Text("@" + 名字)`；`tryMute/tryUnmute/tryAvailable/isFriend` 空实现/返回 true；`getPermission()` 返回 `USER`；**`setGame(Game)` 里要抓住真实平台 group/channel 句柄**（因为构造时可能还没有）。

### 2.5 AbstractRoom —— 房间（群 / 频道）

```java
public interface AbstractRoom {
    AbstractUser getOwner();
    AbstractUser get(UserIdentifier id);          // 房间内查人（真人 + 已创建的机器人替身）
    void sendMessage(IMessage msg);  void sendMessage(String msg);
    Object getInstance();                          // 平台原生对象（Group / Channel），适配器内部强转用
    void setMuteAll(boolean isMute);               // 全员禁言
    String getHostNameCard();                      // 机器人自己的群名片
    UserIdentifier getId();
    AbstractBotUser createBot();                   // 取一个空闲机器人替身

    // 监听（default）
    default void registerRoomListener(Object game, RoomMessageListener ml);
    default void releaseRoomListener(Object game);
    default void registerListener(UserIdentifier id, MessageListener ml);
    default void releaseListener(UserIdentifier id);
}
```

实现要点：

1. **`createBot()` 要复用**：先扫一遍已创建的替身，挑 `!isReferred() && !GameUtils.hasMember(abu.getId())` 的；没有才用 `AtomicInteger` 递增序号新建。Mirai 里替身 id = `100 + 序号`，Kook 里是 `"robot" + (100 + 序号)`。
2. `get(id)` 只处理本平台的 `UserIdentifier` 类型（`id instanceof QQId` / `SBId`），否则返回 null；查人失败后还要在 `currentBots` 里找机器人替身。
3. **房间对象必须缓存单例**（`MiraiGroup.createInstance` / `SBChannel.createInstance` 都是 `Map` 缓存）。原因同 2.1-2：`DynamicListeners` 用 `==` 比较房间 id，且 `GameUtils.games` 用 `AbstractRoom` 作为 key。
4. **序列化**：只存 `botId / groupId / channelId` 之类的字符串，`readObject` 时重新 `Bot.getInstances()` 取回。`MiraiGroup.readObject` 直接用了 `Bot.getInstances().get(0)`（多机器人账号时会取错，接新平台时建议改成按保存的 botId 取）。
5. `setMuteAll` 是"频道/群级别"禁言，Kook 走 `KookAPI.setAllMute/setAllUnmute`。

### 2.6 事件 / 监听 / 派发

```java
public interface RoomMessageEvent {        // 事件上下文
    AbstractUser getSender();
    AbstractRoom getRoom();
}
@FunctionalInterface public interface MessageListener     { void handle(IMessageCompound msg, MsgType type); }
@FunctionalInterface public interface RoomMessageListener { void handle(AbstractUser user, IMessageCompound msg, MsgType type); }
public enum MsgType { AT, PRIVATE, PUBLIC }
```

`DynamicListeners` 是全局单例注册表：

```java
mls: ConcurrentHashMap<UserIdentifier, MessageListenerWrapper>   // 用户维度（游戏等待某人操作）
gls: ConcurrentHashMap<Object, RoomMessageListenerWrapper>       // 房间维度，key 是任意 game 对象

registerListener(uid, roomid, ml)   /  registerListener(uid, ml)
registerListener(game, roomid, ml)  // 房间监听
releaseListener(uid) / releaseListener(game)
transferListener(uid, anotherUser)  // 接管：把监听搬到另一个用户对象上

dispatch(uid, type, msg)                                  // 只给用户监听
dispatch(uid, rid, userSupplier, type, msg)               // 先房间监听，再用户监听（带房间过滤）
dispatchAsync(...)                                        // 丢到 GlobalMain.dispatchexec 线程池
InvalidListeners()                                        // 把所有用户监听标为失效
```

派发规则（**必须遵守，否则监听收不到**）：

1. 房间过滤用 **引用相等** `e.from == rid`，所以传进来的 `rid` 必须是缓存过的同一实例。
2. 用户监听只在 `ml.from == null`（私聊注册）或 `rid.equals(ml.from)`（同房间）时触发。
3. 没有注册监听时 `dispatch` 返回 `false`，消息被丢弃（不会报错）。
4. `dispatchAsync` 用于避免在平台事件线程里阻塞。

### 2.7 权限与日志

- `Permission { USER, ADMIN, SYSTEM }`。
- `GlobalMain.privmatcher`（`GlobalMatcher`）按 `(botId, roomId, userId, permission, command)` 匹配 `PermissionResult { ALLOW / UNSPECIFIED / DISALLOW }`；`isAllowed()` 含 UNSPECIFIED，`isForceAllowed()` 仅 ALLOW。
- 配置文件：数据目录下 `global.permission`（全局）与 `<id>.permission`（局部，`id` 由入口传入的 `botid` 决定），一行一条规则，`#` 起注释，`\` 转义 `$`/`#`。`-` 前缀表示禁止、`+` 前缀表示允许：

| 规则写法 | 含义 | 例 |
|---|---|---|
| `+*` / `-*` | 兜底允许 / 禁止全部 | `-*` |
| `admin` / `owner` / `admins` / `member` / `members` | 按群权限等级 | `+admin` |
| `friend` / `stranger` | 是否好友 / 陌生人 | `+friend` |
| `<userId>` | 针对某个用户 | `+123456` |
| `<规则>@<groupId>` | 只在某房间生效 | `+admin@123456` |
| `<规则>@<等级>` | 仅当**发送者**在该房间的权限等级匹配时生效，等级只能写 `admin`(=ADMIN)、`admins`(≥ADMIN)、`owner`(=SYSTEM)、`member`(=USER)、`members`(任何人) | `-*@member`（普通成员一律禁止） |
| `<命令>$<规则>` | 只对某命令生效 | `强制开始$+owner` |

- 命中逻辑：`wildcard` → 各命名 matcher → 好友规则 → 房间/等级规则 → 命令规则，逐个 `and` 合并（后者的结论覆盖前者，`UNSPECIFIED` 不影响结果）。
- `UnifiedLogger { debug/info/warning/error/severe/error(Exception) }`，入口层必须 `GlobalMain.setLogger(...)`，**且要在 `GlobalMain.init()` 之前**（init 启动的 60s 到期任务线程会用到 logger，否则 NPE）。现有实现：`MiraiGameLogger`、`SBGameLogger`。

---

## 3. 接入一个新平台：完整步骤

以下以虚构的 `MyPlatform` 为例，逐层给骨架。已有实现可直接对照 `mirai/`。

### Step 1 依赖与目录

- 平台 SDK 依赖加入 `pom.xml`；若与 mirai 一样是 `provided`，注意打包方式（现有 `mirai-console`/`mirai-core-all` 为 `provided`）。
- 新建包 `com.khjxiaogu.TableGames.platform.myplatform`，内部按 `mirai/` 的布局：`MyAdapter`、`MyUser`、`MyHumanUser`、`MyUserFriend`、`MyBotUser`、`MyRoom`、`MyRoomMessageEvent`、`MyPrivateMessageEvent`、`MyUtils`、`MyGameLogger`，`message/` 子包放 `MyMessageCompound` / `MyImage` / `MyPlatformMessage`。
- 平台无关的游戏代码**不要**引用你的包。

### Step 2 实现 `MyId implements UserIdentifier`

```java
public class MyId implements UserIdentifier {
    private static final Map<String, MyId> cache = new HashMap<>();
    private final String id;
    private MyId(String id) { this.id = id; }
    public static MyId of(String id) { return cache.computeIfAbsent(id, MyId::new); }
    public static MyId load(String s) { return s.startsWith("my:") ? of(s.substring(3)) : null; }

    @Override public String getId()   { return id; }
    @Override public String serialize(){ return "my:" + id; }   // 前缀！且不能含 '.'
    @Override public boolean isActual(){ return !id.startsWith("robot"); }
    // hashCode / equals 按 id 实现；toString 返回可读形式
}
```

### Step 3 写适配器 `MyAdapter`（双向消息转换）

```java
public class MyAdapter {
    public static final MyAdapter INSTANCE = new MyAdapter();

    // 平台 → 中台
    public IMessage toUnified(native.Message m, native.Bot b) {
        if (m 是复合消息) return new MyMessageCompound(m, b);
        if (m 是纯文本)   return new Text(m.getText());
        if (m 是 at)      return new At(MyId.of(m.getTargetId()));
        if (m 是图片)     return new MyImage(m, b);
        return new MyPlatformMessage(m);          // 透传兜底
    }
    // 中台 → 平台
    public native.Message toPlatform(IMessage msg, native.Contact c) {
        if (msg instanceof MessageCompound) { 逐片递归拼接 }
        if (msg instanceof Text)   return new native.PlainText(((Text) msg).getText());
        if (msg instanceof At)     return new native.At(((MyId) ((At) msg).getId()).getId());
        if (msg instanceof Image)  return c.uploadImage(((Image) msg).getData());
        if (msg instanceof MyPlatformMessage) return ((MyPlatformMessage) msg).getMsg();
        return new native.PlainText("");
    }
}
```

要点：`Image.getData()` 是懒加载（`MiraiImage` 现场下载、`SBImage` 读 `Resource.openStream()`），失败要能容忍；`MyMessageCompound.first(Class)` 无匹配**返回 null**。

### Step 4 实现真人用户（群成员 + 私聊用户两种）

```java
public abstract class MyUser implements AbstractUser, Serializable {
    transient protected native.Contact contact;     // 平台原生句柄，transient
    transient protected Object roleObject;

    @Override public void bind(Object o) { roleObject = o; }
    @Override public Object getRoleObject() { return roleObject; }
    @Override public UserIdentifier getHostId() { return MyId.of(contact.getBot().getId()); }
    @Override public AbstractRoom getRoom() { return MyRoom.createInstance(contact.getGroup()); }

    @Override public void sendPublic(String s)   { contact.getGroup().sendMessage(getAt() + s); }
    @Override public void sendPublic(IMessage m) { contact.getGroup().sendMessage(toPlatform(getAt().asMessage().append(m))); }
    @Override public void sendForName(String s)  { contact.getGroup().sendMessage(getMemberString() + " " + s); }
    @Override public void tryAvailable()         { if (!isFriend()) sendPublic("为了游戏顺利进行，请加我。"); }
    // sendPrivate / getAt / getMemberString / setNameCard / getNameCard / tryMute / tryUnmute 由子类或此层实现
}
```

- 群成员用户：`getId()` 返回 `MyId.of(member.getId())`，`getPermission()` 做 主人/管理员/成员 → SYSTEM/ADMIN/USER 映射。
- 私聊用户（`MyUserFriend implements AbstractUser`）：`getRoom()` 返回 `null`，**重写 registerListener/releaseListener/transferListener/sendPublic/sendForName/setNameCard/getAt 等为安全空实现**（可参考 `MiraiUserFriend`）。
- 两者都要实现 `writeObject`（写 bot/group/member id）和 `readObject`（用 id 取回句柄）。

### Step 5 实现 `MyBotUser implements AbstractBotUser`

```java
public class MyBotUser extends MyUser implements AbstractBotUser {
    protected int rbid;
    protected String nameCard = "机器人" + (100 + rbid);
    transient Game sg;
    private BotUserLogic logic;

    @Override public MyId getId() { return MyId.of("robot" + (100 + rbid)); }   // 与 isActual() 呼应
    @Override public IMessage getAt() { return new Text("@" + nameCard); }
    @Override public void setGame(Game g) { sg = g; contact = g.getGroup().getInstance(); }
    @Override public void setLogic(Class<? extends BotUserLogic> t) { logic = Utils.createLogic(t, this, sg); }
    @Override public boolean hasLogic() { return logic != null; }
    @Override public void onPublic(String m)  { if (logic != null) logic.onPublic(m); }
    @Override public void onPrivate(String m) { if (logic != null) logic.onPrivate(m); }
    @Override public void receiveMessage(UserIdentifier id, String m, boolean pub) { logic.receivedGameMessage(id, m, pub); }
    @Override public void requestOperation(boolean pub, boolean temp, String hint) { logic.requestOperation(pub, temp, hint); }
    @Override public void sendBotMessage(String m) {                 // 默认实现不会真的发群，需要重写
        DynamicListeners.dispatch(getId(), MsgType.PUBLIC, new Text(m).asMessage());
        contact.getGroup().sendMessage(nameCard + "：\n" + m);
    }
    @Override public Permission getPermission() { return Permission.USER; }
    @Override public boolean isFriend() { return true; }
    // tryMute/tryUnmute/tryAvailable 空实现；addRef/release/isReferred 用 int refcount
}
```

### Step 6 实现 `MyRoom implements AbstractRoom`

```java
public class MyRoom implements AbstractRoom, Serializable {
    private static final Map<String, MyRoom> cache = new HashMap<>();
    private final AtomicInteger botId = new AtomicInteger(0);
    transient private native.Group group;
    private String bid, gid;                  // 序列化用：机器人 id / 群 id
    private final List<AbstractBotUser> currentBots = new ArrayList<>();

    public static MyRoom createInstance(native.Group g) {
        return cache.computeIfAbsent(g.getId(), k -> new MyRoom(g));     // 必须缓存！
    }
    @Override public AbstractUser getOwner() { return new MyHumanUser(group.getOwner()); }
    @Override public AbstractUser get(UserIdentifier id) {
        if (id instanceof MyId) {
            native.Member m = group.getMember(((MyId) id).getId());
            if (m != null) return new MyHumanUser(m);
            for (AbstractBotUser b : currentBots) if (b.getId().equals(id)) return b;
        }
        return null;
    }
    @Override public void sendMessage(IMessage m) { group.sendMessage(MyAdapter.INSTANCE.toPlatform(m, group)); }
    @Override public void sendMessage(String s)   { group.sendMessage(s); }
    @Override public Object getInstance() { return group; }
    @Override public void setMuteAll(boolean b)   { group.setMuteAll(b); }
    @Override public String getHostNameCard()     { return group.getBotNick(); }
    @Override public MyId getId()                 { return MyId.of(group.getId()); }
    @Override public AbstractBotUser createBot() {
        synchronized (currentBots) {
            for (AbstractBotUser b : currentBots)
                if (!b.isReferred() && !GameUtils.hasMember(b.getId())) return b;
            AbstractBotUser b = new MyBotUser(botId.incrementAndGet(), this);
            currentBots.add(b);
            return b;
        }
    }
    // writeObject/readObject：只存三个 id，读回时重新取句柄
}
```

### Step 7 实现事件对象

```java
public class MyRoomMessageEvent implements RoomMessageEvent {
    native.GroupMessageEvent ev; MyHumanUser sender;
    @Override public AbstractUser getSender() { return sender != null ? sender : (sender = new MyHumanUser(ev.getMember())); }
    @Override public AbstractRoom getRoom()   { return MyRoom.createInstance(ev.getGroup()); }
}
public class MyPrivateMessageEvent implements RoomMessageEvent {
    native.PrivateMessageEvent ev;
    @Override public AbstractUser getSender() { return new MyUserFriend(ev.getSender()); }
    @Override public AbstractRoom getRoom()   { return null; }
}
```

### Step 8 写入口（`main` 或插件 `onEnable`）

**初始化顺序很重要**：

```java
GlobalMain.setLogger(new MyGameLogger(logger));   // 1. 必须先设日志
GlobalMain.init(dataFolder);                      // 2. 启动中台：DB、积分、绑定、Markov、到期任务线程
GlobalMain.privmatcher.load(dataFolder);          // 3. 权限
GlobalMain.privmatcher.rebuildConfig();           //    可选：把内置规则落盘
UserIdentifierSerializer.addRawSerializer(s -> MyId.load(s));  // 4. 若没改 init()，在这里补注册

// 5. 资源文件（可选，与平台无关，但游戏要用）
new File(dataFolder, "undtext.txt"); new File(dataFolder, "cyyy.csv");

// 6. 注册事件
onGroupMessage(ev -> {
    String command = MyUtils.getPlainText(ev.getMessage());     // 只取纯文本并 trim
    MyId sid = MyId.of(ev.getSenderId());
    MyId rid = MyId.of(ev.getGroupId());

    // 6.1 马氏回声（闲聊）：返回非 null 则已回复，但仍然继续往下走
    String reply = MarkovHelper.handleMarkov(command, rid, sid);
    if (reply != null) ev.getGroup().sendMessage(reply);

    // 6.2 判定"是否在跟我说话"
    boolean hasCmd = false;
    if (ev 里 at 了机器人本身) hasCmd = true;
    else if (command.startsWith("##")) { hasCmd = true; command = Utils.removeLeadings("##", command); }

    // 6.3 交给中台
    GlobalMain.firePublicCommand(
        hasCmd ? command : null,                 // 无命令时传 null → 走 PUBLIC 派发
        sid,
        () -> new MyHumanUser(ev.getMember()),   // Supplier：避免无谓构造
        () -> new MyRoomMessageEvent(ev),
        rid,
        MyUtils.getPermission(ev.getMember()),
        rid,                                     // botId（两套现有实现都传 rid）
        (IMessageCompound) MyAdapter.INSTANCE.toUnified(ev.getMessage(), ev.getBot()));
});

onPrivateMessage(ev -> GlobalMain.defaultFirePrivate(
    MyUtils.getPlainText(ev.getMessage()),
    MyId.of(ev.getSenderId()),
    (IMessageCompound) MyAdapter.INSTANCE.toUnified(ev.getMessage(), ev.getBot()),
    () -> new MyPrivateMessageEvent(ev)));

if (acceptNewFriend) ev.accept();     // 现有 Mirai 实现自动通过好友申请
```

### Step 9 平台能力落地（可选但建议）

| 能力 | 实现位置 | 参考 |
|---|---|---|
| 禁言/解禁 | `AbstractUser.tryMute/tryUnmute` | `MiraiHumanUser` 用 `member.mute(3600)`；Kook 用 `KookAPI.setMute/setUnmute`（频道角色实现） |
| 后端禁言（隐藏消息） | `tryMuteBackend/tryUnmuteBackend` | `SBHumanUser` 用 `KookAPI.setInvisible/setVisible` |
| 改名 | `setNameCard` | Mirai `member.setNameCard`；Kook `KookAPI.setNick` |
| 全员禁言 | `AbstractRoom.setMuteAll` | `MiraiGroup`、`SBChannel` |
| 图片 | `Image` + 适配器上传 | `MiraiAdapter`（`uploadImage`）、`KooKAdapter`（`Resource.of`） |
| 权限 | `getPermission()` | `MiraiUtils.getPermission`、`SBUtils.getPermission` |
| 自动加好友 | 入口事件 | `MiraiMain.onNewFriend → ev.accept()` |

### Step 10 序列化自检

中台对象会被 `Game` 一起 `ObjectOutputStream` 落盘（`继续游戏` 指令会反序列化）。自检清单：

1. 所有平台句柄字段 `transient`；
2. `writeObject` 先写足 id 字段，再 `defaultWriteObject()`；
3. `readObject` 先 `defaultReadObject()`，再用 id 取回句柄；
4. 用户 id 必须与序列化时一致（`serialize()` 格式不可变）；
5. 反序列化失败要能给出可诊断的异常（现有实现有 `e.printStackTrace()`）。

---

## 4. 消息派发与命令规则（细节）

### 4.1 公开消息：`GlobalMain.firePublicCommand`

```java
firePublicCommand(command, uid, userSupplier, eventSupplier, rid, perm, botid, msg)
```

| `command` | 行为 |
|---|---|
| `null`（普通聊天 / 没有 @ 机器人 / 没有 `##`） | `dispatchAsync(uid, rid, user, MsgType.PUBLIC, msg)`：异步，派发类型为 `PUBLIC`，房间监听与用户监听都会收到 |
| 非 `null`（@了机器人 **或** 以 `##` 开头） | 1) 先 `dispatch(uid, rid, user, MsgType.AT, msg)`（**注意类型固定为 AT**，即使玩家是用 `##` 打的字）<br>2) 再按空格切分命令，`args[0]` 查 `normcmd` 与 `privcmd`<br>3) `normcmd` 命中且 `privmatcher.match` 允许 → 执行<br>4) 否则 `privcmd` 命中且（admin 规则 **force allowed** 或命令自身 force allowed）→ 执行<br>5) 都没命中 → 静默丢弃 |

- **`##` 前缀会被 `Utils.removeLeadings("##", command)` 处理**：不仅去掉开头，还会去掉文本中所有 `##`，然后 trim。
- 游戏监听里判断"玩家结束发言"通常写 `if (type == MsgType.AT)`，这在 `##结束` 时同样成立——因为中台把有命令的消息统一按 `AT` 派发（见 `Villager.onDeclareTurn`）。
- **房间级监听两个分支都会收到**（两处都调用了带 `rid` 的重载）：
  - 普通聊天 → `MsgType.PUBLIC`；
  - `@机器人` / `##命令` → `MsgType.AT`（且命令还会在同一线程里先派发监听、再执行命令 lambda）。
  所以"监听全群发言"用 `registerRoomListener(this, (u, msg, type) -> { if (type == MsgType.PUBLIC) ... })` 即可（参考 `IdiomSolitare.gameMain`）。

### 4.2 私聊消息：`GlobalMain.defaultFirePrivate`

```java
defaultFirePrivate(command, senderId, msg, eventSupplier):
    if (command.startsWith("##")) firePrivateCommand(剩余部分, ev);   // 查 pvmgcmd
    dispatchAsync(senderId, MsgType.PRIVATE, msg);                    // 再派发给该用户监听
```

- `pvmgcmd`（私聊设置，例如 `##狼人杀 参数 值`）**没有权限校验**，是历史设计；需要安全性的新命令请放 `privcmd`。
- 私聊监听不绑定房间（`ml.from == null`），因为 `MiraiUserFriend.getRoom()` 为 null，注册时走 `registerListener(uid, ml)`。

### 4.3 命令注册 API（中台提供）

```java
GlobalMain.addCmd("报名", "报名参加当前游戏", (event, args) -> { ... });     // 普通命令 → normcmd
GlobalMain.addPCmd("强制开始", "强制开始当前游戏", (event, args) -> { ... }); // 管理命令 → privcmd
GlobalMain.normcmd.put(...) / privcmd.put(...) / pvmgcmd.put(...)            // 直接放表
GlobalMain.makeGame("狼人杀", WerewolfPreserve.class, new DefaultGameCreater<>(WerewolfGame.class));
    // makeGame 会一次性注册一整套命令：预定/取消预定/预定列表/统计/分析/查询参数/
    // 设置参数/清除参数/立即开始/强制开始/清空预定/提醒/强制预定/强制取消预定/机器人预定/
    // 全局统计/全局分析/开始/定制 + 私聊设置入口
```

`args` 是 `command.split(" ")` 的结果，`args[0]` 是命令本身，参数从 `args[1]` 开始；**空格即分隔，没有引号转义**。

---

## 5. 游戏层使用中台的典型模式

（这一节给写游戏的人，也是接入后自测的用例）

### 5.1 发消息 / 取人

```java
AbstractRoom room = getGroup();
room.sendMessage("游戏开始，请发送“##报名”");
AbstractUser u = room.get(userId);          // 可能 null，务必判空
if (u != null) u.sendPrivate("你是狼人");
room.sendMessage(u.getAt().asMessage().append("请开始发言"));
```

### 5.2 等玩家操作 + 超时（最核心模式）

```java
// 注册一次性监听
room.registerListener(player.getId(), (msg, type) -> {
    if (type == MsgType.AT) {                        // 群里 @ 机器人 / ##xxx
        String text = Utils.getPlainText(msg);
        At at = msg.first(At.class);
        if (text.startsWith("投票") && at != null) {
            room.releaseListener(player.getId());    // 收到即注销，避免重复处理
            game.vote(player, at.getTarget());
            waitThread.stopWait();                   // 提前结束等待
        }
    } else if (type == MsgType.PRIVATE) {            // 私聊投票
        ...
    }
});
waitThread.startWait(120000);                        // 超时兜底
room.releaseListener(player.getId());                // 无论超时/成功都要注销
```

要点：
- **注册后一定要 `releaseListener`**，否则监听表泄漏，玩家下一条消息还会进旧回调。
- `AbstractUser.registerListener` 会把当前房间 id 绑进 wrapper，换房间自动失效（这是设计上的保护）。
- "请求该玩家行动"可先 `player.requestOperation(false)`（真人默认空实现，机器人替身会转给 `BotUserLogic.requestOperation`）。
- ⚠️ 上面 `msg.first(At.class)` 的写法在无 @ 时可能抛 `ClassCastException`（见 2.2 坑 1）。稳妥写法是自行遍历：`for (IMessage m : msg) if (m instanceof At) { ... }`；或者像狼人杀那样改用"游戏号码"文本制，不依赖 At。

### 5.3 机器人替身

```java
AbstractBotUser bot = room.createBot();      // 复用或新建
bot.addRef();
bot.setLogic(Role.getRole(p).getBotClass());  // 反射构造 (AbstractUser, Game)
bot.sendAsBot("投票" + target.getId());       // 以机器人身份走一遍玩家指令流
bot.release();
```

### 5.4 序列化注意

- 游戏对象里可以持有 `AbstractUser` / `AbstractRoom`（它们都实现了 `Serializable`），但**不要**持有 `IMessage`（平台原生包装不保证可序列化）。
- 用户查表一律用 `UserIdentifier`，不要用 `AbstractUser` 当 key（`AbstractUser` 的 `equals` 各实现不一致；用 `UserFunction` 或 id 更稳）。
- `GameUtils.ingame` 是"一个玩家同时只能在一局游戏里"的全局约束，机器人替身 id 也参与其中。

---

## 6. 两套现有实现对照

| 维度 | Mirai（QQ） | SimpleRobot（Kook） |
|---|---|---|
| 入口 | `MiraiMain extends JavaPlugin`（`META-INF/services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin`，`pom.xml` 里 `mirai.plugin.main`） | `KookMain.main()`（**default 包**，不能从其他包直接引用）；`-Dkooktoken=... -Dkookclient=...` |
| 数据目录 | `plugin.getDataFolder()` | `./data/com.khjxiaogu.mirai.TableGames/` |
| 房间 | `MiraiGroup`（`Group`） | `SBChannel`（`Channel` + guildId） |
| 群成员 | `MiraiHumanUser`（`NormalMember`） | `SBHumanUser`（`GuildMember`） |
| 私聊 | `MiraiUserFriend`（`User`） | `SBUserFriend`（`Contact`） |
| 机器人替身 id | `QQId.of(100+n)` | `SBId.of("robot" + (100+n))` |
| 消息复合体 | `MiraiMessageCompound`（包装 `MessageChain`） | `SBMessageCompound`（包装 `Messages`） |
| 图片 | `MiraiImage`（按 URL 现场下载） | `SBImage`（`Resource.openStream()`） |
| 发送节流 | `SlowUtils.runSlowly`：**全局锁 + 0~500ms 随机延迟**，失败无限重试（公开失败退私聊） | 直接 `sendBlocking`/`sendAsync`，无重试、异常吞掉 |
| 禁言 | `member.mute(3600)` | `KookAPI.setMute`（频道角色 4096\|16384） |
| 后端禁言 | 未实现 | `KookAPI.setInvisible/setVisible`（硬编码频道 `2452937317768836`） |
| 权限 | `MemberPermission` | `member.isOwner()/isAdmin()` |
| 自动加好友 | `onNewFriend → accept()` | 不适用 |
| 额外能力 | — | `KookAPI`：`setNick`、`createUserRole/deleteUserRole`、`setAllMute` |

**已知差异 / 待改进点（接新平台时不要照抄）**

1. `MiraiMessageCompound.first()` / `SBMessageCompound.first()` 无匹配时返回 `PlatformMessage(null)`，强转会 CCE（见 2.2 坑 1）。
2. `SBHumanUser.sendPrivate(IMessage)` 实际把消息发到了**频道**而不是私聊（`sendMessage(group, msg, ...)`），疑似缺陷；`sendPrivate(String)` 才是真私聊。
3. `SBUserFriend.getAt()` 返回 `null`，私聊场景里对 `getAt()` 的调用要判空。
4. `SBUserFriend.getHostId()` 返回固定 `SBId.of("private")`，权限匹配时无法区分机器人。
5. `MiraiGroup.readObject` 用 `Bot.getInstances().get(0)`，多账号会取错机器人。
6. `MiraiBotUser.sendPublic` 会同时 `onPublic` 和真正发群；`SBBotUser.sendPublic` 用 `executeLater(...,500)` 延迟触发，时序上不可依赖"同步生效"。
7. `KookMain` 在 default 包；如果要做成库被引用，需要包声明。
8. `Platform` 接口（`platform/Platform.java`）是空的，且无任何引用；`game/spwarframe/Platform` 是同名不同物。

---

## 7. 接入验收清单（Checklist）

- [ ] `MyId.serialize()` 有唯一前缀，`equals/hashCode` 正确，实例有缓存（`==` 可用）。
- [ ] `UserIdentifierSerializer` 能 `read(serialize())` 往返，且不会误吃纯数字 QQ id。
- [ ] `MyRoom.createInstance()` 缓存单例；`MyRoom.getId()` 稳定。
- [ ] `getRoom()` 为 null 的用户实现重写了 `registerListener/releaseListener/transferListener`。
- [ ] `sendPrivate/sendPublic/sendForName` 各有 String 与 IMessage 重载，且异常不抛出。
- [ ] `getAt()` 返回的 `At` 能把 `MyId` 转成平台 at（或机器人替身返回 `Text("@"+名字)`）。
- [ ] `getPermission()` 正确映射群主/管理员/成员。
- [ ] `createBot()` 会复用空闲替身；替身 `getId()` 稳定且 `isActual()` 为 false。
- [ ] 机器人替身 `setLogic` 能用 `(AbstractUser, Game)` 构造器实例化；`sendBotMessage` 真的发到房间。
- [ ] `MyMessageCompound.first()` 无匹配返回 `null`；`getText()` 与入口处 `getPlainText` 一致。
- [ ] 入口：`setLogger` → `init` → `privmatcher.load` → 序列化器注册 → 事件注册，顺序正确。
- [ ] 群消息：`@机器人` 与 `##命令` 都能触发；命令参数按空格切分正确；纯聊天走 PUBLIC。
- [ ] 私聊：`##xxx` 走 `pvmgcmd`；普通私聊能到达用户监听（`MsgType.PRIVATE`）。
- [ ] `Game` 序列化 → 反序列化后，房间和用户句柄能恢复，`继续游戏` 可用。
- [ ] 权限文件 `<id>.permission` 能被加载（`MyId.serialize()` 不含 `.`；`id` 由入口传入的 `botid` 决定）。
- [ ] 图像、禁言、改名在目标平台可用（或明确记录为不支持）。

---

## 8. 附录

### 8.1 关键类索引

| 关注点 | 文件 |
|---|---|
| 用户契约 | `src/main/java/com/khjxiaogu/TableGames/platform/AbstractUser.java` |
| 机器人替身契约 | `.../platform/AbstractBotUser.java`、`BotUserLogic.java`、`BotUser.java` |
| 房间契约 | `.../platform/AbstractRoom.java` |
| 身份与序列化 | `.../platform/UserIdentifier.java`、`UserIdentifierSerializer.java`、`QQId.java`、`SBId.java` |
| 监听与派发 | `.../platform/DynamicListeners.java`、`MessageListener.java`、`RoomMessageListener.java`、`MsgType.java` |
| 命令/派发中枢 | `.../platform/GlobalMain.java`（`init` 112 行、`defaultFirePrivate` 157 行、`firePublicCommand` 171 行、`addCmd` 195 行、`makeGame` 205 行） |
| 消息模型 | `.../platform/message/*.java` |
| QQ 适配 | `.../platform/mirai/*.java`（入口 `MiraiMain.java`） |
| Kook 适配 | `.../platform/simplerobot/*.java`（入口 `KookMain.java`，HTTP 扩展 `KookAPI.java`） |
| 权限 | `src/main/java/com/khjxiaogu/TableGames/permission/*.java` |

### 8.2 一条群消息的完整调用链

```
平台事件 (GroupMessageEvent / ChannelMessageEvent)
  └─ MyMain 事件回调
       ├─ MyUtils.getPlainText(msg)            → 纯文本
       ├─ MarkovHelper.handleMarkov(...)        → 闲聊回复（可选）
       ├─ 判定 hasCmd（@机器人 或 "##"）
       └─ GlobalMain.firePublicCommand(...)
            ├─ command == null → DynamicListeners.dispatchAsync(uid, rid, user, PUBLIC, msg)
            └─ command != null
                 ├─ DynamicListeners.dispatch(uid, rid, user, AT, msg)
                 │    ├─ 遍历 gls：e.from == rid → RoomMessageListener
                 │    └─ mls.get(uid) → 房间匹配 → MessageListener
                 └─ normcmd / privcmd 查表 + privmatcher 鉴权 → 执行命令 lambda
                      └─ 命令里 sendPublic/sendPrivate → MyUser → MyAdapter → 平台发送
```

### 8.3 常见错误速查

| 现象 | 原因 |
|---|---|
| 监听永远收不到 | 监听已被别处 `releaseListener` 释放；或注册时用的 `rid` 不是同一实例（房间 id 未缓存）；或 `type` 判断错了（普通聊天是 `PUBLIC`） |
| `ClassCastException: MyPlatformMessage` | `msg.first(At.class)` 无匹配（见 2.2 坑 1） |
| `NullPointerException` 在 `registerListener` | 私聊用户 `getRoom()` 为 null，且没重写 default 方法 |
| 重启后积分/统计丢失 | `serialize()` 格式变了，或新平台的 `addRawSerializer` 没注册 |
| 命令不响应 | 命令没进 `normcmd`；或进了 `privcmd` 但没有权限匹配 `ALLOW`（`UNSPECIFIED` 不算 force allowed） |
| 一个玩家同时在两局游戏 | `GameUtils.ingame` 未清理（`RemoveMember`），或机器人替身被重复创建 |
| `ExceptionInInitializerError` / 启动即 NPE | `GlobalMain.setLogger` 在 `init` 之后才调用 |

---

*文档基于当前仓库代码整理；接口细节以源码为准。*
