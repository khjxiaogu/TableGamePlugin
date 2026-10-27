package com.khjxiaogu.TableGames.game.chess;

public class CombatActor {

	public static void main(String[] args) {
		PlayerSave player1=new PlayerSave();
		PlayerSave player2=new PlayerSave();
		player1.addActor(Characters.SHIELDER);
		player2.addActor(Characters.WARRIOR);

		player1.addActor(Characters.WARRIOR);
		player2.addActor(Characters.HEALER);
		Party p1=player1.createParty();
		Party p2=player2.createParty();
		p1.gameStart(p2);
		p2.gameStart(p1);
		while(true) {
			Combatant c1=p1.popNext();
			Combatant c2=p2.popNext();
			if(c1==null||c2==null) {
				break;
			}
			StringBuilder status=new StringBuilder();
			if(p1.isRefilled) {
				for(Combatant cbt:p1.list) {
					if(cbt.hp>0) {
						cbt.battleEnd(p2);
					}
				}
				p1.isRefilled=false;
				for(Combatant cbt:p1.list) {
					if(cbt.hp>0) {
						cbt.battleStart(p2);
					}
				}

				status.append("己方：\n").append(p1.getDesc()).append("\n对方：\n").append(p2.getDesc()).append("\n");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(p2.isRefilled) {
				for(Combatant cbt:p2.list) {
					if(cbt.hp>0) {
						cbt.battleEnd(p1);
					}
				}

				p2.isRefilled=false;
				for(Combatant cbt:p2.list) {
					if(cbt.hp>0) {
						cbt.battleStart(p1);
					}
				}
			}
			status.append(c1.index).append(c1.getName()).append("vs").append(c2.index).append(c2.getName());
			c1.rowStart(c2);
			c2.rowStart(c1);
			c1.beforeAttack(c2);
			c2.beforeAttack(c1);
			float dmg1=c1.createAttack(c2);
			float dmg2=c2.createAttack(c1);
			status.append("造成").append(dmg1).append("伤害，受到").append(dmg2).append("伤害");
			System.out.println(status.toString());
			c2.receiveAttack(c1, dmg1);
			c1.receiveAttack(c2, dmg2);
			c1.afterAttack(c2);
			c2.afterAttack(c1);
			c1.rowEnd(c2);
			c2.rowEnd(c1);
		}
		StringBuilder status=new StringBuilder("己方：\n");
		status.append(p1.getDesc()).append("\n对方：\n").append(p2.getDesc()).append("\n");
		System.out.println(status.toString());
	}

}
