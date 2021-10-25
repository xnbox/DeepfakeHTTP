/**
 * Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0)
 * https://creativecommons.org/licenses/by-sa/3.0/
 * https://codegolf.stackexchange.com/a/48333
 * https://codegolf.stackexchange.com/users/10588/ilya-gazman
 */
package org.deepfake_http.thirdparty;

public class RemoveCommentsUtils {
	public static final int DEFAULT            = 1;
	public static final int ESCAPE             = 2;
	public static final int STRING             = 3;
	public static final int ONE_LINE_COMMENT   = 4;
	public static final int MULTI_LINE_COMMENT = 5;

	/**
	 * 
	 * @param s
	 * @return
	 */
	public static String removeComments(String s) {
		String out = "";
		int    mod = DEFAULT;
		for (int i = 0; i < s.length(); i++) {
			String substring = s.substring(i, Math.min(i + 2, s.length()));
			char   c         = s.charAt(i);
			switch (mod) {
			case DEFAULT: // default
				mod = substring.equals("/*") ? MULTI_LINE_COMMENT : substring.equals("//") ? ONE_LINE_COMMENT : c == '"' ? STRING : DEFAULT;
				break;
			case STRING: // string
				mod = c == '"' ? DEFAULT : c == '\\' ? ESCAPE : STRING;
				break;
			case ESCAPE: // string
				mod = STRING;
				break;
			case ONE_LINE_COMMENT: // one line comment
				mod = c == '\n' ? DEFAULT : ONE_LINE_COMMENT;
				continue;
			case MULTI_LINE_COMMENT: // multi line comment
				mod = substring.equals("*/") ? DEFAULT : MULTI_LINE_COMMENT;
				i += mod == DEFAULT ? 1 : 0;
				continue;
			}
			out += mod < 4 ? c : "";
		}

		return out;
	}

}
