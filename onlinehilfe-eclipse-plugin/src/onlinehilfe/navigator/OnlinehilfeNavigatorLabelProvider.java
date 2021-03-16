package onlinehilfe.navigator;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.UIPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import onlinehilfe.Activator;
import onlinehilfe.navigator.IOnlinehilfeElement.ElementType;


public class OnlinehilfeNavigatorLabelProvider extends BaseLabelProvider implements ILabelProvider {
	
	private static final Bundle BUNDLE = FrameworkUtil.getBundle(OnlinehilfeNavigatorLabelProvider.class);
	private static final ILog LOGGER = Platform.getLog(OnlinehilfeNavigatorLabelProvider.class);
	
	public String getText(Object element) {
		//LOGGER.info("call getText(" + element + ")");
		if (element !=null) {
			if (element instanceof IOnlinehilfeElement) {
				return ((IOnlinehilfeElement)element).getElementName();
			}
		}
		return null;
	}
	
	public Image getImage(Object element) {
		if (element instanceof IOnlinehilfeElement) {
			
			String imagePathFromBundle = null;
			if (ElementType.NAVROOT == ((IOnlinehilfeElement)element).getElementType()) {
				imagePathFromBundle = "icons/book2d.png";
			} else if (ElementType.NAVPOINT == ((IOnlinehilfeElement)element).getElementType()) {
				imagePathFromBundle = "icons/bookstay.png";
			} else {
				imagePathFromBundle = "icons/page.png";
			}
						
			if (imagePathFromBundle != null) {
				return getImageFromBundleOrCacheIt(imagePathFromBundle);
			}
		} 
		return null;
	}
	

	private Image getImageFromBundleOrCacheIt(String imagePathFromBundle) {
		//Idee: https://stackoverflow.com/questions/26801704/unknown-error-swt-error-no-more-handles
        ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
        Image image = imageRegistry.get(imagePathFromBundle);
        if (image == null) {
        	ImageDescriptor imageDescriptor = ResourceLocator.imageDescriptorFromBundle(BUNDLE.getSymbolicName(), imagePathFromBundle).orElse(null);	
            image = imageDescriptor.createImage();
            imageRegistry.put(imagePathFromBundle, image);
        }
        return image;
    }
	
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}
}
