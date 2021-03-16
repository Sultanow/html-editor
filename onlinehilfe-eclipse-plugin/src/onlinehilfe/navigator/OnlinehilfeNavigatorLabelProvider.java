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
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

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
		//LOGGER.info("call getImage(" + element + ")");
		if (element instanceof IOnlinehilfeElement) {
			ImageDescriptor imageDescriptor = null;
			if (ElementType.NAVROOT == ((IOnlinehilfeElement)element).getElementType()) {
				imageDescriptor = ResourceLocator.imageDescriptorFromBundle(BUNDLE.getSymbolicName(), "icons/book2d.png").orElse(null);
			} else if (ElementType.NAVPOINT == ((IOnlinehilfeElement)element).getElementType()) {
				imageDescriptor = ResourceLocator.imageDescriptorFromBundle(BUNDLE.getSymbolicName(), "icons/bookstay.png").orElse(null);
			} else {
				imageDescriptor = ResourceLocator.imageDescriptorFromBundle(BUNDLE.getSymbolicName(), "icons/page.png").orElse(null);
			}
			
			if (imageDescriptor != null) {
				//return imageDescriptor.createImage();
				return getImageInternal(imageDescriptor);
			}
		} 
		return null;
	}
	
	//https://stackoverflow.com/questions/26801704/unknown-error-swt-error-no-more-handles
	private Image getImageInternal(ImageDescriptor imageDescriptor) {
        String key = imageDescriptor.getClass().getName();
        ImageRegistry imageRegistry = UIPlugin.getDefault().getImageRegistry();
        Image image = imageRegistry.get(key);
        if (image == null) {
            image = imageDescriptor.createImage();
            imageRegistry.put(key, image);
        }
        return image;
    }
	
	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}
}
