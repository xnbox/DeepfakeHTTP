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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

public class JsonUtils {
	/**
	 * This method is not in use!
	 *
	 * @param json
	 * @param var
	 * @return
	 * @throws IOException
	 */

	/* NOT IN USE */
	public static Object getObject(String json, String var) throws IOException {
		try {
			Context cx = Context.enter();
			cx.setLanguageVersion(Context.VERSION_1_8);
			cx.setOptimizationLevel(9);
			cx.getWrapFactory().setJavaPrimitiveWrap(true);
			ScriptableObject scope = new ImporterTopLevel(cx);
			scope = (ScriptableObject) cx.initStandardObjects(scope);
			return cx.evaluateString(scope, "r=" + json + '.' + var, "", 0, null);
		} finally {
			Context.exit();
		}
	}

}
