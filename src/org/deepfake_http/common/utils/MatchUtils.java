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

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import ij.util.WildcardMatch;

public class MatchUtils {

	public static boolean matchPath(String template, String path, Map<String, List<String>> paramMap) {
		List<String> templateList = tokenizePath(template);
		List<String> pathList     = tokenizePath(path);
		if (templateList.size() != pathList.size())
			return false;
		for (int i = 0; i < templateList.size(); i++) {
			String templateEl = templateList.get(i);
			String pathEl     = pathList.get(i);
			if (templateEl.startsWith("{") && templateEl.endsWith("}")) {
				String       paramName   = templateEl.substring(1, templateEl.length() - 1);
				List<String> paramValues = paramMap.get(paramName);
				if (paramValues == null) {
					paramValues = new ArrayList<>();
					paramMap.put(paramName, paramValues);
				}
				paramValues.add(pathEl);
			} else if (!Objects.equals(templateEl, pathEl))
				return false;
		}
		return true;
	}

	public static boolean matchQuery(String template, String query, Map<String, List<String>> paramMap) {
		if (template.equals(query))
			return true;
		Map<String, List<String>> templateMap = new LinkedHashMap<>();
		parseQuery(template, templateMap);

		Map<String, List<String>> queryMap = new LinkedHashMap<>();
		parseQuery(query, queryMap);

		for (Entry<String, List<String>> templateEntry : templateMap.entrySet()) {
			String       paramName           = templateEntry.getKey();
			List<String> paramValuesTemplate = templateEntry.getValue();
			List<String> paramValuesQuery    = queryMap.get(paramName);
			if (paramValuesTemplate == null && paramValuesQuery == null)
				continue;
			if (paramValuesTemplate == null && paramValuesQuery != null)
				return false;
			if (paramValuesTemplate != null && paramValuesQuery == null)
				return false;
			if (paramValuesTemplate.size() != paramValuesQuery.size())
				return false;
			for (int i = 0; i < paramValuesTemplate.size(); i++) {
				String paramValueTemplate = paramValuesTemplate.get(i);
				String paramValueQuery    = paramValuesQuery.get(i);
				if (!match(paramValueTemplate, paramValueQuery))
					return false;
				List<String> paramValues = paramMap.get(paramName);
				if (paramValues == null) {
					paramValues = new ArrayList<>();
					paramMap.put(paramName, paramValues);
				}
				paramValues.add(paramValueQuery);
			}
		}
		return true;
	}

	/**
	 * Parse query string
	 *
	 * @param query
	 * @param paramMap
	 */
	public static void parseQuery(String query, Map<String, List<String>> paramMap) {
		final String[] pairs = query.split("&");
		for (String pair : pairs) {
			int          idx  = pair.indexOf('=');
			String       key  = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
			List<String> list = paramMap.get(key);
			if (list == null) {
				list = new ArrayList<>();
				paramMap.put(key, list);
			}
			String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : null;
			list.add(value);
		}
	}

	/**
	 * 
	 * @param value
	 * @param providedValues
	 * @return
	 */
	public static boolean matchHeaderValue(String value, Collection<String> providedValues) {
		for (String providedValue : providedValues)
			if (match(value, providedValue))
				return true;
		return false;
	}

	private static boolean match(String template, String s) {
		if (Objects.equals(template, s))
			return true;
		if (template == null && s != null)
			return false;
		if (template != null && s == null)
			return false;
		template = template.strip();
		s        = s.strip();
		try {
			return new WildcardMatch().match(s, template);
		} catch (Throwable e) {
			return false; // WildcardMatch can throw Exceptions
		}
	}

	private static List<String> tokenizePath(String s) {
		String[]     arr  = s.split("/");
		List<String> list = new ArrayList<>(arr.length);
		for (String el : arr) {
			el = el.trim();
			if (el.isEmpty())
				continue;
			list.add(el);
		}
		return list;
	}

}
