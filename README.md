# MiraiTableGamePlugin
使用Mirai机器人主持桌游。  
仅供个人学习游玩使用，一切问题概不负责。  
开源自行构建免费，获取构建包收费100一次，获取详细使用说明收费150一次，定制功能50一个，如需购买请联系[khjxiaogu@qq.com](mailto:khjxiaogu@qq.com)。
由于是个人使用，因此部分规则可能和标准的不一样，如有规则类问题，肯定是故意的。
# 使用说明
一个群同一时间只能创建一个游戏。  
同一个人如果在多个群同时参加游戏，可能会导致两个游戏同时竞争一个人从而导致其中一个游戏无法收到私聊指令。  
处于安全限制和游戏体验的原因，仅当机器人是管理员的时候可以开始游戏。  
## 预定系统
预定系统可以帮助玩家自行组织游戏的进行，而很方便的进行游戏。
指令是`At机器人 预定xxx(游戏名)`，当人数足够开始时，会自动进入时间倒数状态。当人数满足需求或者满人时，游戏会尽快开始。  
如果中途想退出游戏，可以`At机器人 取消预定xxx`退出。  
管理员可以通过`At机器人 立即开始xxx`加速游戏开始倒计时，如果人数不足，将会立即开始游戏。  
预定系统在时间结束之后自动使用报名系统创建游戏并自动给所有预定的玩家报名。  
## 报名系统
管理员可以通过`At机器人 开始xxx [人数]`来创建一个固定人数的游戏，玩家可以通过`At机器人 报名`的方式加入，满人立即开始游戏。  
与此同时，管理员也可以通过`At机器人 强制开始`无视当前人数立即开始游戏 __(有游戏因为随机性无法正常游玩的可能性)__ ，也可以通过`At机器人 停止游戏`在游戏过程中或者报名时销毁游戏。  
~~管理员可以通过`At机器人 强制报名 [qq号]`来给某个群员报名~~
## 游戏系统
~~管理员可以通过`At机器人 执行 [qq号] [消息类型] [消息]`来模拟玩家执行操作，在测试或者处理挂机玩家的情况下可以使用。~~
消息类型是可以是AT,PRIVATE,PUBLIC其中之一，对应此人发的`At机器人 信息`，私聊机器人`信息`，和在群里发送`信息`。
### 狼人杀
[游戏规则](https://khjxiaogu.github.io/MiraiTableGamePlugin/%E7%8B%BC%E4%BA%BA%E6%9D%80%E6%B8%B8%E6%88%8F%E8%A7%84%E5%88%99.htm)
### 谁是卧底
游戏流程：
1. 每个玩家收到一个词条  
2. 每个玩家描述自己的词条  
3. 玩家投票决出与自己不同的人，如果无法表决或者平票跳到下一轮  
4. 进行胜负判定，如果玩家数量小于初始卧底数量+2，卧底胜利。卧底全部死亡则玩家胜利。
5. 每个玩家再次描述自己的词条  
6. 再次投票表决  
7. 胜负判定  
8. 回到1  
其中，8人以上的游戏会有两个卧底，否则只有一个。
可以通过配置插件数据目录下的undtext.txt修改词条，一行一对词条，用逗号隔开两个词。  
### 成语接龙
游戏开始后所有玩家都可以进行接龙，只要读音或者文字相同即可接上。一分钟内无人接上或者游戏时间结束则停止游戏。
## 统计数据系统
每个游戏都有一个统计数据系统，可以通过`At机器人 xxx统计`查看，采用sqlite数据库存储。  
