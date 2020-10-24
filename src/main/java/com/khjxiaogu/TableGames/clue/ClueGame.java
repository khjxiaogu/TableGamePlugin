package com.khjxiaogu.TableGames.clue;

import com.khjxiaogu.TableGames.Game;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;

public class ClueGame extends Game {

	public ClueGame(Group group, int cplayer, int nthread) {
		super(group, cplayer, nthread);
	}

	@Override
	public boolean addMember(Member mem) {
		return false;
	}

	@Override
	public void forceStart() {
	}

	@Override
	public String getName() {
		return "妙探寻凶";
	}

	@Override
	public boolean isAlive() {
		return false;
	}

	@Override
	public boolean onReAttach(Long id) {
		return false;
	}

}
