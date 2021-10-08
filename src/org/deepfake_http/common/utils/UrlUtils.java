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
	public static String fileOrUrlToText(String s) throws MalformedURLException, IOException {
		if (s.startsWith(IProtocol.FILE) || s.startsWith(IProtocol.HTTPS) || s.startsWith(IProtocol.HTTP)) {
			try (InputStream is = new BufferedInputStream(new URL(s).openStream())) {
				return new String(is.readAllBytes(), StandardCharsets.UTF_8);
			}
		} else
			return fileOrUrlToText(new File(s).getAbsoluteFile().toURI().toString());
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

}
