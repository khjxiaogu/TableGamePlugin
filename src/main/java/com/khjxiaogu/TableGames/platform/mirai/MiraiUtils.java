package com.khjxiaogu.TableGames.platform.mirai;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.khjxiaogu.TableGames.utils.Utils;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

public class MiraiUtils {
	public static String getPlainText(MessageChain msg) {
		StringBuilder ptb=new StringBuilder();
		for(Message m:msg) {
			if(m instanceof PlainText)
				ptb.append(((PlainText) m).getContent());
		}
		if(ptb.length()==0)
			return "";
		return ptb.toString().trim();
	}
	public static At getAt(MessageChain msg) {
		for(Message m:msg) {
			if(m instanceof At)
				return (At) m;
		}
		return null;
	}
	public static Image getImage(MessageChain msg) {
		for(Message m:msg) {
			if(m instanceof At)
				return (Image) m;
		}
		return null;
	}
	public static Image sendTextAsImage(String text,Contact contact) {
		return contact.uploadImage(ImageResource(Utils.textAsImage(text)));
	}
	public static ExternalResource ImageResource(RenderedImage img) {
		try(ByteArrayOutputStream baos=new ByteArrayOutputStream(4096)){
			ImageIO.write(img,"jpg",baos);
			return ExternalResource.create(baos.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
