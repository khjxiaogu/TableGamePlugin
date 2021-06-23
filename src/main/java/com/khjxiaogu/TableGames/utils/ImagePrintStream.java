package com.khjxiaogu.TableGames.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Locale;

import javax.imageio.ImageIO;

public class ImagePrintStream implements Appendable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7505991592374812133L;
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


	@Override
	public ImagePrintStream append(CharSequence csq) {
		sbs.peekLast().append(csq);
		return this;
	}


	@Override
	public ImagePrintStream append(CharSequence csq, int start, int end) {
		sbs.peekLast().append(csq, start, end);
		return this;
	}


	@Override
	public ImagePrintStream append(char c) {
		sbs.peekLast().append(c);
		return this;
	}
	public ImagePrintStream() {
		sbs.add(new StringBuilder());
	}
	public BufferedImage asImage() {
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		Font font = new Font("黑体", Font.PLAIN, 16);
		g2d.setFont(font);
		FontMetrics fm = g2d.getFontMetrics();
		int width = 0;
		String[] lines=new String[sbs.size()];
		int i=0;
		for(StringBuilder l:sbs) {
			lines[i++]=l.toString();
		}
		int height = fm.getHeight() * lines.length;

		for (String line : lines) {
			Rectangle2D r2d = fm.getStringBounds(line, g2d);
			width = Math.max(width, (int) (r2d.getWidth() - r2d.getX()));
		}
		g2d.dispose();
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, width, height);
		g2d.setFont(font);
		fm = g2d.getFontMetrics();
		g2d.setColor(Color.BLACK);
		i = 0;
		for (String line : lines) {
			g2d.drawString(line, 0, fm.getAscent() + fm.getHeight() * i++);
		}
		g2d.dispose();
		return img;
	}
	public static void main(String[] args) {
		ImagePrintStream ips=new ImagePrintStream();
		ips.println("awa");
		ips.println("bwb");
		ips.println("cwc");
		ips.print("dwd");
		ips.println("dwd");
		try {
			ImageIO.write(ips.asImage(),"jpeg",new File("test.jpg"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public ImagePrintStream append(int i) {
		sbs.peekLast().append(i);
		return this;
	}
}
