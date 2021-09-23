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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deepfake_http.common.ReqResp;
import org.tommy.main.CustomMain;

public class ParseCommandLineUtils {
	/*
	 * Original Tommy command line options
	 */

	//@formatter:off

	/**
	 * print help message
	 */
	private static final String ARGS_HELP_OPTION         = "--help";

	/**
	 * run app from ZIP or WAR archive, directory or URL
	 */
	private static final String ARGS_APP_OPTION          = "--app";

	/**
	 * HTTP TCP port number, default: 8080
	 */
	private static final String ARGS_PORT_OPTION         = "--port";
	/**
	 * HTTPS TCP port number, default: 8443
	 */
	private static final String ARGS_PORT_SSL_OPTION     = "--port-ssl";

	/**
	 * redirect HTTP to HTTPS
	 */
	private static final String ARGS_REDIRECT_OPTION     = "--redirect";

	/**
	 * context path, default: /
	 */
	private static final String ARGS_CONTEXT_PATH_OPTION = "--context-path";

	/**
	 * provide password for encrypted ZIP or WAR archive
	 */
	private static final String ARGS_PASSWORD_OPTION     = "--password";

	//@formatter:on

	/* command line args */
	private static final String ARGS_NO_WATCH      = "--no-watch";      // disable watch dump files for changes
	private static final String ARGS_NO_ETAG       = "--no-etag";       // disable ETag optimization
	private static final String ARGS_NO_LOG        = "--no-log";        // disable request/response console logging
	private static final String ARGS_STRICT_JSON   = "--strict-json";   // disable request/response console logging
	private static final String ARGS_COLLECT       = "--collect";       // collect live request/response dumps to file
	private static final String ARGS_OPENAPI_PATH  = "--openapi-path";  // serve OpenAPI client at specified context path
	private static final String ARGS_OPENAPI_TITLE = "--openapi-title"; // provide custom OpenAPI spec title             
	private static final String ARGS_DATA          = "--data";          // specify json/yaml data file to populate templates

	/**
	 * 
	 * @param dumpFile
	 * @return
	 * @throws Throwable
	 */
	public static List<ReqResp> getDumpReqResp(String dumpFile) throws Throwable {
		String dump = Files.readString(new File(dumpFile).toPath());
		dump = dump.stripLeading();
		List<ReqResp> reqResps;
		if (dump.startsWith("{") || dump.startsWith("---")) {
			Map<String, Object> openApiMap = JacksonUtils.parseJsonYamlToMap(dump);
			reqResps = OpenApiUtils.openApiMapToListReqResps(openApiMap);
		} else {
			List<String> dumpLines = ParseDumpUtils.readLines(dump);
			reqResps = ParseDumpUtils.parseDump(dumpFile, dumpLines);
		}
		return reqResps;
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
			if (logger != null)
				logger.log(Level.INFO, "File: \"{0}\" found {1} entries.", new Object[] { dumpFile, dumpReqResp.size() });
			allReqResps.addAll(dumpReqResp);
			fileCount++;
		}
		if (logger != null)
			logger.log(Level.INFO, "{0} file(s) processed. {1} entries found.", new Object[] { fileCount, allReqResps.size() });
		return allReqResps;
	}

	/**
	 * 
	 * @param logger
	 * @param args
	 * @param dumps
	 * @param noWatchArr
	 * @param noEtagArr
	 * @param noLogArr
	 * @param collectFileArr
	 * @param openApiPathArr
	 * @param openApiTitleArr
	 * @throws Throwable
	 */
	public static void parseCommandLineArgs(Logger logger, //
			String[] args, //
			List<String /* dump file */> dumps, //
			boolean[] noWatchArr, //
			boolean[] noEtagArr, //
			boolean[] noLogArr, //
			boolean[] noColorArr, //
			boolean[] strictJsonArr, //
			String[] collectFileArr, //
			String[] openApiPathArr, //
			String[] openApiTitleArr, //
			String[] dataFileArr //
	) throws Throwable {
		for (int i = 0; i < args.length; i++) {
			/* skip original Tommy options */

			// Tommy options Start
			if (args[i].equals(ARGS_HELP_OPTION))
				i++;
			else if (args[i].equals(ARGS_APP_OPTION))
				i++;
			else if (args[i].equals(ARGS_PORT_OPTION))
				i++;
			else if (args[i].equals(ARGS_PORT_SSL_OPTION))
				i++;
			else if (args[i].equals(ARGS_REDIRECT_OPTION))
				i++;
			else if (args[i].equals(ARGS_CONTEXT_PATH_OPTION))
				i++;
			else if (args[i].equals(ARGS_PASSWORD_OPTION))
				i++;
			// Tommy options End

			else if (args[i].equals(ARGS_NO_WATCH))
				noWatchArr[0] = true;
			else if (args[i].equals(ARGS_NO_ETAG))
				noEtagArr[0] = true;
			else if (args[i].equals(ARGS_NO_LOG))
				noLogArr[0] = true;
			else if (args[i].equals(ARGS_STRICT_JSON))
				strictJsonArr[0] = true;

			else if (args[i].equals(CustomMain.ARGS_NO_COLOR))
				noColorArr[0] = true;
			else if (args[i].equals(ARGS_COLLECT)) {
				if (i < args.length - 1)
					collectFileArr[0] = args[++i];
			} else if (args[i].equals(ARGS_OPENAPI_PATH)) {
				if (i < args.length - 1)
					openApiPathArr[0] = args[++i];
			} else if (args[i].equals(ARGS_OPENAPI_TITLE)) {
				if (i < args.length - 1)
					openApiTitleArr[0] = args[++i];
			} else if (args[i].equals(ARGS_DATA)) {
				if (i < args.length - 1)
					dataFileArr[0] = args[++i];
			} else {
				String fileName = args[i];
				Path   path     = Paths.get(fileName);
				if (Files.exists(path))
					dumps.add(fileName);
				else {
					if (logger != null)
						logger.log(Level.WARNING, "File \"{0}\" does not exists", fileName);
				}
			}
		}
	}
}
