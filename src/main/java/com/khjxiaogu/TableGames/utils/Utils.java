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
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import com.khjxiaogu.TableGames.Game;
import com.khjxiaogu.TableGames.MessageListener;
import com.khjxiaogu.TableGames.MessageListener.MsgType;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;

public class Utils {
	static class MessageListenerWrapper implements MessageListener {
		public MessageListener ml;
		public boolean isValid = true;
		public Group from;

		public MessageListenerWrapper(MessageListener ml, Group from) {
			this.ml = ml;
			this.from = from;
		}

		@Override
		public void handle(MessageChain msg, MsgType type) {
			ml.handle(msg, type);
		}
	}

	public static Map<Group, Game> getGames() {
		return gs;
	}

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

	static Map<Group, Game> gs = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Long, MessageListenerWrapper> mls = new ConcurrentHashMap<>();
	static Map<Group, Map<Class<? extends PreserveInfo<?>>, PreserveInfo<?>>> ps = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public static <T extends PreserveInfo<?>> T getPreserve(Group g, Class<T> type) {
		Map<Class<? extends PreserveInfo<?>>, PreserveInfo<?>> mc = ps.get(g);
		if (mc == null) {
			mc = new ConcurrentHashMap<>();
			ps.put(g, mc);
		}
		PreserveInfo<?> pi = mc.get(type);
		if (pi == null) {
			try {
				pi = type.getConstructor(Group.class).newInstance(g);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mc.put(type, pi);
		}
		return (T) pi;
	}

	public static void registerListener(Long id, Group g, MessageListener ml) {
		mls.put(id, new Utils.MessageListenerWrapper(ml, g));
	}

	public static void registerListener(Long id, MessageListener ml) {
		mls.put(id, new Utils.MessageListenerWrapper(ml, null));
	}

	public static void registerListener(Member m, MessageListener ml) {
		mls.put(m.getId(), new Utils.MessageListenerWrapper(ml, m.getGroup()));
	}

	public static void releaseListener(Long id) {
		mls.remove(id);
	}

	public static Set<Long> ingame = Collections.newSetFromMap(new ConcurrentHashMap<>());

	public static boolean tryAddMember(Long id) {
		// return ingame.add(id);
		return true;
	}

	public static boolean hasMember(Long id) {
		// return ingame.contains(id);
		return false;
	}

	public static void RemoveMember(Long id) {
		ingame.remove(id);
	}

	public static boolean dispatch(Long id, MsgType type, MessageChain msg) {
		if (Utils.getPlainText(msg).startsWith("重置")) {
			for (Game g : getGames().values()) {
				if (g.isAlive())
					if (g.onReAttach(id))
						break;
			}
			return true;
		}
		Utils.MessageListenerWrapper ml = mls.get(id);
		System.out.println("dispatching " + id);
		if (ml == null || !ml.isValid)
			return false;
		ml.handle(msg, type);
		System.out.println("dispatched " + id);
		return true;
	}

	public static boolean dispatch(Long id, Group g, MsgType type, MessageChain msg) {
		Utils.MessageListenerWrapper ml = mls.get(id);
		if (ml == null || !ml.isValid)
			return false;
		if (!(g.equals(ml.from) || ml.from == null))
			return false;
		System.out.println("dispatching msg to " + id);
		ml.handle(msg, type);
		return true;
	}

	public static void InvalidListeners() {
		mls.values().iterator().forEachRemaining(a -> a.isValid = false);
	}

	public static boolean hasActiveGame(Group gp) {
		Game g = getGames().get(gp);
		if (g != null && g.isAlive())
			return true;
		return false;
	}

	public static <T extends Game> T createGame(Class<T> gameClass, Group gp, int count) {
		Game g = getGames().get(gp);
		synchronized (getGames()) {
			if (g != null && g.isAlive()) {
				g.forceStop();
			}
			T ng = null;
			try {
				ng = gameClass.getConstructor(Group.class, int.class).newInstance(gp, count);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getGames().put(gp, ng);
			return ng;
		}
	}

	public static String percent(double v1, double v2) {
		if (v2 != 0) {
			return String.valueOf(Math.round(v1 / v2 * 10000) / 100) + "%";
		}
		return "N/A%";
	}

	public static <T extends Game> T createGame(Class<T> gameClass, Group gp, String... args) {
		Game g = getGames().get(gp);
		synchronized (getGames()) {
			if (g != null && g.isAlive()) {
				g.forceStop();
			}
			T ng = null;
			try {
				ng = gameClass.getConstructor(Group.class, String[].class).newInstance(gp, args);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getGames().put(gp, ng);
			return ng;
		}
	}
	public static <T extends Game> T createGame(Class<T> gameClass, Group gp,int cplayer,Map<String,String> args) {
		Game g = getGames().get(gp);
		synchronized (getGames()) {
			if (g != null && g.isAlive()) {
				g.forceStop();
			}
			T ng = null;
			try {
				ng = gameClass.getConstructor(Group.class,int.class,Map.class).newInstance(gp,cplayer, args);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getGames().put(gp, ng);
			return ng;
		}
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
