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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
import org.deepfake_http.common.utils.DataUriUtils;
import org.deepfake_http.common.utils.HttpPathUtils;
import org.deepfake_http.common.utils.IAnsi;
import org.deepfake_http.common.utils.JacksonUtils;
import org.deepfake_http.common.utils.MatchUtils;
import org.deepfake_http.common.utils.OpenApiUtils;
import org.deepfake_http.common.utils.ParseCommandLineUtils;
import org.deepfake_http.common.utils.ParseDumpUtils;
import org.deepfake_http.common.utils.ResourceUtils;
import org.deepfake_http.common.utils.TemplateUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;
import org.tommy.main.CustomMain;

import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
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

	public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";

	private static final String HTTP_HEADER_CONNECTION     = "Connection";
	private static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";

	private static final String HTTP_HEADER_IF_NONE_MATCH = "If-None-Match";
	private static final String HTTP_HEADER_E_TAG         = "ETag";
	private static final String HTTP_HEADER_X_POWERED_BY  = "X-Powered-By"; // non-standard

	/* internal, not sended with response  */
	private static final String INTERNAL_HTTP_HEADER_X_SERVER_BODY_TYPE      = "X-Body-Type";      // response non-standard
	private static final String INTERNAL_HTTP_HEADER_X_SERVER_REQUEST_DELAY  = "X-Request-Delay";  // response non-standard
	private static final String INTERNAL_HTTP_HEADER_X_SERVER_RESPONSE_DELAY = "X-Response-Delay"; // response non-standard

	public static final String INTERNAL_HTTP_HEADER_X_OPENAPI_SUMMARY     = "X-OpenAPI-Summary";    // request non-standard
	public static final String INTERNAL_HTTP_HEADER_X_OPENAPI_DESCRIPTION = "X-OpenAPI-Description";// request non-standard
	public static final String INTERNAL_HTTP_HEADER_X_OPENAPI_TAGS        = "X-OpenAPI-Tags";       // request non-standard

	private static final String X_POWERED_BY_VALUE = "DeepfakeHTTP " + System.getProperty("build.version") + " (" + System.getProperty("build.timestamp") + ")";

	private byte[] openApiJsonBs;
	private byte[] openApiYamlBs;

	/* CLI flags */
	private boolean noWatch;
	private boolean noEtag;
	private boolean noLog;
	private boolean noCors;
	private boolean noPoweredBy;
	private boolean noColor;
	private boolean noTemplate;
	private boolean strictJson;
	private int     badRequestStatus;

	private String                       collectFile;
	private String                       openApiPath;
	private String                       openApiTitle;
	private String                       dataFile;
	private Path                         dataFilePath;
	private List<String /* dump file */> dumps;

	private Logger logger;

	private Map<Path /* dirPath */, DirectoryWatcher> directoryWatchersMap = new HashMap<>();
	private DirectoryWatcher                          dataFileDirectoryWatcher;

	private Map<String, Object> dataMap;

	private List<ReqResp> allReqResps;
	private ReqResp       badRequest400;

	private Configuration freeMarkerConfiguration;

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

		dumps = new ArrayList<>();

		try {
			InitialContext ctx = new InitialContext();

			/* get custom command-line args */
			String[] args = (String[]) ctx.lookup("java:comp/env/tommy/args");

			Map<String, Object> paramMap = ParseCommandLineUtils.parseCommandLineArgs(logger, args, dumps);
			noWatch          = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_WATCH);
			noEtag           = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_ETAG);
			noLog            = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_LOG);
			noCors           = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_CORS);
			noPoweredBy      = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_POWERED_BY);
			noColor          = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_COLOR);
			noTemplate       = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_NO_TEMPLATE);
			strictJson       = (boolean) paramMap.get(ParseCommandLineUtils.ARGS_STRICT_JSON);
			collectFile      = (String) paramMap.get(ParseCommandLineUtils.ARGS_COLLECT);
			openApiPath      = (String) paramMap.get(ParseCommandLineUtils.ARGS_OPENAPI_PATH);
			openApiTitle     = (String) paramMap.get(ParseCommandLineUtils.ARGS_OPENAPI_TITLE);
			dataFile         = (String) paramMap.get(ParseCommandLineUtils.ARGS_DATA);
			badRequestStatus = (int) paramMap.get(ParseCommandLineUtils.ARGS_STATUS);

			if (openApiTitle == null)
				openApiTitle = "";

			boolean activateDirWatchers = !noWatch;

			if (dataFile != null) {
				dataFilePath = new File(dataFile).toPath();
				Path dirPath  = dataFilePath.getParent();
				Path filePath = dataFilePath.getFileName();

				if (activateDirWatchers) {
					dataFileDirectoryWatcher = new DirectoryWatcher(logger, dirPath, new Runnable() {

						@Override
						public void run() {
							try {
								reload(false);
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
					});
					dataFileDirectoryWatcher.addFile(filePath);
					Thread dirWatcherThread = new Thread(dataFileDirectoryWatcher);
					dirWatcherThread.start();
				}
			}
			reload(activateDirWatchers);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		freeMarkerConfiguration = new Configuration(Configuration.VERSION_2_3_31);
		freeMarkerConfiguration.setDefaultEncoding("UTF-8");
		freeMarkerConfiguration.setNumberFormat("computer");
		freeMarkerConfiguration.setBooleanFormat("c");
		freeMarkerConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		freeMarkerConfiguration.setLogTemplateExceptions(false);
		freeMarkerConfiguration.setWrapUncheckedExceptions(true);
		freeMarkerConfiguration.setFallbackOnNullLoopVariable(false);

		System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%6$s%n");
		SimpleFormatter formatter = new SimpleFormatter();
		logger.getParent().getHandlers()[0].setFormatter(formatter);
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
		logger.log(Level.INFO, "Servlet destroy() method called by container.");
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
					}

					String providedQueryString = request.getQueryString();
					if (providedQueryString == null)
						providedQueryString = "";
					String protocol = request.getProtocol();

					String providedFirstLineStr = method + ' ' + providedPath + (providedQueryString.isEmpty() ? "" : "?" + providedQueryString) + ' ' + protocol;

					byte[] providedBodyBs = request.getInputStream().readAllBytes();
					String providedBody   = new String(providedBodyBs, StandardCharsets.UTF_8);

					Map<String, List<String>> providedParams = new LinkedHashMap<>();

					String requestHeaderContentType = request.getHeader(HTTP_HEADER_CONTENT_TYPE);
					if (requestHeaderContentType != null && requestHeaderContentType.startsWith("application/x-www-form-urlencoded"))
						MatchUtils.parseQuery(providedBody, providedParams);

					String bodyType      = null;
					int    requestDelay  = 0;
					int    responseDelay = 0;

					/* search for request-reponse pair */
					Map<String, List<String>> providedHeaderValuesMap = new LinkedHashMap<>();
					for (ReqResp rr : allReqResps) {

						ReqResp crr = cloneReqResp(rr);

						FirstLineReq firstLineReq = new FirstLineReq(crr.request.firstLine);
						boolean      protocolOk   = firstLineReq.getProtocol().equals(ParseDumpUtils.HTTP_1_1);
						boolean      methodOk     = firstLineReq.getMethod().equals(method);
						String       templatePath = HttpPathUtils.extractPathFromUri(firstLineReq.getUri());

						boolean pathOk = MatchUtils.matchPath(templatePath, providedPath, providedParams);

						String templateQueryString = HttpPathUtils.extractQueryStringFromUri(firstLineReq.getUri());

						boolean queryStringOk = MatchUtils.matchQuery(templateQueryString, providedQueryString, providedParams);
						if ( //
						protocolOk && //
						methodOk && //
						pathOk && //
						queryStringOk //
						) {
							for (String headerStr : crr.request.headers) {
								Header header              = new Header(headerStr);
								String lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);
								if (lowerCaseHeaderName.equals(INTERNAL_HTTP_HEADER_X_SERVER_REQUEST_DELAY.toLowerCase(Locale.ENGLISH)))
									requestDelay = Integer.parseInt(header.value);
							}
							for (String headerStr : crr.response.headers) {
								Header header              = new Header(headerStr);
								String lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);
								if (INTERNAL_HTTP_HEADER_X_SERVER_BODY_TYPE.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									bodyType = header.value;
								else if (INTERNAL_HTTP_HEADER_X_SERVER_REQUEST_DELAY.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									requestDelay = Integer.parseInt(header.value);
								else if (INTERNAL_HTTP_HEADER_X_SERVER_RESPONSE_DELAY.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
									responseDelay = Integer.parseInt(header.value);
							}

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

							/* provided headers */
							providedHeaderValuesMap.clear();
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
										ok = MatchUtils.matchHeaderValue(value, providedValues);
									if (!ok)
										break;
								}
								if (!ok)
									break;
							}
							if (ok) {
								String templateBody = crr.request.body.toString().strip();
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
					if (reqResp == null) // request-reponse pair not found
						reqResp = badRequest400; // may be null

					boolean simpleBadRequest400;
					if (reqResp == null) {
						reqResp                    = new ReqResp();
						reqResp.response.firstLine = ParseDumpUtils.HTTP_1_1 + ' ' + badRequestStatus + ' ' + "Bad request";
						simpleBadRequest400        = true;
					} else
						simpleBadRequest400 = false;

					if (dataFilePath != null || !providedParams.isEmpty()) {
						Map<String, Object> tmpDataMap = new LinkedHashMap<>(dataMap);
						tmpDataMap.put("parameters", providedParams);

						processResp(!noTemplate, freeMarkerConfiguration, reqResp, tmpDataMap);
					}

					String        responseFirstLineStr = reqResp.response.firstLine;
					FirstLineResp firstLineResp        = new FirstLineResp(responseFirstLineStr);

					byte[] bs;
					int    status = firstLineResp.getStatus();
					if (simpleBadRequest400)
						bs = new byte[0];

					String              message         = firstLineResp.getMessage();
					String              mime            = null;
					String              encoding        = null;
					Map<String, String> responseHeaders = new LinkedHashMap<>();

					for (String headerStr : reqResp.response.headers) {
						Header header              = new Header(headerStr);
						String lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);

						if (INTERNAL_HTTP_HEADER_X_SERVER_BODY_TYPE.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							continue;
						else if (INTERNAL_HTTP_HEADER_X_SERVER_REQUEST_DELAY.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							continue;
						else if (INTERNAL_HTTP_HEADER_X_SERVER_RESPONSE_DELAY.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							continue;

						responseHeaders.put(header.name, header.value);
					}

					if (!noPoweredBy)
						if (!responseHeaders.containsKey(HTTP_HEADER_X_POWERED_BY))
							responseHeaders.put(HTTP_HEADER_X_POWERED_BY, X_POWERED_BY_VALUE);

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
					}

					String body = reqResp.response.body.toString();
					try {
						if (bodyType == null)
							bs = body.getBytes(StandardCharsets.UTF_8);
						else if ("application/x-sh".equals(bodyType)) {
							Map<String, Object> http = createHttpObject(method, providedPath, protocol, providedParams, providedHeaderValuesMap, providedBody.getBytes(StandardCharsets.UTF_8), protocol, status, message, responseHeaders, new byte[0]);
							String              json = JacksonUtils.stringifyToJsonYaml(http, JacksonUtils.FORMAT_JSON, false, false) + '\n';
							bs = getnerateOutFromSh(body, json);
						} else if ("application/javascript".equals(bodyType)) {
							Map<String, Object> http = createHttpObject(method, providedPath, protocol, providedParams, providedHeaderValuesMap, providedBody.getBytes(StandardCharsets.UTF_8), protocol, status, message, responseHeaders, new byte[0]);
							bs = getnerateOutFromJs(body, providedFirstLineStr, http);
						} else if ("text/uri-list".equals(bodyType)) {
							String[] mimeArr     = new String[1];
							String[] encodingArr = new String[1];
							bs       = getnerateOutFromUrl(body, mimeArr, encodingArr);
							mime     = mimeArr[0];
							encoding = encodingArr[0];
						} else // Unknown bodyType
							throw new IllegalArgumentException(MessageFormat.format("\"X-Body-Type: {0}\" is not supported.", bodyType));
					} catch (Exception e) {
						bs      = new byte[0];
						status  = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;                                                                                                                             // 500
						message = MessageFormat.format("Error while generating response body. Dump file: {0}, Line number: {1}, Message: ", reqResp.dumpFile, reqResp.response.lineNumber + e.getMessage());
					}
					if (responseDelay != 0)
						Thread.sleep(responseDelay);

					if (mime != null)
						responseHeaders.put(HTTP_HEADER_CONTENT_TYPE, mime + (encoding == null ? "" : "; charset=" + encoding));
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
						logReqRespToFile(request, providedFirstLineStr, providedBodyBs, simpleBadRequest400, bs, status, message, responseHeaders);
					if (!noLog)
						logReqRespToConsole(request, providedFirstLineStr, providedBodyBs, simpleBadRequest400, bs, status, message, responseHeaders, !noColor);

					OutputStream responseOutputStream = response.getOutputStream();
					responseOutputStream.write(bs);
					responseOutputStream.flush();
				} catch (Throwable e) {
					e.printStackTrace();
					String       message = MessageFormat.format("Error while generating response body. Dump file: {0}. Line number: {1}. Message: ", reqResp.dumpFile, reqResp.response.lineNumber + e.getMessage());
					OutputStream responseOutputStream;
					try {
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message); //TODO
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

	private void logReqRespToFile(HttpServletRequest request, String providedFirstLineStr, byte[] providedBodyBs, boolean simpleBadRequest400, byte[] bs, int status, String message, Map<String, String> responseHeaders) throws IOException {
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
			if (!simpleBadRequest400) {
				/* provided headers */
				for (Map.Entry<String, String> entry : responseHeaders.entrySet()) {
					String headerStr = processHeaderName(entry.getKey()) + ": " + entry.getValue();
					baos.write(headerStr.getBytes(StandardCharsets.UTF_8));
					baos.write('\n');
				}
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

	private void logReqRespToConsole(HttpServletRequest request, String providedFirstLineStr, byte[] providedBodyBs, boolean simpleBadRequest400, byte[] bs, int status, String message, Map<String, String> responseHeaders, boolean color) throws IOException {
		byte[] logBs = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			String firstLineColor;
			String headersColor;
			String contentColor;

			if (status == badRequestStatus) {
				firstLineColor = IAnsi.CYAN_BOLD_BRIGHT;
				headersColor   = IAnsi.CYAN;
				contentColor   = IAnsi.CYAN_BRIGHT;
			} else {
				firstLineColor = IAnsi.CYAN_BOLD_BRIGHT;
				headersColor   = IAnsi.CYAN;
				contentColor   = IAnsi.CYAN_BRIGHT;
			}
			StringBuilder reqSb = new StringBuilder();
			if (color)
				reqSb.append(IAnsi.RESET + IAnsi.BLACK_BRIGHT);
			reqSb.append("--------------------------------------------------------------------------------");
			if (color)
				reqSb.append(IAnsi.RESET + firstLineColor);
			reqSb.append('\n');
			reqSb.append(providedFirstLineStr);
			if (color)
				reqSb.append(IAnsi.RESET + headersColor);
			reqSb.append('\n');
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
			if (color)
				reqSb.append(IAnsi.RESET + contentColor);
			baos.write(reqSb.toString().getBytes(StandardCharsets.UTF_8));
			if (providedBodyBs.length != 0) {
				baos.write('\n');
				baos.write(providedBodyBs);
			}
			baos.write('\n');
			if (color)
				baos.write(IAnsi.RESET.getBytes(StandardCharsets.UTF_8));

			if (status == badRequestStatus) {
				firstLineColor = IAnsi.RED_BOLD_BRIGHT;
				headersColor   = IAnsi.PURPLE;
				contentColor   = IAnsi.PURPLE_BRIGHT;
			} else {
				firstLineColor = IAnsi.PURPLE_BOLD_BRIGHT;
				headersColor   = IAnsi.PURPLE;
				contentColor   = IAnsi.PURPLE_BRIGHT;
			}

			StringBuilder respSb = new StringBuilder();
			if (color)
				respSb.append(IAnsi.RESET + firstLineColor);
			String firstLineRespStr = ParseDumpUtils.HTTP_1_1 + ' ' + Integer.toString(status) + (message == null ? "" : ' ' + message);
			respSb.append(firstLineRespStr);
			if (color)
				respSb.append(IAnsi.RESET + headersColor);
			respSb.append('\n');
			if (!simpleBadRequest400) {
				/* provided headers */
				for (Map.Entry<String, String> entry : responseHeaders.entrySet())
					respSb.append(processHeaderName(entry.getKey()) + ": " + entry.getValue() + '\n');
			}
			if (color)
				respSb.append(IAnsi.RESET + contentColor);
			baos.write(respSb.toString().getBytes(StandardCharsets.UTF_8));
			if (bs.length != 0) {
				baos.write('\n');
				baos.write(bs);
			}
			if (color)
				baos.write(IAnsi.RESET.getBytes(StandardCharsets.UTF_8));

			baos.flush();
			logBs = baos.toByteArray();
		}

		logger.log(Level.INFO, new String(logBs, StandardCharsets.UTF_8));
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

	private byte[] getnerateOutFromSh(String body, String json) throws InterruptedException, IOException {
		Path tmpFile = Files.createTempFile("df-", "tmp");
		Files.write(tmpFile, body.getBytes(StandardCharsets.UTF_8));
		Set<PosixFilePermission> perms = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE);
		Files.setPosixFilePermissions(tmpFile, perms);
		ProcessBuilder pb = new ProcessBuilder() //
				.directory(new File(System.getProperty("user.home"))) //
				.command(new String[] { "sh", "-c", tmpFile.toString() }); //

		Process process = pb.start();

		try (InputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)); OutputStream po = process.getOutputStream()) {
			is.transferTo(po);
		}
		try (InputStream is = process.getInputStream(); ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
			is.transferTo(baos);
			byte[] bs       = baos.toByteArray();
			int    exitCode = process.waitFor();
			Files.deleteIfExists(tmpFile);
			logger.log(exitCode == 0 ? Level.INFO : Level.WARNING, "Exit code: {0}", exitCode);
			return bs;
		} finally {
			process.destroy();
		}
	}

	private byte[] getnerateOutFromJs(String body, String providedFirstLineStr, Map<String, Object> http) throws IOException {
		PrintStream stdout = System.out;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos);) {
			System.setOut(ps);
			Context cx = Context.enter();
			cx.setLanguageVersion(Context.VERSION_1_8);
			cx.setOptimizationLevel(9);
			cx.getWrapFactory().setJavaPrimitiveWrap(true);
			ScriptableObject scope = new ImporterTopLevel(cx);
			scope = (ScriptableObject) cx.initStandardObjects(scope);
			Object obj = Context.javaToJS(http, scope);
			ScriptableObject.putProperty(scope, "http", obj);
			cx.evaluateString(scope, body, providedFirstLineStr, 0, null);
			return baos.toByteArray();
		} finally {
			Context.exit();
			System.setOut(stdout);
		}
	}

	private byte[] getnerateOutFromUrl(String body, String[] mimeArr, String[] encodingArr) throws IOException {
		if (body.startsWith("file:") || body.startsWith("https:") || body.startsWith("http:")) {
			try (InputStream is = new URL(body).openStream()) {
				return is.readAllBytes();
			}
		} else if (body.startsWith("data:")) { // data URI
			StringBuilder mediatypeSb = new StringBuilder();
			StringBuilder encodingSb  = new StringBuilder();
			String        mimeStr     = mediatypeSb.toString().strip();
			if (!mimeStr.isEmpty())
				mimeArr[0] = mimeStr;
			String encodingStr = encodingSb.toString().strip();
			if (!encodingStr.isEmpty())
				encodingArr[0] = encodingStr;
			return DataUriUtils.parseDataUri(body, mimeArr, encodingArr);
		} else { // fallback to ignored bodyType
			logger.log(Level.WARNING, "\"X-Body-Type: text/uri-list\", but body content is not valid URL or URL protocol is not supported. Ignored. Valid URL protocols: file, http, https, data");
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Create http object
	 * 
	 * @param requestMethod
	 * @param requestPath
	 * @param requestProtocol
	 * @param requestParameters
	 * @param requestHeaderValuesMap
	 * @param requestBs
	 * @param responseProtocol
	 * @param responseStatus
	 * @param responseMessage
	 * @param responseHeaders
	 * @param responseBs
	 * @return
	 *
	 *
	 * Example:
	 * 
	 * {
	 *   "request": {
	 *     "method": "POST",
	 *     "uri": "/form.html",
	 *     "protocol": "HTTP/1.1",
	 *     "parameters": {
	 *       "fname": ["John"],
	 *       "lname": ["Doe"]
	 *     },
	 *     "body": "fname=John&lname=Doe",
	 *     "headers": {
	 *       "host": ["localhost:8080"],
	 *       "connection": ["keep-alive"],
	 *       "content-length": ["21"],
	 *       "cache-control": ["max-age=0"],
	 *       "upgrade-insecure-requests": ["1"],
	 *       "origin": ["http://localhost:8080"],
	 *       "content-type": ["application/x-www-form-urlencoded"],
	 *       "user-agent": ["Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML", "like Gecko) Chrome/91.0.4472.101 Safari/537.36"],
	 *       "accept": ["text/html", "application/xhtml+xml", "application/xml;q=0.9", "image/avif", "image/webp", "image/apng"],
	 *       "referer": ["http://localhost:8080/form.html"],
	 *       "accept-encoding": ["gzip", "deflate", "br"],
	 *       "accept-language": ["en-GB", "en;q=0.9", "ru;q=0.8", "en-US;q=0.7"]
	 *     },
	 *     "response": {
	 *       "headers": {
	 *         "Content-Type": "text/html"
	 *       },
	 *       "protocol": "HTTP/1.1",
	 *       "message": "OK",
	 *       "body": "<!DOCTYPE html><html lang=en><body><h1>Hello, John Doe!</h1></body></html>",
	 *       "status": 200
	 *     }
	 *   }
	 * }
	 * 
	 */
	private Map<String, Object> createHttpObject(String requestMethod, String requestPath, String requestProtocol, Map<String, List<String>> requestParameters, Map<String, List<String>> requestHeaderValuesMap, byte[] requestBs, String responseProtocol, int responseStatus, String responseMessage, Map<String, String> responseHeaders, byte[] responseBs) {
		Map<String, Object> http         = new HashMap<>();
		Map<String, Object> httpRequest  = new HashMap<>();
		Map<String, Object> httpResponse = new HashMap<>();

		http.put("request", httpRequest);

		httpRequest.put("method", requestMethod);
		httpRequest.put("uri", requestPath);
		httpRequest.put("protocol", requestProtocol);
		httpRequest.put("parameters", requestParameters);
		httpRequest.put("body", requestBs);
		httpRequest.put("headers", requestHeaderValuesMap);

		http.put("response", httpResponse);
		httpResponse.put("protocol", responseProtocol);
		httpResponse.put("status", responseStatus);
		httpResponse.put("message", responseMessage);
		httpResponse.put("headers", responseHeaders);
		httpResponse.put("body", responseBs);
		return http;
	}

	/**
	 * 
	 * @param reqResps
	 * @return
	 * @throws Exception
	 */
	private ReqResp getBadRequest400(List<ReqResp> reqResps) throws Exception {
		for (ReqResp reqResp : reqResps) {
			FirstLineResp firstLineResp = new FirstLineResp(reqResp.response.firstLine);
			if (firstLineResp.getStatus() == badRequestStatus)
				return reqResp;
		}
		return null;
	}

	/**
	 * 
	 * @param activateDirWatchers
	 * @throws Throwable
	 */
	private void reload(boolean activateDirWatchers) throws Throwable {
		/* reload data file */
		if (dataFilePath == null)
			dataMap = new HashMap<>();
		else {
			String dataJson = Files.readString(dataFilePath, StandardCharsets.UTF_8).strip(); // JSON or YAML
			dataMap = JacksonUtils.parseJsonYamlToMap(dataJson);
		}

		allReqResps = CustomMain.getAllReqResp(logger, dumps);

		if (dataFilePath != null)
			for (ReqResp reqResp : allReqResps)
				processReq(!noTemplate, freeMarkerConfiguration, reqResp, dataMap);

		/* Create OpenAPI JSON */
		Map<String, Object> openApiMap = OpenApiUtils.createOpenApiMap(allReqResps, openApiTitle);

		String openApiJson = JacksonUtils.stringifyToJsonYaml(openApiMap, JacksonUtils.FORMAT_JSON, true, false);
		openApiJsonBs = openApiJson.getBytes(StandardCharsets.UTF_8);

		String openApiYaml = JacksonUtils.stringifyToJsonYaml(openApiMap, JacksonUtils.FORMAT_YAML, true, false);
		openApiYamlBs = openApiYaml.getBytes(StandardCharsets.UTF_8);

		for (String dumpFile : dumps) {
			Path path     = new File(dumpFile).toPath();
			Path dirPath  = path.getParent();
			Path filePath = path.getFileName();

			if (activateDirWatchers) {
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
		badRequest400 = getBadRequest400(allReqResps);
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

	private static void processReq(boolean processTemplate, Configuration freeMarkerConfiguration, ReqResp reqResp, Map<String, Object> dataMap) throws IOException, TemplateException {
		reqResp.request.firstLine = TemplateUtils.processTemplate(processTemplate, freeMarkerConfiguration, reqResp.request.firstLine, dataMap);
		for (int i = 0; i < reqResp.request.headers.size(); i++) {
			String headerStr = reqResp.request.headers.get(i);
			headerStr = TemplateUtils.processTemplate(processTemplate, freeMarkerConfiguration, headerStr, dataMap);
			reqResp.request.headers.set(i, headerStr);
		}
		reqResp.request.body = TemplateUtils.processTemplate(processTemplate, freeMarkerConfiguration, reqResp.request.body, dataMap);
	}

	private static void processResp(boolean processTemplate, Configuration freeMarkerConfiguration, ReqResp reqResp, Map<String, Object> dataMap) throws IOException, TemplateException {
		reqResp.response.firstLine = TemplateUtils.processTemplate(processTemplate, freeMarkerConfiguration, reqResp.response.firstLine, dataMap);
		for (int i = 0; i < reqResp.response.headers.size(); i++) {
			String headerStr = reqResp.response.headers.get(i);
			headerStr = TemplateUtils.processTemplate(processTemplate, freeMarkerConfiguration, headerStr, dataMap);
			reqResp.response.headers.set(i, headerStr);
		}
		reqResp.response.body = TemplateUtils.processTemplate(processTemplate, freeMarkerConfiguration, reqResp.response.body, dataMap);
	}

}
