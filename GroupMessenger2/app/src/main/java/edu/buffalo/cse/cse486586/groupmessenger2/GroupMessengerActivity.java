package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.ContentValues.TAG;
import static java.security.AccessController.doPrivileged;
import static java.security.AccessController.getContext;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;
    String myPort;
    String crashedport = "0";
    AtomicInteger proposedseqnum = new AtomicInteger(0);
    //static String[] PORTS = {REMOTE_PORT0,REMOTE_PORT1,REMOTE_PORT2,REMOTE_PORT3,REMOTE_PORT4};
    ArrayList<String> PORTS = new ArrayList<String>(Arrays.asList(REMOTE_PORT0,REMOTE_PORT1,REMOTE_PORT2,REMOTE_PORT3,REMOTE_PORT4));
    //static final String[] PORTS = {REMOTE_PORT0,REMOTE_PORT1,REMOTE_PORT2};
    ContentValues cv = new ContentValues();
    int sequence = 0;
    //float temp = 0;
    ArrayList<MessageContents> Queue = new ArrayList<MessageContents>();
    ArrayList<MessageContents> CrashedElem = new ArrayList<MessageContents>();
    //Object inputLock = new Object();
    int my = 5;
    PriorityQueue<MessageContents> pq = new PriorityQueue<MessageContents>();
    AtomicInteger attom = new AtomicInteger(0);
    private boolean cleanUpDone = false;


    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }


    private final Uri uri = buildUri("content","edu.buffalo.cse.cse486586.groupmessenger2.provider");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        Log.e(TAG, "My port = " + myPort);


        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        final EditText editText = (EditText) findViewById(R.id.editText1);

        // Reference : PA:1
        final Button clickButton = (Button) findViewById(R.id.button4);
        clickButton.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String msg = editText.getText().toString() + "\n";
                editText.setText("");
                TextView tv = (TextView) findViewById(R.id.textView1);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    //Reference : PA:1
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            try {
                while (true) {
                    Socket s = serverSocket.accept();
                    InputStream fis = s.getInputStream();
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    MessageContents messageRecvd =  (MessageContents) ois.readObject();


//                    Log.e("Just before  condition","chce");
//                    if(!crashedport.equals("")){
//                        Log.e("crahed port not null",crashedport);
//                        handleCrash(crashedport);
//                    }
                    if(messageRecvd.getFlag() == 0) {
                        if( messageRecvd.getCrashedPort() != null)
                        {
                            removeShit();
                        }
                        float newpropseq = messageRecvd.getNewproposedseq(messageRecvd.getRecieverPort(), proposedseqnum.incrementAndGet());
                        messageRecvd.setAgreedSequence(newpropseq);
                        messageRecvd.setFlag(1);

                        if ((int) newpropseq > proposedseqnum.get()) {
                            proposedseqnum.set((int) newpropseq);
                        }

                        Queue.add(messageRecvd);
                        // pq.add(messageRecvd);
                        // Log.e("Sourav1  : ", messageRecvd.getSenderPort() + ": " + messageRecvd.getCrashedPort());
                        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                        oos.writeObject(messageRecvd);
                        Log.e(TAG, "new - " + messageRecvd.getTextMessage() + "." + messageRecvd.getSenderPort() + " - " + newpropseq);
                    }
                    if(messageRecvd.getFlag() == 2) {
                        if( messageRecvd.getCrashedPort() != null)
                        {
                            removeShit();
                        }


                        String msg = messageRecvd.getTextMessage();
                        float agrseq = messageRecvd.getAgreedSequence();
                        Log.e(TAG, "final - " + msg + "." + agrseq);

                        if ((int) agrseq > proposedseqnum.get()) {
                            proposedseqnum.set((int) agrseq);
                        }
                        //int flg = messageRecvd.getFlag();
//                        int i = 0;
                        for (int i = 0; i < Queue.size(); i++) {
                            if(Queue.get(i).getTextMessage().equals(msg)){
                                Queue.get(i).setAgreedSequence(agrseq);
                                Queue.get(i).setFlag(2);
                                break;
                            }
                        }

                        Log.e(TAG, "##############################");
                        for (int j = 0; j < Queue.size(); ++j) {
                            Log.e(TAG, Queue.get(j).getTextMessage() + " - " + Queue.get(j).getAgreedSequence() + "-" + Queue.get(j).getSenderPort());
                        }

                        Log.e("Sorting se pehle" , "Gottcha");
                        Collections.sort(Queue, new MessageContents());

                        while (Queue.size() != 0) {

                            if(Queue.get(0).getFlag() == 2) {

                                publishProgress(Queue.get(0).getTextMessage());
                                Log.e("Queue data", Queue.get(0).getTextMessage() +"-" + Queue.get(0).getAgreedSequence());
                                Queue.remove(0);
//                                    Collections.sort(Queue, new MessageContents());
                                Log.e("Queue size after remov",String.valueOf( Queue.size()));
                                if( !Queue.isEmpty())
                                {
                                    Log.e("Queue head now", Queue.get(0).getSenderPort() + Queue.get(0).getTextMessage() + Queue.get(0).getFlag());
                                }
                            } else {
                                boolean to_break = removeShit();
                                if (to_break) {
                                    break;
                                }
                            }
                        }





                    }else
                    {
                        removeShit();
                    }

                    removeShit();
                }

            } catch (Exception e) {
                Log.e(TAG, "Server Socket IO exception ");
                e.printStackTrace();
            }
            return null;
        }

        private boolean removeShit() {
            boolean to_break = true;
            if(attom.get() != 0) {

                List<MessageContents> crashed = new ArrayList<MessageContents>();
                for(int j = 0; j < Queue.size(); ++j) {
                    //synchronized (inputLock){
                    if ((Queue.get(j).getSenderPort() == Integer.parseInt(crashedport)) && (Queue.get(j).getFlag() != 2)) {
                        crashed.add(Queue.get(j));
                        to_break = false;
                    }
                    //}
                }
                for (MessageContents msgContents : crashed) {
                    Queue.remove(msgContents);
                    Log.e(TAG, "Removing message = " + msgContents.getTextMessage());
                }
                Log.e(TAG, "here");
            }
            return to_break;
        }

        protected void onProgressUpdate(String...strings) {

            try {
                String strReceived = strings[0].trim();
                TextView remoteTextView = (TextView) findViewById(R.id.textView1);
                remoteTextView.append(strReceived + "\t\n");
                remoteTextView.append("\n");
                cv.put("key" , sequence);
                cv.put("value" , strReceived);
                sequence++;
                getContentResolver(). insert(uri,cv);

                //Thread.sleep(500);
            } catch (Exception e) {
                Log.e(TAG, "exceptions");
            }
            //Queue.remove(0);

            return;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            ArrayList <Float> vec = new ArrayList<Float>();
            int i = 0;
            for (String remotePort : PORTS) {
                if(remotePort.equals(crashedport)) continue;
                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));
                    String message = msgs[0].trim();
                    socket.setSoTimeout(1500);
                    MessageContents messageContents = new MessageContents();
                    messageContents.setTextMessage(message);
                    messageContents.setFlag(0);
                    messageContents.setSenderPort(Integer.parseInt(myPort));
                    messageContents.setRecieverPort(Integer.parseInt(remotePort));
                    if( !crashedport.equals("0") )
                        messageContents.setCrashedPort(crashedport);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(messageContents);
                    oos.flush();

                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    MessageContents messageRecvd1 =  (MessageContents) ois.readObject();
                    if(messageRecvd1.getFlag()==1){
                        vec.add(messageRecvd1.getAgreedSequence());

                    }

                    // Thread.sleep(500);
                } catch (UnknownHostException e) {
                    crashedport = remotePort;
                    Log.e(TAG, "ClientTask UnknownHostException");
                    Notify();
                    e.printStackTrace();

                }
                catch (SocketException e){
                    crashedport = remotePort;
                    Log.e(TAG,"socket exception");
                    Log.e(TAG, "socket crprt" + crashedport);
                    Notify();
                    e.printStackTrace();

                }
                catch (SocketTimeoutException e){
                    crashedport = remotePort;
                    Notify();
                    e.printStackTrace();
                }

                catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOExceptions");
                    crashedport = remotePort;
                    Log.e(TAG, "IOexception : " + crashedport);
                    //handleCrash(crashedport);
                    Notify();
                    e.printStackTrace();

                }
                catch (Exception e) {
                    crashedport = remotePort;
                    Log.e(TAG,"exceptions");
                    Notify();
                    e.printStackTrace();

                }
            }
            if (!crashedport.equals("")) {
                handleCrash(crashedport);
            }
            //Log.e(TAG, "size" + vec.size());
            float temp = Collections.max(vec);
            if ((int) temp > proposedseqnum.get()) {
                proposedseqnum.set((int) temp);
            }
//            temp = Math.max(temp , agrseq);
//            if(temp == agrseq){
//                ++agrseq;
//            }

            //}

            for (String remotePort : PORTS) {
                if(remotePort.equals(crashedport)) continue;
                try{
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));
                    String message = msgs[0].trim();
                    MessageContents messagefinal = new MessageContents();
                    socket.setSoTimeout(1500);
                    messagefinal.setTextMessage(message);
                    messagefinal.setFlag(2);
                    messagefinal.setSenderPort(Integer.parseInt(myPort));
                    messagefinal.setRecieverPort(Integer.parseInt(remotePort));
                    messagefinal.setAgreedSequence(temp);
                    if( !crashedport.equals("0") )
                        messagefinal.setCrashedPort(crashedport);

                    ObjectOutputStream oos1 = new ObjectOutputStream(socket.getOutputStream());
                    oos1.writeObject(messagefinal);
                    oos1.flush();
                    //Thread.sleep(500);
                    // }

                }catch(UnknownHostException e) {
                    crashedport = remotePort;
                    Log.e(TAG, "ClientTask UnknownHostException");
                    Notify();
//                   handleCrash(crashedport);
                    e.printStackTrace();
                }
                catch (SocketTimeoutException e){
                    crashedport = remotePort;
                    Notify();
//                   handleCrash(crashedport);
                    e.printStackTrace();
                }
                catch (SocketException e){
                    crashedport = remotePort;
                    Log.e(TAG,"socket2 exception");
                    Log.e(TAG, "crprt" + crashedport);
                    Notify();
//                    handleCrash(crashedport);
                    e.printStackTrace();
                }
                catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOExceptions2");
                    crashedport = remotePort;
                    Log.e(TAG, "2 io exception" + crashedport);
                    Notify();
//                    handleCrash(crashedport);
                    e.printStackTrace();
                } catch (Exception e){
                    crashedport = remotePort;
                    Log.e(TAG, "Exceptions");
                    Notify();
//                    handleCrash(crashedport);
                    e.printStackTrace();
                }
            }
            if (!crashedport.equals("")) {
                handleCrash(crashedport);
            }


            return null;
        }
    }

    private void Notify() {

        for (String remotePort : PORTS) {
            if (remotePort.equals(crashedport)) continue;
            try {
                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(remotePort));

                MessageContents messagefinal = new MessageContents();
                socket.setSoTimeout(1500);
                messagefinal.setTextMessage("");
                messagefinal.setFlag(3);
                messagefinal.setSenderPort(Integer.parseInt(myPort));
                messagefinal.setRecieverPort(Integer.parseInt(remotePort));
                ObjectOutputStream oos1 = new ObjectOutputStream(socket.getOutputStream());
                oos1.writeObject(messagefinal);
                oos1.flush();

            } catch (Exception ex) {
                ex.printStackTrace();

            }
        }

    }

    public void handleCrash(String portcrash) {
//        if (cleanUpDone) {
//            return;
//        }
//        cleanUpDone = true;
        Log.e(TAG, "hcrash " + crashedport);
        try {
            int tem = Integer.parseInt(crashedport);
            attom.set(tem);
        }

        catch (Exception e){
            Log.e(TAG,"srv");
            e.printStackTrace();
        }
    }

}