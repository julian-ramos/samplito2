package com.ramos.julian.samplito2;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends Activity {

    final static private String APP_KEY = "";
    final static private String APP_SECRET = "";
    private DropboxAPI<AndroidAuthSession> mDBApi;
    Button authButton,uploadButton,tokenButton;
    String filename,token,dropToken;
    private String hardToken="";
    String TAG = "samplito2";
    FileWriter writer;
    FileReader reader;

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                authButton = (Button) findViewById(R.id.button);
                authButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDBApi.getSession().startOAuth2Authentication(getApplicationContext());


                    }
                });

                uploadButton = (Button) findViewById(R.id.button2);
                uploadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        upload();
                        Log.d(TAG, "upload executed");

                    }
                });


                tokenButton = (Button) findViewById(R.id.button3);
                tokenButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        readToken();





                        mDBApi.getSession().setOAuth2AccessToken(hardToken);

                        if (mDBApi.getSession().authenticationSuccessful()){
                            mDBApi.getSession().finishAuthentication();
                            Log.d(TAG,"Authentication success >>"+mDBApi.getSession().authenticationSuccessful());
                        }
                        else{
                            Log.d(TAG,"Not authenticated trying link >>");
                        }

                        if (mDBApi.getSession().isLinked()){
                            Log.d(TAG,"The session is linked >>");
                        }
                        else{
                            Log.d(TAG,"Not linked to dropbox >>");
                        }



                    }
                });



            }
        });

        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);


        filename = Environment.getExternalStorageDirectory().toString()+"/Notes"+"/somefile.txt";
        token = Environment.getExternalStorageDirectory().toString()+"/Notes"+"/dropboxToken";
        Log.d(TAG,filename);


    }


    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();

                writer = new FileWriter(token);
                writer.append(accessToken);
                writer.flush();
                writer.close();
                Log.d(TAG,"Token stored");

            } catch (IllegalStateException e) {
                Log.d(TAG, "Error authenticating", e);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    void readToken(){
        try {
            reader = new FileReader(token);
            BufferedReader bufferedReader =new BufferedReader(reader);
            dropToken=bufferedReader.readLine().toString();
            bufferedReader.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void upload(){

        createFile();
        new Thread(new Runnable() {
            public void run() {
                Log.d(TAG,filename);
                File file = new File(filename);
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(file);
                    DropboxAPI.Entry response = null;
                    try {
                        response = mDBApi.putFile("/someother-file.txt", inputStream,
                                file.length(), null, null);
                    } catch (DropboxException e) {
                        e.printStackTrace();
                    }
                    Log.i("DbExampleLog", "The uploaded file's rev is: " + response.rev);


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    void createFile(){
        Log.d(TAG,"Executing createFile");
        File root = new File(Environment.getExternalStorageDirectory(), "Notes");
        if (!root.exists()) {
            root.mkdirs();
        }


        try {
            writer = new FileWriter(filename);
            writer.append("This is some random text I write to test this application");
            writer.flush();
            writer.close();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }





}
