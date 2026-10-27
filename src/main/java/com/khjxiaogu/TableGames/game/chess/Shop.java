package com.khjxiaogu.TableGames.game.chess;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Shop {
	public static final float[][] rarityLevel=new float[][] {
		new float[] {1,0,0},
		new float[] {.7f,.3f,0},
		new float[] {.5f,.4f,.1f},
		new float[] {.3f,.4f,.3f},
		new float[] {.1f,.4f,.5f}
	};
	public List<Characters> rollCharas(PlayerSave party){
		Random rndx=new Random();
		List<Integer> rarity=new ArrayList<>();
		float[] crnrnd=rarityLevel[party.wins];
		for(int i=0;i<5;i++) {
			float rnd=rndx.nextFloat();
			if(rnd<crnrnd[0]) {
				rarity.add(1);
			}else if(rnd<crnrnd[0]+crnrnd[1]) {
				rarity.add(2);
			}else if(rnd<crnrnd[0]+crnrnd[1]+crnrnd[2]) {
				rarity.add(3);
			}
		}
		List<Characters> output=new ArrayList<>();
		for(int i=0;i<5;i++) {
			List<Characters> crn=new ArrayList<>();
			int rarityv=rarity.get(i);
			for(Characters c:Characters.values())
				if(c.rarity==rarityv)
					crn.add(c);
			output.add(crn.get(rndx.nextInt(crn.size())));
		}
		return output;
	}
}
