package org.jbpm.android;


import android.os.Bundle;

public class ScanActivity extends AndroidBase {
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		  ksession.startProcess("org.jbpm.android.Scan");
	  }
	}