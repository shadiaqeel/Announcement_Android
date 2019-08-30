package com.pa.announcement;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;



public class MainActivity extends AppCompatActivity  {


    private static final int REQUEST_PERMISSION =11 ;

    String AudioName = null;
    String AudioName2 = null;

    ArrayList<String>  list = new ArrayList<String>(10) ;
    ArrayList<String> list_url= new  ArrayList<String>(10);



    int Item;
    int i=0;



    URI Dir=null;
    ProgressDialog dialog = null;
    int serverResponseCode;
    int serverResponseCode2;



    AlertDialog alert;
     AlertDialog Alert;
    AlertDialog Alert_broadcast;
   //ArrayAdapter<String> mAdapter;
    CustomAdapter mAdapter;



    int isplaying = 0;



    com.github.clans.fab.FloatingActionButton fab_record;



    ImageView broadcast_image ;

    boolean isSaved =true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        try {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);




            /** START
             Permission __________________________________________________________**/
            if((int) Build.VERSION.SDK_INT >= 23){

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    {
                        Toast.makeText(this, "write external storage permission is needed to create file", Toast.LENGTH_SHORT).show();


                    }

                    ActivityCompat.requestPermissions( this ,new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION);
                }


            }
            /** END
             Permission ________________________________________________________**/


            /** START
             login Dialog __________________________________________________________**/


            View record_view = getLayoutInflater().inflate(R.layout.activity_login, null);
            AlertDialog.Builder record_builder = new AlertDialog.Builder(MainActivity.this);
            record_builder.setView(record_view);
            Alert = record_builder.create();
            Alert.show();

            final AutoCompleteTextView username = (AutoCompleteTextView) record_view.findViewById(R.id.email);
            final EditText pass = (EditText) record_view.findViewById(R.id.password);
            final Button sign_in = (Button) record_view.findViewById(R.id.email_sign_in_button);


            Alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    MainActivity.this.finish();
                }
            });
            sign_in.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!(username.getText().toString().equals("admin")))
                        Toast.makeText(MainActivity.this, "this username is incorrect ", Toast.LENGTH_SHORT).show();
                    else {
                        if (!(pass.getText().toString().equals("123")))
                            Toast.makeText(MainActivity.this, "this password is incorrect ", Toast.LENGTH_SHORT).show();
                        else {


                            //  Alert.cancel();
                            Alert.dismiss();


                        }


                    }


                }
            });


            /** END
             login Dialog ________________________________________________________**/

            try {
                /** START
                 Alert Dialog __________________________________________________________**/

                AlertDialog.Builder builder_delete = new AlertDialog.Builder(MainActivity.this);

                builder_delete.setTitle("Confirm Delete ...");
                builder_delete.setIcon(getResources().getDrawable(R.drawable.ic_delete));
                builder_delete.setMessage("Are you shure you want delete this file ?");


                builder_delete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        new Thread(new Runnable() {
                            public void run() {

                                DeleteFromRaspberrry();

                            }
                        }).start();


                        deleteTempFile(list_url.get(Item));
                        list_url.remove(Item);
                        list.remove(Item);
                        i--;
                        Alert_broadcast.cancel();
                        Toast.makeText(MainActivity.this, "the file has been deleted from RaspberryPi ", Toast.LENGTH_SHORT).show();


                    }
                });

                builder_delete.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        alert.cancel();
                    }

                });

                alert = builder_delete.create();

                /** END
                 Alert Dialog ________________________________________________________**/

            }catch (Exception e)
            {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }


                /** START
                 broadcast Dialog __________________________________________________________**/


                View broadcast_view = getLayoutInflater().inflate(R.layout.activity_broadcast, null);
                AlertDialog.Builder broadcast_builder = new AlertDialog.Builder(MainActivity.this);
                broadcast_builder.setView(broadcast_view);
                Alert_broadcast = broadcast_builder.create();


                final Button broadcast = (Button) broadcast_view.findViewById(R.id.button2);
                final Button add_new = (Button) broadcast_view.findViewById(R.id.button);
                final Button cancel = (Button) broadcast_view.findViewById(R.id.button3);
                final ListView list_view = (ListView) broadcast_view.findViewById(R.id.listView);



              //  mAdapter = new ArrayAdapter<String>(broadcast_view.getContext(), R.layout.list_item, list);

                mAdapter = new CustomAdapter(broadcast_view.getContext(), R.layout.list_item , list);

                list_view.setAdapter(mAdapter);


/*
               list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {




                        Item = position;
                        AudioName = list.get(Item);

                        PopupMenu popupMenu = new PopupMenu(MainActivity.this,view, Gravity.RIGHT );
                        popupMenu.getMenuInflater().inflate(R.menu.popup_menu,popupMenu.getMenu());


                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                            @Override
                            public boolean onMenuItemClick (MenuItem item){

                                return true;
                            }
                        });




                        popupMenu.show();





                        return false;
                    }



                });


                */


                list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {



                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                        view.setSelected(true);
                        broadcast.setEnabled(true);
                        Item = position;





                        File f = new File(list_url.get(Item));  // Check if file exist

                        if(!f.exists())
                        {list_url.remove(Item);
                            list.remove(Item);
                            i--;
                           Alert_broadcast.cancel();
                            Toast.makeText(MainActivity.this, "the File doesn't exist ", Toast.LENGTH_SHORT).show();}


                    }





                });




                add_new.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                       ExistFile();


                    }
                });


                broadcast.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


                        if (mWifi.isConnected()) {
                            if (wifiInfo.getSSID().equals("\"Raspberrypi\"")) {



                                if (isplaying == 1) {

                                    isplaying = 0;
                                    new Thread(new Runnable() {
                                        public void run() {

                                            stopbroadcast();

                                        }
                                    }).start();
                                    broadcast.performClick();
                                } else {

                                    Toast.makeText(MainActivity.this, "wait a minute to upload and play audio in radio at 107.9", Toast.LENGTH_LONG).show();


                                    isplaying = 1;



                                    dialog = ProgressDialog.show(MainActivity.this, "", "Uploading file...", true);







                                    new Thread(new Runnable() {
                                        public void run() {

                                            uploadFile(list_url.get(Item));



                                            AudioName = list.get(Item);

                                            new Thread(new Runnable() {
                                                public void run() {

                                                    playbroadcast();

                                                }}).start();





                                        }
                                    }).start();






                                    Toast.makeText(MainActivity.this, "playing audio in radio", Toast.LENGTH_SHORT).show();


                                }


                            } else {
                                Alert_broadcast.dismiss();
                                Toast.makeText(MainActivity.this, "you are not connected to Raspberrypi network", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Alert_broadcast.dismiss();
                            Toast.makeText(MainActivity.this, "wifi is off", Toast.LENGTH_SHORT).show();
                        }


                    }
                });


                Alert_broadcast.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        cancel.performClick();
                    }
                });


                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Alert_broadcast.dismiss();
                    }
                });


                /** END
                 broadcast Dialog ________________________________________________________**/




            broadcast_image = (ImageView) findViewById(R.id.broadcast_button);
            fab_record = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_voice);





if((ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )) {


    CreateList();
}



            broadcast_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {



                    Alert_broadcast.show();
                    Alert_broadcast.setCanceledOnTouchOutside(true);





                }
            });








        fab_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if (isSaved == false && Dir != null) {
                    deleteTempFile(list_url.get(0));
                    list_url.remove(0);
                    list.remove(0);
                }


                isSaved = false;
                Intent intent = new Intent(MainActivity.this, record.class);
                startActivityForResult(intent, 321);

            }
        });







    }catch(Exception e)
    {
        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
    }

    }







/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu){

       getMenuInflater().inflate(R.menu.main,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
        {
            int id = item.getItemId();

            if (id == R.id.id_exit) {
                finish();
                System.exit(0);
                return true;

            }


            if (id == R.id.id_shutdown)

            {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (mWifi.isConnected()) {
                    if (wifiInfo.getSSID().equals("\"Raspberrypi\"")) {
                        Toast.makeText(MainActivity.this, "shutdown Raspberry pi", Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            public void run() {

                                shutdown();

                            }
                        }).start();
                    } else {
                        Toast.makeText(MainActivity.this, "you are not connected to Raspberrypi network", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "wifi is off", Toast.LENGTH_SHORT).show();

                }

            }


            return true;


    }


    */


    public void Delete (int Position){



    Item = Position;
    AudioName = list.get(Item);

    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


    if (mWifi.isConnected()) {
        if (wifiInfo.getSSID().equals("\"Raspberrypi\"")) {


            alert.show();


        } else {
            Alert_broadcast.dismiss();
            Toast.makeText(MainActivity.this, "you are not connected to Raspberrypi network", Toast.LENGTH_SHORT).show();
        }

    } else {
        Alert_broadcast.dismiss();
        Toast.makeText(MainActivity.this, "wifi is off", Toast.LENGTH_SHORT).show();
    }



    }




    public void CreateList (){

        File root = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "AudioRecorder");

        if (!root.exists()) {

            root.mkdirs();


        }

        File [] files = root.listFiles();

        if(files.length>=0) {

            for (;i<files.length;i++)
            {
                if(!isExistInList(files[i].getName()))
                {
                    list.add(0, files[i].getName());
                    list_url.add(0, files[i].getPath());
                }
            }


        }


    }

    public boolean isExistInList(String s)
    {


        for(int j=0;j<list.size();j++)
        {
            if(s.equals(list.get(j)))
                return true;

        }
        return false;

    }


    public void ExistFile (){





        if (isSaved == false && Dir != null) {
            deleteTempFile(list_url.get(0));
            list_url.remove(0);
            list.remove(0);
            i--;
        }


        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);

       // File dir = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "AudioRecorder" + File.separator);
        //intent.setDataAndType(Uri.fromFile(dir), "audio/*");

        File dir = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "AudioRecorder" + File.separator);
        intent.setDataAndType(FileProvider.getUriForFile(getBaseContext(), getApplicationContext().getPackageName() + ".provider", dir),"audio/*");
        try {


            startActivityForResult(intent, 123);

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

        }

    }





    public int uploadFile(String sourceFileUri) {

      //  android:background="@color/colorAccent"

        final String sourceFileUri2 =sourceFileUri;

        String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);




        if (!new File(sourceFileUri).exists()) {




            dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :"
                    +Dir.toString());

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Source File not exist :"
                            +Dir.toString(), Toast.LENGTH_LONG).show();

                }
            });

            return 0;

        }
        else
        {


            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL("http://10.11.13.1/upload.php");

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    runOnUiThread(new Runnable() {
                        public void run() {


                            Toast.makeText(MainActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                // dialog.dismiss();
                dialog.dismiss();

                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(MainActivity.this, "MalformedURLException Exception : check script url.", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch ( Exception e) {

                //dialog.dismiss();
                dialog.dismiss();
                e.printStackTrace();
            //Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

                runOnUiThread(new Runnable() {
                    public void run() {


                        Toast.makeText(MainActivity.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();


                    }
                });
                Log.e("Upload ", "Exception : " + e.getMessage(), e);
            }

            dialog.dismiss();
            return serverResponseCode;

        } // End else block
    }




    public int shutdown() {

        HttpURLConnection conn = null;
        try {

            // open a URL connection to the Servlet
            URL url = new URL("http://10.11.13.1/shutdown.php");

            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Responses from the server (code and message)
            serverResponseCode2 = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode2);

            if (serverResponseCode2 == 200) {

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "stop playing audio.",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            }

            //close the streams //


        } catch (MalformedURLException ex) {

            ex.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                }
            });

            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {

            e.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Got Exception : see logcat ",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("Upload ", "Exception : " + e.getMessage(), e);
        }
        return serverResponseCode2;

    }




    public int playbroadcast() {


        HttpURLConnection conn = null;
        try {

            // open a URL connection to the Servlet
            URL url = new URL("http://10.11.13.1/play.php");
            String postParameters="audioname="+AudioName;
            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");


            conn.setFixedLengthStreamingMode(postParameters.getBytes().length);
            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(postParameters);

            runOnUiThread(new Runnable() {

                public void run() {



                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {


                            new Thread(new Runnable() {
                                public void run() {

                                    isplaying=0;
                                    stopbroadcast();

                                }
                            }).start();




                        }
                    }, Duration(list_url.get(Item)));

                }
            });

            out.close();

            // Responses from the server (code and message)
            serverResponseCode2 = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("broadcast", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode2);


            //close the streams //





        } catch (MalformedURLException ex) {

            ex.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                }
            });

            Log.e("broadcast", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {

            e.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Got Exception : see logcat ",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("broadcast ", "Exception : " + e.getMessage(), e);
        }
        return serverResponseCode2;

    }

    public int DeleteFromRaspberrry() {


        HttpURLConnection conn = null;
        try {

            // open a URL connection to the Servlet
            URL url = new URL("http://10.11.13.1/delete.php");
            String postParameters="audioname="+AudioName;
            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");


            conn.setFixedLengthStreamingMode(postParameters.getBytes().length);
            PrintWriter out = new PrintWriter(conn.getOutputStream());
            out.print(postParameters);


            out.close();

            // Responses from the server (code and message)
            serverResponseCode2 = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("broadcast", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode2);


            //close the streams //





        } catch (MalformedURLException ex) {

            ex.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                }
            });

            Log.e("broadcast", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {

            e.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Got Exception : see logcat ",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("broadcast ", "Exception : " + e.getMessage(), e);
        }
        return serverResponseCode2;

    }






    public int stopbroadcast() {

        HttpURLConnection conn = null;
        try {

            // open a URL connection to the Servlet
            URL url = new URL("http://10.11.13.1/stop.php");

            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("stopbroadcast", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);

            if (serverResponseCode == 200) {

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, "Stop Playing Audio.",
                                Toast.LENGTH_SHORT).show();

                    }
                });
            }

            //close the streams //


        } catch (MalformedURLException ex) {

            ex.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                }
            });

            Log.e("stopbroadcast", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {

            e.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Got Exception : see logcat ",
                            Toast.LENGTH_SHORT).show();
                }
            });
            Log.e("stopbroadcast", "Exception : " + e.getMessage(), e);
        }
        return serverResponseCode;

    }



    private void deleteTempFile(String uri) {
        File file = new File(uri);
        file.delete();


    }


    private int Duration (String url)
    {


        MediaPlayer mediaPlayer = new MediaPlayer();
        Uri uri = Uri.parse(url);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
        }catch(Exception e)
        {
            Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show();
        }



        return mediaPlayer.getDuration();
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {



        if (requestCode == 123) {
            if(resultCode == Activity.RESULT_OK){



                mAdapter.notifyDataSetChanged();
                isSaved=true;





    list_url.add(0, getRealPathFromURI(getApplicationContext(),data.getData()));


                Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                cursor.moveToFirst();



                list.add(0,cursor.getString(nameIndex));
                AudioName2 =cursor.getString(nameIndex);





            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                Toast.makeText(MainActivity.this,"no result", Toast.LENGTH_LONG).show();

            }
        }// end 123


        if(requestCode == 321){
            if(resultCode == Activity.RESULT_OK){

                CreateList();
                isSaved=data.getBooleanExtra("isSaved",true);

                try {



                Dir = URI.create(data.getStringExtra("Uri"));

                    list_url.add(0,Dir.toString());


                    list_url.remove(1);
                    list.remove(0);

                    AudioName2=Uri.parse(Dir.toString()).getLastPathSegment();

                    if(!isSaved)
                    {list.add(0,Uri.parse(Dir.toString()).getLastPathSegment()+" *");}
                    else{ if(!isExistInList(AudioName2)){

                        list.add(0,Uri.parse(Dir.toString()).getLastPathSegment());
                    }
                    }




                }catch (Exception e )
                {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                }


        }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            //Write your code if there's no result

            Toast.makeText(MainActivity.this,"no result 321", Toast.LENGTH_LONG).show();

        }// end 321




    }


    @Override
    public void onBackPressed()
    {

        this.moveTaskToBack(true);


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {


        final Intent intent = new Intent();



        switch (requestCode) {
            case REQUEST_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    setResult(RESULT_CANCELED, intent);
                    finish();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

            }
            default:
            {
                super.onRequestPermissionsResult( requestCode,
                        permissions,  grantResults);}

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }






    public String getRealPathFromURI(Context c,Uri contentUri)
    {

        Cursor cursor = c.getContentResolver().query(contentUri, null, null, null, null);
        int Index = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();


        return cursor.getString(Index);
    }



}