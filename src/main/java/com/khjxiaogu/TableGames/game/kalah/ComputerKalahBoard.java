package com.khjxiaogu.TableGames.game.kalah;

import com.khjxiaogu.TableGames.game.kalah.AIKalahBoard.ComputeResult;
import com.khjxiaogu.TableGames.platform.AbstractRoom;

public class ComputerKalahBoard extends KalahBoard {
	AIKalahBoard ai;

	public ComputerKalahBoard(String name, AbstractRoom ar, int counth, int initc) {
		super(name, ar, counth, initc);
		ai=new AIKalahBoard(counth,initc);
	}

	@Override
	 int input(boolean isFirst) {
        int sl=0;
        short[] data=new short[COUNT_HOLE * 2 + 3];
        this.save(data);
        ai.load(data);
        if(isFirst) {
	        ComputeResult cr=ai.calculateZeroResult(sl);
	        //System.out.println(cr.point);
	        return cr.slot+1;
        }
        ComputeResult cr=ai.calculateBestResult(sl);
        
        return cr.slot+1;
    }

}
