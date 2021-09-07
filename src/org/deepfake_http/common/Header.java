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

package org.deepfake_http.common;

public class Header {
	private String headerLine;

	public String name;
	public String value;

	public Header(String headerLine) throws Exception {
		this.headerLine = headerLine;

		int pos = headerLine.indexOf(':');
		if (pos == -1)
			throw new Exception("Invalid header (missing ':')!");
		name = headerLine.substring(0, pos).strip();
		if (name.isEmpty())
			throw new Exception("Invalid header (missing header name)!");
		value = headerLine.substring(pos + 1).strip();
		if (value.isEmpty())
			throw new Exception("Invalid header (missing header value)!");
	}

	@Override
	public String toString() {
		return headerLine;
	}
}
