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
package com.khjxiaogu.TableGames.platform.simplerobot;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import com.khjxiaogu.TableGames.utils.Utils;

import love.forte.simbot.bot.Bot;
import love.forte.simbot.message.At;
import love.forte.simbot.message.Image;
import love.forte.simbot.message.Message;
import love.forte.simbot.message.Message.Element;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.PlainText;
import love.forte.simbot.resources.Resource;



public class SBUtils {
	public static String getPlainText(Messages msg) {
		StringBuilder ptb=new StringBuilder();
		for(Element<?> m:msg) {
			if(m instanceof PlainText)
				ptb.append(((PlainText<?>) m).getText());
		}
		if(ptb.length()==0)
			return "";
		return ptb.toString().trim();
	}
	public static At getAt(Messages msg) {
		for(Element<?> m:msg) {
			if(m instanceof At)
				return (At) m;
		}
		return null;
	}
	public static Image getImage(Messages msg) {
		for(Element<?> m:msg) {
			if(m instanceof Image)
				return (Image) m;
		}
		return null;
	}
	public static Message join(Message a,Message b) {
		if(a instanceof Message.Element) {
			if(b instanceof Message.Element) {
				return Messages.toMessages((Message.Element)a,(Message.Element)b);
			}
			Messages m=(Messages) b;
			Element[] elms=new Element[m.size()+1];
			elms[0]=(Element) a;
			int i=0;
			for(Element e:m) {
				elms[++i]=e;
			}
			return Messages.toMessages(elms);
		}
		if(b instanceof Message.Element) {
			return ((Messages) a).plus((Element<?>) b);
		}
		Messages m=(Messages)a;
		Messages m2=(Messages) b;
		
		Element[] elms=new Element[m.size()+m2.size()];
		int i=0;
		for(Element e:m) {
			elms[i++]=e;
		}
		for(Element e:m2) {
			elms[i++]=e;
		}
		return Messages.toMessages(elms);
	}
	public static Image sendTextAsImage(String text,Bot bot) {
		return bot.uploadImageBlocking(ImageResource(Utils.textAsImage(text)));
	}
	public static Resource ImageResource(RenderedImage img) {
		try(ByteArrayOutputStream baos=new ByteArrayOutputStream(4096)){
			ImageIO.write(img,"jpg",baos);
			
			return Resource.of(baos.toByteArray(), "TextImage.jpg");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
