package org.deepfake_http.common.utils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.ScriptableObject;

import freemarker.template.DefaultListAdapter;
import freemarker.template.DefaultMapAdapter;
import freemarker.template.TemplateHashModelEx2.KeyValuePair;
import freemarker.template.TemplateHashModelEx2.KeyValuePairIterator;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class RandomMethod implements TemplateMethodModelEx {

	@Override
	public Object exec(List args) throws TemplateModelException {
		if (args.size() != 1)
			throw new TemplateModelException("Wrong args count");
		Object var = (Object) args.get(0);

		int len;
		int rnd;
		if (var instanceof DefaultListAdapter) {
			DefaultListAdapter defaultListAdapter = (DefaultListAdapter) var;
			len = defaultListAdapter.size();
			rnd = ThreadLocalRandom.current().nextInt(0, len);
			return defaultListAdapter.get(rnd);
		} else if (var instanceof DefaultMapAdapter) {
			DefaultMapAdapter defaultMapAdapter = (DefaultMapAdapter) var;
			len = defaultMapAdapter.size();
			rnd = ThreadLocalRandom.current().nextInt(0, len);

			int n = 0;
			for (KeyValuePairIterator keyValuePairIterator = defaultMapAdapter.keyValuePairIterator(); keyValuePairIterator.hasNext();) {
				KeyValuePair keyValuePair = (KeyValuePair) keyValuePairIterator.next();
				if (n == rnd)
					return keyValuePair.getValue();
				n++;
			}
		} else
			throw new TemplateModelException(MessageFormat.format("Random filed. \"{0}\" is not JSON Object or Array", var.getClass()));
		return new UnknownError();
	}

	/**
	 * 
	 * @param json
	 * @param var
	 * @return
	 * @throws IOException
	 */
	private static Object getObject(String json, String var) {
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