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
import org.deepfake_http.common.utils.OpenApiUtils;
import org.deepfake_http.common.utils.ParseCommandLineUtils;

public class CustomMain {
	private static final String ARGS_HELP_OPTION    = "--help";
	private static final String ARGS_PRINT_INFO     = "--print-info";
	private static final String ARGS_PRINT_REQUESTS = "--print-requests";
	private static final String ARGS_PRINT_OPENAPI  = "--print-openapi";
	private static final String ARGS_FORMAT         = "--format";
	private static final String ARGS_PRETTYPRINT    = "--prettyprint";

	/**
	 * Custom main method (called for emmbedded web apps)
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		boolean help        = false;
		boolean info        = false;
		boolean requests    = false;
		boolean openapi     = false;
		boolean prettyprint = false;
		String  format      = "json";

		for (int i = 0; i < args.length; i++)
			if (args[i].equals(ARGS_HELP_OPTION)) {
				help = true;
				break;
			} else if (args[i].equals(ARGS_PRINT_INFO))
				info = true;
			else if (args[i].equals(ARGS_PRINT_REQUESTS))
				requests = true;
			else if (args[i].equals(ARGS_PRINT_OPENAPI))
				openapi = true;
			else if (args[i].equals(ARGS_PRETTYPRINT))
				prettyprint = true;
			else if (args[i].equals(ARGS_FORMAT)) {
				if (i < args.length - 1)
					format = args[++i].toLowerCase(Locale.ENGLISH);
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
			sb.append("   --help                print help message\n");
			sb.append("   --print-info          print dump files statistics to stdout as JSON/YAML\n");
			sb.append("   --print-requests      print dump requests to stdout as JSON/YAML\n");
			sb.append("   --print-openapi       print OpenAPI specification to stdout as JSON/YAML\n");
			sb.append("   --format <json|yaml>  output format for --print-* options, default: json\n");
			sb.append("   --prettyprint         prettyprint for --print-* options\n");
			sb.append("   --openapi-path <path> serve OpenAPI spec and client at specified context path\n");
			sb.append("   --port                TCP port number, default: 8080\n");
			sb.append("   --collect <file>      append live request/response dumps to file\n");
			sb.append("   --no-log              disable request/response console logging\n");
			sb.append("   --no-etag             disable ETag optimization\n");
			sb.append("   --no-watch            disable watch dump files for changes\n");

			System.out.println(sb);
			System.exit(0);
		} else if (info) {
			List<String /* dump file */> dumps          = new ArrayList<>();
			boolean[]                    noWatchArr     = new boolean[1];
			boolean[]                    noEtagArr      = new boolean[1];
			boolean[]                    noLogArr       = new boolean[1];
			String[]                     collectFileArr = new String[1];
			String[]                     openApiPathArr = new String[1];
			try {
				ParseCommandLineUtils.parseCommandLineArgs(null, args, dumps, noWatchArr, noEtagArr, noLogArr, collectFileArr, openApiPathArr);

				String json = serializeInfoToJson(dumps, format, prettyprint);
				System.out.println(json);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			System.exit(0);
		} else if (requests) {
			List<String /* dump file */ > dumps          = new ArrayList<>();
			boolean[]                     noWatchArr     = new boolean[1];
			boolean[]                     noEtagArr      = new boolean[1];
			boolean[]                     noLogArr       = new boolean[1];
			String[]                      collectFileArr = new String[1];
			String[]                      openApiPathArr = new String[1];

			try {
				ParseCommandLineUtils.parseCommandLineArgs(null, args, dumps, noWatchArr, noEtagArr, noLogArr, collectFileArr, openApiPathArr);
				List<ReqResp> allReqResps = ParseCommandLineUtils.getAllReqResp(null, dumps);

				String json = serializeRequestsToJson(allReqResps, format, prettyprint);
				System.out.println(json);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			System.exit(0);
		} else if (openapi) {
			List<String /* dump file */ > dumps          = new ArrayList<>();
			boolean[]                     noWatchArr     = new boolean[1];
			boolean[]                     noEtagArr      = new boolean[1];
			boolean[]                     noLogArr       = new boolean[1];
			String[]                      collectFileArr = new String[1];
			String[]                      openApiPathArr = new String[1];

			try {
				ParseCommandLineUtils.parseCommandLineArgs(null, args, dumps, noWatchArr, noEtagArr, noLogArr, collectFileArr, openApiPathArr);
				List<ReqResp> allReqResps = ParseCommandLineUtils.getAllReqResp(null, dumps);

				Map<String, Object> openApiMap = OpenApiUtils.createOpenApiMap(allReqResps);
				String              json       = JacksonUtils.stringifyToJsonYaml(openApiMap, format, prettyprint);
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
	private static String serializeInfoToJson(List<String> dumps, String format, boolean prettyprint) throws Throwable {
		List<Map<String, Object>> list = new ArrayList<>(dumps.size());
		for (String dumpFile : dumps) {
			List<ReqResp>       dumpReqResp = ParseCommandLineUtils.getDumpReqResp(dumpFile);
			Map<String, Object> map         = new LinkedHashMap<>();
			map.put("dumpFile", dumpFile);
			map.put("requestCount", dumpReqResp.size());
			list.add(map);
		}
		return JacksonUtils.stringifyToJsonYaml(list, format, prettyprint);
	}

	/**
	 * Serialize requests to JSON 
	 * 
	 * @param allReqResps
	 * @return
	 * @throws Exception
	 */
	private static String serializeRequestsToJson(List<ReqResp> allReqResps, String format, boolean prettyprint) throws Throwable {
		List<Map<String, Object>> list = new ArrayList<>(allReqResps.size());
		for (ReqResp reqResp : allReqResps) {
			Map<String, Object> map          = new LinkedHashMap<>();
			FirstLineReq        firstLineReq = new FirstLineReq(reqResp.request.firstLine);
			map.put("method", firstLineReq.getMethod().toUpperCase(Locale.ENGLISH));
			map.put("requestURI", firstLineReq.getUri());
			map.put("dumpFile", reqResp.dumpFile);
			map.put("dumpFileLineNumber", reqResp.request.lineNumber);
			list.add(map);
		}
		return JacksonUtils.stringifyToJsonYaml(list, format, prettyprint);
	}

}
