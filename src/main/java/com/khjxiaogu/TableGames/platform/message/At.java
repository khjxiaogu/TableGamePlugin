package com.khjxiaogu.TableGames.platform.message;

import com.khjxiaogu.TableGames.platform.AbstractUser;

public class At implements IMessage {
	long id;

	public At(long id) {
		this.id = id;
	}
	public At(AbstractUser p) {
		id = p.getId();
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	public long getTarget() {
		return id;
	}
}
