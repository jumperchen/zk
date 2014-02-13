package org.zkoss.zktest.test2;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Flashchart;
import org.zkoss.zul.SimplePieModel;

public class B65_ZK_2161_Composer extends SelectorComposer {
	@Wire
	Flashchart mychart;
	SimplePieModel model;

	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		model = new SimplePieModel();
		model.setValue("C/C++", new Double(0));
		model.setValue("VB", new Double(51));
		model.setValue("Java", new Double(202));
		model.setValue("PHP", new Double(141));
		mychart.setModel(model);
	}

	@Listen("onClick = #btnOne")
	public void setVB() {
		model.setValue("VB", 0);
	}
}
