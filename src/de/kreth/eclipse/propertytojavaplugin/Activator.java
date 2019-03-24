package de.kreth.eclipse.propertytojavaplugin;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin {

	public static final String PluginID = "PropertyToJavaPlugin";

	private BundleContext context;

	private static Activator instance;

	public BundleContext getContext() {
		return context;
	}

	public static Activator getDefault() {
		return instance;
	}

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		this.context = bundleContext;
		Activator.instance = this;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		this.context = null;
		Activator.instance = null;
	}

}
