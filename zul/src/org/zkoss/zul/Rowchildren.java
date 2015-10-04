/* Rowchildren.java

	Purpose:
		
	Description:
		
	History:
		Thu, Jan 24, 2013  12:41:30 PM, Created by Neil

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zul;

import java.io.IOException;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.sys.ContentRenderer;

/**
 * 
 * @author Neil
 */
public class Rowchildren extends HtmlBasedComponent {

	private int _colspan = 1;
	private int _offset = 0;
	
	public int getColspan() {
		return _colspan;
	}
	
	public void setColspan(int colspan) {
		if (colspan < 0) colspan = 1;
		if (colspan != _colspan) {
			_colspan = colspan;
			this.smartUpdate("colspan", _colspan);
		}
	}
	
	public int getOffset() {
		return _offset;
	}
	
	public void setOffset(int offset) {
		if (offset < 0) offset = 0;
		if (offset != _offset) {
			_offset = offset;
			smartUpdate("offset", _offset);
		}
	}
	
	protected void renderProperties(ContentRenderer renderer) 
		throws IOException {
		super.renderProperties(renderer);
		
		if (_colspan != 1) this.render(renderer, "colspan", _colspan);
		if (_offset != 0) this.render(renderer, "offset", _offset);
	}
	
	public void beforeParentChanged(Component parent) {
		if (parent != null && !(parent instanceof Rowlayout))
			throw new UiException(
				"Wrong parent for " + this.getClass().getName()	+ ": " + parent);
		super.beforeParentChanged(parent);
	}
}
