/* WebManager.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Thu Jun 15 13:28:19     2006, Created by tomyeh@potix.com
}}IS_NOTE

Copyright (C) 2006 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package com.potix.zk.ui.http;

import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.Enumeration;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.potix.lang.D;
import com.potix.util.CollectionsX;
import com.potix.util.logging.Log;
import com.potix.util.resource.Labels;
import com.potix.el.impl.AttributesMap;

import com.potix.web.util.resource.ServletContextLocator;
import com.potix.web.util.resource.ServletLabelLocator;
import com.potix.web.util.resource.ServletLabelResovler;
import com.potix.web.util.resource.ClassWebResource;

import com.potix.zk.ui.WebApp;
import com.potix.zk.ui.Desktop;
import com.potix.zk.ui.Session;
import com.potix.zk.ui.UiException;
import com.potix.zk.ui.util.Configuration;
import com.potix.zk.ui.sys.DesktopCacheProvider;
import com.potix.zk.ui.sys.UiFactory;
import com.potix.zk.ui.sys.SessionCtrl;
import com.potix.zk.ui.sys.WebAppCtrl;
import com.potix.zk.ui.sys.UiEngine;
import com.potix.zk.ui.sys.ConfigParser;
import com.potix.zk.ui.impl.AbstractWebApp;
import com.potix.zk.ui.impl.RequestInfoImpl;
import com.potix.zk.ui.impl.SessionDesktopCacheProvider;
import com.potix.zk.ui.impl.UiEngineImpl;
import com.potix.zk.ui.impl.UiFactoryImpl;

/**
 * A bridge bewteen Web server and ZK.
 *
 * <p>Each Web application that uses ZK will have an independent instance
 * of {@link WebManager}.
 *
 * @author <a href="mailto:tomyeh@potix.com">tomyeh@potix.com</a>
 */
public class WebManager {
	private static final Log log = Log.lookup(WebManager.class);

	/** A session attribute. */
	private static final String ATTR_SESS = "javax.potix.zk.ui.Session";
		//Naming with javax to be able to shared among portlets

	/** A context attribute for storing an instance of this class. */
	/*package*/ static final String ATTR_WEB_MANAGER
		= "com.potix.zk.ui.WebManager";
	/** A request attribute to store the current desktop.
	 * Because we store this portlet request, we have to name it
	 * with javax such that it is visible to other servlets and portlets.
	 */
	/*package*/ static final String DESKTOP = "javax.potix.zk.ui.desktop";

	private final ServletContext _ctx;
	private final WebApp _wapp;
	private final String _updateURI;
	private final ClassWebResource _cwr;

	/** Creates a new Web
	 */
	/*package*/ WebManager(ServletContext ctx, String updateURI) {
		if (log.debugable()) log.debug("Starting WebManager at "+ctx);

		if (ctx == null || updateURI == null)
			throw new IllegalArgumentException("null");
		_ctx = ctx;
		_updateURI = updateURI;

		Labels.register(new ServletLabelLocator(_ctx));
		Labels.setVariableResolver(new ServletLabelResovler());

		_cwr = ClassWebResource.getInstance(_ctx, _updateURI);

		final Configuration cfg = new Configuration();
		try {
			final URL cfgUrl = _ctx.getResource("/WEB-INF/zk.xml");
			if (cfgUrl != null)
				new ConfigParser()
					.parse(cfgUrl, cfg, new ServletContextLocator(_ctx));
		} catch (Throwable ex) {
			log.error("Unable to load /WEB-INF/zk.xml", ex);
		}

		Class cls = cfg.getUiEngineClass();
		final UiEngine engine;
		if (cls == null) {
			engine = new UiEngineImpl();
		} else {
			try {
				engine = (UiEngine)cls.newInstance();
			} catch (Exception ex) {
				throw UiException.Aide.wrap(ex, "Unable to construct "+cls);
			}
		}

		cls = cfg.getDesktopCacheProviderClass();
		final DesktopCacheProvider provider;
		if (cls == null) {
			provider = new SessionDesktopCacheProvider();
		} else {
			try {
				provider = (DesktopCacheProvider)cls.newInstance();
			} catch (Exception ex) {
				throw UiException.Aide.wrap(ex, "Unable to construct "+cls);
			}
		}

		cls = cfg.getUiFactoryClass();
		final UiFactory factory;
		if (cls == null) {
			factory = new UiFactoryImpl();
		} else {
			try {
				factory = (UiFactory)cls.newInstance();
			} catch (Exception ex) {
				throw UiException.Aide.wrap(ex, "Unable to construct "+cls);
			}
		}

		_wapp = new MyWebApp(cfg, engine, provider, factory);
		engine.start(_wapp);
		provider.start(_wapp);
		factory.start(_wapp);

		_ctx.setAttribute(ATTR_WEB_MANAGER, this);
	}

	public void destroy() {
		final WebAppCtrl wappc = (WebAppCtrl)_wapp;
		wappc.getUiFactory().stop(_wapp);
		wappc.getDesktopCacheProvider().stop(_wapp);
		wappc.getUiEngine().stop(_wapp);
	}

	/** Returns the handler to retrieve resource from class path,
	 * under /web.
	 */
	public final ClassWebResource getClassWebResource() {
		return _cwr;
	}

	/** Returns the Web application.
	 * Notice: a Web application is allocated for each servlet.
	 */
	public final WebApp getWebApp() {
		return _wapp;
	}

	//-- static --//

	/** Returns the Web manager of the specified context.
	 * @exception UiException if not found (i.e., not initialized
	 * properly)
	 */
	public static final WebManager getWebManager(ServletContext ctx) {
		final WebManager webman = getWebManagerIfAny(ctx);
		if (webman == null)
			throw new UiException("The Web manager not found. Make sure <load-on-startup> is specified for "+DHtmlLayoutServlet.class.getName());
		return webman;
	}
	/** Returns the Web manager, or null if not found.
	 */
	/*package*/ static final WebManager getWebManagerIfAny(ServletContext ctx) {
		return (WebManager)ctx.getAttribute(ATTR_WEB_MANAGER);
	}

	/** Returns the session. */
	public static final
	Session getSession(ServletContext ctx, HttpServletRequest request) {
		final HttpSession hsess = request.getSession();
		Session sess = getSession(hsess);
		if (sess != null)
			return sess;

		if (D.ON && log.debugable()) log.debug("Creating a new sess for "+hsess);

		final WebApp webapp = getWebManager(ctx).getWebApp();
		sess = new SessionImpl(
			hsess, webapp,
			request.getRemoteAddr(), request.getRemoteHost());
		hsess.setAttribute(ATTR_SESS, sess);

		//Note: we set timeout here, because HttpSession might have been created
		//by other servlet or filter
		final Integer v =
			webapp.getConfiguration().getSessionMaxInactiveInterval();
		if (v != null)
			hsess.setMaxInactiveInterval(v.intValue());
		return sess;
	}
	/** Returns the session of the specified HTTP session, or null if n/a. */
	/*package*/ static final Session getSession(HttpSession hsess) {
		return (Session)hsess.getAttribute(ATTR_SESS);
	}
	/** Called when a HTTP session listner is notified.
	 * <p>Once called the session is cleaned. All desktops are dropped.
	 */
	/*package*/ static final
	void onSessionDestroyed(HttpSession hsess) {
		final Session sess = (Session)getSession(hsess);
		if (sess != null) {
			try {
				final WebApp wapp =
					getWebManager(hsess.getServletContext()).getWebApp();
				((WebAppCtrl)wapp)
					.getDesktopCacheProvider().sessionDestroyed(sess);
			} catch (Throwable ex) {
				log.error("Failed to cleanup session", ex);
			}

			try {
				((SessionCtrl)sess).onDestroyed();
			} catch (Throwable ex) {
				log.error("Failed to cleanup session", ex);
			}
			hsess.removeAttribute(ATTR_SESS);
		}
	}

	/** Returns the desktop of the specified request.
	 * @param path the path of the ZUML page.
	 */
	/*package*/ Desktop getDesktop(Session sess, ServletRequest request,
	String path) {
		Desktop desktop = (Desktop)request.getAttribute(DESKTOP);
		if (desktop == null) {
			if (D.ON && log.debugable()) log.debug("Create desktop for "+path);
			request.setAttribute(DESKTOP,
				desktop = newDesktop(sess, request, path));
		}
		return desktop;
	}
	/** Creates an desktop. */
	/*package*/ Desktop newDesktop(Session sess, Object request, String path) {
		return ((WebAppCtrl)_wapp).getUiFactory().newDesktop(
			new RequestInfoImpl(_wapp, sess, null, request,
				PageDefinitions.getLocator(_ctx, path)),
			_updateURI, path);
	}
	/** Sets the desktop to the specified request.
	 * Used internally for implementation only.
	 */
	public static void setDesktop(HttpServletRequest request,
	Desktop desktop) {
		if (D.ON) {
			final Desktop dt = (Desktop)request.getAttribute(DESKTOP);
			assert dt == null || dt == desktop: "old:"+dt+", new:"+desktop;
		}
		request.setAttribute(DESKTOP, desktop);
	}

	//-- inner classes --//
	private class MyWebApp extends AbstractWebApp {
		private MyWebApp(Configuration cfg, UiEngine engine,
		DesktopCacheProvider provider, UiFactory factory) {
			super(cfg, engine, provider, factory);
		}
		private final Map _attrs = new AttributesMap() {
			protected Enumeration getKeys() {
				return _ctx.getAttributeNames();
			}
			protected Object getValue(String key) {
				return _ctx.getAttribute(key);
			}
			protected void setValue(String key, Object val) {
				_ctx.setAttribute(key, val);
			}
			protected void removeValue(String key) {
				_ctx.removeAttribute(key);
			}
		};

		public Object getAttribute(String name) {
			return _ctx.getAttribute(name);
		}
		public void setAttribute(String name, Object value) {
			_ctx.setAttribute(name, value);
		}
		public void removeAttribute(String name) {
			_ctx.removeAttribute(name);
		}
		public Map getAttributes() {
			return _attrs;
		}

		public WebApp getWebApp(String uripath) {
			final ServletContext another = _ctx.getContext(uripath);
			if (another != null) {
				final WebManager webman = getWebManagerIfAny(another);
				if (webman != null)
					return webman.getWebApp();
			}
			return null;
		}
		public URL getResource(String path) {
			try {
				return _ctx.getResource(path);
			} catch (MalformedURLException ex) {
				throw new UiException("Failed to retrieve "+path, ex);
			}
		}
		public InputStream getResourceAsStream(String path) {
			return _ctx.getResourceAsStream(path);
		}

		public String getInitParameter(String name) {
			return _ctx.getInitParameter(name);
		}
		public Iterator getInitParameterNames() {
			return new CollectionsX.EnumerationIterator(
				_ctx.getInitParameterNames());
		}
		public String getRealPath(String path) {
			return _ctx.getRealPath(path);
		}
		public String getMimeType(String file) {
			return _ctx.getMimeType(file);
		}
		public Set getResourcePaths(String path) {
			return _ctx.getResourcePaths(path);
		}
		public Object getNativeContext() {
			return _ctx;
		}
	}
}
