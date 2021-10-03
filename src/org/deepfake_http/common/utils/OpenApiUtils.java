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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.deepfake_http.common.FirstLineReq;
import org.deepfake_http.common.FirstLineResp;
import org.deepfake_http.common.Header;
import org.deepfake_http.common.HttpMethod;
import org.deepfake_http.common.ReqResp;
import org.deepfake_http.common.servlet.DeepfakeHttpServlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OpenApiUtils {
	/**
	 * Serialize requests/responses to OpenAPI 
	 *
	 * @param allReqResps
	 * @return
	 * @throws Exception
	 */
	public static List<ReqResp> openApiMapToListReqResps(Map<String, Object> openApiMap) throws Throwable {
		List<ReqResp>       allReqResps = new ArrayList<>();
		Map<String, Object> mapPaths    = (Map<String, Object>) openApiMap.get("paths");
		if (mapPaths == null)
			return allReqResps;
		for (Entry<String, Object> pathEntry : mapPaths.entrySet()) {
			String              path       = pathEntry.getKey();
			Map<String, Object> mapMethods = (Map<String, Object>) mapPaths.get(path);
			if (mapMethods == null || mapMethods.isEmpty())
				continue;
			String              method         = mapMethods.keySet().iterator().next();
			Map<String, Object> mapMethodProps = (Map<String, Object>) mapMethods.get(method);
			if (mapMethodProps == null || mapMethodProps.isEmpty())
				continue;

			String              requestExample = null;
			String              requestMime    = null;
			Map<String, Object> mapRequestBody = (Map<String, Object>) mapMethodProps.get("requestBody");
			if (mapRequestBody != null && !mapRequestBody.isEmpty()) {
				Map<String, Object> mapRequestBodyContent = (Map<String, Object>) mapRequestBody.get("content");
				if (mapRequestBodyContent != null && !mapRequestBodyContent.isEmpty()) {
					Map.Entry<String, Object> application_jsonMapEntry = mapRequestBodyContent.entrySet().iterator().next();
					requestMime = application_jsonMapEntry.getKey();
					Map<String, Object> mapOpenApiRequestBodyContentMime = (Map<String, Object>) application_jsonMapEntry.getValue();
					if (mapOpenApiRequestBodyContentMime != null && !mapOpenApiRequestBodyContentMime.isEmpty()) {
						Object exampleObj = mapOpenApiRequestBodyContentMime.get("example");
						if (exampleObj != null) {
							if (exampleObj instanceof String)
								requestExample = (String) exampleObj;
							else if (exampleObj instanceof Map || exampleObj instanceof List)
								requestExample = JacksonUtils.stringifyToJsonYaml(exampleObj, JacksonUtils.FORMAT_JSON, false, false);
						}
					}
				}
			}

			Map<String, Object> mapStatuses = (Map<String, Object>) mapMethodProps.get("responses");
			if (mapStatuses == null || mapStatuses.isEmpty())
				continue;
			String              statusStr      = mapStatuses.keySet().iterator().next();
			Map<String, Object> mapStatusProps = (Map<String, Object>) mapStatuses.get(statusStr);
			if (mapStatusProps == null || mapStatusProps.isEmpty())
				continue;
			Map<String, Object> mapContentProps = (Map<String, Object>) mapStatusProps.get("content");
			if (mapContentProps == null || mapContentProps.isEmpty())
				continue;
			String contentType = mapContentProps.keySet().iterator().next();

			Map<String, Object> mapContentTypeProps = (Map<String, Object>) mapContentProps.get(contentType);
			if (mapContentTypeProps == null || mapContentTypeProps.isEmpty())
				continue;
			String example     = (String) mapContentTypeProps.get("example");
			String description = (String) mapStatusProps.get("description");

			List<Map<String, Object>> listParameters = (List<Map<String, Object>>) mapMethodProps.get("parameters");
			if (listParameters == null)
				continue;

			StringBuilder queryStringSb = new StringBuilder();
			boolean       firstParam    = true;
			for (Map<String, Object> mapParamProps : listParameters) {
				String in = (String) mapParamProps.get("in");
				if (!"query".equals(in))
					continue;
				boolean required = (boolean) mapParamProps.get("required");
				if (!required)
					continue;
				String param = (String) mapParamProps.get("name");
				if (firstParam)
					firstParam = false;
				else
					queryStringSb.append('&');
				queryStringSb.append(param + '=');
				if (mapParamProps == null || mapParamProps.isEmpty())
					continue;
				String paramExample = (String) mapParamProps.get("example");
				queryStringSb.append(paramExample);
				Map<String, Object> mapSchemaProps = (Map<String, Object>) mapParamProps.get("schema");
			}
			String queryString = queryStringSb.toString();

			String       openApiMethodSummary     = (String) mapMethodProps.get("summary");
			String       openApiMethodDescription = (String) mapMethodProps.get("description");
			List<String> openApiMethodTags        = (List<String>) mapMethodProps.get("tags");

			ReqResp reqResp = new ReqResp();
			if (openApiMethodTags != null && !openApiMethodTags.isEmpty()) {
				StringBuilder tagsSb   = new StringBuilder();
				boolean       firstTag = true;
				for (String tag : openApiMethodTags) {
					if (firstTag)
						firstTag = false;
					else
						tagsSb.append(',');
					tagsSb.append(tag);
				}
				reqResp.request.headers.add(DeepfakeHttpServlet.INTERNAL_HTTP_HEADER_X_OPENAPI_TAGS + ": " + tagsSb);
			}
			if (openApiMethodSummary != null && !openApiMethodSummary.isEmpty())
				reqResp.request.headers.add(DeepfakeHttpServlet.INTERNAL_HTTP_HEADER_X_OPENAPI_SUMMARY + ": " + openApiMethodSummary);
			if (openApiMethodDescription != null && !openApiMethodDescription.isEmpty())
				reqResp.request.headers.add(DeepfakeHttpServlet.INTERNAL_HTTP_HEADER_X_OPENAPI_DESCRIPTION + ": " + openApiMethodDescription);
			if (requestMime != null)
				reqResp.request.headers.add(DeepfakeHttpServlet.HTTP_HEADER_CONTENT_TYPE + ": " + requestMime);
			reqResp.request.firstLine = method.toUpperCase(Locale.ENGLISH) + ' ' + path + (queryString.isEmpty() ? "" : '?' + queryString) + ' ' + ParseDumpUtils.HTTP_1_1;
			if (requestExample != null)
				reqResp.request.body = requestExample;
			reqResp.response.firstLine = ParseDumpUtils.HTTP_1_1 + ' ' + statusStr;
			reqResp.response.headers.add(DeepfakeHttpServlet.HTTP_HEADER_CONTENT_TYPE + ": " + contentType);
			reqResp.response.body = example;
			allReqResps.add(reqResp);
		}
		return allReqResps;
	}

	/**
	 * Serialize requests/responses to OpenAPI 
	 *
	 * @param allReqResps
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> createOpenApiMap(List<ReqResp> allReqResps, String title) throws Throwable {
		Map<String, Object> mapPaths = new LinkedHashMap<>(allReqResps.size());
		for (ReqResp reqResp : allReqResps) {
			FirstLineReq  firstLineReq  = new FirstLineReq(reqResp.request.firstLine);
			FirstLineResp firstLineResp = new FirstLineResp(reqResp.response.firstLine);

			String                    path            = HttpPathUtils.extractPathFromUri(firstLineReq.getUri());
			String                    method          = firstLineReq.getMethod();
			String                    methodLowerCase = method.toLowerCase(Locale.ENGLISH);
			String                    queryString     = HttpPathUtils.extractQueryStringFromUri(firstLineReq.getUri());
			Map<String, List<String>> queryParams     = new LinkedHashMap<>();
			String                    requestBody     = reqResp.request.body;

			String requestContentType = null;
			for (String headerLine : reqResp.request.headers) {
				Header header = new Header(headerLine);
				if (header.name.toLowerCase(Locale.ENGLISH).equals(DeepfakeHttpServlet.HTTP_HEADER_CONTENT_TYPE.toLowerCase(Locale.ENGLISH))) {
					requestContentType = header.value;
					if (requestContentType.startsWith("application/x-www-form-urlencoded"))
						if (!requestBody.isEmpty())
							MatchUtils.parseQuery(requestBody, queryParams);
					break;
				}
			}

			if (!queryString.isEmpty())
				MatchUtils.parseQuery(queryString, queryParams);
			List<String> pathParams = MatchUtils.extractPathParams(path);

			Map<String, Object> mapMethods = (Map<String, Object>) mapPaths.get(path);
			if (mapMethods == null) {
				mapMethods = new TreeMap<>(new Comparator<String>() {

					@Override
					public int compare(String method1, String method2) {
						int ord1 = HttpMethod.valueOf(method1.toUpperCase(Locale.ENGLISH)).ordinal();
						int ord2 = HttpMethod.valueOf(method2.toUpperCase(Locale.ENGLISH)).ordinal();
						if (ord1 > ord2)
							return 1;
						else if (ord1 < ord2)
							return -1;
						else
							return 0;
					}
				});
				mapPaths.put(path, mapMethods);
			}

			Map<String, Object> mapMethodProps = new LinkedHashMap<>();
			mapMethods.put(methodLowerCase, mapMethodProps);

			List<Map<String, Object>> listParameters = new ArrayList<>();
			Map<String, Object>       mapSchemaProps = new LinkedHashMap<>();
			mapSchemaProps.put("type", "string");
			for (String param : pathParams) {
				Map<String, Object> mapParamProps = new LinkedHashMap<>();
				mapParamProps.put("in", "path");
				mapParamProps.put("name", param);
				mapParamProps.put("required", true);
				mapParamProps.put("schema", mapSchemaProps);
				listParameters.add(mapParamProps);
			}
			for (String param : queryParams.keySet()) {
				Map<String, Object> mapParamProps = new LinkedHashMap<>();
				mapParamProps.put("in", "query");
				mapParamProps.put("name", param);
				mapParamProps.put("required", true);
				mapParamProps.put("schema", mapSchemaProps);
				List<String> examples = queryParams.get(param);
				String       example  = examples.get(0);
				if (example == null || example.isEmpty()) {
					mapParamProps.put("allowEmptyValue", true);
					mapParamProps.put("default", "");
				} else if (example.equals("*"))
					mapParamProps.put("allowEmptyValue", true);
				else {
					if (!example.contains("?"))
						mapParamProps.put("example", example);
				}
				listParameters.add(mapParamProps);
			}

			Map<String, Object> mapStatuses = new LinkedHashMap<>();
			mapMethodProps.put("parameters", listParameters);
			mapMethodProps.put("responses", mapStatuses);

			Map<String, Object> mapStatusProps = new LinkedHashMap<>();
			mapStatuses.put(Integer.toString(firstLineResp.getStatus()), mapStatusProps);
			Map<String, Object> mapContentProps = new LinkedHashMap<>();

			String       openApiMethodSummary     = "";
			String       openApiMethodDescription = "";
			List<String> openApiMethodTags        = new ArrayList<>();

			for (String headerLine : reqResp.request.headers) {
				Header header              = new Header(headerLine);
				String lowerCaseHeaderName = header.name.toLowerCase();
				String headerValue         = header.value;
				if (lowerCaseHeaderName.equals(DeepfakeHttpServlet.INTERNAL_HTTP_HEADER_X_OPENAPI_SUMMARY.toLowerCase(Locale.ENGLISH)))
					openApiMethodSummary = headerValue;
				else if (lowerCaseHeaderName.equals(DeepfakeHttpServlet.INTERNAL_HTTP_HEADER_X_OPENAPI_DESCRIPTION.toLowerCase(Locale.ENGLISH)))
					openApiMethodDescription = headerValue;
				else if (lowerCaseHeaderName.equals(DeepfakeHttpServlet.INTERNAL_HTTP_HEADER_X_OPENAPI_TAGS.toLowerCase(Locale.ENGLISH))) {
					String[] arr = headerValue.split(",");
					for (String val : arr) {
						val = val.strip();
						if (!val.isEmpty())
							openApiMethodTags.add(val);
					}
				}
			}
			if (!openApiMethodSummary.isEmpty())
				mapMethodProps.put("summary", openApiMethodSummary);
			if (!openApiMethodDescription.isEmpty())
				mapMethodProps.put("description", openApiMethodDescription);
			if (!openApiMethodTags.isEmpty())
				mapMethodProps.put("tags", openApiMethodTags);

			Object objBody;
			if (requestContentType != null && !requestBody.isEmpty()) {
				Map<String, Object> mapOpenApiRequestBody = new LinkedHashMap<>();
				mapMethodProps.put("requestBody", mapOpenApiRequestBody);

				Map<String, Object> mapOpenApiRequestBodyContent = new LinkedHashMap<>();
				mapOpenApiRequestBody.put("content", mapOpenApiRequestBodyContent);

				Map<String, Object> mapOpenApiRequestBodyContentMime = new LinkedHashMap<>();
				if (requestContentType != null)
					mapOpenApiRequestBodyContent.put(requestContentType, mapOpenApiRequestBodyContentMime);

				if (requestContentType.startsWith("application/json")) {
					JsonNode jsonNode = JacksonUtils.parseJsonYamlToMap(requestBody);
					objBody = new ObjectMapper().treeToValue(jsonNode, Object.class);
				} else
					objBody = requestBody;
				mapOpenApiRequestBodyContentMime.put("example", objBody);
				mapOpenApiRequestBodyContentMime.put("schema", new HashMap<>(0));
			}

			String contentType = null;
			for (String headerLine : reqResp.response.headers) {
				Header header              = new Header(headerLine);
				String lowerCaseHeaderName = header.name.toLowerCase();
				String headerValue         = header.value;
				if (lowerCaseHeaderName.equals(DeepfakeHttpServlet.HTTP_HEADER_CONTENT_TYPE.toLowerCase(Locale.ENGLISH))) {
					contentType = headerValue;
					break;
				}
			}
			if (contentType == null) {
				for (String headerLine : reqResp.request.headers) {
					Header header = new Header(headerLine);
					if (header.name.toLowerCase().equals(DeepfakeHttpServlet.HTTP_HEADER_CONTENT_TYPE.toLowerCase(Locale.ENGLISH))) {
						contentType = header.value;
						break;
					}
				}
			}
			Map<String, Object> mapContentTypeProps = new LinkedHashMap<>();
			mapContentTypeProps.put("example", reqResp.response.body.toString().strip());

			if (contentType != null)
				mapContentProps.put(contentType, mapContentTypeProps);

			mapStatusProps.put("content", mapContentProps);
			mapStatusProps.put("description", "");
		}
		Map mapOpenApi = new LinkedHashMap<>();
		Map mapInfo    = new LinkedHashMap<>();
		mapOpenApi.put("openapi", "3.0.3");
		mapOpenApi.put("info", mapInfo);
		mapOpenApi.put("paths", mapPaths);
		mapInfo.put("version", "");
		mapInfo.put("title", title);
		return mapOpenApi;
	}
}
