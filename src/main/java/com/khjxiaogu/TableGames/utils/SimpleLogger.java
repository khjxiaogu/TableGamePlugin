package com.khjxiaogu.TableGames.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;


public class SimpleLogger {
	private final static SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss"); //$NON-NLS-1$

	public static enum Level {
		CONFIG("[%s]"+new Ansi().fgBright(Ansi.Color.BLUE).toString()+"%s" + new Ansi().fg(Ansi.Color.CYAN).toString() + "[配置]" + new Ansi().reset().toString()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		INFO("[%s]"+new Ansi().fgBright(Ansi.Color.BLUE).toString()+"%s" + new Ansi().fg(Ansi.Color.GREEN).toString() + "[信息]" + new Ansi().reset().toString()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		WARNING("[%s]"+new Ansi().fgBright(Ansi.Color.BLUE).toString()+"%s" + new Ansi().fg(Ansi.Color.YELLOW).toString() + "[警告]" + new Ansi().reset().toString()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ERROR("[%s]"+new Ansi().fgBright(Ansi.Color.BLUE).toString()+"%s" + new Ansi().fgBright(Ansi.Color.RED).toString() + "[错误]" + new Ansi().reset().toString()), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		SEVERE("[%s]"+new Ansi().fgBright(Ansi.Color.BLUE).toString()+"%s" + new Ansi().fg(Ansi.Color.RED).toString() + "[致命]" + new Ansi().reset().toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		private Level(String format) { this.format = format; }

		protected final String format;
	}

	static {
		AnsiConsole.systemInstall();
	}

	public String getName() { return name; }

	private String name;
	public String getQuoteName() {return "["+name+"]";} //$NON-NLS-1$ //$NON-NLS-2$
	private static final OutputStream nullstream=new OutputStream() {
		private volatile boolean closed;

		private void ensureOpen() throws IOException {
			if (closed)
				throw new IOException("Stream closed"); //$NON-NLS-1$
		}

		@Override
		public void write(int b) throws IOException { ensureOpen(); }

		@Override
		public void write(byte b[], int off, int len) throws IOException {
			// Objects.checkFromIndexSize(off, len, b.length);
			// ensureOpen();
		}

		@Override
		public void close() { closed = true; }
	};

	PrintStream out;
	public void setOut(PrintStream out) { this.out = out; }
	
	public SimpleLogger(String name) {
		out=System.out;
		this.name = name;
		if (name == null) { name = "未知"; } //$NON-NLS-1$
	}
	private static class DummyPrintWriter extends PrintWriter{
		public DummyPrintWriter(){ super(nullstream); }
		SimpleLogger logger;
		public DummyPrintWriter(SimpleLogger logger, Level level) {
			this();
			this.logger = logger;
			this.level = level;
		}
		Level level;
		@Override
		public void println(Object o) {
			synchronized(this) {
				logger.log(level, String.valueOf(o));
			}
		}
	};
	private static class DummyAsPrintWriter extends PrintWriter{
		public DummyAsPrintWriter(){ super(nullstream); }
		String head;
		SimpleLogger logger;
		public DummyAsPrintWriter(SimpleLogger logger,String head) {
			super(nullstream);
			this.head=head;
			this.logger=logger;
		}
		@Override
		public void println(Object o) {
			synchronized(this) {
				logger.logInternal(head,String.valueOf(o));
			}
		}
	};

	DummyPrintWriter[] dpw=new DummyPrintWriter[5];
	protected DummyPrintWriter getForLevel(Level l) {
		DummyPrintWriter maybe=dpw[l.ordinal()];
		if(maybe!=null)return maybe;
		maybe=dpw[l.ordinal()]=new DummyPrintWriter(this,l);
		return maybe;
	}
	public void printStackTrace(Throwable t) {
		printStackTrace(Level.ERROR,t);
	}
	public void printStackTrace(Level l,Throwable t) {
		handleException(t);
		printFullStackTrace(l,t);
	}
	private static void handleException(Throwable t) {
		handleException(t,Thread.currentThread().getStackTrace());
	}
	private static void handleException(Throwable t,StackTraceElement[] current) {
		if(t==null)return;
		StackTraceElement[] ot=t.getStackTrace();
        int m = ot.length - 1;
        int n = current.length - 1;
        while (m >= 0 && n >=0 && ot[m].equals(current[n])) {
            m--; n--;
        }
		t.setStackTrace(Arrays.copyOfRange(ot,0, m+1));
		handleException(t.getCause(),current);
		for(Throwable ex:t.getSuppressed())
			handleException(ex,current);
	}
	public void printFullStackTrace(Throwable t) {
		printFullStackTrace(Level.ERROR,t);
	}
	public void printFullStackTrace(Level l,Throwable t) {
		t.printStackTrace(getForLevel(l));
	}

	public void log(Level l, String s) {
		logInternal(doTranslateHeader(l, SimpleLogger.format.format(new Date()),getQuoteName()), s);
		
	}
	public void logAs(Level l,String as, String s) {
		logInternal(doTranslateHeader(l,SimpleLogger.format.format(new Date()), getQuoteName()+as),s);
	}
	public String doTranslateHeader(Level l,String date,String name) {
		return String.format(l.format,date,name);
	}
	private void logInternal(String head,String info) {
		out.print(head);
		out.println(info);
	}
	public void severe(Object s) { log(Level.SEVERE, String.valueOf(s)); }

	public void error(Object s) { log(Level.ERROR, String.valueOf(s)); }

	public void warning(Object s) { log(Level.WARNING, String.valueOf(s)); }

	public void info(Object s) { log(Level.INFO, String.valueOf(s)); }

	public void config(Object s) { log(Level.CONFIG, String.valueOf(s)); }
}
