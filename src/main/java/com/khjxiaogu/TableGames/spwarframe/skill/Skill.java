package com.khjxiaogu.TableGames.spwarframe.skill;

import java.util.List;

import com.google.gson.JsonArray;
import com.khjxiaogu.TableGames.spwarframe.GameManager;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.CantSaveException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.CantSelfException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.InvalidInterruptedTargetException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.InvalidSkillTargetException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.RoleDiedException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.RoleNotExistException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.spwarframe.GameManager.GameTurn;
import com.khjxiaogu.TableGames.spwarframe.events.Event;
import com.khjxiaogu.TableGames.spwarframe.role.Role;

// TODO: Auto-generated Javadoc
/**
 * Class Skill. 技能
 * 
 * @author khjxiaogu file: Skill.java time: 2020年6月30日
 */
public abstract class Skill {
	protected int locked = 0;
	private int remain;
	protected Role owner;
	protected GameManager game;

	/**
	 * Calls on turn start.<br>
	 * 在回合开始的时候调用
	 * 
	 * @return true, if skill is not to be removed<br>
	 *         如果技能无需被移除，返回true。
	 */
	public boolean onTurnStart() { return true; };

	public Skill(GameManager game, Role owner) {
		this.owner = owner;
		this.game = game;
		resetRemain();
		owner.addSkill(this);
	}

	protected boolean fireEvent(Event ev) {
		return game.fireEvent(ev);
	}

	/**
	 * Calls on turn end before skills take effects.<br>
	 * 回合结束时技能生效前调用
	 * 
	 * @return true, if skill is not to be removed<br>
	 *         如果技能无需被移除，返回true。
	 */
	public boolean onBeforeTurnEnd() {
		if (locked > 0)
			locked--;
		return true;
	};

	public boolean onSkillCall(List<String> params) {
		if (available()) {
			try {
				if(this.onSkillUse(params)) {
					this.reduceRemain();
				}else{
					owner.sendMessage("技能发动失败！");
					return false;
				}
			}catch(CantSaveException ex) {
				owner.sendMessage("技能发动失败！选择的玩家没有生命危险。");
				return false;
			}catch(RoleNotExistException ex) {
				owner.sendMessage("技能发动失败！选择的目标不存在或非游戏玩家。");
				return false;
			}catch(RoleDiedException ex) {
				owner.sendMessage("技能发动失败！目标玩家已经死亡。");
				return false;
			}catch(CantSelfException ex) {
				owner.sendMessage("技能发动失败！技能目标不能是自己。");
				return false;
			}catch(InvalidInterruptedTargetException ex) {
				owner.sendMessage("技能发动失败！无法干扰该技能。");
				return false;
			}catch(InvalidSkillTargetException ex) {
			}catch(InvalidSkillParamException ex) {
				owner.sendMessage("格式错误，请输入：“"+ex.getShouldbe()+"”。");
				return false;
			}catch(SkillException ex) {
			}
		}else return false;
		owner.sendMessage("技能发动成功。");
		return canOnceOnly();
	}

	protected abstract boolean onSkillUse(List<String> params) throws SkillException;

	/**
	 * Calls on turn end.<br>
	 * 回合完全结束时调用
	 * 
	 * @return true, if skill is not to be removed<br>
	 *         如果技能无需被移除，返回true。
	 */
	public boolean onTurnEnd() { return true; };

	/**
	 * Calls on effect pending.<br>
	 * 技能生效判定时调用
	 */
	public void reduceRemain() {
		if (remain > 0)
			remain--;
	}

	/**
	 * Checks if is available.<br>
	 * 检查是否可用
	 * 
	 * @return true, if available<br>
	 *         如果可用，返回true。
	 */
	public boolean available() { return locked == 0 && remain > 0; }
	
	public boolean isUsableOn(GameTurn turn) {
		return turn.isAvailableFor(getType());
	}
	public abstract SkillType getType();

	/**
	 * Gets the name.<br>
	 * 获取技能名字.
	 * 
	 * @return name<br>
	 */
	public abstract String getName();

	/**
	 * Gets the description.<br>
	 * 获取技能描述.
	 * 
	 * @return desc<br>
	 */
	public abstract String getDesc();

	public abstract int getMaxRemain();

	/**
	 * Gets the turns before unlock.<br>
	 * 获取锁定回合数.
	 * 
	 * @return turns<br>
	 */
	public int getLocked() { return locked; }

	/**
	 * Changes the locked turns, replace current with the max value of current and
	 * new.<br>
	 * 添加锁定回合，将选择当前和参数最大的回合
	 * 
	 * @param locked lock turn,-1 means infinity<br>
	 *               锁定回合，-1表示无限期
	 */
	public void addLocked(int locked) {
		if(this.locked==locked)return;
		if (this.locked < locked || locked == -1 && this.locked != -1) {
			//owner.sendMessage("你的"+this.getName()+"技能被"+(locked==-1?"无限期封锁":("封锁"+locked+"回合")));
			this.locked = locked;
		}
	}

	public Role getOwner() {
		return owner;
	}

	/**
	 * Gets the remain uses.<br>
	 * 获取剩余使用次数
	 * 
	 * @return remain uses<br>
	 */
	public int getRemain() { return remain; }

	/**
	 * set remain uses.<br>
	 * 设置剩余使用次数.
	 * 
	 * @param remain value to set remain uses to.<br>
	 *               设置剩余次数为的值
	 */
	public void setRemain(int remain) { this.remain = remain; }
	public void retainRemain() { this.remain++; }
	public String[] getSpecialParamType() {
		return null;
	}
	/**
	 * Reset remain.<br>
	 * 恢复剩余使用次数
	 */
	public void resetRemain() { remain = getMaxRemain(); }
	//是否只能用一次
	public boolean canOnceOnly() {
		return true;
	}
	public void ensureInterruptType(SkillType st) throws SkillException {
		if(st==getType())return;
		throw new InvalidInterruptedTargetException(this.getName());
	}
}
