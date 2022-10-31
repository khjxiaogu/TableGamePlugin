package com.khjxiaogu.TableGames.platform.simplerobot.kookobjects;

public class KookAPIException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6089774880413851352L;

	public KookAPIException() {
	}
	public KookAPIException(String message, Throwable cause) {
		super(message, cause);
	}
	int code;
	public KookAPIException(int code,String message) {
		super(message);
		this.code=code;
	}
	@Override
	public String getMessage() {
		return code+":"+super.getMessage();
	}

}
