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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deepfake_http.common.FirstLineReq;
import org.deepfake_http.common.ReqResp;
import org.deepfake_http.common.utils.JacksonUtils;
import org.deepfake_http.common.utils.OpenApiUtils;
import org.deepfake_http.common.utils.ParseCommandLineUtils;
import org.deepfake_http.common.utils.ParseDumpUtils;
import org.deepfake_http.common.utils.SystemProperties;
import org.deepfake_http.common.utils.UrlUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomMain {
	/**
	 * Custom main method (called for emmbedded web apps)
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		Logger logger = Logger.getLogger("cli.logger");
		logger.setLevel(Level.ALL);

		Map<String, Object> paramMap = ParseCommandLineUtils.parseCommandLineArgs(logger, args);

		List<String> dumps    = (List<String>) paramMap.get(ParseCommandLineUtils.ARGS_DUMP);
		boolean      help     = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_HELP_OPTION);
		boolean      info     = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_PRINT_INFO);
		boolean      requests = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_PRINT_REQUESTS);
		boolean      openapi  = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_PRINT_OPENAPI);
		boolean      noPretty = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_PRETTY);
		boolean      noColor  = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_COLOR);
		String       format   = (String) paramMap.get(ParseCommandLineUtils.ARGS_FORMAT);

		if (help || args.length == 0)
			doHelp();
		else if (info) {
			try {
				String json = serializeInfoToJson(dumps, format, !noPretty, !noColor);
				System.out.println(json);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			System.exit(0);
		} else if (requests) {
			try {
				List<ReqResp> allReqResps = getAllReqResp(logger, dumps);

				String json = serializeRequestsToJson(allReqResps, format, !noPretty, !noColor);
				System.out.println(json);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			System.exit(0);
		} else if (openapi) {
			try {
				List<ReqResp> allReqResps = getAllReqResp(logger, dumps);

				String              openApiTitle = (String) paramMap.get(ParseCommandLineUtils.ARGS_OPENAPI_TITLE);
				Map<String, Object> openApiMap   = OpenApiUtils.createOpenApiMap(allReqResps, openApiTitle);
				String              json         = JacksonUtils.stringifyToJsonYaml(openApiMap, format, !noPretty, !noColor);
				System.out.println(json);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			System.exit(0);
		}
	}

	private static void doHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append("DeepfakeHTTP Web Server " + System.getProperty("build.version") + ". Build: " + System.getProperty("build.timestamp") + '\n');
		sb.append("\n");
		sb.append(" OS: " + SystemProperties.OS_NAME + " (" + SystemProperties.OS_ARCH + ")" + '\n');
		sb.append("JVM: " + SystemProperties.JAVA_JAVA_VM_NAME + " (" + SystemProperties.JAVA_JAVA_VERSION + ")\n");
		sb.append("                                                                               \n");
		sb.append("Usage:                                                                         \n");
		sb.append("                                                                               \n");
		sb.append("java -jar df.jar [OPTIONS] [FLAGS] [COMMANDS]                                  \n");
		sb.append("                                                                               \n");
		sb.append("OPTIONS:                                                                       \n");
		sb.append("   --host <host>            host name, default: localhost                      \n");
		sb.append("   --port <number>          HTTP TCP port number, default: 8080                \n");
		sb.append("   --port-ssl <number>      HTTPS TCP port number, default: 8443               \n");
		sb.append("   --dump <file|url>...     dump text file(s)/URL(s)                           \n");
		sb.append("   --db <file|url>          json/yaml/csv memory file to populate templates    \n");
		sb.append("   --db-export <file>       export memory to json file                         \n");
		sb.append("   --db-path <path>         serve live memory file at specified context        \n");
		sb.append("   --dir <dir>              forward unmatched requests to specified directory  \n");
		sb.append("   --js <file|url>...       JavaScript file(s) for script engine context       \n");
		sb.append("   --openapi-path <path>    serve built-in OpenAPI client at specified context \n");
		sb.append("   --openapi-title <text>   provide custom OpenAPI specification title         \n");
		sb.append("   --collect <file>         collect live request/response to file              \n");
		sb.append("   --format <json|yaml>     output format for --print-* commands, default: json\n");
		sb.append("   --status <number>        status code for non-matching requests, default: 404\n");
		sb.append("   --max-log-body <number>  max body bytes in console log, default: unlimited  \n");
		sb.append("                                                                               \n");
		sb.append("FLAGS:                                                                         \n");
		sb.append("   --no-log                 disable request/response console logging           \n");
		sb.append("   --no-log-request-info    disable request info in console logging            \n");
		sb.append("   --no-log-headers         disable request/response headers in console logging\n");
		sb.append("   --no-log-body            disable request/response body in console logging   \n");
		sb.append("   --no-cors                disable CORS headers                               \n");
		sb.append("   --no-etag                disable 'ETag' header                              \n");
		sb.append("   --no-server              disable 'Server' header                            \n");
		sb.append("   --no-watch               disable watch files for changes                    \n");
		sb.append("   --no-color               disable ANSI color output for --print-* commands   \n");
		sb.append("   --no-pretty              disable prettyprint for --print-* commands         \n");
		sb.append("   --no-template            disable template processing                        \n");
		sb.append("   --no-wildcard            disable wildcard processing                        \n");
		sb.append("   --no-bak                 disable backup old memory file before overwrite    \n");
		sb.append("   --strict-json            enable strict JSON comparison                      \n");
		sb.append("   --redirect               enable redirect HTTP to HTTPS                      \n");
		sb.append("   --db-export-on-exit      export memory only on server close event           \n");
		sb.append("                                                                               \n");
		sb.append("COMMANDS:                                                                      \n");
		sb.append("   --help                   print help message                                 \n");
		sb.append("   --print-info             print dump files statistics to stdout as json/yaml \n");
		sb.append("   --print-requests         print dump requests to stdout as json/yaml         \n");
		sb.append("   --print-openapi          print OpenAPI specification to stdout as json/yaml \n");

		System.out.println(sb);
		System.exit(0);
	}

	/**
	 * 
	 * @param logger
	 * @param dumps
	 * @return
	 * @throws Throwable
	 */
	public static List<ReqResp> getAllReqResp(Logger logger, List<String /* dump file */> dumps) throws Throwable {
		List<ReqResp> allReqResps = new ArrayList<>();
		int           fileCount   = 0;
		for (String dumpFile : dumps) {
			List<ReqResp> dumpReqResp = getDumpReqResp(dumpFile);
			logger.log(Level.INFO, "File: \"{0}\" found {1} entries.", new Object[] { dumpFile, dumpReqResp.size() });
			allReqResps.addAll(dumpReqResp);
			fileCount++;
		}
		logger.log(Level.INFO, "{0} file(s) processed. {1} entries found.", new Object[] { fileCount, allReqResps.size() });
		return allReqResps;
	}

	/**
	 * 
	 * @param dumpFile
	 * @return
	 * @throws Throwable
	 */
	private static List<ReqResp> getDumpReqResp(String dumpFile) throws Throwable {
		String dump = UrlUtils.fileOrUrlToText(dumpFile);
		dump = dump.stripLeading();
		List<ReqResp> reqResps;
		if (dump.startsWith("{") || dump.startsWith("---")) {
			JsonNode            openApiJsonNode = JacksonUtils.parseJsonYamlToMap(dump);
			Map<String, Object> openApiMap      = (Map<String, Object>) new ObjectMapper().treeToValue(openApiJsonNode, Map.class);
			reqResps = OpenApiUtils.openApiMapToListReqResps(openApiMap);
		} else {
			List<String> dumpLines = ParseDumpUtils.readLines(dump);
			reqResps = ParseDumpUtils.parseDump(dumpFile, dumpLines);
		}
		return reqResps;
	}

	/**
	 * Serialize info to JSON
	 *
	 * @param dumps
	 * @return
	 * @throws Throwable
	 */
	private static String serializeInfoToJson(List<String> dumps, String format, boolean prettyprint, boolean color) throws Throwable {
		List<Map<String, Object>> list = new ArrayList<>(dumps.size());
		for (String dumpFile : dumps) {
			List<ReqResp>       dumpReqResp = getDumpReqResp(dumpFile);
			Map<String, Object> map         = new LinkedHashMap<>();
			map.put("dumpFile", dumpFile);
			map.put("requestCount", dumpReqResp.size());
			list.add(map);
		}
		return JacksonUtils.stringifyToJsonYaml(list, format, prettyprint, color);
	}

	/**
	 * Serialize requests to JSON 
	 * 
	 * @param allReqResps
	 * @return
	 * @throws Exception
	 */
	private static String serializeRequestsToJson(List<ReqResp> allReqResps, String format, boolean prettyprint, boolean color) throws Throwable {
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
		return JacksonUtils.stringifyToJsonYaml(list, format, prettyprint, color);
	}

}
