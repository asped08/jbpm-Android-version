package org.jbpm.android;

import android.os.Bundle;


public class GalleryActivity extends AndroidBase {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       ksession.startProcess("org.jbpm.android.Gallery");
    }

}