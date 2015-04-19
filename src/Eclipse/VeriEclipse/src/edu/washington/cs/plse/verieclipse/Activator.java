package edu.washington.cs.plse.verieclipse;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.washington.cs.plse.verieclipse.preferences.PreferenceConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "VeriEclipse"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/**
	 * Recursively determine the inner exception message for <tt>t</tt>
	 * @param thrown the throwable
	 * @param depth the current depth
	 * @return the inner exception message for <tt>t</tt>
	 */
	public static String innerMessage(Throwable thrown, int depth){
		if (thrown.getMessage() != null){
			return thrown.getMessage();
		}else if (thrown.getCause() != null && depth < 5){
			return innerMessage(thrown.getCause(), depth + 1);
		}else{
			return null;
		}
	}
	
	/**
	 * Recursively determine the inner exception message for <tt>t</tt>
	 * @param thrown the throwable
	 * @return the inner exception message for <tt>t</tt>
	 */
	public static String innerMessage(Throwable thrown){
		return innerMessage(thrown, 0);
	}
	
	public static File makeOutputDirectory() throws IOException{
		IPreferenceStore store = getDefault().getPreferenceStore();
		String dir = store.getString(PreferenceConstants.P_PATH);
		
		File result = new File(dir);
		
		if (!result.exists()){
			if (!result.mkdirs()){
				throw new IOException("Could not make output directory" + dir);
			}			
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
