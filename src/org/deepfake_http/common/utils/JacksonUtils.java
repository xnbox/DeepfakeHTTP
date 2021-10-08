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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TSFBuilder;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.csv.CsvFactoryBuilder;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.skjolber.jackson.jsh.AnsiSyntaxHighlight;
import com.github.skjolber.jackson.jsh.DefaultSyntaxHighlighter;
import com.github.skjolber.jackson.jsh.SyntaxHighlighter;
import com.github.skjolber.jackson.jsh.SyntaxHighlightingJsonGenerator;

public class JacksonUtils {

	public static final String FORMAT_JSON = "json";
	public static final String FORMAT_YAML = "yaml";

	/**
	 * Convert Java objects (E.g. Map or List) to JSON formatted string
	 *
	 * @param obj
	 * @param format
	 * @param prettyprint
	 * @param color
	 * @return
	 * @throws IOException
	 */
	public static String stringifyToJsonYaml(Object obj, String format, boolean prettyprint, boolean color) throws IOException {
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
		try (StringWriter sw = new StringWriter(); JsonGenerator delegate = om.createGenerator(sw)) {
			if (color) {
				SyntaxHighlighter highlighter   = DefaultSyntaxHighlighter.newBuilder()                                   //

						.withCurlyBrackets(AnsiSyntaxHighlight.YELLOW, AnsiSyntaxHighlight.LOW_INTENSITY)                 //
						.withSquareBrackets(AnsiSyntaxHighlight.YELLOW, AnsiSyntaxHighlight.LOW_INTENSITY)                //
						.withColon(AnsiSyntaxHighlight.YELLOW, AnsiSyntaxHighlight.LOW_INTENSITY)                         //
						.withComma(AnsiSyntaxHighlight.YELLOW, AnsiSyntaxHighlight.LOW_INTENSITY)                         //

						.withField(AnsiSyntaxHighlight.WHITE, AnsiSyntaxHighlight.LOW_INTENSITY)                          //

						.withBoolean(AnsiSyntaxHighlight.MAGENTA, AnsiSyntaxHighlight.HIGH_INTENSITY)                     //
						.withNull(AnsiSyntaxHighlight.CYAN, AnsiSyntaxHighlight.HIGH_INTENSITY)                           //
						.withNumber(AnsiSyntaxHighlight.BLUE, AnsiSyntaxHighlight.HIGH_INTENSITY)                         //
						.withString(AnsiSyntaxHighlight.GREEN)                                                            //

						.build();
				JsonGenerator     jsonGenerator = new SyntaxHighlightingJsonGenerator(delegate, highlighter, prettyprint);
				if (obj == null)
					jsonGenerator.writeNull();
				else if (obj instanceof String)
					jsonGenerator.writeString((String) obj);
				else if (obj instanceof Boolean)
					jsonGenerator.writeBoolean((Boolean) obj);
				else if (obj instanceof Integer)
					jsonGenerator.writeNumber((Integer) obj);
				else if (obj instanceof Long)
					jsonGenerator.writeNumber((Long) obj);
				else if (obj instanceof Float)
					jsonGenerator.writeNumber((Float) obj);
				else if (obj instanceof Float)
					jsonGenerator.writeNumber((Double) obj);
				else if (obj instanceof Short)
					jsonGenerator.writeNumber((Short) obj);
				else
					jsonGenerator.writeObject(obj);
				jsonGenerator.close();
			} else
				delegate.writeObject(obj);
			return sw.toString();
		}
	}

	/**
	 * Parse JSON/YAML/CSV object to Map<String, Object> or List<String>
	 *
	 * @param s - JSON/YAML/CSV
	 * @return
	 * @throws JsonProcessingException
	 */
	public static JsonNode parseJsonYamlToMap(String s) throws JsonProcessingException {
		s = s.strip();
		TSFBuilder tsfBuilder;
		if (s.startsWith("{") || s.startsWith("[")) {
			tsfBuilder = JsonFactory.builder(); //
			tsfBuilder //
					.enable(JsonReadFeature.ALLOW_TRAILING_COMMA) //
					.enable(JsonReadFeature.ALLOW_YAML_COMMENTS) //
					.enable(JsonReadFeature.ALLOW_MISSING_VALUES) //
					.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES) //
					.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES); //
			return parseJsonYamlToJsonNode(tsfBuilder.build(), s);
		} else if (s.startsWith("---")) {
			tsfBuilder = YAMLFactory.builder(); //
			tsfBuilder //
					.enable(JsonReadFeature.ALLOW_TRAILING_COMMA) //
					.enable(JsonReadFeature.ALLOW_YAML_COMMENTS) //
					.enable(JsonReadFeature.ALLOW_MISSING_VALUES) //
					.enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES) //
					.enable(JsonReadFeature.ALLOW_SINGLE_QUOTES); //
			return parseJsonYamlToJsonNode(tsfBuilder.build(), s);
		} else {
			tsfBuilder = CsvFactory.builder(); //
			((CsvFactoryBuilder) tsfBuilder) //
					.enable(CsvParser.Feature.WRAP_AS_ARRAY) //
					.enable(CsvParser.Feature.TRIM_SPACES) //
					.enable(CsvParser.Feature.ALLOW_COMMENTS) //
					.enable(CsvParser.Feature.EMPTY_STRING_AS_NULL) //
					.enable(CsvParser.Feature.INSERT_NULLS_FOR_MISSING_COLUMNS) //
					.enable(CsvParser.Feature.SKIP_EMPTY_LINES); //
			JsonNode           jsonNodeCsv = parseJsonYamlToJsonNode(tsfBuilder.build(), s);
			ObjectMapper       om          = new ObjectMapper();
			List<List<String>> csv         = om.treeToValue(jsonNodeCsv, List.class);
			if (csv.isEmpty())
				return parseJsonYamlToJsonNode(tsfBuilder.build(), "{}");
			List<String>              headers = csv.get(0);
			List<Map<String, Object>> list    = new ArrayList(csv.size() - 1);
			for (int r = 1; r < csv.size(); r++) {
				List<String>        values = csv.get(r);
				Map<String, Object> rowMap = new LinkedHashMap<>(headers.size());
				for (int c = 0; c < headers.size(); c++) {
					String columnName  = headers.get(c);
					String columnValue = values.get(c);
					rowMap.put(columnName, columnValue);
				}
				list.add(rowMap);
			}
			return om.valueToTree(list);
		}
	}

	/**
	 * Parse JSON/YAML/CSV
	 *
	 * @param s - JSON/YAML/CSV
	 * @return
	 * @throws JsonProcessingException
	 */
	private static JsonNode parseJsonYamlToJsonNode(JsonFactory jsonFactory, String s) throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper(jsonFactory);
		om.configure(Feature.ALLOW_COMMENTS, true);
		om.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		om.configure(Feature.ALLOW_SINGLE_QUOTES, true);
		return om.readTree(s);
	}

}
