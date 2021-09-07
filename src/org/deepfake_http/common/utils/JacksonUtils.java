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

import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TSFBuilder;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class JacksonUtils {

	public static final String FORMAT_JSON = "json";
	public static final String FORMAT_YAML = "yaml";

	/**
	 * Convert Java objects (E.g. Map or List) to JSON formatted string
	 *
	 * @param obj
	 * @param format
	 * @param prettyprint
	 * @return
	 * @throws JsonProcessingException
	 */
	public static String stringifyToJsonYaml(Object obj, String format, boolean prettyprint) throws JsonProcessingException {
		format = format.toLowerCase(Locale.ENGLISH);
		JsonFactory jsonFactory;
		if (FORMAT_JSON.equals(format))
			jsonFactory = new JsonFactory();
		else if (FORMAT_YAML.equals(format))
			jsonFactory = new YAMLFactory();
		else
			throw new IllegalArgumentException(format);

		ObjectMapper om = new ObjectMapper(jsonFactory);

		om.configure(SerializationFeature.INDENT_OUTPUT, prettyprint);
		return om.writeValueAsString(obj);
	}

	/**
	 * Parse JSON/YAML object to Map<String, Object>
	 *
	 * @param s - JSON/YAML
	 * @return
	 * @throws JsonProcessingException
	 */
	public static Map<String, Object> parseJsonYamlToMap(String s) throws JsonProcessingException {
		s = s.strip();
		TSFBuilder tsfBuilder;
		if (s.startsWith("{"))
			tsfBuilder = JsonFactory.builder(); //
		else if (s.startsWith("---"))
			tsfBuilder = YAMLFactory.builder(); //
		else
			throw new IllegalArgumentException();
		tsfBuilder //
				.enable(JsonReadFeature.ALLOW_TRAILING_COMMA) //
				.enable(JsonReadFeature.ALLOW_YAML_COMMENTS) //
				.enable(JsonReadFeature.ALLOW_MISSING_VALUES) //
				.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES) //
				.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES); //
		return parseJsonYaml(tsfBuilder.build(), s, Map.class);
	}

	/**
	 * Parse JSON/YAML
	 *
	 * @param s - JSON/YAML
	 * @return
	 * @throws JsonProcessingException
	 */
	public static <T> T parseJsonYaml(JsonFactory jsonFactory, String s, Class<T> t) throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper(jsonFactory);
		om.configure(Feature.ALLOW_COMMENTS, true);
		om.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		om.configure(Feature.ALLOW_SINGLE_QUOTES, true);
		return om.readValue(s, t);
	}
}
