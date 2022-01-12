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

public interface IAnsi {
	// Reset
	String RESET = "\033[0m"; // Text Reset

	// Regular Colors
	String BLACK  = "\033[0;30m"; // BLACK
	String RED    = "\033[0;31m"; // RED
	String GREEN  = "\033[0;32m"; // GREEN
	String YELLOW = "\033[0;33m"; // YELLOW
	String BLUE   = "\033[0;34m"; // BLUE
	String PURPLE = "\033[0;35m"; // PURPLE
	String CYAN   = "\033[0;36m"; // CYAN
	String WHITE  = "\033[0;37m"; // WHITE

	// Low Intensity
	String BLACK_LOW  = "\033[2;30m"; // BLACK
	String RED_LOW    = "\033[2;31m"; // RED
	String GREEN_LOW  = "\033[2;32m"; // GREEN
	String YELLOW_LOW = "\033[2;33m"; // YELLOW
	String BLUE_LOW   = "\033[2;34m"; // BLUE
	String PURPLE_LOW = "\033[2;35m"; // PURPLE
	String CYAN_LOW   = "\033[2;36m"; // CYAN
	String WHITE_LOW  = "\033[2;37m"; // WHITE

	// Bold
	String BLACK_BOLD  = "\033[1;30m"; // BLACK
	String RED_BOLD    = "\033[1;31m"; // RED
	String GREEN_BOLD  = "\033[1;32m"; // GREEN
	String YELLOW_BOLD = "\033[1;33m"; // YELLOW
	String BLUE_BOLD   = "\033[1;34m"; // BLUE
	String PURPLE_BOLD = "\033[1;35m"; // PURPLE
	String CYAN_BOLD   = "\033[1;36m"; // CYAN
	String WHITE_BOLD  = "\033[1;37m"; // WHITE

	// Underline
	String BLACK_UNDERLINED  = "\033[4;30m"; // BLACK
	String RED_UNDERLINED    = "\033[4;31m"; // RED
	String GREEN_UNDERLINED  = "\033[4;32m"; // GREEN
	String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
	String BLUE_UNDERLINED   = "\033[4;34m"; // BLUE
	String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
	String CYAN_UNDERLINED   = "\033[4;36m"; // CYAN
	String WHITE_UNDERLINED  = "\033[4;37m"; // WHITE

	// Background
	String BLACK_BACKGROUND  = "\033[40m"; // BLACK
	String RED_BACKGROUND    = "\033[41m"; // RED
	String GREEN_BACKGROUND  = "\033[42m"; // GREEN
	String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
	String BLUE_BACKGROUND   = "\033[44m"; // BLUE
	String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
	String CYAN_BACKGROUND   = "\033[46m"; // CYAN
	String WHITE_BACKGROUND  = "\033[47m"; // WHITE

	// High Intensity
	String BLACK_BRIGHT  = "\033[0;90m"; // BLACK
	String RED_BRIGHT    = "\033[0;91m"; // RED
	String GREEN_BRIGHT  = "\033[0;92m"; // GREEN
	String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
	String BLUE_BRIGHT   = "\033[0;94m"; // BLUE
	String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
	String CYAN_BRIGHT   = "\033[0;96m"; // CYAN
	String WHITE_BRIGHT  = "\033[0;97m"; // WHITE

	// Low Intensity
	String BLACK_BACKGROUND_LOW  = "\033[2;40m"; // BLACK
	String RED_BACKGROUND_LOW    = "\033[2;41m"; // RED
	String GREEN_BACKGROUND_LOW  = "\033[2;42m"; // GREEN
	String YELLOW_BACKGROUND_LOW = "\033[2;43m"; // YELLOW
	String BLUE_BACKGROUND_LOW   = "\033[2;44m"; // BLUE
	String PURPLE_BACKGROUND_LOW = "\033[2;45m"; // PURPLE
	String CYAN_BACKGROUND_LOW   = "\033[2;46m"; // CYAN
	String WHITE_BACKGROUND_LOW  = "\033[2;47m"; // WHITE

	// Bold High Intensity
	String BLACK_BOLD_BRIGHT  = "\033[1;90m"; // BLACK
	String RED_BOLD_BRIGHT    = "\033[1;91m"; // RED
	String GREEN_BOLD_BRIGHT  = "\033[1;92m"; // GREEN
	String YELLOW_BOLD_BRIGHT = "\033[1;93m"; // YELLOW
	String BLUE_BOLD_BRIGHT   = "\033[1;94m"; // BLUE
	String PURPLE_BOLD_BRIGHT = "\033[1;95m"; // PURPLE
	String CYAN_BOLD_BRIGHT   = "\033[1;96m"; // CYAN
	String WHITE_BOLD_BRIGHT  = "\033[1;97m"; // WHITE

	// High Intensity backgrounds
	String BLACK_BACKGROUND_BRIGHT  = "\033[0;100m"; // BLACK
	String RED_BACKGROUND_BRIGHT    = "\033[0;101m"; // RED
	String GREEN_BACKGROUND_BRIGHT  = "\033[0;102m"; // GREEN
	String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m"; // YELLOW
	String BLUE_BACKGROUND_BRIGHT   = "\033[0;104m"; // BLUE
	String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
	String CYAN_BACKGROUND_BRIGHT   = "\033[0;106m"; // CYAN
	String WHITE_BACKGROUND_BRIGHT  = "\033[0;107m"; // WHITE
}