package org.jbpm.android;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class AndroidBase extends Activity {

    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;
    private static final int SCAN_CODE = 3;
    private static final int SHARE_PICTURE = 4;

    protected StatefulKnowledgeSession ksession;
    private WorkItemManager workItemManager;
    private WorkItem currentWorkItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        workItemManager = null;


        RuleFlowProcess processMessage = new RuleFlowProcess();
        processMessage.setId("org.jbpm.android.message");
        processMessage.setPackageName("org.jbpm.android");
        processMessage = createProcessMessage(processMessage);


        RuleFlowProcess processBrowser = new RuleFlowProcess();
        processBrowser.setId("org.jbpm.android.browser");
        processBrowser.setPackageName("org.jbpm.android");
        processBrowser = createProcessBrowser(processBrowser);


        RuleFlowProcess processScan = new RuleFlowProcess();
        processScan.setId("org.jbpm.android.Scan");
        processScan.setPackageName("org.jbpm.android");
        processScan = createProcessScan(processScan);


        RuleFlowProcess processGallery = new RuleFlowProcess();
        processGallery.setId("org.jbpm.android.Gallery");
        processGallery.setPackageName("org.jbpm.android");
        processGallery = createProcessGallery(processGallery);

        RuleFlowProcess processCamera = new RuleFlowProcess();
        processCamera.setId("org.jbpm.android.Camera");
        processCamera.setPackageName("org.jbpm.android");
        processCamera = createProcessCamera(processCamera);

        RuleFlowProcess processSharing = new RuleFlowProcess();
        processSharing.setId("org.jbpm.android.sharing");
        processSharing.setPackageName("org.jbpm.android");
        processSharing = createProcessSharing(processSharing);


        KnowledgeBaseFactory.setKnowledgeBaseServiceFactory(new ProcessBaseFactoryService());
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        List<KnowledgePackage> packages = new ArrayList<>();

        ProcessPackage p1 = new ProcessPackage("org.jbpm.android");
        p1.addProcess(processMessage);
        p1.addProcess(processBrowser);
        p1.addProcess(processScan);
        p1.addProcess(processGallery);
        p1.addProcess(processCamera);
        p1.addProcess(processSharing);
        packages.add(p1);
        kbase.addKnowledgePackages(packages);

        kbase.addKnowledgePackages(packages);

        Properties properties = new Properties();
        properties.put("drools.processInstanceManagerFactory", "org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory");
        properties.put("drools.processSignalManagerFactory", "org.jbpm.process.instance.event.DefaultSignalManagerFactory");
        KnowledgeSessionConfiguration config = KnowledgeBaseFactory.newKnowledgeSessionConfiguration(properties);

        ksession = kbase.newStatefulKnowledgeSession(config, EnvironmentFactory.newEnvironment());

        //Handlers for send sms
        ksession.getWorkItemManager().registerWorkItemHandler("RequestTextInput", new WorkItemHandler() {

            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                setContentView(R.layout.activity_message);
                Button button = (Button) findViewById(R.id.btnSendSMS);
                button.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        EditText textPhone = (EditText) findViewById(R.id.phoneNum);
                        EditText textSms = (EditText) findViewById(R.id.smsContent);
                        final String phoneNumber = textPhone.getText().toString();
                        final String message = textSms.getText().toString();
                        Map<String, Object> results = new HashMap<>();
                        results.put("phoneNmbr", "" + phoneNumber);
                        results.put("message", "" + message);
                        m.completeWorkItem(workItem.getId(), results);

                    }
                });
            }
        });
        ksession.getWorkItemManager().registerWorkItemHandler("SendSMS", new WorkItemHandler() {
            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                Map<String, Object> results = workItem.getParameters();
                System.out.println(results.toString());
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(results.get("PHONENUMBER").toString(), null, results.get("MESSAGE").toString(), null, null);
                    Toast.makeText(getApplicationContext(), "SMS Sent!",
                            Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again later!",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                m.completeWorkItem(workItem.getId(), null);
            }
        });

        //Handlers for Browser
        ksession.getWorkItemManager().registerWorkItemHandler("RequestTextInputBrowser", new WorkItemHandler() {

            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                setContentView(R.layout.activity_browser);
                Button button = (Button) findViewById(R.id.ok);
                button.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        EditText text = (EditText) findViewById(R.id.entry);
                        final String message = text.getText().toString();
                        Map<String, Object> results = new HashMap<>();
                        results.put("Text", "http://community.jboss.org/search.jspa?rankBy=date&q=" + message);
                        m.completeWorkItem(workItem.getId(), results);
                    }
                });
            }
        });
        ksession.getWorkItemManager().registerWorkItemHandler("ShowWebPage", new WorkItemHandler() {
            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                String url = (String) workItem.getParameter("URL");
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                m.completeWorkItem(workItem.getId(), null);
            }
        });


        //Handlers for Scan Tag
        ksession.getWorkItemManager().registerWorkItemHandler("ScanQrImage", new WorkItemHandler() {

            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                setContentView(R.layout.activity_scan);

                Button button = (Button) findViewById(R.id.buttonScan);
                button.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {

                        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

                        startActivityForResult(intent, SCAN_CODE);

                        workItemManager = m;
                        currentWorkItem = workItem;

                    }
                });

            }
        });
        ksession.getWorkItemManager().registerWorkItemHandler("ShowURL", new WorkItemHandler() {
            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                String qrUrl = (String) workItem.getParameter("QRURL");

                Toast.makeText(getApplicationContext(), qrUrl,
                        Toast.LENGTH_SHORT).show();

                TextView resultView = (TextView) findViewById(R.id.scanReport);
                resultView.setText(qrUrl);


                m.completeWorkItem(workItem.getId(), null);
            }
        });


        //Handlers for Gallery
        ksession.getWorkItemManager().registerWorkItemHandler("RequestActionGallery", new WorkItemHandler() {

            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                setContentView(R.layout.activity_gallery);
                findViewById(R.id.Button01)
                        .setOnClickListener(new View.OnClickListener() {
                            public void onClick(View arg0) {
                                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
                                workItemManager = m;
                                currentWorkItem = workItem;
                            }
                        });


            }
        });
        ksession.getWorkItemManager().registerWorkItemHandler("SetImage", new WorkItemHandler() {
            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                setContentView(R.layout.activity_gallery);

                String imageUriString = (String) workItem.getParameter("IMAGEUrl");
                Uri ImageUri = Uri.parse(imageUriString);
                String selectedImagePath = getPath(ImageUri);

                Toast.makeText(AndroidBase.this, "" + selectedImagePath, Toast.LENGTH_SHORT).show();

                ImageView imageView = (ImageView) findViewById(R.id.ImageView01);
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                Bitmap bp = Bitmap.createScaledBitmap(bitmap, 2048, 2048, true);
                imageView.setImageBitmap(bp);

                m.completeWorkItem(workItem.getId(), null);
            }
        });

        // Handlers for Camera
        ksession.getWorkItemManager().registerWorkItemHandler("StartCameraIntent", new WorkItemHandler() {
            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                setContentView(R.layout.activity_camera);
                Button button = (Button) findViewById(R.id.capturebutton);
                button.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, TAKE_PICTURE);
                        workItemManager = m;
                        currentWorkItem = workItem;


                    }
                });
            }
        });
        ksession.getWorkItemManager().registerWorkItemHandler("SetImagePath", new WorkItemHandler() {
            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                setContentView(R.layout.activity_camera);
                String imageUrlString = (String) workItem.getParameter("ImageURL");

                String selectedImagePath = getPath(Uri.parse(imageUrlString));

                Toast.makeText(getApplicationContext(), "" +selectedImagePath ,
                        Toast.LENGTH_SHORT).show();



                ImageView imageView = (ImageView) findViewById(R.id.imageView);

                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                Bitmap bp = Bitmap.createScaledBitmap(bitmap, 512, 768, true);

                imageView.setImageBitmap(bp);
                m.completeWorkItem(workItem.getId(), null);
            }
        });

        // Handlers for Sharing Image
        ksession.getWorkItemManager().registerWorkItemHandler("RequestImage", new WorkItemHandler() {

            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                setContentView(R.layout.activity_sharing);

                findViewById(R.id.butshare)
                        .setOnClickListener(new View.OnClickListener() {
                            public void onClick(View arg0) {
                                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(Intent.createChooser(i, "Select Picture"), SHARE_PICTURE);
                                workItemManager = m;
                                currentWorkItem = workItem;
                            }
                        });
            }
        });
        ksession.getWorkItemManager().registerWorkItemHandler("ShareImage", new WorkItemHandler() {
            public void abortWorkItem(WorkItem workItem, WorkItemManager m) {
            }

            public void executeWorkItem(final WorkItem workItem, final WorkItemManager m) {
                String imageUrl = "" + workItem.getParameter("IMAGEUrl");

                Uri selectedImageUri = Uri.parse(imageUrl);

                Toast.makeText(AndroidBase.this, getPath(selectedImageUri), Toast.LENGTH_SHORT).show();

                String selectedImagePath = getPath(selectedImageUri);

                ImageView imageView = (ImageView) findViewById(R.id.ImageView01);

                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                Bitmap bp = Bitmap.createScaledBitmap(bitmap, 2048, 2048, true);
                imageView.setImageBitmap(bp);

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");

                intent.putExtra(Intent.EXTRA_STREAM, selectedImageUri);
                startActivity(intent);

                System.out.println("Image Path : " + selectedImagePath);
                m.completeWorkItem(workItem.getId(), null);
            }
        });


    }

    //create file sharing process
    private RuleFlowProcess createProcessSharing(RuleFlowProcess processSharing) {
        VariableScope variableScopeBrowser = (VariableScope) processSharing.getContexts(VariableScope.VARIABLE_SCOPE).get(0);
        List<Variable> variablesProcess = new ArrayList<>();
        final Variable imageUrl = new Variable();
        imageUrl.setName("ImageUrl");
        imageUrl.setType(new StringDataType());
        variablesProcess.add(imageUrl);
        variableScopeBrowser.setVariables(variablesProcess);

        StartNode startNode = new StartNode();
        startNode.setId(1);
        processSharing.addNode(startNode);

        WorkItemNode workItemNode1 = new WorkItemNode();
        Work work1 = new WorkImpl();
        work1.setName("RequestImage");
        workItemNode1.setWork(work1);
        workItemNode1.addOutMapping("Image", "ImageUrl");
        workItemNode1.setId(2);
        processSharing.addNode(workItemNode1);

        WorkItemNode workItemNode2 = new WorkItemNode();
        Work work2 = new WorkImpl();
        work2.setName("ShareImage");
        workItemNode2.setWork(work2);
        workItemNode2.addInMapping("IMAGEUrl", "ImageUrl");
        workItemNode2.setId(3);
        processSharing.addNode(workItemNode2);

        EndNode endNode = new EndNode();
        endNode.setId(4);
        processSharing.addNode(endNode);

        new ConnectionImpl(startNode, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNode1, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNode1, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNode2, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNode2, NodeImpl.CONNECTION_DEFAULT_TYPE, endNode, NodeImpl.CONNECTION_DEFAULT_TYPE);

        System.out.println("Created process Sharing");


        return processSharing;
    }

    // Create take picture process
    private RuleFlowProcess createProcessCamera(RuleFlowProcess process) {

        VariableScope variableScope = (VariableScope) process.getContexts(VariableScope.VARIABLE_SCOPE).get(0);
        List<Variable> variables = new ArrayList<>();
        Variable url = new Variable();
        url.setName("url");
        url.setType(new StringDataType());
        variables.add(url);
        variableScope.setVariables(variables);
        StartNode startNode = new StartNode();
        startNode.setId(1);
        process.addNode(startNode);

        WorkItemNode workItemNode1 = new WorkItemNode();
        Work work1 = new WorkImpl();
        work1.setName("StartCameraIntent");
        workItemNode1.setWork(work1);
        workItemNode1.addOutMapping("ImagePath", "url");
        workItemNode1.setId(2);
        process.addNode(workItemNode1);

        WorkItemNode workItemNode2 = new WorkItemNode();
        Work work2 = new WorkImpl();
        work2.setName("SetImagePath");
        workItemNode2.setWork(work2);
        workItemNode2.addInMapping("ImageURL", "url");
        workItemNode2.setId(4);
        process.addNode(workItemNode2);

        EndNode endNode = new EndNode();
        endNode.setId(5);
        process.addNode(endNode);
        new ConnectionImpl(startNode, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNode1, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNode1, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNode2, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNode2, NodeImpl.CONNECTION_DEFAULT_TYPE, endNode, NodeImpl.CONNECTION_DEFAULT_TYPE);
        System.out.println("Created process Camera");

        return process;
    }

    // select image from gallery
    private RuleFlowProcess createProcessGallery(RuleFlowProcess processGallery) {


        VariableScope variableScopeGallery = (VariableScope) processGallery.getContexts(VariableScope.VARIABLE_SCOPE).get(0);
        List<Variable> variablesProcessGallery = new ArrayList<>();
        final Variable imageUrl = new Variable();
        imageUrl.setName("ImageUrl");
        imageUrl.setType(new StringDataType());
        variablesProcessGallery.add(imageUrl);
        variableScopeGallery.setVariables(variablesProcessGallery);

        StartNode startNodeGallery = new StartNode();
        startNodeGallery.setId(1);
        processGallery.addNode(startNodeGallery);

        WorkItemNode workItemNodeGallery1 = new WorkItemNode();
        Work workGallery1 = new WorkImpl();
        workGallery1.setName("RequestActionGallery");
        workItemNodeGallery1.setWork(workGallery1);
        workItemNodeGallery1.addOutMapping("Image", "ImageUrl");
        workItemNodeGallery1.setId(2);
        processGallery.addNode(workItemNodeGallery1);

        WorkItemNode workItemNodeGallery2 = new WorkItemNode();
        Work workGallery2 = new WorkImpl();
        workGallery2.setName("SetImage");
        workItemNodeGallery2.setWork(workGallery2);
        workItemNodeGallery2.addInMapping("IMAGEUrl", "ImageUrl");
        workItemNodeGallery2.setId(3);
        processGallery.addNode(workItemNodeGallery2);

        EndNode endNodeGallery = new EndNode();
        endNodeGallery.setId(4);
        processGallery.addNode(endNodeGallery);

        new ConnectionImpl(startNodeGallery, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNodeGallery1, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNodeGallery1, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNodeGallery2, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNodeGallery2, NodeImpl.CONNECTION_DEFAULT_TYPE, endNodeGallery, NodeImpl.CONNECTION_DEFAULT_TYPE);

        System.out.println("Created process Gallery");


        return processGallery;
    }

    // scan a qr code
    private RuleFlowProcess createProcessScan(RuleFlowProcess processLocation) {

        VariableScope variableScopeLocation = (VariableScope) processLocation.getContexts(VariableScope.VARIABLE_SCOPE).get(0);
        List<Variable> variablesLocation = new ArrayList<>();

        Variable qrUrl = new Variable();
        qrUrl.setName("qrUrl");
        qrUrl.setType(new StringDataType());
        variablesLocation.add(qrUrl);

        variableScopeLocation.setVariables(variablesLocation);

        StartNode startNodeLocation = new StartNode();
        startNodeLocation.setId(1);
        processLocation.addNode(startNodeLocation);

        WorkItemNode workItemNodeLocation1 = new WorkItemNode();
        Work workLocation1 = new WorkImpl();
        workLocation1.setName("ScanQrImage");
        workItemNodeLocation1.setWork(workLocation1);
        workItemNodeLocation1.addOutMapping("TextQrUrl", "qrUrl");
        workItemNodeLocation1.setId(2);
        processLocation.addNode(workItemNodeLocation1);

        WorkItemNode workItemNodeLocation2 = new WorkItemNode();
        Work workLocation2 = new WorkImpl();
        workLocation2.setName("ShowURL");
        workItemNodeLocation2.setWork(workLocation2);
        workItemNodeLocation2.addInMapping("QRURL", "qrUrl");
        workItemNodeLocation2.setId(3);
        processLocation.addNode(workItemNodeLocation2);

        EndNode endNodeLocation = new EndNode();
        endNodeLocation.setId(4);
        processLocation.addNode(endNodeLocation);

        new ConnectionImpl(startNodeLocation, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNodeLocation1, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNodeLocation1, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNodeLocation2, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNodeLocation2, NodeImpl.CONNECTION_DEFAULT_TYPE, endNodeLocation, NodeImpl.CONNECTION_DEFAULT_TYPE);
        System.out.println("Created Location process");
        return processLocation;
    }

    // search text in browser
    private RuleFlowProcess createProcessBrowser(RuleFlowProcess processBrowser) {


        VariableScope variableScopeBrowser = (VariableScope) processBrowser.getContexts(VariableScope.VARIABLE_SCOPE).get(0);
        List<Variable> variablesProcess = new ArrayList<>();
        Variable url = new Variable();
        url.setName("url");
        url.setType(new StringDataType());
        variablesProcess.add(url);
        variableScopeBrowser.setVariables(variablesProcess);

        StartNode startNodeBrowser = new StartNode();
        startNodeBrowser.setId(5);
        processBrowser.addNode(startNodeBrowser);

        WorkItemNode workItemNode1Browser = new WorkItemNode();
        Work work1Browser = new WorkImpl();
        work1Browser.setName("RequestTextInputBrowser");
        workItemNode1Browser.setWork(work1Browser);
        workItemNode1Browser.addOutMapping("Text", "url");
        workItemNode1Browser.setId(6);
        processBrowser.addNode(workItemNode1Browser);

        WorkItemNode workItemNode2Browser = new WorkItemNode();
        Work work2Browser = new WorkImpl();
        work2Browser.setName("ShowWebPage");
        workItemNode2Browser.setWork(work2Browser);
        workItemNode2Browser.addInMapping("URL", "url");
        workItemNode2Browser.setId(7);
        processBrowser.addNode(workItemNode2Browser);

        EndNode endNodeBrowser = new EndNode();
        endNodeBrowser.setId(8);
        processBrowser.addNode(endNodeBrowser);

        new ConnectionImpl(startNodeBrowser, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNode1Browser, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNode1Browser, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNode2Browser, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNode2Browser, NodeImpl.CONNECTION_DEFAULT_TYPE, endNodeBrowser, NodeImpl.CONNECTION_DEFAULT_TYPE);

        System.out.println("Created process Browser");


        return processBrowser;
    }

    // send a sms message
    private RuleFlowProcess createProcessMessage(RuleFlowProcess processMessage) {


        VariableScope variableScope = (VariableScope) processMessage.getContexts(VariableScope.VARIABLE_SCOPE).get(0);

        List<Variable> variables = new ArrayList<>();

        final Variable phoneNumber = new Variable();
        phoneNumber.setName("phoneNumber");
        phoneNumber.setType(new StringDataType());
        variables.add(phoneNumber);

        Variable smsContent = new Variable();
        smsContent.setName("smsContent");
        smsContent.setType(new StringDataType());
        variables.add(smsContent);

        variableScope.setVariables(variables);

        StartNode startNode = new StartNode();
        startNode.setId(1);
        processMessage.addNode(startNode);

        WorkItemNode workItemNode1 = new WorkItemNode();
        Work work1 = new WorkImpl();
        work1.setName("RequestTextInput");
        workItemNode1.setWork(work1);
        workItemNode1.addOutMapping("phoneNmbr", "phoneNumber");
        workItemNode1.addOutMapping("message", "smsContent");

        workItemNode1.setId(2);
        processMessage.addNode(workItemNode1);


        WorkItemNode workItemNode2 = new WorkItemNode();
        Work work2 = new WorkImpl();
        work2.setName("SendSMS");
        workItemNode2.setWork(work2);
        workItemNode2.addInMapping("PHONENUMBER", "phoneNumber");
        workItemNode2.addInMapping("MESSAGE", "smsContent");

        workItemNode2.setId(3);
        processMessage.addNode(workItemNode2);

        EndNode endNode = new EndNode();
        endNode.setId(4);
        processMessage.addNode(endNode);

        new ConnectionImpl(startNode, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNode1, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNode1, NodeImpl.CONNECTION_DEFAULT_TYPE, workItemNode2, NodeImpl.CONNECTION_DEFAULT_TYPE);
        new ConnectionImpl(workItemNode2, NodeImpl.CONNECTION_DEFAULT_TYPE, endNode, NodeImpl.CONNECTION_DEFAULT_TYPE);

        System.out.println("Created process Send Message");

        return processMessage;
    }

    // handle intent results.
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Map<String, Object> results = new HashMap<>();
        if (resultCode == RESULT_OK) {

            // result of select image from gallery
            if (requestCode == SELECT_PICTURE) {
                Uri data1 = data.getData();
                results.put("Image", data1.toString());

                // result of taking picture from gallery
            } else if (requestCode == TAKE_PICTURE) {

                Uri data1 = data.getData();
                results.put("ImagePath", data1.toString());
                System.out.println("Image Path : " + data1.toString());

                // result of scan a qr/bar code
            } else if (requestCode == SCAN_CODE) {

                String selectedUrl = data.getStringExtra("SCAN_RESULT");
                results.put("TextQrUrl", selectedUrl);

                // // result of sharing picture
            } else if (requestCode == SHARE_PICTURE) {

                Uri selectedImageUri = data.getData();
                results.put("Image", selectedImageUri.toString());
            }

            workItemManager.completeWorkItem(currentWorkItem.getId(), results);
        }
    }


    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String returnPath = cursor.getString(column_index);
        cursor.close();
        return returnPath;
    }
}