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

import org.deepfake_http.common.utils.ParseDumpUtils;

public class FirstLineResp {

	private String protocol;
	private int    status;
	private String message;

	private String firstLineRespStr;

	public FirstLineResp(String firstLineRespStr) throws Exception {
		this.firstLineRespStr = firstLineRespStr;

		firstLineRespStr = firstLineRespStr.trim().replace('\t', ' ');
		int pos = firstLineRespStr.indexOf(' ');
		if (pos == -1)
			protocol = firstLineRespStr;
		else
			protocol = firstLineRespStr.substring(0, pos);
		if (!ParseDumpUtils.HTTP_1_1.equals(protocol))
			throw new Exception("Bad protocol");
		if (pos != -1) {
			int pos2 = firstLineRespStr.indexOf(' ', pos + 1);
			if (pos2 == -1)
				pos2 = firstLineRespStr.length();
			try {
				String statusStr = firstLineRespStr.substring(pos + 1, pos2);
				if (ParseDumpUtils.isZeroStatus(statusStr))
					status = 0;
				else
					status = Integer.parseInt(statusStr);
			} catch (Exception e) {
				throw new Exception("Bad HTTP status");
			}
			if (pos2 != firstLineRespStr.length())
				message = firstLineRespStr.substring(pos2 + 1).strip();
			if (message != null && message.isEmpty())
				message = null;
		}
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		// E.g.: "HTTP/1.1 200 OK"
		return firstLineRespStr;
	}
}
