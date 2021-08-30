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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.deepfake_http.common.ReqResp;

public class ParseCommandLineUtils {
	/* command line args */
	private static final String ARGS_NO_WATCH_OPTION = "--no-watch"; // disable listening on dump(s) changes
	private static final String ARGS_NO_ETAG_OPTION  = "--no-etag";  // disable ETag optimization

	public static List<ReqResp> getDumpReqResp(String dumpFile) throws Throwable {
		String       dump      = Files.readString(new File(dumpFile).toPath());
		List<String> dumpLines = ParseDumpUtils.readLines(dump);
		return ParseDumpUtils.parseDump(dumpFile, dumpLines);
	}

	public static List<ReqResp> getAllReqResp(Logger logger, List<String /* dump file */> dumps) throws Throwable {
		List<ReqResp> allReqResps = new ArrayList<>();
		int           fileCount   = 0;
		for (String dumpFile : dumps) {
			List<ReqResp> dumpReqResp = getDumpReqResp(dumpFile);
			if (logger != null)
				logger.log(Level.INFO, "File: \"{0}\" ({1} entries found)", new Object[] { dumpFile, dumpReqResp.size() });
			allReqResps.addAll(dumpReqResp);
			fileCount++;
		}
		if (logger != null)
			logger.log(Level.INFO, "{0} file(s) processed.{1} entries found", new Object[] { fileCount, allReqResps.size() });
		return allReqResps;
	}

	public static void parseCommandLineArgs(Logger logger, String[] args, List<String /* dump file */> dumps, boolean[] noWatchArr, boolean[] noEtagArr) throws Throwable {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(ARGS_NO_WATCH_OPTION))
				noWatchArr[0] = true;
			else if (args[i].equals(ARGS_NO_ETAG_OPTION))
				noEtagArr[0] = true;
			else {
				String fileName = args[i];
				Path   path     = Paths.get(fileName);
				if (Files.exists(path))
					dumps.add(fileName);
				else {
					if (logger != null)
						logger.log(Level.WARNING, "File \"{0}\" does not exists", fileName);
				}
			}
		}
	}
}
