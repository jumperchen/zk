/* Rowlayout.java

	Purpose:
		
	Description:
		
	History:
		Thu, Jan 24, 2013  10:46:06 AM, Created by Neil

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zul;

import java.io.IOException;

import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.sys.ContentRenderer;

/**
 * Divide the container into a row of equal-width columns with a spacing in between.
 * @author Neil
 * @since 6.5.2
 */
public class Rowlayout extends HtmlBasedComponent {
	public static int DEFAULT_NUM_COLUMNS  = 12;
	public static int DEFAULT_SPACING      = 20;
	public static int DEFAULT_COLUMN_WIDTH = 60;
	
	private int _ncols    = DEFAULT_NUM_COLUMNS;
	private int _spacing  = DEFAULT_SPACING;
	private int _colWidth = DEFAULT_COLUMN_WIDTH;
	
	/**
	 * <p>Default: 12
	 * @return Number of columns to divide the row into
	 */
	public int getNcols() {
		return _ncols;
	}
	public void setNcols(int ncols) {
		if (ncols < 1) ncols = 1;
		if (ncols != _ncols) {
			_ncols = ncols;
			smartUpdate("ncols", ncols);
		}
	}
	
	/**
	 * Spacing between columns is measured in device-independent unit.
	 * <p>Default: 20
	 * @return Spacing between columns
	 */
	public int getSpacing() {
		return _spacing;
	}
	public void setSpacing(int spacing) {
		if (spacing < 0) spacing = 0;
		if (spacing != _spacing) {
			_spacing = spacing;
			smartUpdate("spacing", spacing);
		}
	}
	
	/**
	 * Column width is measured in device-independent unit.
	 * <p>Default: 60
	 * @return Width of each column
	 */
	public int getColWidth() {
		return _colWidth;
	}
	public void setColWidth(int colWidth) {
		if (colWidth < 0) colWidth = 0;
		if (colWidth != _colWidth) {
			_colWidth = colWidth;
			smartUpdate("colWidth", colWidth);
		}
	}
	
	protected void renderProperties(ContentRenderer renderer) 
		throws IOException {
		super.renderProperties(renderer);
		
		if (_ncols    != DEFAULT_NUM_COLUMNS)  render(renderer, "ncols", _ncols);
		if (_spacing  != DEFAULT_SPACING)      render(renderer, "spacing", _spacing);
		if (_colWidth != DEFAULT_COLUMN_WIDTH) render(renderer, "colWidth", _colWidth);
	}
	
	public void beforeChildAdded(Component child, Component refChild) {
		if (!(child instanceof Rowchildren))
			throw new UiException("Unsupported child for Rowlayout: " + child);
		super.beforeChildAdded(child, refChild);
	}
}
