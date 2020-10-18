package com.khjxiaogu.TableGames.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;

public class Utils {
	public static String getPlainText(MessageChain msg) {
		PlainText pt = msg.first(PlainText.Key);
		if (pt == null)
			return "";
		return pt.getContent().trim();
	}

	public static String removeLeadings(String leading, String orig) {
		if (orig.startsWith(leading))
			return orig.substring(leading.length()).replace(leading, "").trim();
		return orig;
	}

	public static String percent(double v1, double v2) {
		if (v2 != 0) {
			return String.valueOf(Math.round(v1 / v2 * 10000) / 100) + "%";
		}
		return "N/A%";
	}

	public static Image sendTextAsImage(String text,Contact contact) {
		return contact.uploadImage(textAsImage(text));
	}
	public static BufferedImage textAsImage(String text) {
		String[] lines = text.split("\n");
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		Font font = new Font("黑体", Font.PLAIN, 16);
		g2d.setFont(font);
		FontMetrics fm = g2d.getFontMetrics();
		int width = 0;
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
		int i = 0;
		for (String line : lines) {
			g2d.drawString(line, 0, fm.getAscent() + fm.getHeight() * (i++));
		}

		g2d.dispose();
		return img;
	}
}
