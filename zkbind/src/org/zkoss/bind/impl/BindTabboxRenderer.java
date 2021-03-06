/* BindTabboxRenderer.java

	Purpose:
		
	Description:
		
	History:
		2013/11/11 Created by dennis

Copyright (C) 2012 Potix Corporation. All Rights Reserved.
*/
package org.zkoss.bind.impl;

import java.io.Serializable;

import org.zkoss.bind.Binder;
import org.zkoss.bind.sys.BinderCtrl;
import org.zkoss.bind.sys.TemplateResolver;
import org.zkoss.bind.xel.zel.BindELContext;
import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.util.ForEachStatus;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Attributes;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.TabboxRenderer;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;

/**
 * @author dennis
 * @since 7.0.0
 */
public class BindTabboxRenderer extends AbstractRenderer implements TabboxRenderer<Object>,Serializable{
	private static final long serialVersionUID = 4628797162244533928L;

	public void renderTab(Tab item, final Object data, final int index) throws Exception {
		final Tabbox tabbox = item.getTabbox();
		final Tabs tabs = tabbox.getTabs();
		final int size = tabbox.getModel().getSize();
		final String tmn = "model";
		final Template tm = resolveTemplate(tabbox,item,data,index,size,tmn,"tab");
		if (tm == null) {
			item.setLabel(Objects.toString(data));
			item.setValue(data);
		} else {
			final ForEachStatus iterStatus = new AbstractForEachStatus(){//provide iteration status in this context
				private static final long serialVersionUID = 1L;
				
				public int getIndex() {
					return index;
				}
				
				public Object getCurrent(){
					return data;
				}
				
				public Integer getEnd(){
					return size;
				}
			};
			
			final String var = (String) tm.getParameters().get(EACH_ATTR);
			final String varnm = var == null ? EACH_VAR : var; //var is not specified, default to "each"
			final String itervar = (String) tm.getParameters().get(STATUS_ATTR);
			final String itervarnm = itervar == null ? ( var==null?EACH_STATUS_VAR:varnm+STATUS_POST_VAR) : itervar; //provide default value if not specified
			
			//bug 1188, EL when nested var and itervar
			Object oldVar = tabs.getAttribute(varnm);
			Object oldIter = tabs.getAttribute(itervarnm);
			tabs.setAttribute(varnm, data);
			tabs.setAttribute(itervarnm, iterStatus);
			
			final Component[] items = filterOutShadows(tabs, tm.create(tabs, item, null, null));
			
			tabs.setAttribute(varnm, oldVar);
			tabs.setAttribute(itervarnm, oldIter);
			
			if (items.length != 1)
				throw new UiException("The model template must have exactly one item, not "+items.length);

			final Tab ntab = (Tab)items[0];
			ntab.setAttribute(BinderCtrl.VAR, varnm); // for the converter to get the value
			
			// ZK-2552
			recordRenderedIndex(tabs, items.length);
			ntab.setAttribute(AbstractRenderer.IS_TEMPLATE_MODEL_ENABLED_ATTR, true);
			ntab.setAttribute(AbstractRenderer.CURRENT_INDEX_RESOLVER_ATTR, new IndirectBinding() {
				public Binder getBinder() {
					return BinderUtil.getBinder(ntab, true);
				}

				public void setValue(BindELContext ctx, Object value) {
					int idx = BindTabboxRenderer.this.getRenderedIndex(
							tabs, ntab.getIndex());
					ListModel<?> listmodel = tabbox.getModel();
					if (idx >= 0 && idx < listmodel.getSize()) {
		            	if (listmodel instanceof ListModelArray){
		            		((ListModelArray<Object>)listmodel).set(idx, value);
		            	} else if(listmodel instanceof ListModelList<?>){
		            		((ListModelList<Object>)listmodel).set(idx, value);
		            	}
		            } else {
		            	//out of range, should ignore to compatible with old version(when we didn't implement save) or throw exception?
		            }
				}
				
				public Component getComponent() {
					return ntab;
				}

				public Object getValue(BindELContext ctx) {
					return tabbox.getModel().getElementAt(BindTabboxRenderer.this.getRenderedIndex(
							tabs, ntab.getIndex()));
				}
			});
			addItemReference(tabbox, ntab, index, varnm); //kept the reference to the data, before ON_BIND_INIT
			
			ntab.setAttribute(itervarnm, iterStatus);
			
			//ZK-1787 When the viewModel tell binder to reload a list, the other component that bind a bean in the list will reload again
			//move TEMPLATE_OBJECT (was set in resoloveTemplate) to current for check in addTemplateTracking
			ntab.setAttribute(TemplateResolver.TEMPLATE_OBJECT, item.removeAttribute(TemplateResolver.TEMPLATE_OBJECT));
			//add template dependency
			addTemplateTracking(tabs, ntab, data, index, size);
			
			if (ntab.getValue() == null) //template might set it
				ntab.setValue(data);
			item.setAttribute(Attributes.MODEL_RENDERAS, ntab);
				//indicate a new item is created to replace the existent one
			item.detach();
		}
	}

	public void renderTabpanel(Tabpanel item, final Object data, final int index) throws Exception {
		final Tabbox tabbox = item.getTabbox();
		final Tabpanels tabpanels = tabbox.getTabpanels();
		final int size = tabbox.getModel().getSize();
		final String tmn = "model";
		final Template tm = resolveTemplate(tabbox,item,data,index,size,tmn,"tabpanel");
		if (tm == null) {
			item.appendChild(new Label(Objects.toString(data)));
		} else {
			final ForEachStatus iterStatus = new AbstractForEachStatus(){//provide iteration status in this context
				private static final long serialVersionUID = 1L;
				
				public int getIndex() {
					return index;
				}
				
				public Object getCurrent(){
					return data;
				}
				
				public Integer getEnd(){
					return size;
				}
			};
			
			final String var = (String) tm.getParameters().get(EACH_ATTR);
			final String varnm = var == null ? EACH_VAR : var; //var is not specified, default to "each"
			final String itervar = (String) tm.getParameters().get(STATUS_ATTR);
			final String itervarnm = itervar == null ? ( var==null?EACH_STATUS_VAR:varnm+STATUS_POST_VAR) : itervar; //provide default value if not specified
			
			//bug 1188, EL when nested var and itervar
			Object oldVar = tabpanels.getAttribute(varnm);
			Object oldIter = tabpanels.getAttribute(itervarnm);
			tabpanels.setAttribute(varnm, data);
			tabpanels.setAttribute(itervarnm, iterStatus);
			
			final Component[] items = tm.create(tabpanels, item, null, null);
			
			tabpanels.setAttribute(varnm, oldVar);
			tabpanels.setAttribute(itervarnm, oldIter);
			
			if (items.length != 1)
				throw new UiException("The model template must have exactly one item, not "+items.length);

			final Tabpanel ntabpanel = (Tabpanel)items[0];
			ntabpanel.setAttribute(BinderCtrl.VAR, varnm); // for the converter to get the value
			// ZK-2552
			recordRenderedIndex(tabpanels, items.length);

			ntabpanel.setAttribute(AbstractRenderer.IS_TEMPLATE_MODEL_ENABLED_ATTR, true);
			ntabpanel.setAttribute(AbstractRenderer.CURRENT_INDEX_RESOLVER_ATTR, new IndirectBinding() {
				public Binder getBinder() {
					return BinderUtil.getBinder(ntabpanel, true);
				}

				@SuppressWarnings("unchecked")
				public void setValue(BindELContext ctx, Object value) {
					int idx = BindTabboxRenderer.this.getRenderedIndex(
							tabpanels, ntabpanel.getIndex());
					ListModel<?> listmodel = tabbox.getModel();
					if (idx >= 0 && idx < listmodel.getSize()) {
		            	if (listmodel instanceof ListModelArray){
		            		((ListModelArray<Object>)listmodel).set(idx, value);
		            	} else if(listmodel instanceof ListModelList<?>){
		            		((ListModelList<Object>)listmodel).set(idx, value);
		            	}
		            } else {
		            	//out of range, should ignore to compatible with old version(when we didn't implement save) or throw exception?
		            }
				}
				public Component getComponent() {
					return ntabpanel;
				}

				public Object getValue(BindELContext ctx) {
					return tabbox.getModel().getElementAt(BindTabboxRenderer.this.getRenderedIndex(
							tabpanels, ntabpanel.getIndex()));
				}
			});
			addItemReference(tabbox, ntabpanel, index, varnm); //kept the reference to the data, before ON_BIND_INIT
			
			ntabpanel.setAttribute(itervarnm, iterStatus);
			
			//ZK-1787 When the viewModel tell binder to reload a list, the other component that bind a bean in the list will reload again
			//move TEMPLATE_OBJECT (was set in resoloveTemplate) to current for check in addTemplateTracking
			ntabpanel.setAttribute(TemplateResolver.TEMPLATE_OBJECT, item.removeAttribute(TemplateResolver.TEMPLATE_OBJECT));
			//add template dependency
			addTemplateTracking(tabpanels, ntabpanel, data, index, size);
			
			item.setAttribute(Attributes.MODEL_RENDERAS, ntabpanel);
				//indicate a new item is created to replace the existent one
			item.detach();
		}
	}

}
