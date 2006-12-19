package org.csstudio.trends.databrowser.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
    private static final String BUNDLE_NAME = "org.csstudio.trends.databrowser.preferences.messages"; //$NON-NLS-1$

    public static String Default_URLS;

    public static String PageTitle;

    public static String URLS_Label;
        
    static
    {   // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages()
    {}
}
