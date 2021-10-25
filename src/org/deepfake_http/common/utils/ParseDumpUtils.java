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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.deepfake_http.common.HttpMethod;
import org.deepfake_http.common.ReqResp;
import org.deepfake_http.thirdparty.RemoveCommentsUtils;

public class ParseDumpUtils {
	public static final String  USE_SERVER         = "use server";
	public static final String  HTTP_1_1           = "HTTP/1.1";
	private static final char   COMMENT_CHAR       = '#';
	private static final String END_OF_BODY_MARKER = ".";

	/**
	 * MIN_REQUEST_LENGTH
	 *
	 * GET / HTTP/1.1
	 */
	private static final int MIN_REQUEST_LENGTH = 3 + 1 + 1 + 1 + HTTP_1_1.length();

	/* PARSERS */

	/**
	 * Parse dump
	 *
	 * @param dumpFile
	 * @param text
	 * @return
	 * @throws Throwable
	 */
	public static List<ReqResp> parseDump(String dumpFile, List<String> lines) throws Throwable {
		boolean first = true;

		boolean inRequest  = false;
		boolean inResponse = false;
		boolean inBody     = false;

		List<ReqResp> list = new ArrayList<>();

		ReqResp reqResp       = null;
		int     lineNo        = 0;
		int     requestLineNo = 0;

		for (String line : lines) {
			lineNo++;

			if (!inBody)
				if (line.stripLeading().indexOf(COMMENT_CHAR) == 0)
					continue;

			String   firstLineCandidate    = line.strip().toUpperCase(Locale.ENGLISH).replace('\t', ' ');
			String[] firstLineCandidateArr = firstLineCandidate.split("\s");

			/**
			 * requestMethodOk
			 */
			boolean requestMethodOk;
			try {
				String method = firstLineCandidateArr[0];
				HttpMethod.valueOf(method.toUpperCase(Locale.ENGLISH));
				requestMethodOk = true;
			} catch (Exception e) {
				requestMethodOk = false;
			}

			/**
			 * responseStatusOk
			 */
			boolean responseStatusOk;
			try {
				int status;
				if (firstLineCandidateArr.length == 1)
					status = 0;
				else
					status = Integer.parseInt(firstLineCandidateArr[1]);
				responseStatusOk = status == 0 || (status >= 100 && status <= 599);
			} catch (Exception e) {
				responseStatusOk = false;
			}

			if (firstLineCandidate.length() >= MIN_REQUEST_LENGTH && firstLineCandidate.endsWith(' ' + HTTP_1_1) && requestMethodOk) {
				if (inRequest)
					throw new Exception("Request without response! Line: " + requestLineNo);
				if (first)
					first = false;
				else {
					reqResp.request.body  = trimLastLineBreak(reqResp.request.body);
					reqResp.response.body = trimLastLineBreak(reqResp.response.body);
					list.add(reqResp);
				}

				requestLineNo = lineNo;

				reqResp                    = new ReqResp();
				reqResp.dumpFile           = dumpFile;
				reqResp.request.firstLine  = line.strip();
				reqResp.request.lineNumber = lineNo;

				inBody     = false;
				inRequest  = true;
				inResponse = false;
			} else if (firstLineCandidate.startsWith(HTTP_1_1) && responseStatusOk) {
				if (inResponse)
					throw new Exception("Response without request! Line: " + requestLineNo);

				reqResp.response.firstLine  = line.strip();
				reqResp.response.lineNumber = lineNo;

				inBody     = false;
				inRequest  = false;
				inResponse = true;
			} else {
				if (inBody) {
					if (END_OF_BODY_MARKER.equals(line.stripTrailing())) {
						inBody = false;
						if (inRequest)
							inRequest = false;
						else if (inResponse)
							inResponse = false;
						continue;
					}
					if (inRequest)
						reqResp.request.body += line;
					else if (inResponse)
						reqResp.response.body += line;
				} else {
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
						} else if (END_OF_BODY_MARKER.equals(line.stripTrailing()))
							continue;
						else {
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
		if (reqResp != null)
			list.add(reqResp);

		return list;
	}

	private static String trimLastLineBreak(String s) {
		StringBuilder sb  = new StringBuilder(s);
		int           len = sb.length();
		if (len == 0)
			return s;
		int lastPos = len - 1;
		if (sb.charAt(lastPos) == '\n' || sb.charAt(lastPos) == '\r')
			sb.setLength(lastPos);
		return sb.toString();
	}

	/**
	 * Read text by lines with preservations of all possible combinations of CR and LF
	 *
	 * https://en.wikipedia.org/wiki/Newline
	 *
	 * @param text
	 * @return
	 */
	public static List<String> readLines(String text) {
		List<String>  lines = new ArrayList<>();
		StringBuilder sb    = new StringBuilder();

		boolean wasCr = false;
		boolean wasLf = false;

		for (int i = 0; i < text.length(); i++) {
			if (i == text.length()) {
				lines.add(sb.toString());
				break;
			}
			char c = text.charAt(i);
			if (c == '\n') { // LF
				wasLf = true;
				sb.append((char) c);
				if (wasCr || wasLf) {
					lines.add(sb.toString());
					sb.setLength(0);
				}
			} else if (c == '\r') { // CR
				wasCr = true;
				sb.append((char) c);
				if (wasCr || wasLf) {
					lines.add(sb.toString());
					sb.setLength(0);
				}
			} else {
				if (wasCr || wasLf) {
					if (!sb.isEmpty()) {
						lines.add(sb.toString());
						sb.setLength(0);
					}
				}
				sb.append((char) c);
				wasCr = false;
				wasLf = false;
			}
		}
		if (!sb.isEmpty())
			lines.add(sb.toString());
		return lines;
	}

	public static int getHeadersPartSize(byte[] bs) {
		for (int i = 0; i < bs.length; i++)
			if (i < bs.length - 1 && bs[i] == 10 && bs[i + 1] == 10)
				return i + 1;
		return bs.length - 1;
	}

	public static boolean isHttpResp(Path path) throws IOException {
		try (InputStream is = Files.newInputStream(path)) {
			return isHttpResp(is);
		}
	}

	public static boolean isServerJsResp(Path path) throws IOException {
		String pathStr = path.toString();
		if (!pathStr.endsWith(".js"))
			return false;
		String js = Files.readString(path);
		js = RemoveCommentsUtils.removeComments(js.strip()).stripLeading();
		return js.startsWith("'" + USE_SERVER + "'") || js.startsWith('"' + USE_SERVER + '"');
	}

	private static String extractFirstLine(InputStream is) throws IOException {
		byte[] buf          = new byte[1024];
		is.read(buf);
		int    firstLineLen = getRespFirstLineLength(buf);
		byte[] firstLineBs  = new byte[firstLineLen];
		System.arraycopy(buf, 0, firstLineBs, 0, firstLineLen);
		return new String(firstLineBs, StandardCharsets.UTF_8);
	}

	private static boolean isHttpResp(InputStream is) throws IOException {
		String firstLine = extractFirstLine(is).stripTrailing();
		if (!firstLine.startsWith(HTTP_1_1 + ' '))
			return false;
		int pos = firstLine.indexOf(' ');
		if (pos == -1)
			return false;
		String rest = firstLine.substring(pos + 1);
		int    pos2 = rest.indexOf(' ');
		if (pos2 == -1) {
			pos2 = rest.length();
		}
		String statusStr = rest.substring(0, pos2);
		if (statusStr.length() != 3)
			return false;
		try {
			int status = Integer.parseInt(statusStr);
			if (status < 100 || status > 599) // https://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml
				return false;
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private static int getRespFirstLineLength(byte[] bs) {
		for (int i = 0; i < bs.length; i++) {
			byte b = bs[i];
			if (b == 0 || b == 10 || b == 13)
				return i;
		}
		return bs.length;
	}
}
