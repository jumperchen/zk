package org.zkoss.zktest.test2;

import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Rowchildren;
import org.zkoss.zul.Rowlayout;

public class F65_ZK_Rowlayout_TestViewModel1 {

	private int _ncols = 12;
	private int _colWidth = 60;
	private int _spacing = 20;
	
	@Wire("#rowlayout1")
	Rowlayout rowlayout;
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view){
        Selectors.wireComponents(view, this, false);
    }

	public int getNcols() {
		return _ncols;
	}

	@NotifyChange("ncols")
	public void setNcols(int ncols) {
		List<Component> children = rowlayout.getChildren();
		int size = children.size();
		if (ncols < _ncols) {
			while (size > ncols) {
				children.get(--size).detach();
			}
		} else {
			while (size++ < ncols) {
				Rowchildren rc = new Rowchildren();
				Div div = new Div();
				div.setStyle("background-color: #ccc; border-radius: 10px; min-height: 30px");
				div.setHflex("1");
				div.setParent(rc);
				rc.setParent(rowlayout);
			}
		}
			
		_ncols = ncols;
	}

	public int getColWidth() {
		return _colWidth;
	}

	@NotifyChange("colWidth")
	public void setColWidth(int colWidth) {
		_colWidth = colWidth;
	}

	public int getSpacing() {
		return _spacing;
	}

	@NotifyChange("spacing")
	public void setSpacing(int spacing) {
		_spacing = spacing;
	}
}
