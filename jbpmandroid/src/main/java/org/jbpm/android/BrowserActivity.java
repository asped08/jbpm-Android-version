package org.jbpm.android;

import android.os.Bundle;


public class BrowserActivity extends AndroidBase {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ksession.startProcess("org.jbpm.android.browser");
    }
    
    
}