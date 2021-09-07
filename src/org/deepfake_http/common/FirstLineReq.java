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

package org.deepfake_http.common;

import java.util.Locale;

import org.deepfake_http.common.utils.ParseDumpUtils;

public class FirstLineReq {

	private String method;
	private String uri;
	private String protocol;

	private String firstLine;

	public FirstLineReq(String firstLine) throws Exception {
		this.firstLine = firstLine;

		firstLine = firstLine.trim();
		String[] arr = firstLine.split("\s");
		method = arr[0];
		try {
			HttpMethod.valueOf(method.toUpperCase(Locale.ENGLISH));
		} catch (Exception e) {
			throw new Exception("Bad HTTP method");
		}
		uri = arr[1].trim();
		if (uri.isEmpty())
			throw new Exception("Empty URI");

		protocol = arr[2];
		if (!ParseDumpUtils.HTTP_1_1.equals(protocol))
			throw new Exception("Bad protocol");
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	@Override
	public String toString() {
		// E.g.: "GET /form.html HTTP/1.1"
		return firstLine;
	}
}
