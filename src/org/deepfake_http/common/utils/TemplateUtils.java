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
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TemplateUtils {
	private static final String TEMPLATE_RANDOM_FUNCTION_NAME = "$";

	/**
	 * 
	 * @param processTemplate
	 * @param freeMarkerConfiguration
	 * @param s
	 * @param dataMap
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public static String processTemplate(boolean processTemplate, Configuration freeMarkerConfiguration, String s, Map<String, Object> dataMap) throws IOException, TemplateException {
		if (processTemplate) {
			dataMap.put(TEMPLATE_RANDOM_FUNCTION_NAME, new RandomMethod());
			Template freeMarkerTemplate = new Template("", new StringReader(s), freeMarkerConfiguration);
			try (StringWriter writer = new StringWriter()) {
				freeMarkerTemplate.process(dataMap, writer);
				return writer.toString();
			}
		}
		return s;
	}

}
