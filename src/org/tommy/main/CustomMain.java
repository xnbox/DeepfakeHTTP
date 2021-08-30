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

package org.tommy.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.deepfake_http.common.FirstLineReq;
import org.deepfake_http.common.ReqResp;
import org.deepfake_http.common.utils.ParseCommandLineUtils;
import org.deepfake_http.common.utils.ParseDumpUtils;

public class CustomMain {
	private static final String ARGS_HELP_OPTION = "--help";
	private static final String ARGS_INFO        = "--info";
	private static final String ARGS_REQUESTS    = "--requests";

	/**
	 * Custom main method (called for emmbedded web apps)
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		boolean help     = false;
		boolean info     = false;
		boolean requests = false;
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(ARGS_HELP_OPTION)) {
				help = true;
				break;
			} else if (args[i].equals(ARGS_INFO)) {
				info = true;
				break;
			} else if (args[i].equals(ARGS_REQUESTS)) {
				requests = true;
				break;
			}

		if (help) {
			StringBuilder sb = new StringBuilder();
			sb.append("\n");
			sb.append(" 🟩 DeepfakeHTTP Web Server " + System.getProperty("build.version") + '\n');
			sb.append("\n");
			sb.append(" Usage:\n");
			sb.append("\n");
			sb.append(" java -jar df.jar [options] [dump1.txt] [dump2.txt] ...\n");
			sb.append("\n");
			sb.append(" Options:\n");
			sb.append("          --help          print help message\n");
			sb.append("          --info          print dump(s) statistics as JSON\n");
			sb.append("          --requests      print dump(s) requests as JSON\n");
			sb.append("          --port          TCP port number, default: 8080\n");
			sb.append("          --no-watch      disable watch dump(s) for changes\n");
			sb.append("          --no-etag       disable ETag optimization\n");
			System.out.println(sb);
			System.exit(0);
		} else if (info) {
			List<String /* dump file */> dumps       = new ArrayList<>();
			boolean[]                    noListenArr = new boolean[1];
			boolean[]                    noEtagArr   = new boolean[1];

			try {
				ParseCommandLineUtils.parseCommandLineArgs(null, args, dumps, noListenArr, noEtagArr);
				StringBuilder sb = new StringBuilder();
				sb.append('[');
				boolean first = true;
				for (String dumpFile : dumps) {
					List<ReqResp> dumpReqResp = ParseCommandLineUtils.getDumpReqResp(dumpFile);
					if (first)
						first = false;
					else
						sb.append(',');
					sb.append('{');
					sb.append("\"dumpFile\":\"" + dumpFile + "\",");
					sb.append("\"requestCount\":" + dumpReqResp.size());
					sb.append('}');
				}
				sb.append(']');
				System.out.println(sb);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			System.exit(0);
		} else if (requests) {
			List<String /* dump file */ > dumps       = new ArrayList<>();
			boolean[]                     noListenArr = new boolean[1];
			boolean[]                     noEtagArr   = new boolean[1];

			try {
				ParseCommandLineUtils.parseCommandLineArgs(null, args, dumps, noListenArr, noEtagArr);
				List<ReqResp> allReqResps = ParseCommandLineUtils.getAllReqResp(null, dumps);

				StringBuilder sb = new StringBuilder();
				sb.append('[');
				boolean first = true;
				for (ReqResp reqResp : allReqResps) {
					if (first)
						first = false;
					else
						sb.append(',');
					sb.append('{');
					FirstLineReq firstLineReq = ParseDumpUtils.parseFirstLineReq(reqResp.request.firstLine);
					sb.append("\"method\":\"" + firstLineReq.method.toUpperCase(Locale.ENGLISH) + "\",");
					sb.append("\"requestURI\":\"" + firstLineReq.uri + "\",");
					sb.append("\"dumpFile\":\"" + reqResp.dumpFile + "\",");
					sb.append("\"dumpFileLineNumber\":" + reqResp.request.lineNumber);
					sb.append('}');
				}
				sb.append(']');
				System.out.println(sb);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
	}
}
