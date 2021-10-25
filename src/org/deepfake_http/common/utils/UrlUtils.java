/*
MIT License

Copyright (c) 2021 xnbox team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

HOME:   https://xnbox.github.io
E-Mail: xnbox.team@outlook.com
*/

package org.deepfake_http.common.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class UrlUtils {

	/**
	 * 
	 * @param s
	 * @return
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	private static byte[] fileOrUrlToBytes(String s) throws MalformedURLException, IOException {
		if (s.startsWith(IProtocol.FILE) || s.startsWith(IProtocol.HTTPS) || s.startsWith(IProtocol.HTTP)) {
			try (InputStream is = new BufferedInputStream(new URL(s).openStream())) {
				return is.readAllBytes();
			}
		} else
			return fileOrUrlToBytes(new File(s).getAbsoluteFile().toURI().toString());
	}

	/**
	 * 
	 * @param s
	 * @return
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static String fileOrUrlToText(String s) throws MalformedURLException, IOException {
		return new String(fileOrUrlToBytes(s), StandardCharsets.UTF_8);
	}

	/**
	 * Parse data URL
	 *
	 * @param url
	 * @param contentTypeArr
	 * @return
	 * @throws IOException
	 */
	public static byte[] getDataUrlContent(String url, String[] contentTypeArr) throws IOException {
		// data:[<mediatype>][;base64],<data>
		String mime;
		String encoding;
		String data;
		String s       = url.substring(5);
		int    posSemi = s.indexOf(';');
		int    pos     = posSemi;
		if (pos == -1)
			pos = s.indexOf(',');
		if (pos == 0)
			mime = null;
		else
			mime = s.substring(0, pos);
		int posComa = s.indexOf(',');
		if (posSemi == -1)
			encoding = null;
		else
			encoding = s.substring(posSemi + 1, posComa);
		data = s.substring(posComa + 1);
		byte[] bs;
		if ("base64".equals(encoding))
			bs = Base64.getMimeDecoder().decode(data);
		else
			bs = data.getBytes(StandardCharsets.UTF_8);
		if (mime != null)
			contentTypeArr[0] = mime + (encoding == null ? "" : "; charset=" + encoding);
		return bs;
	}

	/**
	 * 
	 * @param url
	 * @param contentTypeArr
	 * @return
	 * @throws IOException
	 */
	public static byte[] getUrlContent(String url, String[] contentTypeArr) throws IOException {
		URLConnection connection = new URL(url).openConnection();
		contentTypeArr[0] = connection.getContentType();
		try (InputStream is = connection.getInputStream()) {
			return is.readAllBytes();
		}
	}

	public static String getMimeByFile(String file) {
		int pos = file.lastIndexOf('.');
		if (pos == -1)
			return "application/octet-stream";
		String ext = file.substring(pos + 1);
		if (ext.equals("html") || ext.equals("htm"))
			return "text/html";
		if (ext.equals("js"))
			return "application/js";
		if (ext.equals("json"))
			return "application/json; charset=utf-8";
		if (ext.equals("css"))
			return "text/css";
		if (ext.equals("txt"))
			return "text/plain";
		if (ext.equals("csv"))
			return "text/csv";
		if (ext.equals("pdf"))
			return "application/pdf";
		if (ext.equals("doc"))
			return "application/msword";
		if (ext.equals("odt"))
			return "application/vnd.oasis.opendocument.text";
		if (ext.equals("ods"))
			return "application/vnd.oasis.opendocument.spreadsheet";
		if (ext.equals("png"))
			return "image/png";
		if (ext.equals("gif"))
			return "image/gif";
		if (ext.equals("jpeg") || ext.equals("jpg"))
			return "image/jpeg";
		if (ext.equals("ico"))
			return "image/vnd.microsoft.icon";
		if (ext.equals("svg"))
			return "image/svg+xml";
		if (ext.equals("mp3"))
			return "audio/mpeg";
		if (ext.equals("mp4"))
			return "video/mp4";
		if (ext.equals("mpeg"))
			return "video/mpeg";
		if (ext.equals("xml"))
			return "application/xml";
		if (ext.equals("ttf"))
			return "font/ttf";
		if (ext.equals("woff"))
			return "font/woff";
		if (ext.equals("woff2"))
			return "font/woff2";
		if (ext.equals("otf"))
			return "font/otf";
		if (ext.equals("wav"))
			return "audio/wav";
		if (ext.equals("weba"))
			return "audio/webm";
		if (ext.equals("webm"))
			return "video/webm";
		if (ext.equals("webp"))
			return "image/webp";
		if (ext.equals("oga"))
			return "audio/ogg";
		if (ext.equals("ogv"))
			return "video/ogg";
		if (ext.equals("ogx"))
			return "application/ogg";
		return "application/octet-stream";
	}

}
