/* ExtendletConfig.java

	Purpose:
		
	Description:
		
	History:
		Thu Jul  5 09:56:30     2007, Created by tomyeh

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under LGPL Version 2.1 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.web.util.resource;

/**
 * The configuration information used to initialize an Extendlet
 * (a.k.a., a resource processor).
 *
 * @author tomyeh
 * @since 2.4.1
 * @see Extendlet#init
 */
public interface ExtendletConfig {
	/** Returns the Extendlet context.
	 */
	public ExtendletContext getExtendletContext();
	/** Adds an extension that shall be compressed (unless being disabled
	 * by the client).
	 * @param ext
	 * @since 5.0.0
	 */
	public void addCompressExtension(String ext);
}
