/* LogConfigurer.java

	Purpose:
		
	Description:
		
	History:
		Fri Dec 16 23:14:07 TST 2011, Created by tomyeh

Copyright (C) 2011 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zk.ui.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.lang.Library;
import org.zkoss.mesg.MCommon;
import org.zkoss.mesg.Messages;

/**
 * Utilities for configure the log.
 * @author tomyeh
 * @since 6.0.0
 * @deprecated As of release 7.0.0, use SLF4J API for logging instead.
 */
/*package*/ class LogConfigurer {
	private static final Logger log = LoggerFactory.getLogger(LogConfigurer.class);

	/*package*/ static void configure() {
		final String LIBPROP = "org.zkoss.util.logging.config.file";
		final String path = Library.getProperty(LIBPROP);
		if (path == null || path.length() == 0)
			return;

		InputStream is = LogConfigurer.class.getResourceAsStream(path);
		try {
			if (is == null) {
				File file = new File(path);
				if (!file.isAbsolute())
					file = new File(System.getProperty("user.dir", "."), path);
				if (file.exists())
					try {
						is = new FileInputStream(file);
					} catch (java.io.FileNotFoundException ex) {
						//silent
					}
				if (is == null) {
					log.error(Messages.get(MCommon.FILE_NOT_FOUND, path));
					return;
				}
			}

			log.info(Messages.get(MCommon.FILE_OPENING, path));
			final Properties props = new Properties();
			props.load(is);
			org.zkoss.util.logging.Log.configure(props);
		} catch (Throwable ex) {
			log.error("Failed to load "+path, ex);
		} finally {
			if (is != null)
				try {is.close();} catch (Throwable ex) {}
		}
	}
}

