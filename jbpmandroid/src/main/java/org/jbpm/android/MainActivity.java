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

