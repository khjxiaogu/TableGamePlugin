package com.khjxiaogu.TableGames.game.idiomsolitare;

public class IdiomInfo {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IdiomInfo other = (IdiomInfo) obj;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}
	String word;
	String start;
	String end;
	public boolean startsWith(IdiomInfo ii) {
		return ii.end.equals(this.start)||ii.word.endsWith(this.word.substring(0,1));
	}
	public boolean endsWith(IdiomInfo ii) {
		return ii.start.equals(this.end)||ii.word.startsWith(this.word.substring(word.length()-1));
	}
	public IdiomInfo(String word, String start, String end) {
		this.word = word;
		this.start = start;
		this.end = end;
	}
}
