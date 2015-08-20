package org.jbpm.android;

import android.os.Bundle;

public class CameraActivity extends AndroidBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onStart() {
        super.onStart();
        ksession.startProcess("org.jbpm.android.Camera");
    }
}