package onlinehilfe;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.jetty.JettyServerHandler;
import onlinehilfe.navigation.NavigationMetadataController;

public class Activator extends AbstractUIPlugin implements BundleActivator {

	private static final Bundle BUNDLE = FrameworkUtil.getBundle(Activator.class);
	private static final ILog LOGGER = Platform.getLog(BUNDLE);
	
	private static Activator uniqueInstance = null;
		
	public Activator() {
		uniqueInstance = this;
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		LOGGER.info("Activator start");
		
		JettyServerHandler.getInstance().start();
		
		CurrentPropertiesStore.getInstance().addPropertiesEventListener(NavigationMetadataController.getInstance());
		ResourcesPlugin.getWorkspace().addResourceChangeListener(NavigationMetadataController.getInstance(), IResourceChangeEvent.POST_CHANGE);
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		LOGGER.info("Activator stop");

		CurrentPropertiesStore.getInstance().removePropertiesEventListener(NavigationMetadataController.getInstance());
		
		NavigationMetadataController.getInstance().terminate();
		
		JettyServerHandler.getInstance().stop();
		
	}
	
	public static Activator getDefault() {
		return uniqueInstance;

	}
	
}
