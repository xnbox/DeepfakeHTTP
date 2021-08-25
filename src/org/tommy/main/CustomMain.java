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

package org.tommy.main;

public class CustomMain {
	private static final String ARGS_HELP_OPTION = "--help";

	/**
	 * Custom main method (called for emmbedded web apps)
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		boolean help = false;
		for (int i = 0; i < args.length; i++)
			if (args[i].equals(ARGS_HELP_OPTION)) {
				help = true;
				break;
			}

		if (help) {
			StringBuilder sb = new StringBuilder();
			sb.append("\n");
			sb.append(" 🟩 DeepfakeHTTP Web Server " + System.getProperty("build.version") + '\n');
			sb.append("\n");
			sb.append(" Usage:\n");
			sb.append("\n");
			sb.append(" java -jar df.jar [options]\n");
			sb.append("\n");
			sb.append(" Options:\n");
			sb.append("  --help          print help message\n");
			sb.append("  --port          port number, default: 8080\n");
			sb.append("  --no-listen     disable listening on dump(s) changes\n");
			sb.append("  --no-etag       disable ETag optimization\n");
			System.out.println(sb);
			System.exit(0);
		}
	}
}
