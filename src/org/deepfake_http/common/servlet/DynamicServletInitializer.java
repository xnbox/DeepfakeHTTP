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

package org.deepfake_http.common.servlet;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRegistration.Dynamic;
import jakarta.servlet.annotation.WebListener;

/**
 * Servlets initialization.
 */
@WebListener
public class DynamicServletInitializer implements ServletContextListener {
	private static final String SERVLET_NAME = "DeepfakeHttpServlet";

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext servletContext = servletContextEvent.getServletContext();
		Dynamic        registered     = servletContext.addServlet(SERVLET_NAME, DeepfakeHttpServlet.class);

		/**
		The maximum size allowed for uploaded files, in bytes.
		If the size of any uploaded file is greater than this size, the web container will throw an exception (IllegalStateException).
		The default size is unlimited.
		*/
		final long maxFileSize = Long.MAX_VALUE;

		/**
		The maximum size allowed for a multipart/form-data request, in bytes.
		The web container will throw an exception if the overall size of all uploaded files exceeds this threshold.
		The default size is unlimited.
		*/
		final long maxRequestSize = Long.MAX_VALUE;

		registered.setMultipartConfig(new MultipartConfigElement("", maxFileSize, maxRequestSize, 0));
		registered.addMapping("/*");
		registered.setLoadOnStartup(0);
		registered.setAsyncSupported(true);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}
