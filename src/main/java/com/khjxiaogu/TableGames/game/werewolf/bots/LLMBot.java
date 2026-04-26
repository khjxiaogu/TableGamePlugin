package com.khjxiaogu.TableGames.game.werewolf.bots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.JsonObject;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.platform.AbstractBotUser;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.aiwuxia.llm.AIOutput;
import com.khjxiaogu.aiwuxia.llm.AIRequest;
import com.khjxiaogu.aiwuxia.llm.LLMConnector;
import com.khjxiaogu.aiwuxia.llm.ModelRouteException;
import com.khjxiaogu.aiwuxia.llm.AIRequest.Builder;
import com.khjxiaogu.aiwuxia.llm.AIRequest.ReasoningStrength;
import com.khjxiaogu.aiwuxia.llm.AIRequest.TaskType;
import com.khjxiaogu.aiwuxia.state.Role;
import com.khjxiaogu.aiwuxia.state.history.HistoryItem;
import com.khjxiaogu.aiwuxia.state.history.MemoryHistory;
import com.khjxiaogu.aiwuxia.utils.JsonBuilder;
import com.khjxiaogu.aiwuxia.utils.JsonBuilder.JsonArrayBuilder;
import com.khjxiaogu.aiwuxia.utils.JsonBuilder.JsonObjectBuilder;

public class LLMBot extends GenericBot {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6612099915871427062L;
	StringBuilder messageBacklog=new StringBuilder();
	transient Object backLogLock=new Object();
	MemoryHistory history=new MemoryHistory();
	static String system="你是一个参与狼人杀游戏的智能玩家，正在进行一场正式游戏。你需要以专业的素养根据当前局势、你的身份、技能、阵营以及场上其他玩家的行为，做出合理的发言、投票和技能使用决策。你的目标是帮助你的阵营获得胜利。\n"
			+ "\n"
			+ "**游戏基本规则**（请理解并遵守）：\n"
			+ "- **阵营**：好人阵营（神职+平民） vs 狼人阵营。\n"
			+ "- **流程**：夜晚 → 白天 → 投票 → 重复，直至一方胜利。\n"
			+ "- **夜晚顺序**：\n"
			+ "  1. 狼人回合（选择击杀目标）\n"
			+ "  2. 技能回合（预言家、女巫、守卫、乌鸦、石像鬼、猎魔人、守墓人等）\n"
			+ "  3. 死亡技能回合（猎人、狼王开枪）\n"
			+ "- **白天**：所有存活玩家发言，然后投票驱逐一名玩家。被驱逐者可能有遗言。\n"
			+ "- **胜利条件**：\n"
			+ "  - 好人胜利：所有狼人死亡。\n"
			+ "  - 狼人胜利：场上存活的好人人数 ≤ 狼人人数（狼人可以绑票获胜），或所有好人死亡。\n"
			+ "  - 平局：所有玩家同时死亡。\n"
			+ "- **特殊规则**（部分）：\n"
			+ "  - 长老被狼人杀需两次，但被毒、枪、驱逐则一次死。\n"
			+ "  - 老流氓死亡必有遗言，不受魅惑。\n"
			+ "  - 狼王/猎人死亡时可开枪带走一人（除非被毒死）。\n"
			+ "  - 女巫第一晚可自救，同救同守（守卫守+女巫救）会死。\n"
			+ "  - 守卫不能连续两晚守同一人。\n"
			+ "  - 骑士可在白天挑战一人：若挑战狼人则狼死，若挑战好人则骑士死。\n"
			+ "  - 白狼王自爆可带走一人。\n"
			+ "  - 狼人可在白天自爆（跳过白天进入黑夜）。\n"
			+ "  - 警长投票计1.5票，可决定发言顺序，死亡时可移交警徽。\n"
			+ "\n"
			+ "**职业描述**"
			+ "狼人：夜晚投票杀人，可自爆跳过白天。\n"
			+ "白狼王：自爆时可杀死一名玩家。\n"
			+ "狼王：死亡时可开枪杀死一人（被毒或自爆除外）。\n"
			+ "石像鬼：可查验玩家具体身份，与狼人不互知，其他狼人死后获得杀人权。\n"
			+ "隐狼：知晓狼人身份，但被预言家查验为平民。不被其他狼人知晓，其他狼人死后获得杀人权。\n"
			+ "恶魔：可查验玩家阵营，不能自爆。\n"
			+ "巨狼：免疫所有夜间伤害。\n"
			+ "狼美人：魅惑一名玩家，死后与该玩家同归于尽。\n"
			+ "平民：白天发言投票。\n"
			+ "长老：是民阵营，需被狼杀两次才死。\n"
			+ "老流氓：死亡必有遗言，免疫魅惑。\n"
			+ "预言家：每晚查验一名玩家的具体职业。\n"
			+ "女巫：拥有解药和毒药，每晚限用一瓶。\n"
			+ "守卫：每晚守护一人，不能连续守同一人。\n"
			+ "猎人：死后可开枪带走一人（被毒除外）。\n"
			+ "白痴：被投票出局后可继续发言但不能投票。\n"
			+ "骑士：白天可决斗一人，若对方是狼则狼死，否则骑士死。\n"
			+ "乌鸦：每晚诅咒一人，使其次日被多投一票，该票信息公屏可见。\n"
			+ "猎魔人：夜晚狩猎狼人成功则狼死，否则自己死。\n"
			+ "守墓人：每晚得知前一晚被放逐者是否为好人。\n"
			+ "熊：若相邻玩家有狼人，天亮时会咆哮。若其死亡则固定提示没有咆哮。\n"
			+ "纵火者：可纵火翻牌一人，若其被狼杀则反杀前一狼。\n"
			+ "验尸官：白天得知前一晚死者的阵营和死因。\n"
			+ "奇迹弓手：可射杀狼人或保护好人并反杀前一狼。\n"
			+ "狐狸：查验三人中是否有狼，若无则失去技能。\n"
			+ "禁言长老：可禁言一人，使其白天不能发言。\n"
			+ "**你的身份和技能**：每次游戏开始时，你会收到具体身份和技能说明。请根据当前局势和你的身份做出最有利于你阵营的行动。\n"
			+ "**输出要求**：\n"
			+ "- 如果是发言，用自然语言表达你的观点。\n"
			+ "- 如果是投票或使用技能，请严格按照指定格式输出指令（如“投票 张三”、“查验 李四”等）。具体格式会在每次询问时给出。\n"
			+ "- 不需要输出任何额外内容。"
			+ "- 不需要思考过多，限制思维在500字以内，尽可能靠直觉。"
			+ "- 如果你是狼人，除非确实必要，否则不要自爆，不要因为猜疑可能暴露就自爆，需要有确凿的胜利可能才自爆，因为自爆会让所有人知道你的狼人身份。"
			+ "- 如果公屏提示xx玩家燃起来了，意味着场上必然存在纵火者且纵火者玩家选择对被烧起来的玩家使用技能。"
			+ "- 如果公屏提示熊咆哮了/没有咆哮，意味着游戏必然存在熊，但如果熊死了依然会显示没有咆哮。";
	static ExecutorService exc=Executors.newFixedThreadPool(1);
	transient boolean hasTriggered=false;
	transient boolean isPublic;
	transient Object triggerLock=new Object();
	public LLMBot(AbstractBotUser p, WerewolfGame gam) {
		super(p, gam);
	}

	@Override
	public void onPublic(String msg) {
		synchronized(backLogLock) {
			//super.onPublic(msg);
			receivedGameMessage(null,msg,true);
			
		}

	}@Override
	public void requestOperation(boolean isPublic,boolean isTemperal,String tempralHint) {
		this.isPublic=isPublic;
		requestAIsend(isTemperal,tempralHint);
	}
	public void requestAIsend(boolean isTemperal,String hint) {
		if(hasTriggered)return;
		synchronized(triggerLock) {
			if(hasTriggered)return;
			hasTriggered=true;
		}
		exc.submit(()->{
			synchronized(triggerLock) {
				hasTriggered=false;
			}
			String temperal=null;
			if(!isTemperal)
				synchronized(backLogLock) {
					history.add(Role.USER, messageBacklog.toString(), true);
					System.out.println(messageBacklog.toString());
					messageBacklog=new StringBuilder();
				}
			else
				synchronized(backLogLock) {
					temperal=messageBacklog.toString()+"\n"+hint;
					System.out.println(messageBacklog.toString());
				}
			Builder b=AIRequest.builder("werewolf").taskType(TaskType.STORY).strength(ReasoningStrength.WEAK).temperature(1.3f).maxTokens(8192);
			b.addHistoryItem(Role.SYSTEM, system+"你的身份是："+this.getPlayer().getMemberString());
			Iterator<HistoryItem> it=history.validContextIterator();
			while(it.hasNext()) {
				HistoryItem hi=it.next();
				b.addHistoryItem(hi);
			}
			if(isTemperal)
				b.addHistoryItem(Role.USER, temperal);
			try {
				System.out.println("Triggered AI:");
				AIOutput op = LLMConnector.call(b.build());
				System.out.println("Reasoner:===============");
				printAndCollectContent(op.getReasoner());
				System.out.println("Content:================");
				String content=printAndCollectContent(op.getContent());
				
				if(isPublic) {
					StringBuilder sb=new StringBuilder();
					List<String> commands=new ArrayList<>();
					for(String s:content.split("\n")) {
						s=s.trim();
						if(s.startsWith("##"))
							commands.add(Utils.removeLeadings("##",s));
						else
							sb.append(s);
					}
					this.sendBotMessage(sb.toString());
					if(commands.isEmpty()) {
						commands.add("##");
						content+="\n##";
					}
					if(!isTemperal)
						history.add(Role.ASSISTANT, content, true);
					for(String s:commands) {
						this.sendAtAsBot(s.trim());
					}
				}else {
					if(!isTemperal)
						history.add(Role.ASSISTANT, content, true);
					for(String s:content.split("\n")) {
						this.sendAsBot(s);
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
		
	}
	public static String printAndCollectContent(Reader output) throws IOException {
		BufferedReader br=new BufferedReader(output);
		int read;
		char[] ch=new char[32];
		StringBuilder sb=new StringBuilder();
		while((read=br.read(ch,0,32))!=-1) {
			if(read>0) {
				String input=String.valueOf(ch,0,read);
				System.out.print(input);
				sb.append(input);
			}
		}
		System.out.println();
		return sb.toString();
	}
	@Override
	public void onPrivate(String msg) {
		//super.onPrivate(msg);
		receivedGameMessage(null,msg,false);
	}

	@Override
	public void receivedGameMessage(UserIdentifier sender, String msg,boolean isPublic) {
		super.receivedGameMessage(sender, msg,isPublic);
		synchronized(backLogLock) {
			messageBacklog.append("【");
			if(sender==null)
				messageBacklog.append("系统");
			else
				messageBacklog.append(game.getPlayerById(sender).getMemberString());
			messageBacklog.append("】");
			if(isPublic)
				messageBacklog.append("（公屏）");
			else
				messageBacklog.append("（私聊）");
			messageBacklog.append("：");
			messageBacklog.append(msg);
			messageBacklog.append("\n");
		}
	}

}
