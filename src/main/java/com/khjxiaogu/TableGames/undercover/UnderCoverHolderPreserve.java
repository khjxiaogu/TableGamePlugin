package com.khjxiaogu.TableGames.undercover;

import com.khjxiaogu.TableGames.utils.PreserveInfo;

import net.mamoe.mirai.contact.Group;

public class UnderCoverHolderPreserve extends PreserveInfo<UnderCoverHolder> {

	public UnderCoverHolderPreserve(Group g) {
		super(g);
	}

	@Override
	protected int getSuitMembers() {
		return 7;
	}

	@Override
	protected int getMinMembers() {
		return 5;
	}

	@Override
	protected int getMaxMembers() {
		return 14;
	}

	@Override
	protected Class<UnderCoverHolder> getGameClass() {
		return UnderCoverHolder.class;
	}

	@Override
	public String getName() {
		return "谁是卧底发词";
	}

}
