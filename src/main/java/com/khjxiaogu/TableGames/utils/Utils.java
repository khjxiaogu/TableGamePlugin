/**
 * Mirai Tablegames Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.BotUserLogic;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;

public class Utils {
	public static String getPlainText(IMessageCompound msg) {
		return msg.getText().trim();
	}

	public static String removeLeadings(String leading, String orig) {
		if (orig.startsWith(leading))
			return orig.substring(leading.length()).replace(leading, "").trim();
		return orig;
	}
	public static byte[] readAll(InputStream i) throws IOException {
		ByteArrayOutputStream ba = new ByteArrayOutputStream(16384);
		int nRead;
		byte[] data = new byte[4096];

		try {
			while ((nRead = i.read(data, 0, data.length)) != -1) { ba.write(data, 0, nRead); }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}

		return ba.toByteArray();
	}
	public static String percent(double v1, double v2) {
		long val=Math.round(v1 / v2 * 10000) / 100;
		if(val>100) {
			val=100;
		}
		if(val<0) {
			val=0;
		}
		if (v2 != 0)
			return String.valueOf(val) + "%";
		return "N/A%";
	}


	/**
	 * @param contact  
	 */
	public static com.khjxiaogu.TableGames.platform.message.Image sendTextAsImage(String text,AbstractRoom contact) {
		return new com.khjxiaogu.TableGames.platform.message.Image(Utils.textAsImage(text));
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
			g2d.drawString(line, 0, fm.getAscent() + fm.getHeight() * i++);
		}

		g2d.dispose();
		return img;
	}
	public static BotUserLogic createLogic(Class<? extends BotUserLogic> logicType,AbstractUser p,Game in) {
		for(Constructor<?> ctor:logicType.getConstructors()) {
			if(ctor.getParameterCount()==2)
				if(ctor.getParameterTypes()[0].isInstance(p)&&ctor.getParameterTypes()[1].isInstance(in))
					try {
						return (BotUserLogic) ctor.newInstance(p,in);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		}
		throw new RuntimeException();
	}
}
