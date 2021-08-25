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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DataUriUtils {

	/**
	 * Parse data URL
	 *
	 * @param url
	 * @param mediatypeSb
	 * @param encodingSb
	 * @return
	 * @throws IOException
	 */
	public static byte[] parseDataUri(String url, StringBuilder mediatypeSb, StringBuilder encodingSb) throws IOException {
		// data:[<mediatype>][;base64],<data>
		String mediatype;
		String encoding;
		String data;
		String s       = url.substring(5);
		int    posSemi = s.indexOf(';');
		int    pos     = posSemi;
		if (pos == -1)
			pos = s.indexOf(',');
		if (pos == 0)
			mediatype = "";
		else
			mediatype = s.substring(0, pos);
		mediatypeSb.append(mediatype);
		int posComa = s.indexOf(',');
		if (posSemi == -1)
			encoding = "";
		else
			encoding = s.substring(posSemi + 1, posComa);
		data = s.substring(posComa + 1);
		if ("base64".equals(encoding))
			return Base64.getMimeDecoder().decode(data);
		else {
			encodingSb.append(encoding);
			return data.getBytes(StandardCharsets.UTF_8);
		}
	}

}
