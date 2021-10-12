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
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

public class TemplateUtils {
	private static final String RANDOM_JS = "function random(obj){let keys=Object.keys(obj);return obj[keys[Math.floor(Math.random()*keys.length)]];};";

	/**
	 * 
	 * @param ctx
	 * @param scope
	 * @param js
	 * @param dataMap
	 * @return
	 * @throws IOException
	 */
	private static String eval(Context ctx, ScriptableObject scope, String js, Object dataMap) throws IOException {
		try {
			Object obj  = ctx.evaluateString(scope, "r=" + js, "", 0, null);
			Object jobj = Context.jsToJava(obj, Object.class);
			if (jobj instanceof String) {
				js = (String) jobj;
			} else if (jobj instanceof Number) {
				js = jobj.toString();
			} else if (jobj instanceof Boolean) {
				js = jobj.toString();
			} else
				js = JacksonUtils.stringifyToJsonYaml(jobj, JacksonUtils.FORMAT_JSON, false, false);
		} catch (NullPointerException e) {
			js = "null";
		}
		return js;
	}

	/**
	 * 
	 * @param ctx
	 * @return
	 */
	public static ScriptableObject createScope(Context ctx) {
		ScriptableObject scope = new ImporterTopLevel(ctx);
		scope = (ScriptableObject) ctx.initStandardObjects(scope);
		ctx.evaluateString(scope, RANDOM_JS, "", 0, null);
		return scope;
	}

	/**
	 * 
	 * @param template
	 * @param dataMap
	 * @return
	 * @throws IOException
	 */
	public static String processTemplate(ScriptableObject scope, String template, Map<String, Object> dataMap) throws IOException {
		Context ctx = Context.enter();
		ctx.setLanguageVersion(Context.VERSION_1_8);
		ctx.setOptimizationLevel(9);
		ctx.getWrapFactory().setJavaPrimitiveWrap(true);

		ScriptableObject.putProperty(scope, "data", Context.javaToJS(dataMap.get("data"), scope));
		ScriptableObject.putProperty(scope, "request", Context.javaToJS(dataMap.get("request"), scope));

		while (true) {
			int pos = template.indexOf("${"); // js begin                                      
			if (pos == -1)
				break;
			String rest = template.substring(pos + 2);
			int    pos2 = rest.indexOf("}");          // js end                     
			String js   = rest.substring(0, pos2);

			String replacer = eval(ctx, scope, js, dataMap);
			template = template.substring(0, pos) + replacer + template.substring(pos + js.length() + 3);
		}
		Context.exit();
		return template;
	}

	/**
	 * 
	 * @param script
	 * @param map
	 * @return
	 * @throws IOException
	 */
	public static List<String> processData(ScriptableObject scope, String funcName, Map<String, Object> map, boolean jsonRequest) throws IOException {
		Context ctx = Context.enter();
		ctx.setLanguageVersion(Context.VERSION_1_8);
		ctx.setOptimizationLevel(9);
		ctx.getWrapFactory().setJavaPrimitiveWrap(true);
		String script = "(function(){let map=" + JacksonUtils.stringifyToJsonYaml(map, JacksonUtils.FORMAT_JSON, false, false) + ";let data=map.data;let request=map.request;if(" + jsonRequest + ") request.body=JSON.parse(request.body);let response={status:200,headers:{},body:''};";
		script += funcName + "(request,response,data);";
		script += "if (!(response.body === null || typeof response.body === 'string' || response.body instanceof String)) response.body=JSON.stringify(response.body);";
		script += "return [JSON.stringify(data), JSON.stringify(response)];})()";
		List<String> dataJson = (List<String>) ctx.evaluateString(scope, script, "", 0, null);
		Context.exit();
		return dataJson;
	}

}
