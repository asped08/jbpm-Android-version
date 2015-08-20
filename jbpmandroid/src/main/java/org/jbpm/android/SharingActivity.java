package org.jbpm.android;


import android.os.Bundle;



public class SharingActivity extends AndroidBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ksession.startProcess("org.jbpm.android.sharing");
    }
}

