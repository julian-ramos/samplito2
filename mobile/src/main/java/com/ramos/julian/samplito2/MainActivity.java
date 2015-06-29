package com.ramos.julian.samplito2;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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


public class MainActivity extends ActionBarActivity {
    final static private String APP_KEY = "";
    final static private String APP_SECRET = "";
    private DropboxAPI<AndroidAuthSession> mDBApi;
    Button authButton,uploadButton,tokenButton;
    String filename,token,dropToken;
    String TAG = "samplito2";
    FileWriter writer;
    FileReader reader;
    private String hardToken=""; //This token can be used so that there is no need for an authorization





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // And later in some initialization function:
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

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
                readToken();
                Log.d(TAG, "Reading token executed");
                Log.d(TAG,"Token obtained"+dropToken);


//                mDBApi.getSession().setOAuth2AccessToken(dropToken);
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
