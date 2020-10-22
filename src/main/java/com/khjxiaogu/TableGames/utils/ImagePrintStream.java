package com.khjxiaogu.TableGames.utils;

import java.util.LinkedList;
import java.util.Locale;

public class ImagePrintStream implements Appendable{
	LinkedList<StringBuilder> sbs=new LinkedList<>();
	protected void newLine() {
		sbs.add(new StringBuilder());
	}
	public void write(int b) {
		sbs.peekLast().append(b);
	}

	public void print(boolean b) {
		sbs.peekLast().append(b);
	}

	
	public void print(char c) {
		sbs.peekLast().append(c);
	}

	
	public void print(int i) {
		sbs.peekLast().append(i);
	}

	
	public void print(long l) {
		sbs.peekLast().append(l);
	}

	
	public void print(float f) {
		sbs.peekLast().append(f);
	}

	
	public void print(double d) {
		sbs.peekLast().append(d);
	}

	
	public void print(char[] s) {
		sbs.peekLast().append(s);
	}

	
	public void print(String s) {
		sbs.peekLast().append(s);
	}

	
	public void print(Object obj) {
		sbs.peekLast().append(obj);
	}

	
	public void println() {
		newLine();
	}

	
	public void println(boolean x) {
		sbs.peekLast().append(x);
		newLine();
	}

	
	public void println(char x) {
		sbs.peekLast().append(x);
		newLine();
	}

	
	public void println(int x) {
		sbs.peekLast().append(x);
		newLine();
	}

	
	public void println(long x) {
		sbs.peekLast().append(x);
		newLine();
	}

	
	public void println(float x) {
		sbs.peekLast().append(x);
		newLine();
	}

	
	public void println(double x) {
		sbs.peekLast().append(x);
		newLine();
	}

	
	public void println(char[] x) {
		sbs.peekLast().append(x);
		newLine();
	}

	
	public void println(String x) {
		sbs.peekLast().append(x);
		newLine();
	}

	
	public void println(Object x) {
		sbs.peekLast().append(x);
		newLine();
	}

	
	public ImagePrintStream printf(String format, Object... args) {
		sbs.peekLast().append(String.format(format, args));
		return this;
	}

	
	public ImagePrintStream printf(Locale l, String format, Object... args) {
		sbs.peekLast().append(String.format(l, format, args));
		return this;
	}

	
	public ImagePrintStream format(String format, Object... args) {
		sbs.peekLast().append(String.format(format, args));
		return this;
	}

	
	public ImagePrintStream format(Locale l, String format, Object... args) {
		sbs.peekLast().append(String.format(l, format, args));
		return this;
	}

	
	public ImagePrintStream append(CharSequence csq) {
		sbs.peekLast().append(csq);
		return this;
	}

	
	public ImagePrintStream append(CharSequence csq, int start, int end) {
		sbs.peekLast().append(csq, start, end);
		return this;
	}

	
	public ImagePrintStream append(char c) {
		sbs.peekLast().append(c);
		return this;
	}
	public ImagePrintStream() {
		sbs.add(new StringBuilder());
	}
}
