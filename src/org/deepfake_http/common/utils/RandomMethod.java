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
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import freemarker.template.DefaultListAdapter;
import freemarker.template.DefaultMapAdapter;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateHashModelEx2.KeyValuePair;
import freemarker.template.TemplateHashModelEx2.KeyValuePairIterator;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class RandomMethod implements TemplateMethodModelEx {

	@Override
	public Object exec(List args) throws TemplateModelException {
		if (args.size() != 1)
			throw new TemplateModelException("Wrong args count");
		Object var = (Object) args.get(0);

		int           len;
		int           rnd;
		TemplateModel tm;
		try {
			if (var == null)
				return "null";
			else if (var instanceof DefaultListAdapter) {
				DefaultListAdapter defaultListAdapter = (DefaultListAdapter) var;
				len = defaultListAdapter.size();
				rnd = ThreadLocalRandom.current().nextInt(0, len);
				tm  = defaultListAdapter.get(rnd);
				return convertTemplateModel(tm);
			} else if (var instanceof DefaultMapAdapter) {
				DefaultMapAdapter defaultMapAdapter = (DefaultMapAdapter) var;
				len = defaultMapAdapter.size();
				rnd = ThreadLocalRandom.current().nextInt(0, len);

				int n = 0;
				for (KeyValuePairIterator keyValuePairIterator = defaultMapAdapter.keyValuePairIterator(); keyValuePairIterator.hasNext();) {
					KeyValuePair keyValuePair = (KeyValuePair) keyValuePairIterator.next();
					if (n == rnd) {
						tm = keyValuePair.getValue();
						return convertTemplateModel(tm);
					}
					n++;
				}
			} else if (var instanceof TemplateModel) {
				tm = (TemplateModel) var;
				if (tm instanceof SimpleNumber)
					return ((SimpleNumber) tm).getAsNumber();
				else if (tm instanceof TemplateBooleanModel)
					return ((TemplateBooleanModel) tm).getAsBoolean();
				else
					return "\"" + tm + "\"";
			} else
				throw new TemplateModelException(MessageFormat.format("Random filed. \"{0}\" is not JSON Object or Array", var.getClass()));
		} catch (Exception e) {
			throw new TemplateModelException(e);
		}
		return new UnknownError();
	}

	private static Object convertTemplateModel(TemplateModel tm) throws IOException, TemplateModelException {
		if (tm == null)
			return "null";
		else if (tm instanceof SimpleNumber)
			return ((SimpleNumber) tm).getAsNumber();
		else if (tm instanceof TemplateBooleanModel)
			return ((TemplateBooleanModel) tm).getAsBoolean();
		else if (tm instanceof DefaultListAdapter) {
			Object obj = ((DefaultListAdapter) tm).getWrappedObject();
			return JacksonUtils.stringifyToJsonYaml(obj, JacksonUtils.FORMAT_JSON, false, false);
		} else if (tm instanceof DefaultMapAdapter) {
			Object obj = ((DefaultMapAdapter) tm).getWrappedObject();
			return JacksonUtils.stringifyToJsonYaml(obj, JacksonUtils.FORMAT_JSON, false, false);
		}
		return "\"" + tm + "\"";
	}

}