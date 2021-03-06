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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletResponse;

public class ParseCommandLineUtils {
	/**
	 * https://no-color.org
	 */
	private static final String NO_COLOR = System.getenv("NO_COLOR");

	/*
	 * Original Tommy command line options
	 */

	//@formatter:off

	/**
	 * print help message
	 */
	public  static final String ARGS_HELP_OPTION         = "--help";

	/**
	 * run app from ZIP or WAR archive, directory or URL
	 */
	private static final String ARGS_APP_OPTION          = "--app";

	/**
	 * host name, default: localhost
	 */
	private static final String ARGS_HOST_OPTION         = "--host";

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
	public static final String ARGS_DUMP                = "--dump";                // dump text file(s)/URL(s)
	public static final String ARGS_DB                  = "--db";                  // json/yaml/csv memory file to populate templates
	public static final String ARGS_DB_EXPORT           = "--db-export";           // export memory to json file
	public static final String ARGS_DB_PATH             = "--db-path";             // serve live memory file at specified context
	public static final String ARGS_DIR                 = "--dir";                 // forward unmatched requests to specified directory
	public static final String ARGS_JS                  = "--js";                  // JavaScript file(s) for script engine context
	public static final String ARGS_NO_BAK              = "--no-bak";              // disable backup old memory file before overwrite
	public static final String ARGS_NO_WATCH            = "--no-watch";            // disable watch dump files for changes
	public static final String ARGS_NO_ETAG             = "--no-etag";             // disable ETag optimization
	public static final String ARGS_NO_LOG              = "--no-log";              // disable request/response console logging
	public static final String ARGS_NO_CORS             = "--no-cors";             // disable CORS headers
	public static final String ARGS_NO_POWERED_BY       = "--no-powered-by";       // disable 'X-Powered-By' header
	public static final String ARGS_STRICT_JSON         = "--strict-json";         // enable strict JSON comparison
	public static final String ARGS_COLLECT             = "--collect";             // collect live request/response dumps to file
	public static final String ARGS_OPENAPI_PATH        = "--openapi-path";        // serve OpenAPI client at specified context path
	public static final String ARGS_OPENAPI_TITLE       = "--openapi-title";       // provide custom OpenAPI spec title             
	public static final String ARGS_PRINT_INFO          = "--print-info";          // print dump files statistics to stdout as json/yaml
	public static final String ARGS_PRINT_REQUESTS      = "--print-requests";      // print dump requests to stdout as json/yaml
	public static final String ARGS_PRINT_OPENAPI       = "--print-openapi";       // print OpenAPI specification to stdout as json/yaml
	public static final String ARGS_FORMAT              = "--format";              // output format for --print-* commands, default: json
	public static final String ARGS_STATUS              = "--status";              // status code for non-matching requests, default: 404
	public static final String ARGS_NO_COLOR            = "--no-color";            // disable ANSI color output for --print-* commands
	public static final String ARGS_NO_PRETTY           = "--no-pretty";           // disable prettyprint for --print-* commands
	public static final String ARGS_NO_TEMPLATE         = "--no-template";         // disable template processing
	public static final String ARGS_NO_WILDCARD         = "--no-wildcard";         // disable wildcard processing
	public static final String ARGS_MAX_LOG_BODY        = "--max-log-body";        // max body bytes in console log, default: unlimited
	public static final String ARGS_NO_LOG_HEADERS      = "--no-log-headers";      // disable request/response headers in console logging
	public static final String ARGS_NO_LOG_REQUEST_INFO = "--no-log-request-info"; // disable request info in console logging
	public static final String ARGS_NO_LOG_BODY         = "--no-log-body";         // disable request/response body in console logging
	public static final String ARGS_EXPORT_ON_EXIT      = "--db-export-on-exit";   // export memory only on server close event

	/**
	 * 
	 * @param logger
	 * @param args
	 * @param dumps
	 * @return
	 * @throws Throwable
	 */
	public static Map<String, Object> parseCommandLineArgs(Logger logger, String[] args) {
		Map<String, Object> paramMap = new HashMap<>();

		/* CLI options defaults */
		paramMap.put(ARGS_DUMP, new ArrayList<String>());
		paramMap.put(ARGS_JS, new ArrayList<String>());
		paramMap.put(ARGS_DB, null);
		paramMap.put(ARGS_DB_EXPORT, null);
		paramMap.put(ARGS_DB_PATH, null);
		paramMap.put(ARGS_DIR, null);
		paramMap.put(ARGS_NO_BAK, false);
		paramMap.put(ARGS_HELP_OPTION, false);
		paramMap.put(ARGS_PRINT_INFO, false);
		paramMap.put(ARGS_PRINT_REQUESTS, false);
		paramMap.put(ARGS_PRINT_OPENAPI, false);
		paramMap.put(ARGS_NO_COLOR, NO_COLOR != null);
		paramMap.put(ARGS_NO_WATCH, false);
		paramMap.put(ARGS_NO_ETAG, false);
		paramMap.put(ARGS_NO_LOG, false);
		paramMap.put(ARGS_NO_CORS, false);
		paramMap.put(ARGS_NO_POWERED_BY, false);
		paramMap.put(ARGS_STRICT_JSON, false);
		paramMap.put(ARGS_NO_COLOR, false);
		paramMap.put(ARGS_NO_PRETTY, false);
		paramMap.put(ARGS_NO_TEMPLATE, false);
		paramMap.put(ARGS_NO_WILDCARD, false);
		paramMap.put(ARGS_COLLECT, null);
		paramMap.put(ARGS_OPENAPI_PATH, null);
		paramMap.put(ARGS_OPENAPI_TITLE, "");
		paramMap.put(ARGS_FORMAT, "json");
		paramMap.put(ARGS_STATUS, HttpServletResponse.SC_NOT_FOUND); // 404
		paramMap.put(ARGS_MAX_LOG_BODY, Integer.MAX_VALUE); // unlimited
		paramMap.put(ARGS_NO_LOG_REQUEST_INFO, false);
		paramMap.put(ARGS_NO_LOG_HEADERS, false);
		paramMap.put(ARGS_NO_LOG_BODY, false);
		paramMap.put(ARGS_EXPORT_ON_EXIT, false);

		for (int i = 0; i < args.length; i++) {
			/* skip original Tommy options */

			// Tommy options Start
			if (args[i].equals(ARGS_HELP_OPTION))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_APP_OPTION))
				i++;
			else if (args[i].equals(ARGS_HOST_OPTION))
				i++;
			else if (args[i].equals(ARGS_PORT_OPTION))
				i++;
			else if (args[i].equals(ARGS_PORT_SSL_OPTION))
				i++;
			else if (args[i].equals(ARGS_REDIRECT_OPTION))
				;
			else if (args[i].equals(ARGS_CONTEXT_PATH_OPTION))
				i++;
			else if (args[i].equals(ARGS_PASSWORD_OPTION))
				i++;
			// Tommy options End

			else if (args[i].equals(ARGS_NO_WATCH))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_NO_ETAG))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_NO_LOG))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_NO_LOG_REQUEST_INFO))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_NO_LOG_HEADERS))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_NO_LOG_BODY))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_EXPORT_ON_EXIT))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_NO_CORS))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_NO_POWERED_BY))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_STRICT_JSON))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_NO_COLOR))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_NO_BAK))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_COLLECT)) {
				if (i < args.length - 1)
					paramMap.put(args[i], args[++i]);
			} else if (args[i].equals(ARGS_OPENAPI_PATH)) {
				if (i < args.length - 1)
					paramMap.put(args[i], args[++i]);
			} else if (args[i].equals(ARGS_OPENAPI_TITLE)) {
				if (i < args.length - 1)
					paramMap.put(args[i], args[++i]);
			} else if (args[i].equals(ARGS_DB_EXPORT)) {
				if (i < args.length - 1)
					paramMap.put(args[i], args[++i]);
			} else if (args[i].equals(ARGS_DB)) {
				if (i < args.length - 1)
					paramMap.put(args[i], args[++i]);
			} else if (args[i].equals(ARGS_DB_PATH)) {
				if (i < args.length - 1)
					paramMap.put(args[i], args[++i]);
			} else if (args[i].equals(ARGS_DIR)) {
				if (i < args.length - 1)
					paramMap.put(args[i], args[++i]);
			} else if (args[i].equals(ARGS_DUMP)) {
				if (i < args.length - 1) {
					List<String> files = (List<String>) paramMap.get(args[i]);
					while (true) {
						i++;
						if (args[i].startsWith("--")) {
							i--;
							i--;
							break;
						}
						files.add(args[i]);
						if (i == args.length - 1)
							break;
					}
				}
			} else if (args[i].equals(ARGS_JS)) {
				if (i < args.length - 1) {
					List<String> files = (List<String>) paramMap.get(args[i]);
					while (true) {
						i++;
						if (args[i].startsWith("--")) {
							i--;
							i--;
							break;
						}
						files.add(args[i]);
						if (i == args.length - 1)
							break;
					}
				}
			} else if (args[i].equals(ARGS_PRINT_INFO))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_PRINT_REQUESTS))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_PRINT_OPENAPI))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_NO_PRETTY))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_NO_TEMPLATE))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_NO_WILDCARD))
				paramMap.put(args[i], true);
			else if (args[i].equals(ARGS_FORMAT)) {
				if (i < args.length - 1)
					paramMap.put(args[i], args[++i].toLowerCase(Locale.ENGLISH));
			} else if (args[i].equals(ARGS_STATUS)) {
				if (i < args.length - 1)
					paramMap.put(args[i], Integer.parseInt(args[++i]));
			} else if (args[i].equals(ARGS_MAX_LOG_BODY)) {
				if (i < args.length - 1)
					paramMap.put(args[i], Integer.parseInt(args[++i]));
			} else {
				String fileName = args[i];
				if (fileName.startsWith("--"))
					if (logger != null)
						logger.log(Level.WARNING, "Unknown option: \"{0}\". Ignored.", fileName);
			}
		}
		return paramMap;
	}
}
