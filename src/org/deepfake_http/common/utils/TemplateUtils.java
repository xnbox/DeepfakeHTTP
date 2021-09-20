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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TemplateUtils {
	/**
	 * 
	 * @param freeMarkerConfiguration
	 * @param s
	 * @param dataJson
	 * @param dataMap
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public static String processTemplate(Configuration freeMarkerConfiguration, String s, String dataJson, Map<String, Object> dataMap) throws IOException, TemplateException {
		while (true) {
			String newStr = replaceRandomInTemplate(dataJson, s, dataMap);
			if (newStr == null)
				break;
			else
				s = newStr;
		}

		Template freeMarkerTemplate = new Template("", new StringReader(s), freeMarkerConfiguration);
		try (StringWriter writer = new StringWriter()) {
			freeMarkerTemplate.process(dataMap, writer);
			return writer.toString();
		}
	}

	/**
	 * 
	 * @param dataJson
	 * @param s
	 * @param map
	 * @return
	 * @throws IOException
	 */
	private static String replaceRandomInTemplate(String dataJson, String s, Map<String, Object> map) throws IOException {
		int z = 0;
		while (true) {
			int pos = s.indexOf("${", z);
			if (pos == -1)
				return null;
			int pos2 = s.indexOf("}", pos);
			if (pos2 == -1)
				return null;
			z = pos2;
			int pos3 = s.indexOf("[", pos);
			if (pos3 == -1)
				continue;
			if (pos3 > pos2)
				continue;
			int pos4 = s.indexOf("]", pos3);
			if (pos4 == -1)
				continue;
			if (pos4 > pos2)
				continue;
			int pos5 = s.indexOf("$", pos3);
			if (pos5 == -1)
				continue;
			if (pos5 > pos4)
				continue;

			String name   = s.substring(pos + 2, pos3);
			Object object = getObject(dataJson, name);
			if (object instanceof List) {
				List   list  = (List) object;
				int    len   = list.size();
				int    rnd   = ThreadLocalRandom.current().nextInt(0, len);
				String left  = s.substring(0, pos5);
				String right = s.substring(pos5 + 1);
				return left + Integer.toString(rnd) + right;
			}
		}
	}

	/**
	 * 
	 * @param json
	 * @param var
	 * @return
	 * @throws IOException
	 */
	private static Object getObject(String json, String var) throws IOException {
		String s = "r=" + json + '.' + var;
		try {
			Context cx = Context.enter();
			cx.setLanguageVersion(Context.VERSION_1_8);
			cx.setOptimizationLevel(9);
			cx.getWrapFactory().setJavaPrimitiveWrap(true);
			ScriptableObject scope = new ImporterTopLevel(cx);
			scope = (ScriptableObject) cx.initStandardObjects(scope);
			Object result = cx.evaluateString(scope, s, "", 0, null);
			return result;
		} finally {
			Context.exit();
		}
	}
}
