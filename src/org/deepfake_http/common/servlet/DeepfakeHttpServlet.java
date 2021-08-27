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
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import org.apache.hive.common.util.Murmur3;
import org.deepfake_http.common.FirstLineResp;
import org.deepfake_http.common.Header;
import org.deepfake_http.common.ReqResp;
import org.deepfake_http.common.utils.DataUriUtils;
import org.deepfake_http.common.utils.HttpPathUtils;
import org.deepfake_http.common.utils.JacksonUtils;
import org.deepfake_http.common.utils.ParseCommandLineUtils;
import org.deepfake_http.common.utils.ParseDumpUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import ij.util.WildcardMatch;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * TODO 
 * - support block comments in dumps
 */
public class DeepfakeHttpServlet extends HttpServlet {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private static final String HTTP_HEADER_CONNECTION     = "Connection";
	private static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
	private static final String HTTP_HEADER_CONTENT_TYPE   = "Content-Type";
	private static final String HTTP_HEADER_IF_NONE_MATCH  = "If-None-Match";
	private static final String HTTP_HEADER_E_TAG          = "ETag";
	private static final String HTTP_HEADER_X_POWERED_BY   = "X-Powered-By";  // non-standard

	/* internal, not sended with response  */
	private static final String INTERNAL_HTTP_HEADER_X_SERVER_BODY_TYPE      = "X-Body-Type";     // non-standard
	private static final String INTERNAL_HTTP_HEADER_X_SERVER_REQUEST_DELAY  = "X-Request-Delay"; // non-standard
	private static final String INTERNAL_HTTP_HEADER_X_SERVER_RESPONSE_DELAY = "X-Response-Delay";// non-standard

	private static final String X_POWERED_BY_VALUE = "DeepfakeHTTP";

	/* CLI flags */
	private boolean noListen = false;
	private boolean noEtag   = false;

	private Map<String /* dump file */, String /* dump content */ > dumps;

	private Logger logger;

	private Map<Path /* dirPath */, DirectoryWatcher> directoryWatchersMap = new HashMap<>();

	private List<ReqResp> allReqResps;
	private ReqResp       badRequest400;

	private Configuration freeMarkerConfiguration;

	private class DirectoryWatcher implements Runnable {

		private Path             dirPath;
		private Collection<Path> filePaths = new HashSet<>();

		public DirectoryWatcher(Path dirPath) {
			this.dirPath = dirPath;
		}

		public void addFile(Path filePath) {
			filePaths.add(filePath);
		}

		private void printEvent(WatchEvent<?> event) {
			Kind<?> kind = event.kind();
			Path    path = (Path) event.context();

			for (Path filePath : filePaths) {
				if (filePath.equals(path)) {
					logger.log(Level.INFO, "File \"{0}\" was changed. Kind: {1}.", new Object[] { dirPath.resolve(filePath), kind });
					try {
						reload(false);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void run() {
			try {
				WatchService watchService = dirPath.getFileSystem().newWatchService();
				dirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

				while (true) {
					WatchKey watchKey;
					watchKey = watchService.take();

					for (final WatchEvent<?> event : watchKey.pollEvents())
						printEvent(event);

					if (!watchKey.reset()) {
						watchKey.cancel();
						watchService.close();
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

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

		dumps = new LinkedHashMap<>();
		boolean[] noListenArr = new boolean[1];
		boolean[] noEtagArr   = new boolean[1];

		try {
			InitialContext ctx = new InitialContext();

			/* get custom command-line args */
			String[] args = (String[]) ctx.lookup("java:comp/env/tommy/args");

			ParseCommandLineUtils.parseCommandLineArgs(logger, args, dumps, noListenArr, noEtagArr);
			noListen = noListenArr[0];
			noEtag   = noEtagArr[0];

			boolean activateDirWatchers = !noListen;
			reload(activateDirWatchers);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		String contextPath = servletConfig.getServletContext().getContextPath();
		logger.log(Level.INFO, "Context path: {0}", contextPath);

		freeMarkerConfiguration = new Configuration(Configuration.VERSION_2_3_31);
		freeMarkerConfiguration.setDefaultEncoding("UTF-8");
		freeMarkerConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		freeMarkerConfiguration.setLogTemplateExceptions(false);
		freeMarkerConfiguration.setWrapUncheckedExceptions(true);
		freeMarkerConfiguration.setFallbackOnNullLoopVariable(false);
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

				try {

					String method              = request.getMethod().trim().toUpperCase(Locale.ENGLISH);
					String providedPath        = request.getServletPath() + request.getPathInfo();
					String providedQueryString = HttpPathUtils.extractQueryStringFromPath(providedPath);
					String protocol            = request.getProtocol();

					String providedFirstLineStr = method + ' ' + providedPath + ' ' + protocol;
					String providedBody         = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

					Map<String, List<String>> providedParams = new LinkedHashMap<>();
					if (!providedQueryString.isEmpty())
						HttpPathUtils.parseQueryString(providedQueryString, providedParams);
					String requestHeaderContentType = request.getHeader(HTTP_HEADER_CONTENT_TYPE);
					if (requestHeaderContentType != null && requestHeaderContentType.startsWith("application/x-www-form-urlencoded"))
						HttpPathUtils.parseQueryString(providedBody, providedParams);

					String bodyType      = null;
					int    requestDelay  = 0;
					int    responseDelay = 0;

					/* search for request-reponse pair */
					ReqResp                   reqResp                 = null;
					Map<String, List<String>> providedHeaderValuesMap = new LinkedHashMap<>();
					for (ReqResp rr : allReqResps) {
						if (new WildcardMatch().match(providedFirstLineStr, rr.request.firstLine)) {
							for (String headerStr : rr.request.headers) {
								Header header              = ParseDumpUtils.parseHeader(headerStr);
								String lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);
								if (lowerCaseHeaderName.equals(INTERNAL_HTTP_HEADER_X_SERVER_REQUEST_DELAY.toLowerCase(Locale.ENGLISH)))
									requestDelay = Integer.parseInt(header.value);
							}
							for (String headerStr : rr.response.headers) {
								Header header              = ParseDumpUtils.parseHeader(headerStr);
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
							for (String headerStr : rr.request.headers) {
								Header       header              = ParseDumpUtils.parseHeader(headerStr);
								String       lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);
								List<String> headerValuesList    = headerValuesMap.get(lowerCaseHeaderName);
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
										ok = matchValue(value, providedValues);
									if (!ok)
										break;
								}
								if (!ok)
									break;
							}
							if (ok)
								if (rr.request.body.toString().strip().isEmpty()) {
									reqResp = rr;
									break;
								} else {
									if (rr.request.body.toString().strip().equals(providedBody.strip())) {
										reqResp = rr;
										break;
									}
								}
						}
					}

					if (reqResp == null) { // request-reponse pair not found
						if (badRequest400 == null) {
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							asyncContext.complete();
							return;
						}
						reqResp = badRequest400;
					}

					String        responseFirstLineStr = reqResp.response.firstLine;
					FirstLineResp firstLineResp        = ParseDumpUtils.parseFirstLineResp(responseFirstLineStr);

					int                 status          = firstLineResp.status;
					String              message         = firstLineResp.message;
					String              mime            = null;
					String              encoding        = null;
					Map<String, String> responseHeaders = new LinkedHashMap<>();

					for (String headerStr : reqResp.response.headers) {
						Header header              = ParseDumpUtils.parseHeader(headerStr);
						String lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);

						if (INTERNAL_HTTP_HEADER_X_SERVER_BODY_TYPE.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							continue;
						else if (INTERNAL_HTTP_HEADER_X_SERVER_REQUEST_DELAY.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							continue;
						else if (INTERNAL_HTTP_HEADER_X_SERVER_RESPONSE_DELAY.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							continue;

						responseHeaders.put(header.name, header.value);
					}

					if (!responseHeaders.containsKey(HTTP_HEADER_X_POWERED_BY))
						responseHeaders.put(HTTP_HEADER_X_POWERED_BY, X_POWERED_BY_VALUE);

					if (requestDelay != 0)
						Thread.sleep(requestDelay);

					for (String headerStr : reqResp.request.headers) {
						Header header              = ParseDumpUtils.parseHeader(headerStr);
						String lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);

						if (HTTP_HEADER_CONNECTION.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							connectionKeepAlive = "keep-alive".equals(header.value.toLowerCase(Locale.ENGLISH));
					}

					for (String headerStr : reqResp.response.headers) {
						Header header              = ParseDumpUtils.parseHeader(headerStr);
						String lowerCaseHeaderName = header.name.toLowerCase(Locale.ENGLISH);

						if (HTTP_HEADER_CONNECTION.toLowerCase(Locale.ENGLISH).equals(lowerCaseHeaderName))
							connectionKeepAlive = "keep-alive".equals(header.value.toLowerCase(Locale.ENGLISH));
					}

					String body = reqResp.response.body.toString();

					byte[] bs;
					if ("text/template".equals(bodyType)) {
						Template     freeMarkerTemplate = new Template("", new StringReader(body), freeMarkerConfiguration);
						StringWriter writer             = new StringWriter();
						try {
							freeMarkerTemplate.process(providedParams, writer);
						} catch (Exception e) {
							status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR; // setting HTTP 500 and returning with empty body
							bs     = new byte[0];
							e.printStackTrace();
						}
						body = writer.toString();
						bs   = body.getBytes(StandardCharsets.UTF_8);
					} else if ("application/x-sh".equals(bodyType)) {
						Map<String, Object> http    = createHttpObject(method, providedPath, protocol, providedParams, providedHeaderValuesMap, providedBody.getBytes(StandardCharsets.UTF_8), protocol, status, message, responseHeaders, new byte[0]);
						String              json    = JacksonUtils.stringifyToJson(http) + '\n';
						Path                tmpFile = Files.createTempFile("df-", "tmp");
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
							bs = baos.toByteArray();
						}
						int exitCode = process.waitFor();
						Files.deleteIfExists(tmpFile);
						logger.log(exitCode == 0 ? Level.INFO : Level.WARNING, "Exit code: {0}", exitCode);
						process.destroy();
					} else if ("application/javascript".equals(bodyType)) {
						Map<String, Object> http = createHttpObject(method, providedPath, protocol, providedParams, providedHeaderValuesMap, providedBody.getBytes(StandardCharsets.UTF_8), protocol, status, message, responseHeaders, new byte[0]);
						try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos);) {
							PrintStream stdout = System.out;
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
							bs = baos.toByteArray();
							System.setOut(stdout);
						} catch (Throwable e) {
							status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR; // setting HTTP 500 and returning with empty body
							bs     = new byte[0];
							e.printStackTrace();
						} finally {
							Context.exit();
						}
					} else if ("text/uri-list".equals(bodyType)) {
						if (body.startsWith("file:") || body.startsWith("https:") || body.startsWith("http:")) {
							InputStream is = new URL(body).openStream();
							bs = is.readAllBytes();
							is.close();
						} else if (body.startsWith("data:")) { // data URI
							StringBuilder mediatypeSb = new StringBuilder();
							StringBuilder encodingSb  = new StringBuilder();
							bs = DataUriUtils.parseDataUri(body, mediatypeSb, encodingSb);
							String mimeStr = mediatypeSb.toString().strip();
							if (!mimeStr.isEmpty())
								mime = mimeStr;
							String encodingStr = encodingSb.toString().strip();
							if (!encodingStr.isEmpty())
								encoding = encodingStr;
						} else { // fallback to ignored bodyType
							logger.log(Level.WARNING, "\"X-Body-Type: {0}\", but body content is not valid URL or URL protocol is not supported. Ignored. Valid URL protocols: file, http, https, data", bodyType);
							bs = body.getBytes(StandardCharsets.UTF_8);
						}
					} else { // Unknown or null bodyType
						bs = body.getBytes(StandardCharsets.UTF_8);
						if (bodyType != null) // Unknown bodyType
							logger.log(Level.WARNING, "\"X-Body-Type: {0}\" is not supported. Ignored.", bodyType);
					}
					if (responseDelay != 0)
						Thread.sleep(responseDelay);

					if (mime != null)
						responseHeaders.put(HTTP_HEADER_CONTENT_TYPE, mime + (encoding == null ? "" : "; charset=" + encoding));
					if (!responseHeaders.containsKey(HTTP_HEADER_CONTENT_LENGTH))
						responseHeaders.put(HTTP_HEADER_CONTENT_LENGTH, Integer.toString(bs.length));

					if (!noEtag) {
						String etag = "\"" + Integer.toHexString(Murmur3.hash32(bs)) + "\""; // Murmur3 32-bit variant

						String  etagFromClient = request.getHeader(HTTP_HEADER_IF_NONE_MATCH);
						boolean etagMatched    = etag.equals(etagFromClient);
						if (etagMatched)
							status = HttpServletResponse.SC_NOT_MODIFIED; // setting HTTP 304 and returning with empty body
						else
							responseHeaders.put(HTTP_HEADER_E_TAG, etag);
					}

					if (message == null)
						response.setStatus(status);
					else
						response.setStatus(status, message);

					for (Map.Entry<String, String> entry : responseHeaders.entrySet())
						response.setHeader(entry.getKey(), entry.getValue());

					OutputStream responseOutputStream = response.getOutputStream();
					responseOutputStream.write(bs);
					responseOutputStream.flush();
				} catch (Throwable e) {
					e.printStackTrace();
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
	 *     "path": "/form.html",
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
		httpRequest.put("path", requestPath);
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
	private static ReqResp getBadRequest400(List<ReqResp> reqResps) throws Exception {
		for (ReqResp reqResp : reqResps) {
			FirstLineResp firstLineResp = ParseDumpUtils.parseFirstLineResp(reqResp.response.firstLine);
			if (firstLineResp.status == HttpServletResponse.SC_BAD_REQUEST)
				return reqResp;
		}
		return null;
	}

	/**
	 * 
	 * @param value
	 * @param providedValues
	 * @return
	 */
	private boolean matchValue(String value, Collection<String> providedValues) {
		for (String providedValue : providedValues)
			if (new WildcardMatch().match(providedValue, value))
				return true;
		return false;
	}

	/**
	 * 
	 * @param activateDirWatchers
	 * @throws Throwable
	 */
	private void reload(boolean activateDirWatchers) throws Throwable {
		allReqResps = ParseCommandLineUtils.getAllReqResp(logger, dumps);

		for (Map.Entry<String /* dump file */, String /* dump content */ > entry : dumps.entrySet()) {
			String dumpFile = entry.getKey();
			Path   path     = Paths.get(dumpFile);
			Path   dirPath  = path.getParent();
			Path   filePath = path.getFileName();

			if (activateDirWatchers) {
				DirectoryWatcher dirWatcher = directoryWatchersMap.get(dirPath);
				if (dirWatcher == null) {
					dirWatcher = new DirectoryWatcher(dirPath);
					Thread dirWatcherThread = new Thread(dirWatcher);
					dirWatcherThread.start();
					directoryWatchersMap.put(dirPath, dirWatcher);
				}
				dirWatcher.addFile(filePath);
			}
		}
		badRequest400 = getBadRequest400(allReqResps);
	}

}
