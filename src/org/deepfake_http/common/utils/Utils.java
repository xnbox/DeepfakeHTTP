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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.deepfake_http.common.FirstLineReq;
import org.deepfake_http.common.FirstLineResp;
import org.deepfake_http.common.Header;
import org.deepfake_http.common.HttpMethods;
import org.deepfake_http.common.ReqResp;

public class Utils {
	private static final String HTTP_1_1     = "HTTP/1.1";
	private static final char   COMMENT_CHAR = '#';

	/**
	 * MIN_REQUEST_LENGTH
	 *
	 * GET / HTTP/1.1
	 */
	private static final int MIN_REQUEST_LENGTH = 3 + 1 + 1 + 1 + HTTP_1_1.length();

	/**
	 * MIN_RESPONSE_LENGTH
	 *
	 * HTTP/1.1 200
	 */
	private static final int MIN_RESPONSE_LENGTH = HTTP_1_1.length() + 1 + 3;

	/* PARSERS */

	/**
	 * Parse dump
	 *
	 * @param text
	 * @return
	 * @throws Throwable
	 */
	public static List<ReqResp> parseDump(String text) throws Throwable {
		boolean first = true;

		boolean inRequest  = false;
		boolean inResponse = false;
		boolean inBody     = false;

		List<ReqResp> map = new ArrayList<>();

		ReqResp reqResp       = null;
		int     lineNo        = 0;
		int     requestLineNo = 0;

		try (BufferedReader br = new BufferedReader(new StringReader(text))) {
			for (String line; (line = br.readLine()) != null;) {
				lineNo++;

				if (line.stripLeading().indexOf(COMMENT_CHAR) == 0)
					continue;

				String   firstLineCandidate    = line.trim().toUpperCase(Locale.ENGLISH).strip().replace('\t', ' ');
				String[] firstLineCandidateArr = firstLineCandidate.split("\s");

				/**
				 * requestMethodOk
				 */
				boolean requestMethodOk;
				try {
					String method = firstLineCandidateArr[0];
					HttpMethods.valueOf(method.toUpperCase(Locale.ENGLISH));
					requestMethodOk = true;
				} catch (Exception e) {
					requestMethodOk = false;
				}

				/**
				 * responseStatusOk
				 */
				boolean responseStatusOk;
				try {
					String statusStr = firstLineCandidateArr[1];
					int    status    = Integer.parseInt(statusStr);
					responseStatusOk = status >= 100 && status <= 599;
				} catch (Exception e) {
					responseStatusOk = false;
				}

				if (firstLineCandidate.length() >= MIN_REQUEST_LENGTH && firstLineCandidate.endsWith(' ' + HTTP_1_1) && requestMethodOk) {
					if (inRequest)
						throw new Exception("Request without response! Line: " + requestLineNo);
					if (first)
						first = false;
					else
						map.add(reqResp);
					requestLineNo = lineNo;

					reqResp                   = new ReqResp();
					reqResp.request.firstLine = line;

					inBody     = false;
					inRequest  = true;
					inResponse = false;
				} else if (firstLineCandidate.length() >= MIN_RESPONSE_LENGTH && firstLineCandidate.startsWith(HTTP_1_1 + ' ') && responseStatusOk) {
					if (inResponse)
						throw new Exception("Response without request! Line: " + requestLineNo);

					reqResp.response.firstLine = line;

					inBody     = false;
					inRequest  = false;
					inResponse = true;
				} else {
					if (inBody) {
						if (inRequest)
							reqResp.request.body.append(line + '\n');
						else if (inResponse)
							reqResp.response.body.append(line + '\n');
					} else {
						if (!inBody)
							if (line.strip().isEmpty())
								inBody = true;
							else {
								if (line.startsWith(" ") || line.startsWith("\t")) {
									int lastElIndex;
									if (inRequest) {
										lastElIndex = reqResp.request.headers.size() - 1;
										String lastHeader = reqResp.request.headers.get(lastElIndex);
										reqResp.request.headers.set(lastElIndex, lastHeader + line.strip());
									} else if (inResponse) {
										lastElIndex = reqResp.response.headers.size() - 1;
										String lastHeader = reqResp.response.headers.get(lastElIndex);
										reqResp.response.headers.set(lastElIndex, lastHeader + line.strip());
									}
								} else {
									if (inRequest)
										reqResp.request.headers.add(line.strip());
									else if (inResponse)
										reqResp.response.headers.add(line.strip());
								}
							}
					}
				}
			}
			if (inRequest)
				throw new Exception("Request without response! Line: " + requestLineNo);
			map.add(reqResp);
		}

		return map;
	}

	public static FirstLineReq parseFirstLineReq(String firstLine) throws Exception {
		firstLine = firstLine.trim();
		FirstLineReq firstLineReq = new FirstLineReq();
		String[]     arr          = firstLine.split("\s");
		firstLineReq.method = arr[0];
		try {
			HttpMethods.valueOf(firstLineReq.method.toUpperCase(Locale.ENGLISH));
		} catch (Exception e) {
			throw new Exception("Bad HTTP method");
		}
		firstLineReq.path = arr[1].trim();
		if (firstLineReq.path.isEmpty())
			throw new Exception("Empry path");
		firstLineReq.protocol = arr[2];
		if (!HTTP_1_1.equals(firstLineReq.protocol))
			throw new Exception("Bad protocol");
		return firstLineReq;
	}

	public static FirstLineResp parseFirstLineResp(String firstLine) throws Exception {
		firstLine = firstLine.trim().replace('\t', ' ');
		FirstLineResp firstLineResp = new FirstLineResp();
		String[]      arr           = firstLine.split("\s");
		firstLineResp.protocol = arr[0].strip();
		if (!HTTP_1_1.equals(firstLineResp.protocol))
			throw new Exception("Bad protocol");
		try {
			firstLineResp.status = Integer.parseInt(arr[1].strip());
		} catch (Exception e) {
			throw new Exception("Bad HTTP status");
		}
		if (arr.length > 2)
			firstLineResp.message = arr[2].strip();
		return firstLineResp;
	}

	public static Header parseHeader(String line) throws Exception {
		Header header = new Header();
		int    pos    = line.indexOf(':');
		if (pos == -1)
			throw new Exception("Invalid header (missing ':')!");
		header.name = line.substring(0, pos).trim();
		if (header.name.isEmpty())
			throw new Exception("Invalid header (missing header name)!");
		header.value = line.substring(pos + 1).trim();
		if (header.value.isEmpty())
			throw new Exception("Invalid header (missing header value)!");
		return header;
	}

	/**
	 * Parse data URL
	 *
	 * @param url
	 * @param mediatypeSb
	 * @param encodingSb
	 * @return
	 * @throws IOException
	 */
	public static byte[] parseDataUrl(String url, StringBuilder mediatypeSb, StringBuilder encodingSb) throws IOException {
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

	/**
	 * Extract query string from path
	 *
	 * @param path
	 * @return
	 */
	public static String extractQueryStringFromPath(String path) {
		int pos = path.indexOf('?');
		if (pos == -1)
			return "";
		return path.substring(pos + 1);
	}

	/**
	 * Parse query string
	 *
	 * @param q
	 * @param map
	 */
	public static void parseQueryString(String q, Map<String, List<String>> map) {
		final String[] pairs = q.split("&");
		for (String pair : pairs) {
			int          idx  = pair.indexOf('=');
			String       key  = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
			List<String> list = map.get(key);
			if (list == null) {
				list = new ArrayList<>();
				map.put(key, list);
			}
			String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : null;
			list.add(value);
		}
	}
}
