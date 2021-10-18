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

package org.deepfake_http.common.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.naming.InitialContext;

import org.apache.hive.common.util.Murmur3;
import org.deepfake_http.common.FirstLineReq;
import org.deepfake_http.common.FirstLineResp;
import org.deepfake_http.common.Header;
import org.deepfake_http.common.ReqResp;
import org.deepfake_http.common.dir_watcher.DirectoryWatcher;
import org.deepfake_http.common.utils.HttpPathUtils;
import org.deepfake_http.common.utils.IAnsi;
import org.deepfake_http.common.utils.IProtocol;
import org.deepfake_http.common.utils.JacksonUtils;
import org.deepfake_http.common.utils.MatchUtils;
import org.deepfake_http.common.utils.OpenApiUtils;
import org.deepfake_http.common.utils.ParseCommandLineUtils;
import org.deepfake_http.common.utils.ParseDumpUtils;
import org.deepfake_http.common.utils.ResourceUtils;
import org.deepfake_http.common.utils.TemplateUtils;
import org.deepfake_http.common.utils.UrlUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.tommy.main.CustomMain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DeepfakeHttpServlet extends HttpServlet {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public static final String  HTTP_HEADER_CONTENT_TYPE   = "Content-Type";
	private static final String HTTP_HEADER_CONNECTION     = "Connection";
	private static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
	private static final String HTTP_HEADER_SERVER         = "Server";        // A name for the server

	private static final String HTTP_HEADER_IF_NONE_MATCH = "If-None-Match";
	private static final String HTTP_HEADER_E_TAG         = "ETag";

	/* internal, not sended with response  */
	private static final String INTERNAL_HTTP_HEADER_X_SERVER_DELAY          = "X-Delay";          // response non-standard
	private static final String INTERNAL_HTTP_HEADER_X_SERVER_CONTENT_SOURCE = "X-Content-Source"; // response non-standard
	private static final String INTERNAL_HTTP_HEADER_X_SERVER_HANDLER_CGI    = "X-Handler-CGI";    // response non-standard
	private static final String INTERNAL_HTTP_HEADER_X_SERVER_HANDLER_XGI    = "X-Handler-XGI";    // response non-standard
	private static final String INTERNAL_HTTP_HEADER_X_SERVER_HANDLER_JS     = "X-Handler-JS";     // response non-standard
	private static final String INTERNAL_HTTP_HEADER_X_SERVER_FORWARD_TO     = "X-Forward-To";     // response non-standard

	public static final String INTERNAL_HTTP_HEADER_X_OPENAPI_SUMMARY     = "X-OpenAPI-Summary";    // request non-standard
	public static final String INTERNAL_HTTP_HEADER_X_OPENAPI_DESCRIPTION = "X-OpenAPI-Description";// request non-standard
	public static final String INTERNAL_HTTP_HEADER_X_OPENAPI_TAGS        = "X-OpenAPI-Tags";       // request non-standard

	private static final String X_SERVER_VALUE = "DeepfakeHTTP " + System.getProperty("build.version") + " (" + System.getProperty("build.timestamp") + ")";

	private byte[] openApiJsonBs;
	private byte[] openApiYamlBs;

	/* CLI flags */
	private boolean noWatch;
	private boolean noEtag;
	private boolean noLog;
	private boolean noLogBody;
	private boolean noLogHeaders;
	private boolean nologRequestInfo;
	private boolean noCors;
	private boolean noPoweredBy;
	private boolean noColor;
	private boolean noTemplate;
	private boolean noWildcard;
	private boolean noBak;
	private boolean strictJson;
	private int     badRequestStatus;
	private int     maxLogBody;

	private String                             collectFile;
	private String                             openApiPath;
	private String                             openApiTitle;
	private String                             dataFile;
	private String                             dbExportFile;
	private String                             dbPath;
	private List<String /* dump file */>       dumps;
	private List<String /* JavaScript file */> jss;

	private Logger logger;

	private Map<Path /* dirPath */, DirectoryWatcher> directoryWatchersMap = new HashMap<>();

	private Object   dataMap;
	private JsonNode dataJsonNode;
	private String   dataJson;

	private List<ReqResp> allReqResps;

	private Path catalinaBase;
	private Path catalinaHome;

	private ScriptableObject scope;

	/**
	 * Called by the servlet container to indicate to a servlet that the 
	 * servlet is being placed into service.
	 * 
	 * The servlet container calls the init
	 * method exactly once after instantiating the servlet.
	 * The init method must complete successfully
	 * before the servlet can receive any requests.
	 * 
	 * 
	 * The servlet container cannot place the servlet into service
	 * if the init method
	 * 
	 * @param servletConfig a servlet configuration object used by a servlet container to pass information to a servlet during initialization.
	 * 
	 * Throws a ServletException
	 * Does not return within a time period defined by the Web server
	 */
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger = Logger.getLogger(getClass().getName());
		logger.log(Level.INFO, "DeepfakeHTTP Logger: HELLO!");
		try {
			InitialContext ctx = new InitialContext();

			/* get custom command-line args */
			String[] args = (String[]) ctx.lookup("java:comp/env/tommy/args");

			catalinaBase = (Path) ctx.lookup("java:comp/env/tommy/catalina_base");
			catalinaHome = (Path) ctx.lookup("java:comp/env/tommy/catalina_home");

			Map<String, Object> paramMap = ParseCommandLineUtils.parseCommandLineArgs(null, args);
			dumps            = (List<String>) paramMap.get(ParseCommandLineUtils.ARGS_DUMP);
			jss              = (List<String>) paramMap.get(ParseCommandLineUtils.ARGS_JS);
			dataFile         = (String) paramMap.get(ParseCommandLineUtils.ARGS_DB);
			dbExportFile     = (String) paramMap.get(ParseCommandLineUtils.ARGS_DB_EXPORT);
			dbPath           = (String) paramMap.get(ParseCommandLineUtils.ARGS_DB_PATH);
			noWatch          = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_WATCH);
			noEtag           = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_ETAG);
			noLog            = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_LOG);
			noLogHeaders     = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_LOG_HEADERS);
			nologRequestInfo = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_LOG_REQUEST_INFO);
			noLogBody        = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_LOG_BODY);
			noCors           = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_CORS);
			noPoweredBy      = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_POWERED_BY);
			noColor          = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_COLOR);
			noTemplate       = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_TEMPLATE);
			noWildcard       = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_WILDCARD);
			noBak            = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_BAK);
			strictJson       = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_STRICT_JSON);
			collectFile      = (String) paramMap.get(ParseCommandLineUtils.ARGS_COLLECT);
			openApiPath      = (String) paramMap.get(ParseCommandLineUtils.ARGS_OPENAPI_PATH);
			openApiTitle     = (String) paramMap.get(ParseCommandLineUtils.ARGS_OPENAPI_TITLE);
			badRequestStatus = (int) paramMap.get(ParseCommandLineUtils.ARGS_STATUS);
			maxLogBody       = (int) paramMap.get(ParseCommandLineUtils.ARGS_MAX_LOG_BODY);

			if (openApiTitle == null)
				openApiTitle = "";

			boolean activateDirWatchers = !noWatch;

			reload(activateDirWatchers);

			logger.log(Level.INFO, "{0} dump file(s) loaded.", dumps.size());
			logger.log(Level.INFO, "{0} JavaScript file(s) loaded.", jss.size());
			logger.log(Level.INFO, "Data file loaded.");

		} catch (Throwable e) {
			e.printStackTrace();
		}

		System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%6$s%n");
		SimpleFormatter formatter = new SimpleFormatter();
		logger.getParent().getHandlers()[0].setFormatter(formatter);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					exportMemoryDataToFile();
					deleteDir(catalinaBase);
					deleteDir(catalinaHome);
				} catch (IOException e) {
					e.printStackTrace();
				}
				logger.log(Level.INFO, "DeepfakeHTTP shutdown");
			}

		});

		logger.log(Level.INFO, "DeepfakeHTTP started.");
	}

	/**
	 * Called by the servlet container to indicate to a servlet that the
	 * servlet is being taken out of service.  This method is
	 * only called once all threads within the servlet's
	 * service method have exited or after a timeout
	 * period has passed. After the servlet container calls this 
	 * method, it will not call the service method again
	 * on this servlet.
	 * 
	 * This method gives the servlet an opportunity 
	 * to clean up any resources that are being held (for example, memory,
	 * file handles, threads) and make sure that any persistent state is
	 * synchronized with the servlet's current state in memory. 
	 */
	@Override
	public void destroy() {
		logger.log(Level.INFO, "DeepfakeHTTP shutdown.");
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (!noCors) {
			/* https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Origin */
			response.addHeader("Access-Control-Allow-Origin", "*");

			/* https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Methods */
			response.addHeader("Access-Control-Allow-Methods", "*");

			/* https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Headers */
			response.addHeader("Access-Control-Allow-Headers", "*");
		}
		if (request.getMethod().equalsIgnoreCase("PATCH"))
			doPatch(request, response);
		else
			super.service(request, response);
	}

	protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doDbRequest(request, response);
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_ACCEPTED); // 202 Accepted. (https://tools.ietf.org/html/rfc7231#section-6.3.3)
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doDbRequest(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doDbRequest(request, response);
	}

	@Override
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doDbRequest(request, response);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doDbRequest(request, response);
	}

	@Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doDbRequest(request, response);
	}

	@Override
	protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doDbRequest(request, response);
	}

	/**
	 * 
	 * @param req
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
	private void doDbRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		/**
		 * https://tomcat.apache.org/tomcat-9.0-doc/api/org/apache/catalina/Globals.html#ASYNC_SUPPORTED_ATTR
		 */
		req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
		AsyncContext asyncContext = req.startAsync(req, res);
		asyncContext.setTimeout(0); // A timeout value of zero or less indicates no timeout. (https://docs.oracle.com/javaee/6/api/javax/servlet/AsyncContext.html#setTimeout(long))
		asyncContext.addListener(new AsyncListener() {

			@Override
			public void onComplete(AsyncEvent ae) {
			}

			@Override
			public void onTimeout(AsyncEvent ae) {
				AsyncContext asyncContext = null;
				try {
					asyncContext = ae.getAsyncContext();
					asyncContext.complete();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(AsyncEvent ae) {
				AsyncContext asyncContext = null;
				try {
					asyncContext = ae.getAsyncContext();
					asyncContext.complete();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onStartAsync(AsyncEvent ae) throws IOException {
			}
		});

		asyncContext.start(new Runnable() {

			private boolean connectionKeepAlive;

			@Override
			public void run() {
				HttpServletRequest  request  = (HttpServletRequest) asyncContext.getRequest();
				HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();

				ReqResp reqResp = null;
				try {
					String method       = request.getMethod().trim().toUpperCase(Locale.ENGLISH);
					String providedPath = request.getServletPath() + request.getPathInfo();

					if (providedPath.equals(openApiPath) || providedPath.startsWith(openApiPath + '/')) {
						providedPath = providedPath.substring(openApiPath.length());
						hostOpenApiUi(asyncContext, response, providedPath);
						return;
					} else if (providedPath.equals(dbPath) || providedPath.startsWith(dbPath + '/')) {
						hostDb(asyncContext, response);
						return;
					}

					String providedQueryString = request.getQueryString();
					if (providedQueryString == null)
						providedQueryString = "";
					String protocol = request.getProtocol();

					int                 port    = req.getServerPort();
					Enumeration<String> headers = req.getHeaderNames();

					String providedFirstLineStr = method + ' ' + providedPath + (providedQueryString.isEmpty() ? "" : "?" + providedQueryString) + ' ' + protocol;

					byte[] providedBodyBs = request.getInputStream().readAllBytes();
					String providedBody   = new String(providedBodyBs, StandardCharsets.UTF_8);

					Map<String, List<String>> providedParams = new LinkedHashMap<>();

					String cgi           = null;
					String xgi           = null;
					String jsFunc        = null;
					String forwardOrigin = null;

					String requestContentSource  = null;
					String responseContentSource = null;

					int requestDelay  = 0;
					int responseDelay = 0;

					boolean jsonRequest = false;

					String requestHeaderContentType = request.getHeader(HTTP_HEADER_CONTENT_TYPE);

					if (requestHeaderContentType != null && requestHeaderContentType.startsWith("application/x-www-form-urlencoded"))
						MatchUtils.parseQuery(providedBody, providedParams);
					if (requestHeaderContentType != null && requestHeaderContentType.startsWith("application/json"))
						jsonRequest = true;

					/* search for request-reponse pair */
					Map<String, List<String>> providedHeaderValuesMap = new LinkedHashMap<>();
					extractRequestHeaders(request, providedHeaderValuesMap);

					for (ReqResp rr : allReqResps) {

						ReqResp crr = cloneReqResp(rr);

						FirstLineReq firstLineReq = new FirstLineReq(crr.request.firstLine);
						boolean      protocolOk   = firstLineReq.getProtocol().equals(ParseDumpUtils.HTTP_1_1);
						boolean      methodOk     = firstLineReq.getMethod().equals(method);
						String       templatePath = HttpPathUtils.extractPathFromUri(firstLineReq.getUri());

						boolean pathOk = MatchUtils.matchPath(templatePath, providedPath, providedParams);

						String templateQueryString = HttpPathUtils.extractQueryStringFromUri(firstLineReq.getUri());

						boolean queryStringOk = MatchUtils.matchQuery(!noWildcard, templateQueryString, providedQueryString, providedParams);
						if ( //
						protocolOk && //
						methodOk && //
						pathOk && //
						queryStringOk //
						) {
							/* Before we can use headers information, we need process header templates */
							Map<String, Object> tmpDataMap = new LinkedHashMap<>();
							Map<String, Object> requestMap = new LinkedHashMap<>();
							requestMap.put("parameters", providedParams);
							requestMap.put("method", method);
							requestMap.put("path", providedPath);
							requestMap.put("query", providedQueryString);
							requestMap.put("headers", providedHeaderValuesMap);
							requestMap.put("body", providedBody);
							tmpDataMap.put("request", requestMap);
							tmpDataMap.put("data", dataMap);

							processRespHeaders(!noTemplate, crr, tmpDataMap);

							for (String headerStr : crr.request.headers) {
								Header header              = new Header(headerStr);
								String lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);
								if (INTERNAL_HTTP_HEADER_X_SERVER_DELAY.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									requestDelay = Integer.parseInt(header.value);
								else if (INTERNAL_HTTP_HEADER_X_SERVER_CONTENT_SOURCE.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									requestContentSource = header.value;
							}

							for (String headerStr : crr.response.headers) {
								Header header              = new Header(headerStr);
								String lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);
								if (INTERNAL_HTTP_HEADER_X_SERVER_DELAY.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									responseDelay = Integer.parseInt(header.value);
								else if (INTERNAL_HTTP_HEADER_X_SERVER_CONTENT_SOURCE.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									responseContentSource = header.value;
								else if (INTERNAL_HTTP_HEADER_X_SERVER_HANDLER_CGI.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									cgi = header.value;
								else if (INTERNAL_HTTP_HEADER_X_SERVER_HANDLER_XGI.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									xgi = header.value;
								else if (INTERNAL_HTTP_HEADER_X_SERVER_FORWARD_TO.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									forwardOrigin = header.value;
								else if (INTERNAL_HTTP_HEADER_X_SERVER_HANDLER_JS.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									jsFunc = header.value;
							}

							/* After oricessing of header templates, we can process body template with updated data. */
							tmpDataMap = new LinkedHashMap<>();
							requestMap = new LinkedHashMap<>();
							requestMap.put("parameters", providedParams);
							requestMap.put("method", method);
							requestMap.put("path", providedPath);
							requestMap.put("query", providedQueryString);
							requestMap.put("headers", providedHeaderValuesMap);
							requestMap.put("body", providedBody);
							tmpDataMap.put("request", requestMap);
							tmpDataMap.put("data", dataMap);

							processRespBody(!noTemplate, crr, tmpDataMap);

							/* headers from file */
							Map<String, List<String>> headerValuesMap = new LinkedHashMap<>();
							for (String headerStr : crr.request.headers) {
								Header header              = new Header(headerStr);
								String lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);
								// ignore internal headers
								if (INTERNAL_HTTP_HEADER_X_OPENAPI_DESCRIPTION.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									continue;
								if (INTERNAL_HTTP_HEADER_X_OPENAPI_SUMMARY.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									continue;
								if (INTERNAL_HTTP_HEADER_X_OPENAPI_TAGS.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									continue;
								if (INTERNAL_HTTP_HEADER_X_SERVER_DELAY.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									continue;
								if (INTERNAL_HTTP_HEADER_X_SERVER_CONTENT_SOURCE.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									continue;

								List<String> headerValuesList = headerValuesMap.get(lowerCaseHeaderName);
								if (headerValuesList == null) {
									headerValuesList = new ArrayList<>();
									headerValuesMap.put(lowerCaseHeaderName, headerValuesList);
								}

								String[] headerValues = header.value.split(",");
								for (String headerValue : headerValues) {
									headerValue = headerValue.trim();
									if (!headerValue.isEmpty())
										headerValuesList.add(headerValue);
								}
							}

							boolean ok = true;
							/* match headers */
							for (Map.Entry<String, List<String>> entry : headerValuesMap.entrySet()) {
								String       name           = entry.getKey();
								List<String> values         = entry.getValue();
								List<String> providedValues = providedHeaderValuesMap.get(name);
								for (String value : values) {
									if (providedValues == null)
										ok = false;
									else
										ok = MatchUtils.matchHeaderValue(!noWildcard, value, providedValues);
									if (!ok)
										break;
								}
								if (!ok)
									break;
							}
							if (ok) {
								String templateBody;
								if (requestContentSource == null)
									templateBody = crr.request.body.toString().strip();
								else {
									byte[] bs;
									if (responseContentSource.startsWith(IProtocol.FILE) || responseContentSource.startsWith(IProtocol.HTTP) || responseContentSource.startsWith(IProtocol.HTTPS)) {
										String[] contentTypeArr = new String[1];
										bs = UrlUtils.getUrlContent(responseContentSource, contentTypeArr);
									} else if (responseContentSource.startsWith(IProtocol.DATA)) {
										String[] contentTypeArr = new String[1];
										bs = UrlUtils.getDataUrlContent(responseContentSource, contentTypeArr);
									} else
										throw new IllegalArgumentException(MessageFormat.format("Bad {0} value: {1}", INTERNAL_HTTP_HEADER_X_SERVER_CONTENT_SOURCE, responseContentSource));
									templateBody = new String(bs, StandardCharsets.UTF_8);
								}
								if (templateBody.isEmpty()) {
									reqResp = crr;
									break;
								} else {
									boolean jsonContent = requestHeaderContentType != null && requestHeaderContentType.startsWith("application/json");
									if (jsonContent && !strictJson) {
										ObjectMapper om = new ObjectMapper();
										if (om.readTree(templateBody).equals(om.readTree(providedBody.strip()))) {
											reqResp = crr;
											break;
										}
									}
									if (templateBody.equals(providedBody.strip())) {
										reqResp = crr;
										break;
									}
								}
							}
						}
					}

					if (reqResp == null) { // request-reponse pair not found
						reqResp                    = new ReqResp();
						reqResp.response.firstLine = ParseDumpUtils.HTTP_1_1 + ' ' + badRequestStatus;
					}

					String responseBbody = reqResp.response.body.toString();

					String        responseFirstLineStr = reqResp.response.firstLine;
					FirstLineResp firstLineResp        = new FirstLineResp(responseFirstLineStr);

					byte[]              bs              = new byte[0];
					int                 status          = firstLineResp.getStatus();
					String              message         = firstLineResp.getMessage();
					String              contentType     = null;
					Map<String, String> responseHeaders = new LinkedHashMap<>();

					for (String headerStr : reqResp.response.headers) {
						Header header              = new Header(headerStr);
						String lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);

						if (INTERNAL_HTTP_HEADER_X_SERVER_DELAY.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							continue;
						else if (INTERNAL_HTTP_HEADER_X_SERVER_CONTENT_SOURCE.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							continue;
						else if (INTERNAL_HTTP_HEADER_X_SERVER_HANDLER_CGI.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							continue;
						else if (INTERNAL_HTTP_HEADER_X_SERVER_HANDLER_XGI.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							continue;
						else if (INTERNAL_HTTP_HEADER_X_SERVER_FORWARD_TO.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							continue;
						else if (INTERNAL_HTTP_HEADER_X_SERVER_HANDLER_JS.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							continue;

						responseHeaders.put(header.name, header.value);
					}

					if (!noPoweredBy)
						if (!responseHeaders.containsKey(HTTP_HEADER_SERVER))
							responseHeaders.put(HTTP_HEADER_SERVER, X_SERVER_VALUE);

					if (requestDelay != 0)
						Thread.sleep(requestDelay);

					for (String headerStr : reqResp.request.headers) {
						Header header              = new Header(headerStr);
						String lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);

						if (HTTP_HEADER_CONNECTION.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							connectionKeepAlive = "keep-alive".equals(header.value.toLowerCase(Locale.ENGLISH));
					}

					for (String headerStr : reqResp.response.headers) {
						Header header              = new Header(headerStr);
						String lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);

						if (HTTP_HEADER_CONNECTION.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							connectionKeepAlive = "keep-alive".equals(header.value.toLowerCase(Locale.ENGLISH));

						if (HTTP_HEADER_CONTENT_TYPE.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							contentType = header.value;
					}

					/*
					 * Create environment map
					 */
					Map<String, String> env = new LinkedHashMap<>();
					/*
					 * https://datatracker.ietf.org/doc/html/rfc3875#section-4.1.18
					 * RFC 3875 The Common Gateway Interface (CGI) Version 1.1
					 */

					/* Request Meta-Variables */
					// 4.1.1.  AUTH_TYPE. . . .  https://datatracker.ietf.org/doc/html/rfc3875#page-11
					// 4.1.2.  CONTENT_LENGTH .  https://datatracker.ietf.org/doc/html/rfc3875#page-12
					// 4.1.3.  CONTENT_TYPE . .  https://datatracker.ietf.org/doc/html/rfc3875#page-12
					// 4.1.4.  GATEWAY_INTERFACE https://datatracker.ietf.org/doc/html/rfc3875#page-13
					// 4.1.5.  PATH_INFO. . . .  https://datatracker.ietf.org/doc/html/rfc3875#page-13
					// 4.1.6.  PATH_TRANSLATED.  https://datatracker.ietf.org/doc/html/rfc3875#page-14
					// 4.1.7.  QUERY_STRING . .  https://datatracker.ietf.org/doc/html/rfc3875#page-15
					// 4.1.8.  REMOTE_ADDR. . .  https://datatracker.ietf.org/doc/html/rfc3875#page-15
					// 4.1.9.  REMOTE_HOST. . .  https://datatracker.ietf.org/doc/html/rfc3875#page-16
					// 4.1.10. REMOTE_IDENT . .  https://datatracker.ietf.org/doc/html/rfc3875#page-16
					// 4.1.11. REMOTE_USER. . .  https://datatracker.ietf.org/doc/html/rfc3875#page-16
					// 4.1.12. REQUEST_METHOD .  https://datatracker.ietf.org/doc/html/rfc3875#page-17
					// 4.1.13. SCRIPT_NAME. . .  https://datatracker.ietf.org/doc/html/rfc3875#page-17
					// 4.1.14. SERVER_NAME. . .  https://datatracker.ietf.org/doc/html/rfc3875#page-17
					// 4.1.15. SERVER_PORT. . .  https://datatracker.ietf.org/doc/html/rfc3875#page-18
					// 4.1.16. SERVER_PROTOCOL.  https://datatracker.ietf.org/doc/html/rfc3875#page-18
					// 4.1.17. SERVER_SOFTWARE.  https://datatracker.ietf.org/doc/html/rfc3875#page-19

					env.put("SERVER_SOFTWARE", X_SERVER_VALUE);
					env.put("SERVER_NAME", valToStr(req.getServerName()));
					env.put("GATEWAY_INTERFACE", "CGI/1.1");
					env.put("SERVER_PROTOCOL", valToStr(req.getProtocol()));
					env.put("SERVER_PORT", valToStr(Integer.toString(port)));
					env.put("REQUEST_METHOD", valToStr(method));
					env.put("REQUEST_URI", valToStr(req.getRequestURI()));
					env.put("PATH_INFO", valToStr(providedPath));
					env.put("PATH_TRANSLATED", valToStr(providedPath));
					env.put("SCRIPT_NAME", "");
					env.put("QUERY_STRING", valToStr(providedQueryString));
					env.put("REMOTE_HOST", valToStr(req.getRemoteHost()));
					env.put("REMOTE_ADDR", valToStr(req.getRemoteAddr()));
					env.put("AUTH_TYPE", valToStr(req.getAuthType()));
					env.put("REMOTE_USER", valToStr(req.getRemoteUser()));
					env.put("REMOTE_IDENT", ""); //not necessary for full compliance
					env.put("CONTENT_TYPE", valToStr(req.getContentType()));
					long contentLength = req.getContentLengthLong();
					env.put("CONTENT_LENGTH", valToStr(contentLength <= 0 ? "" : Long.toString(contentLength)));

					while (headers.hasMoreElements()) {
						String header = headers.nextElement().toUpperCase(Locale.ENGLISH);
						env.put("HTTP_" + header.replace('-', '_'), req.getHeader(header));
					}

					if (status != badRequestStatus)
						if (responseContentSource != null) {
							if (responseContentSource.startsWith(IProtocol.FILE) || responseContentSource.startsWith(IProtocol.HTTP) || responseContentSource.startsWith(IProtocol.HTTPS)) {
								String[] contentTypeArr = new String[1];
								bs = UrlUtils.getUrlContent(responseContentSource, contentTypeArr);
								if (contentType == null)
									contentType = contentTypeArr[0];
							} else if (responseContentSource.startsWith(IProtocol.DATA)) {
								String[] contentTypeArr = new String[1];
								bs = UrlUtils.getDataUrlContent(responseContentSource, contentTypeArr);
								if (contentType == null)
									contentType = contentTypeArr[0];
							} else
								throw new IllegalArgumentException(MessageFormat.format("Bad {0} value: {1}", INTERNAL_HTTP_HEADER_X_SERVER_CONTENT_SOURCE, responseContentSource));
						} else if (xgi != null) {
							byte[]        requestBs        = createRequestBytes(providedFirstLineStr, providedHeaderValuesMap, providedBodyBs);
							byte[]        outBs            = runCgi(xgi, requestBs, env);
							String        outStr           = new String(outBs, StandardCharsets.UTF_8);
							int           pos              = outStr.indexOf('\n');
							String        firstLineRespStr = outStr.substring(0, pos).strip();
							FirstLineResp firstLineRespCgi = new FirstLineResp(firstLineRespStr);
							if (status == 0) {
								status  = firstLineRespCgi.getStatus();
								message = firstLineRespCgi.getMessage();
							}
							String   headersAndBodyStr = outStr.substring(pos + 1);
							int      pos2              = headersAndBodyStr.indexOf("\n\n");
							String   headersStr        = headersAndBodyStr.substring(0, pos2);
							String[] headersArr        = headersStr.split("\\n");
							for (String headerStr : headersArr) {
								Header header = new Header(headerStr);
								if (!responseHeaders.containsKey(header.name))
									responseHeaders.put(header.name, header.value);
							}
							if (!responseBbody.isEmpty())
								bs = responseBbody.getBytes(StandardCharsets.UTF_8);
							else {
								bs = new byte[outBs.length - (pos + pos2 + 2)];
								System.arraycopy(outBs, pos + pos2 + 2, bs, 0, bs.length);
							}
						} else if (cgi != null) {
							byte[]   outBs             = runCgi(cgi, providedBodyBs, env);
							String   headersAndBodyStr = new String(outBs, StandardCharsets.UTF_8);
							int      pos2              = headersAndBodyStr.indexOf("\n\n");
							String   headersStr        = headersAndBodyStr.substring(0, pos2);
							String[] headersArr        = headersStr.split("\\n");
							for (String headerStr : headersArr) {
								Header header = new Header(headerStr);
								if (!responseHeaders.containsKey(header.name))
									responseHeaders.put(header.name, header.value);
							}
							if (!responseBbody.isEmpty())
								bs = responseBbody.getBytes(StandardCharsets.UTF_8);
							else {
								bs = new byte[outBs.length - (pos2 + 2)];
								System.arraycopy(outBs, pos2 + 2, bs, 0, bs.length);
							}
						} else if (jsFunc != null) {
							/* update data */
							Map<String, Object> tmpDataMap = new LinkedHashMap<>();
							Map<String, Object> requestMap = new LinkedHashMap<>();
							requestMap.put("parameters", providedParams);
							requestMap.put("method", method);
							requestMap.put("path", providedPath);
							requestMap.put("query", providedQueryString);
							requestMap.put("headers", providedHeaderValuesMap);
							requestMap.put("body", providedBody);
							tmpDataMap.put("request", requestMap);
							tmpDataMap.put("data", dataMap);

							List<String> lst = TemplateUtils.processData(scope, jsFunc, tmpDataMap, jsonRequest);
							dataJson     = lst.get(0);
							dataJsonNode = JacksonUtils.parseJsonYamlToMap(dataJson);
							dataMap      = new ObjectMapper().treeToValue(dataJsonNode, Object.class);
							String              responseFromJs         = lst.get(1);
							JsonNode            responseJsonNodeFromJs = JacksonUtils.parseJsonYamlToMap(responseFromJs);
							Map<String, Object> responseObjFromJs      = new ObjectMapper().treeToValue(responseJsonNodeFromJs, Map.class);
							String              bodyFromJs             = (String) responseObjFromJs.get("body");
							Integer             statusFromJs           = (Integer) responseObjFromJs.get("status");
							if (firstLineResp.getStatus() == 0)
								if (statusFromJs != null)
									status = statusFromJs;

							Map<String, List<String>> headersFromJs = (Map<String, List<String>>) responseObjFromJs.get("headers");
							for (Entry<String, List<String>> entry : headersFromJs.entrySet()) {
								String       headerName   = entry.getKey();
								List<String> headerValues = entry.getValue();
								String       headerValue  = httpHeaderValuesToString(headerValues);
								response.addHeader(headerName, headerValue);
							}
							if (responseBbody.isEmpty())
								if (bodyFromJs != null)
									bs = bodyFromJs.getBytes(StandardCharsets.UTF_8);
								else
									bs = new byte[0];
							else {
								responseBbody = TemplateUtils.processTemplate(scope, responseBbody, tmpDataMap);
								bs            = responseBbody.getBytes(StandardCharsets.UTF_8);
							}
						} else if (forwardOrigin != null) {
							String url = forwardOrigin + providedPath;
							if (!providedQueryString.isEmpty())
								url += '?' + providedQueryString;
							URL               obj = new URL(url);
							HttpURLConnection con = (HttpURLConnection) obj.openConnection();
							con.setRequestMethod(method);
							for (Entry<String, List<String>> entry : providedHeaderValuesMap.entrySet()) {
								String        key    = entry.getKey();
								List<String>  values = entry.getValue();
								boolean       first  = true;
								StringBuilder valSb  = new StringBuilder();
								for (String value : values) {
									if (first)
										first = false;
									else
										valSb.append(", ");
									valSb.append(value);
								}
								try {
									con.setRequestProperty(key, valSb.toString());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							if (providedBodyBs.length != 0) {
								con.setDoOutput(true);
								try (OutputStream os = con.getOutputStream()) {
									os.write(providedBodyBs);
									os.flush();
								}
							}
							status = con.getResponseCode();
							for (Entry<String, List<String>> entry : con.getHeaderFields().entrySet()) {
								String key = entry.getKey();
								if (key == null)
									continue;
								List<String> values = entry.getValue();

								boolean       first = true;
								StringBuilder valSb = new StringBuilder();
								for (String value : values) {
									if (first)
										first = false;
									else
										valSb.append(", ");
									valSb.append(value);
								}

								if (!responseHeaders.containsKey(key))
									responseHeaders.put(key, valSb.toString());
							}

							try (InputStream is = con.getInputStream()) {
								bs = is.readAllBytes();
							} catch (FileNotFoundException e) {
								// Ignore 404
								bs = new byte[0];
							}
						} else
							bs = responseBbody.getBytes(StandardCharsets.UTF_8);

					if (responseDelay != 0)
						Thread.sleep(responseDelay);

					if (contentType != null)
						responseHeaders.put(HTTP_HEADER_CONTENT_TYPE, contentType);
					if (!responseHeaders.containsKey(HTTP_HEADER_CONTENT_LENGTH))
						responseHeaders.put(HTTP_HEADER_CONTENT_LENGTH, Integer.toString(bs.length));

					if (!noEtag) {
						if (status != badRequestStatus) {
							String etag = "\"" + Integer.toHexString(Murmur3.hash32(bs)) + "\""; // Murmur3 32-bit variant

							String  etagFromClient = request.getHeader(HTTP_HEADER_IF_NONE_MATCH);
							boolean etagMatched    = etag.equals(etagFromClient);
							if (etagMatched)
								status = HttpServletResponse.SC_NOT_MODIFIED; // setting HTTP 304 and returning with empty body
							else
								responseHeaders.put(HTTP_HEADER_E_TAG, etag);
						}
					}

					if (message == null)
						response.setStatus(status);
					else
						response.setStatus(status, message);

					for (Map.Entry<String, String> entry : responseHeaders.entrySet())
						response.setHeader(entry.getKey(), entry.getValue());

					if (collectFile != null)
						logReqRespToFile(request, providedFirstLineStr, providedBodyBs, bs, status, message, responseHeaders);
					if (!noLog)
						logReqRespToConsole(request, providedFirstLineStr, providedBodyBs, bs, status, message, responseHeaders, !noColor, !nologRequestInfo, !noLogHeaders, !noLogBody, maxLogBody);

					OutputStream responseOutputStream = response.getOutputStream();
					responseOutputStream.write(bs);
					responseOutputStream.flush();
				} catch (Throwable e) {
					e.printStackTrace();
					String       message = MessageFormat.format("Error while generating response body. Dump file: {0}. Line number: {1}. Message: {2}", reqResp.dumpFile, reqResp.response.lineNumber, e.getMessage());
					OutputStream responseOutputStream;
					try {
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message); // TODO
						responseOutputStream = response.getOutputStream();
						responseOutputStream.write(message.getBytes(StandardCharsets.UTF_8));
						responseOutputStream.flush();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} finally {
					if (!connectionKeepAlive)
						try {
							asyncContext.complete();
						} catch (IllegalStateException e2) {
							logger.log(Level.WARNING, e2.getMessage());
						}
				}
			}
		});
	}

	private static void extractRequestHeaders(HttpServletRequest request, Map<String, List<String>> providedHeaderValuesMap) {
		/* provided headers */
		for (Enumeration<String> e = request.getHeaderNames(); e.hasMoreElements();) {
			String       headerName       = e.nextElement().toLowerCase(Locale.ENGLISH);
			List<String> headerValuesList = providedHeaderValuesMap.get(headerName);
			if (headerValuesList == null) {
				headerValuesList = new ArrayList<>();
				providedHeaderValuesMap.put(headerName, headerValuesList);
			}

			for (Enumeration<String> e1 = request.getHeaders(headerName); e1.hasMoreElements();) {
				String headerValue = e1.nextElement();
				headerValue = headerValue.trim();
				String[] headerValues = headerValue.split(",");
				for (String value : headerValues) {
					value = value.trim();
					if (!value.isEmpty())
						headerValuesList.add(value);
				}
			}
		}
	}

	private void logReqRespToFile(HttpServletRequest request, String providedFirstLineStr, byte[] providedBodyBs, byte[] bs, int status, String message, Map<String, String> responseHeaders) throws IOException {
		byte[] logBs = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			baos.write(providedFirstLineStr.getBytes(StandardCharsets.UTF_8));
			baos.write('\n');
			/* provided headers */
			for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
				String headerName = headerNames.nextElement();
				baos.write(processHeaderName(headerName).getBytes(StandardCharsets.UTF_8));
				baos.write(':');
				baos.write(' ');
				boolean first = true;
				for (Enumeration<String> headerValues = request.getHeaders(headerName); headerValues.hasMoreElements();) {
					String headerValue = headerValues.nextElement();
					if (first)
						first = false;
					else
						baos.write(';');
					baos.write(headerValue.getBytes(StandardCharsets.UTF_8));
				}
				baos.write('\n');
			}
			if (providedBodyBs.length != 0) {
				baos.write('\n');
				baos.write(providedBodyBs);
			}
			baos.write('\n');

			String firstLineRespStr = ParseDumpUtils.HTTP_1_1 + ' ' + Integer.toString(status) + (message == null ? "" : ' ' + message);
			baos.write(firstLineRespStr.getBytes(StandardCharsets.UTF_8));
			baos.write('\n');
			/* provided headers */
			for (Map.Entry<String, String> entry : responseHeaders.entrySet()) {
				String headerStr = processHeaderName(entry.getKey()) + ": " + entry.getValue();
				baos.write(headerStr.getBytes(StandardCharsets.UTF_8));
				baos.write('\n');
			}
			if (bs.length != 0) {
				baos.write('\n');
				baos.write(bs);
			}
			baos.write('\n');

			baos.flush();
			logBs = baos.toByteArray();
		}
		Files.write(new File(collectFile).toPath(), logBs, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
	}

	private void logReqRespToConsole(HttpServletRequest request, String providedFirstLineStr, byte[] providedBodyBs, byte[] bs, int status, String message, Map<String, String> responseHeaders, boolean color, boolean logRequestInfo, boolean logHeaders, boolean logBody, int maxLogBody) throws IOException {
		byte[] logBs = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			String firstLineColor   = IAnsi.CYAN_BOLD_BRIGHT;
			String headersColor     = IAnsi.CYAN;
			String contentColor     = IAnsi.CYAN_BRIGHT;

			StringBuilder reqSb = new StringBuilder();
			if (color)
				reqSb.append(IAnsi.RESET + IAnsi.BLACK_BRIGHT);
			reqSb.append("================================================================================");
			if (logRequestInfo) {
				int           remotePort = request.getRemotePort();
				ZonedDateTime zdt        = ZonedDateTime.now();
				String        timestamp  = zdt.format(DateTimeFormatter.ISO_INSTANT);

				String remoteAddr = request.getRemoteAddr();
				String remoteHost = request.getRemoteHost();
				String remoteUser = request.getRemoteUser();
				if (remoteUser == null)
					remoteUser = "";
				if (color)
					reqSb.append(IAnsi.RESET + IAnsi.BLACK_BOLD_BRIGHT);
				reqSb.append("\n[" + timestamp + "]\n\n");
				if (color)
					reqSb.append(IAnsi.RESET + IAnsi.BLACK_BRIGHT);
				reqSb.append("REMOTE_ADDR: " + remoteAddr + '\n');
				reqSb.append("REMOTE_HOST: " + remoteHost + '\n');
				reqSb.append("REMOTE_PORT: " + remotePort + '\n');
				reqSb.append("REMOTE_USER: " + remoteUser + '\n');
				reqSb.append("--------------------------------------------------------------------------------");
			}
			if (color)
				reqSb.append(IAnsi.RESET + firstLineColor);
			reqSb.append('\n');
			reqSb.append(providedFirstLineStr);
			reqSb.append('\n');
			if (logHeaders) {
				if (color)
					reqSb.append(IAnsi.RESET + headersColor);
				/* provided headers */
				for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements();) {
					String headerName = headerNames.nextElement();
					reqSb.append(processHeaderName(headerName) + ": ");
					boolean first = true;
					for (Enumeration<String> headerValues = request.getHeaders(headerName); headerValues.hasMoreElements();) {
						String headerValue = headerValues.nextElement();
						if (first)
							first = false;
						else
							reqSb.append(';');
						reqSb.append(headerValue);
					}
					reqSb.append('\n');
				}
			}
			baos.write(reqSb.toString().getBytes(StandardCharsets.UTF_8));
			if (logBody) {
				if (color)
					baos.write((IAnsi.RESET + contentColor).getBytes(StandardCharsets.UTF_8));
				if (providedBodyBs.length != 0) {
					baos.write('\n');
					int len = providedBodyBs.length;
					if (len > maxLogBody)
						len = maxLogBody;
					baos.write(providedBodyBs, 0, len);
				}
			}

			if (status == badRequestStatus) {
				firstLineColor = IAnsi.RED_BOLD_BRIGHT;
				headersColor   = IAnsi.RED;
				contentColor   = IAnsi.RED_BRIGHT;
			} else {
				firstLineColor = IAnsi.PURPLE_BOLD_BRIGHT;
				headersColor   = IAnsi.PURPLE;
				contentColor   = IAnsi.PURPLE_BRIGHT;
			}

			StringBuilder respSb = new StringBuilder();
			if (color)
				baos.write((IAnsi.RESET + IAnsi.BLACK_BRIGHT).getBytes(StandardCharsets.UTF_8));
			respSb.append('\n');
			respSb.append("--------------------------------------------------------------------------------");
			respSb.append('\n');

			if (color)
				respSb.append(IAnsi.RESET + firstLineColor);
			String firstLineRespStr = ParseDumpUtils.HTTP_1_1 + ' ' + Integer.toString(status) + (message == null ? "" : ' ' + message);
			respSb.append(firstLineRespStr);
			respSb.append('\n');
			if (logHeaders) {
				if (color)
					respSb.append(IAnsi.RESET + headersColor);
				/* provided headers */
				for (Map.Entry<String, String> entry : responseHeaders.entrySet())
					respSb.append(processHeaderName(entry.getKey()) + ": " + entry.getValue() + '\n');
			}
			baos.write(respSb.toString().getBytes(StandardCharsets.UTF_8));
			if (logBody) {
				if (color)
					baos.write((IAnsi.RESET + contentColor).getBytes(StandardCharsets.UTF_8));
				if (bs.length != 0) {
					baos.write('\n');
					int len = bs.length;
					if (len > maxLogBody)
						len = maxLogBody;
					baos.write(bs, 0, len);
				}
			}
			if (color)
				baos.write(IAnsi.RESET.getBytes(StandardCharsets.UTF_8));

			baos.flush();
			logBs = baos.toByteArray();
		}

		logger.log(Level.INFO, new String(logBs, StandardCharsets.UTF_8));
	}

	private void hostDb(AsyncContext asyncContext, HttpServletResponse response) throws IOException {
		if (dataJson != null) {
			response.setContentType("application/json");
			OutputStream responseOutputStream = response.getOutputStream();
			String       outJson              = JacksonUtils.stringifyToJsonYaml(dataMap, JacksonUtils.FORMAT_JSON, true, false);
			responseOutputStream.write(outJson.getBytes(StandardCharsets.UTF_8));
			responseOutputStream.flush();
		}
		asyncContext.complete();
	}

	private void hostOpenApiUi(AsyncContext asyncContext, HttpServletResponse response, String providedPath) throws IOException {
		byte[] bs   = null;
		String mime = null;
		if (providedPath.isEmpty() || "/".equals(providedPath))
			providedPath = "/index.html";
		String resourcePath = "/openapi" + providedPath;
		if ("/openapi.json".equals(providedPath)) {
			mime = "application/json";
			bs   = openApiJsonBs;
		} else if ("/openapi.yaml".equals(providedPath)) {
			mime = "text/yaml";
			bs   = openApiYamlBs;
		} else if (providedPath.endsWith(".html")) {
			mime = "text/html";
			if (providedPath.startsWith("/index.html")) {
				String text             = ResourceUtils.readResourceAsString(resourcePath);
				String openApiHtmlTitle = openApiTitle;
				if (openApiHtmlTitle == null)
					openApiHtmlTitle = "";
				text = text.replace("${title}", openApiHtmlTitle);
				bs   = text.getBytes(StandardCharsets.UTF_8);
			} else
				bs = ResourceUtils.readResourceAsBytes(resourcePath);
		} else if (providedPath.endsWith(".js")) {
			mime = "text/javascript";
			bs   = ResourceUtils.readResourceAsBytes(resourcePath);
		} else if (providedPath.endsWith(".png")) {
			mime = "image/png";
			bs   = ResourceUtils.readResourceAsBytes(resourcePath);
		} else if (providedPath.endsWith(".css")) {
			mime = "text/css";
			bs   = ResourceUtils.readResourceAsBytes(resourcePath);
		}
		if (mime != null)
			response.setContentType(mime);
		if (bs != null) {
			OutputStream responseOutputStream = response.getOutputStream();
			responseOutputStream.write(bs);
			responseOutputStream.flush();
		}
		asyncContext.complete();
	}

	private static String httpHeaderValuesToString(List<String> values) {
		boolean       first = true;
		StringBuilder sb    = new StringBuilder();
		for (String value : values) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(value);
		}
		return sb.toString();
	}

	private byte[] runCgi(String cmd, byte[] requestBs, Map<String, String> env) throws IOException, InterruptedException {
		boolean  windowsOs = System.getProperty("os.name").toLowerCase().startsWith("windows");
		String[] command;
		if (windowsOs)
			command = new String[] { "cmd.exe", "/c", cmd };
		else
			command = new String[] { "sh", "-c", cmd };
		ProcessBuilder      pb   = new ProcessBuilder()                 //
				.directory(new File(System.getProperty("user.home")))   //
				.command(command);                                      //
		Map<String, String> envp = pb.environment();
		envp.putAll(env);

		Process process = pb.start();

		try (InputStream is = new ByteArrayInputStream(requestBs); OutputStream po = process.getOutputStream()) {
			is.transferTo(po);
		}
		try (InputStream is = process.getInputStream(); ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
			is.transferTo(baos);
			byte[] bs       = baos.toByteArray();
			int    exitCode = process.waitFor();
			if (exitCode != 0)
				logger.log(Level.WARNING, MessageFormat.format("CGI program: {0} ended with exit code: {1}", cmd, exitCode));
			return bs;
		} finally {
			process.destroy();
		}
	}

	private static byte[] createRequestBytes(String providedFirstLineStr, Map<String, List<String>> providedHeaderValuesMap, byte[] providedBodyBs) throws IOException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			os.write((providedFirstLineStr + '\n').getBytes(StandardCharsets.UTF_8));

			for (Entry<String, List<String>> entry : providedHeaderValuesMap.entrySet()) {
				String       key    = entry.getKey();
				List<String> values = entry.getValue();
				os.write((key + ": ").getBytes(StandardCharsets.UTF_8));
				String headerValue = httpHeaderValuesToString(values);
				os.write((headerValue + '\n').getBytes(StandardCharsets.UTF_8));
			}
			os.write(("\n").getBytes(StandardCharsets.UTF_8));
			os.write(providedBodyBs);
			return os.toByteArray();
		}
	}

	/**
	 * 
	 * @param activateDirWatchers
	 * @throws Throwable
	 */
	private void reload(boolean activateDirWatchers) throws Throwable {
		Context ctx = Context.enter();
		ctx.setLanguageVersion(Context.VERSION_1_8);
		ctx.setOptimizationLevel(9);
		ctx.getWrapFactory().setJavaPrimitiveWrap(true);

		scope = TemplateUtils.createScope(ctx);

		for (String jsFile : jss) {
			String js = UrlUtils.fileOrUrlToText(jsFile);
			ctx.evaluateString(scope, js, jsFile, 0, null);
		}
		Context.exit();

		/* reload data files */
		if (dataFile == null)
			dataJson = "{}";
		else
			dataJson = UrlUtils.fileOrUrlToText(dataFile);

		dataJsonNode = JacksonUtils.parseJsonYamlToMap(dataJson);
		dataMap      = new ObjectMapper().treeToValue(dataJsonNode, Object.class);

		Map<String, Object> tmpDataMap = new LinkedHashMap<>();
		tmpDataMap.put("data", dataMap);

		allReqResps = CustomMain.getAllReqResp(logger, dumps);

		for (ReqResp reqResp : allReqResps)
			processReq(!noTemplate, reqResp, tmpDataMap);

		/* Create OpenAPI JSON */
		Map<String, Object> openApiMap = OpenApiUtils.createOpenApiMap(allReqResps, openApiTitle);

		String openApiJson = JacksonUtils.stringifyToJsonYaml(openApiMap, JacksonUtils.FORMAT_JSON, true, false);
		openApiJsonBs = openApiJson.getBytes(StandardCharsets.UTF_8);

		String openApiYaml = JacksonUtils.stringifyToJsonYaml(openApiMap, JacksonUtils.FORMAT_YAML, true, false);
		openApiYamlBs = openApiYaml.getBytes(StandardCharsets.UTF_8);

		if (activateDirWatchers) {
			for (String dumpFile : dumps) {
				Path path     = new File(dumpFile).toPath();
				Path dirPath  = path.getParent();
				Path filePath = path.getFileName();

				DirectoryWatcher dirWatcher = directoryWatchersMap.get(dirPath);
				if (dirWatcher == null) {
					dirWatcher = new DirectoryWatcher(logger, dirPath, new Runnable() {

						@Override
						public void run() {
							try {
								reload(false);
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
					});
					Thread dirWatcherThread = new Thread(dirWatcher);
					dirWatcherThread.start();
					directoryWatchersMap.put(dirPath, dirWatcher);
				}
				dirWatcher.addFile(filePath);
			}
			for (String jsFile : jss) {
				Path path     = new File(jsFile).toPath();
				Path dirPath  = path.getParent();
				Path filePath = path.getFileName();

				DirectoryWatcher dirWatcher = directoryWatchersMap.get(dirPath);
				if (dirWatcher == null) {
					dirWatcher = new DirectoryWatcher(logger, dirPath, new Runnable() {

						@Override
						public void run() {
							try {
								reload(false);
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
					});
					Thread dirWatcherThread = new Thread(dirWatcher);
					dirWatcherThread.start();
					directoryWatchersMap.put(dirPath, dirWatcher);
				}
				dirWatcher.addFile(filePath);
			}
			if (dataFile != null) {
				if (!dataFile.startsWith(IProtocol.HTTPS) && !dataFile.startsWith(IProtocol.HTTP)) {
					String file;
					if (dataFile.startsWith(IProtocol.FILE))
						file = new URL(dataFile).getPath();
					else
						file = dataFile;
					Path path     = new File(file).toPath();
					Path dirPath  = path.getParent();
					Path filePath = path.getFileName();

					DirectoryWatcher dirWatcher = directoryWatchersMap.get(dirPath);
					if (dirWatcher == null) {
						dirWatcher = new DirectoryWatcher(logger, dirPath, new Runnable() {

							@Override
							public void run() {
								try {
									reload(false);
								} catch (Throwable e) {
									e.printStackTrace();
								}
							}
						});

						Thread dirWatcherThread = new Thread(dirWatcher);
						dirWatcherThread.start();
						directoryWatchersMap.put(dirPath, dirWatcher);
					}
					dirWatcher.addFile(filePath);
				}
			}
		}
	}

	/**
	 * Process header name before output
	 *
	 * @param s
	 * @return
	 */
	private static String processHeaderName(String s) {
		char[] arr    = s.toCharArray();
		int    first  = 0;
		int    second = first + 1;
		arr[first] = Character.toUpperCase(arr[first]);
		for (int i = second; i < arr.length - 1; i++) {
			char c = arr[i];
			if (c == '-') {
				int next = i + 1;
				arr[next] = Character.toUpperCase(arr[next]);
			}
		}
		return new String(arr);
	}

	private static ReqResp cloneReqResp(ReqResp reqResp) {
		ReqResp rr = new ReqResp();
		rr.dumpFile = reqResp.dumpFile;

		rr.request.firstLine  = reqResp.request.firstLine;
		rr.request.lineNumber = reqResp.request.lineNumber;
		for (int i = 0; i < reqResp.request.headers.size(); i++) {
			String headerStr = reqResp.request.headers.get(i);
			rr.request.headers.add(headerStr);
		}
		rr.request.body = reqResp.request.body;

		rr.response.firstLine  = reqResp.response.firstLine;
		rr.response.lineNumber = reqResp.response.lineNumber;
		for (int i = 0; i < reqResp.response.headers.size(); i++) {
			String headerStr = reqResp.response.headers.get(i);
			rr.response.headers.add(headerStr);
		}
		rr.response.body = reqResp.response.body;

		return rr;
	}

	private void processReq(boolean processTemplate, ReqResp reqResp, Map<String, Object> dataMap) throws IOException {
		if (processTemplate)
			reqResp.request.firstLine = TemplateUtils.processTemplate(scope, reqResp.request.firstLine, dataMap);
		for (int i = 0; i < reqResp.request.headers.size(); i++) {
			String headerStr = reqResp.request.headers.get(i);
			if (processTemplate)
				headerStr = TemplateUtils.processTemplate(scope, headerStr, dataMap);
			reqResp.request.headers.set(i, headerStr);
		}
		reqResp.request.body = TemplateUtils.processTemplate(scope, reqResp.request.body, dataMap);
	}

	private void processRespHeaders(boolean processTemplate, ReqResp reqResp, Map<String, Object> dataMap) throws IOException {
		if (processTemplate)
			reqResp.response.firstLine = TemplateUtils.processTemplate(scope, reqResp.response.firstLine, dataMap);
		for (int i = 0; i < reqResp.response.headers.size(); i++) {
			String headerStr = reqResp.response.headers.get(i);
			if (processTemplate)
				headerStr = TemplateUtils.processTemplate(scope, headerStr, dataMap);
			reqResp.response.headers.set(i, headerStr);
		}
	}

	private void processRespBody(boolean processTemplate, ReqResp reqResp, Map<String, Object> dataMap) throws IOException {
		if (processTemplate)
			reqResp.response.body = TemplateUtils.processTemplate(scope, reqResp.response.body, dataMap);
	}

	/**
	 * 
	 * @param path
	 * @throws IOException
	 */
	private static void deleteDir(Path path) throws IOException {
		if (!path.isAbsolute())
			throw new IllegalArgumentException(path.toString());
		Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
	}

	private static String valToStr(Object value) throws IOException {
		if (value == null)
			return "";
		return value.toString();
	}

	private void exportMemoryDataToFile() throws IOException {
		Path dbExportFilePath = new File(dbExportFile).getAbsoluteFile().toPath();
		if (Files.exists(dbExportFilePath))
			if (!noBak)
				Files.move(dbExportFilePath, Paths.get(dbExportFilePath.toString() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
		Files.writeString(dbExportFilePath, dataJson);
	}

}
