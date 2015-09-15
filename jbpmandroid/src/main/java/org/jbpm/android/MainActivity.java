/*
*   Copyright 2015 A.S.P. Athukorala
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
*/
package org.jbpm.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity implements View.OnClickListener {

    Button browser;
    Button messanger;
    Button location;
    Button bluetoothSharing;
    Button scannerTag;
    Button cameraTake;
    Button BrowseGallery;


    Intent intent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        browser = (Button) findViewById(R.id.buttonBrowser);
        browser.setOnClickListener(this);

        messanger = (Button) findViewById(R.id.buttonMessage);
        messanger.setOnClickListener(this);

        location = (Button) findViewById(R.id.buttonLocation);
        location.setOnClickListener(this);

        bluetoothSharing = (Button) findViewById(R.id.buttonSharing);
        bluetoothSharing.setOnClickListener(this);

        scannerTag = (Button) findViewById(R.id.buttonScan);
        scannerTag.setOnClickListener(this);

        cameraTake = (Button) findViewById(R.id.buttonCamera);
        cameraTake.setOnClickListener(this);

        BrowseGallery = (Button) findViewById(R.id.buttonGallery);
        BrowseGallery.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonBrowser:
                intent = new Intent(this, BrowserActivity.class);
                startActivity(intent);
                break;
            case R.id.buttonMessage:
                intent = new Intent(this, MessageActivity.class);
                startActivity(intent);

                break;
            case R.id.buttonLocation:
                intent = new Intent(this, LocationActivity.class);
                startActivity(intent);

                break;
            case R.id.buttonSharing:
                intent = new Intent(this, SharingActivity.class);
                startActivity(intent);

                break;
            case R.id.buttonScan:
                intent = new Intent(this, ScanActivity.class);
                startActivity(intent);

                break;
            case R.id.buttonCamera:
                intent = new Intent(this, CameraActivity.class);
                startActivity(intent);


                break;
            case R.id.buttonGallery:
                intent = new Intent(this, GalleryActivity.class);
                startActivity(intent);

                break;
        }
    }

}

