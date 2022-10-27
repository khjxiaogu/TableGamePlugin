package com.khjxiaogu.TableGames.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;


public class FileUtil {
	public static void transfer(InputStream i, OutputStream os) throws IOException {
		int nRead;
		byte[] data = new byte[4096];

		try {
			while ((nRead = i.read(data, 0, data.length)) != -1) {
				os.write(data, 0, nRead);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}

	public static void transfer(File i, OutputStream os) throws IOException {
		try (FileInputStream fis = new FileInputStream(i)) {
			transfer(fis, os);
		}
	}

	public static void transfer(InputStream i, File f) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(f)) {
			transfer(i, fos);
		}
	}
	public static void transfer(String i, OutputStream os) throws IOException {
		os.write(i.getBytes(StandardCharsets.UTF_8));
	}
	public static void transfer(String i, File os) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(os)) {
			transfer(i,fos);
		}
	}
	public static void transfer(String i,HttpURLConnection os) throws IOException {
		transfer(i,os.getOutputStream());
	}
	
	public static byte[] readAll(InputStream i) throws IOException {
		ByteArrayOutputStream ba = new ByteArrayOutputStream(16384);
		int nRead;
		byte[] data = new byte[4096];

		try {
			while ((nRead = i.read(data, 0, data.length)) != -1) {
				ba.write(data, 0, nRead);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}

		return ba.toByteArray();
	}
	public static byte[] readIgnoreSpace(InputStream i) throws IOException {
		ByteArrayOutputStream ba = new ByteArrayOutputStream(16384);
		int nRead;
		byte[] data = new byte[4096];

		try {
			while ((nRead = i.read(data, 0, data.length)) != -1) {
				for(int j=0;j<nRead;j++) {
					byte b=data[j];
					if (!(b == 9 || b == 10 || b == 13 || b == 32)) {
						ba.write(b);
						
					}
					
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}

		return ba.toByteArray();
	}
	public static String readString(InputStream i) throws IOException {
		return new String(readAll(i),StandardCharsets.UTF_8);
	}
	public static String readString(HttpURLConnection i) throws IOException {
		return readString(i.getInputStream());
	}
	public static String readString(File f) throws IOException {
		return new String(readAll(f),StandardCharsets.UTF_8);
	}
	public static byte[] readIgnoreSpace(File f) throws IOException {
		try (FileInputStream fis = new FileInputStream(f)) {
			return readIgnoreSpace(fis);
		}
	}
	public static byte[] readAll(File f) throws IOException {
		try (FileInputStream fis = new FileInputStream(f)) {
			return readAll(fis);
		}
	}
    public static InputStream fetch(String address) {
        try{
        	URL url = new URL(address);
        	URLConnection urlConnection = url.openConnection();
            return urlConnection.getInputStream();
        } catch (Throwable e) {
            
        }
        return null;
    }
}
