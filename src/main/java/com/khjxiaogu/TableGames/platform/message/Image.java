package com.khjxiaogu.TableGames.platform.message;

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Image implements IMessage {
	public Image() {
	}

	protected byte[] data;
	public Image(RenderedImage img) {
		try(ByteArrayOutputStream baos=new ByteArrayOutputStream(4096)){
			ImageIO.write(img,"jpg",baos);
			data=baos.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Image(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
