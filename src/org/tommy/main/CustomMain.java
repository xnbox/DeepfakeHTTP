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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.deepfake_http.common.FirstLineReq;
import org.deepfake_http.common.ReqResp;
import org.deepfake_http.common.utils.JacksonUtils;
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
			sb.append("          --no-etag       disable ETag optimization\n");
			sb.append("          --no-watch      disable watch dump(s) for changes\n");
			System.out.println(sb);
			System.exit(0);
		} else if (info) {
			List<String /* dump file */> dumps       = new ArrayList<>();
			boolean[]                    noListenArr = new boolean[1];
			boolean[]                    noEtagArr   = new boolean[1];

			try {
				ParseCommandLineUtils.parseCommandLineArgs(null, args, dumps, noListenArr, noEtagArr);

				String json = serializeInfoToJson(dumps);
				System.out.println(json);
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

				String json = serializeRequestsToJson(allReqResps);
				System.out.println(json);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
	}

	/**
	 * Serialize info to JSON
	 *
	 * @param dumps
	 * @return
	 * @throws Throwable
	 */
	private static String serializeInfoToJson(List<String> dumps) throws Throwable {
		List<Map<String, Object>> list = new ArrayList<>(dumps.size());
		for (String dumpFile : dumps) {
			List<ReqResp>       dumpReqResp = ParseCommandLineUtils.getDumpReqResp(dumpFile);
			Map<String, Object> map         = new LinkedHashMap<>();
			map.put("dumpFile", dumpFile);
			map.put("requestCount", dumpReqResp.size());
			list.add(map);
		}
		return JacksonUtils.stringifyToJson(list);
	}

	/**
	 * Serialize requests to JSON 
	 * 
	 * @param allReqResps
	 * @return
	 * @throws Exception
	 */
	private static String serializeRequestsToJson(List<ReqResp> allReqResps) throws Throwable {
		List<Map<String, Object>> list = new ArrayList<>(allReqResps.size());
		for (ReqResp reqResp : allReqResps) {
			Map<String, Object> map          = new LinkedHashMap<>();
			FirstLineReq        firstLineReq = ParseDumpUtils.parseFirstLineReq(reqResp.request.firstLine);
			map.put("method", firstLineReq.method.toUpperCase(Locale.ENGLISH));
			map.put("requestURI", firstLineReq.uri);
			map.put("dumpFile", reqResp.dumpFile);
			map.put("dumpFileLineNumber", reqResp.request.lineNumber);
			list.add(map);
		}
		return JacksonUtils.stringifyToJson(list);
	}
}
