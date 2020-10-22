package com.khjxiaogu.TableGames.werewolf;

public abstract class Behaviour {
	private static class NOPBehaviour extends Behaviour{
		public NOPBehaviour(Villager player) {
			super(player);
		}
		@Override
		public boolean fireBehaviour() {
			return false;
		}
	}
	public static abstract class SkillBehaviour extends Behaviour{

		public SkillBehaviour(Villager player) {
			super(player);
		}
		protected void fireSkill(Villager p, int skid) {
			player.fireSkill(p,skid);
		}
	}
	public final Behaviour NOP=new NOPBehaviour(null);
	protected Villager player;
	public Behaviour(Villager player) {
		this.player = player;
	}
	public abstract boolean fireBehaviour();
}
