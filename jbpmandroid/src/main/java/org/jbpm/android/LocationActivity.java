package org.jbpm.android;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.definition.KnowledgePackage;
import org.drools.impl.EnvironmentFactory;
import org.drools.process.core.Work;
import org.drools.process.core.datatype.impl.type.StringDataType;
import org.drools.process.core.impl.WorkImpl;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.process.ProcessBaseFactoryService;
import org.jbpm.process.ProcessPackage;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.WorkItemNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class LocationActivity extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location lastLocation;

    // Google client
    private GoogleApiClient googleApiClient;

    //  flag to toggle periodic location updates
    private boolean requestingLocationUpdateFlag = false;

    private LocationRequest locationRequest;

    // UI elements
    private TextView locationView;
    private Button buttonShowLocation, buttonLocationUpdates;

    protected StatefulKnowledgeSession ksession;
    private WorkItemManager workItemManager;
    private WorkItem currentWorkItem;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        RuleFlowProcess processLocation = new RuleFlowProcess();
        processLocation.setId("org.jbpm.android.Location");
        processLocation.setPackageName("org.jbpm.android");

        VariableScope variableScopeLocation = (VariableScope) processLocation.getContexts(VariableScope.VARIABLE_SCOPE).get(0);
        List<Variable> variablesLocation = new ArrayList<>();

        Variable latitude = new Variable();
        latitude.setName("latitude");
        latitude.setType(new StringDataType());
        variablesLocation.add(latitude);

        Variable longitude = new Variable();
        longitude.setName("longitude");
        longitude.setType(new StringDataType());
        variablesLocation.add(longitude);

        variableScopeLocation.setVariables(variablesLocation);

        StartNode startNodeLocation = new StartNode();
        startNodeLocation.setId(1);
        processLocation.addNode(startNodeLocation);

        WorkItemNode workItemNodeLocation1 = new WorkItemNode();
        Work workLocation1 = new WorkImpl();
        workLocation1.setName("RequestForLocationAccess");
        workItemNodeLocation1.setWork(workLocation1);
        workItemNodeLocation1.addOutMapping("TextLatitude", "latitude");
        workItemNodeLocation1.addOutMapping("TextLongitude", "longitude");
        workItemNodeLocation1.setId(2);
        processLocation.addNode(workItemNodeLocation1);

        WorkItemNode workItemNodeLocation2 = new WorkItemNode();
        Work workLocation2 = new WorkImpl();
        workLocation2.setName("ShowLocation");
        workItemNodeLocation2.setWork(workLocation2);
        workItemNodeLocation2.addInMapping("LATITUDE", "latitude");
        workItemNodeLocation2.addInMapping("LONGITUDE", "longitude");
        workItemNodeLocation2.setId(3);
        processLocation.addNode(workItemNodeLocation2);

        EndNode endNodeLocation = new EndNode();
        endNodeLocation.setId(4);
        processLocation.addNode(endNodeLocation);

        new ConnectionImpl(startNodeLocation, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNodeLocation1, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNodeLocation1, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNodeLocation2, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNodeLocation2, NodeImpl.CONNECTION_DEFAULT_TYPE, endNodeLocation, NodeImpl.CONNECTION_DEFAULT_TYPE);
        System.out.println("Created Location process");


        KnowledgeBaseFactory.setKnowledgeBaseServiceFactory(new ProcessBaseFactoryService());
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        List<KnowledgePackage> packages = new ArrayList<>();

        ProcessPackage p1 = new ProcessPackage("org.jbpm.android");
        p1.addProcess(processLocation);

        packages.add(p1);
        kbase.addKnowledgePackages(packages);

        kbase.addKnowledgePackages(packages);

        Properties properties = new Properties();
        properties.put("drools.processInstanceManagerFactory", "org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory");
        properties.put("drools.processSignalManagerFactory", "org.jbpm.process.instance.event.DefaultSignalManagerFactory");
        KnowledgeSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(properties);

        ksession = kbase.newStatefulKnowledgeSession(config, EnvironmentFactory.newEnvironment());

        ksession.getWorkItemManager().registerWorkItemHandler("RequestForLocationAccess", new WorkItemHandler() {

            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                setContentView(R.layout.activity_location);
                workItemManager=m;
                currentWorkItem=workItem;

                locationView = (TextView) findViewById(R.id.lblLocation);
                buttonShowLocation = (Button) findViewById(R.id.btnShowLocation);
                buttonLocationUpdates = (Button) findViewById(R.id.btnLocationUpdates);

                // check availability of play services
                if (checkPlayServices()) {
                    //  GoogleApi client and location request
                   // createGoogleApiClient();
                    googleApiClient = new GoogleApiClient.Builder(LocationActivity.this)
                            .addConnectionCallbacks(LocationActivity.this)
                            .addOnConnectionFailedListener(LocationActivity.this)
                            .addApi(LocationServices.API).build();
                   // createLocationRequest();
                    locationRequest = new LocationRequest();

                    int UPDATE_INTERVAL = 10000;
                    locationRequest.setInterval(UPDATE_INTERVAL);

                    int FATEST_INTERVAL = 5000;
                    locationRequest.setFastestInterval(FATEST_INTERVAL);

                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                    int DISPLACEMENT = 10;
                    locationRequest.setSmallestDisplacement(DISPLACEMENT);
                }
                else{

                    googleApiClient = new GoogleApiClient.Builder(LocationActivity.this)
                            .addConnectionCallbacks(LocationActivity.this)
                            .addOnConnectionFailedListener(LocationActivity.this)
                            .addApi(LocationServices.API).build();
                }

                // Show location button
                buttonShowLocation.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        lastLocation = LocationServices.FusedLocationApi
                                .getLastLocation(googleApiClient);

                        if (lastLocation != null) {
                            double latitude = lastLocation.getLatitude();
                            double longitude = lastLocation.getLongitude();

                            Toast.makeText(getApplicationContext(), latitude + " : " + longitude, Toast.LENGTH_LONG).show();

                            Map<String, Object> results = new HashMap<>();
                            results.put("TextLatitude", "" + latitude);
                            results.put("TextLongitude", "" + longitude);
                            workItemManager.completeWorkItem(currentWorkItem.getId(), results);

                        } else {
                            Toast.makeText(getApplicationContext(),"Couldn't get the location. Make sure location is enabled on the device",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                // start Location updates
                buttonLocationUpdates.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        periodicLocationUpdates();
                    }
                });
            }


        });
        ksession.getWorkItemManager().registerWorkItemHandler("ShowLocation", new WorkItemHandler() {
            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                String locationLatitude = (String) workItem.getParameter("LATITUDE");
                String locationLongitude = (String) workItem.getParameter("LONGITUDE");

                Toast.makeText(getApplicationContext(), locationLatitude + " : " + locationLongitude,
                        Toast.LENGTH_SHORT).show();

                locationView.setText(locationLatitude + ", " + locationLongitude);

                m.completeWorkItem(workItem.getId(), null);
            }
        });

        ksession.startProcess("org.jbpm.android.Location");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        // resume location updates
        if (googleApiClient.isConnected() && requestingLocationUpdateFlag) {
           // startLocationUpdates();
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, LocationActivity.this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
       // stopLocationUpdates();
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    /**
     * start/stop location updates
     * */
    private void periodicLocationUpdates() {
        if (!requestingLocationUpdateFlag) {

            buttonLocationUpdates.setText(getString(R.string.btn_stop_location_updates));
            requestingLocationUpdateFlag = true;

            // start the location updates
            //startLocationUpdates();
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            Toast.makeText(getApplicationContext(), "Periodic location updates started!",Toast.LENGTH_SHORT).show();
        } else {

            buttonLocationUpdates.setText(getString(R.string.btn_start_location_updates));
            requestingLocationUpdateFlag = false;

            // Stop the location updates
           // stopLocationUpdates();
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            Toast.makeText(getApplicationContext(),"Periodic location updates stopped!",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Creating google api client object
     * */
    /*protected synchronized void createGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }*/

    /**
     * Creating location request object
     * */
  /*  protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        int UPDATE_INTERVAL = 10000;
        locationRequest.setInterval(UPDATE_INTERVAL);
        int FATEST_INTERVAL = 5000;
        locationRequest.setFastestInterval(FATEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        int DISPLACEMENT = 10;
        locationRequest.setSmallestDisplacement(DISPLACEMENT);
    }*/

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     * */
  /*  protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);

    }
*/
    /**
     * Stopping location updates
     */
    /*protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                googleApiClient, this);
    }*/

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(getApplicationContext(), "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode(),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(Bundle arg0) {

        //  get the location
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (lastLocation != null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();

            Toast.makeText(getApplicationContext(),latitude+" : "+longitude,Toast.LENGTH_LONG).show();

            Map<String, Object> results = new HashMap<>();
            results.put("TextLatitude", "" + latitude);
            results.put("TextLongitude",""+longitude);
            workItemManager.completeWorkItem(currentWorkItem.getId(), results);

        } else {
            Toast.makeText(getApplicationContext(), "(Couldn't get the location. Make sure location is enabled on the device)", Toast.LENGTH_LONG).show();
        }

        if (requestingLocationUpdateFlag) {
            //startLocationUpdates();
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // new location
        lastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        // Display the new location
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (lastLocation != null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();

            Toast.makeText(getApplicationContext(),latitude+" : "+longitude,Toast.LENGTH_LONG).show();

            Map<String, Object> results = new HashMap<>();
            results.put("TextLatitude", "" + latitude);
            results.put("TextLongitude",""+longitude);
            workItemManager.completeWorkItem(currentWorkItem.getId(), results);

        } else {
            Toast.makeText(getApplicationContext(), "(Couldn't get the location. Make sure location is enabled on the device)",Toast.LENGTH_LONG).show();
        }
    }

}