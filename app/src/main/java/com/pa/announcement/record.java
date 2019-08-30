package com.pa.announcement;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Semaphore;

import at.markushi.ui.CircleButton;

public class record extends AppCompatActivity {

    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int REQUEST_PERMISSION =11 ;

    private int bufferSize = 0;
    String AudioName=null;
    String AudioName2=null;


    URI AudioDir=null;
    private AudioRecord recorder = null;
    private MediaPlayer mediaPlayer;
    private Thread recordingThread = null;
    long elapsed=0;

    AlertDialog alert;
    AlertDialog Alert;

    ImageButton playbtn;
    ImageButton stopbtn;
    Button save_btn ;
    Button back_btn;
    EditText nametext;
    Chronometer myChr;
    CircleButton recordbtn;

    boolean recordExist = false;
    boolean isSaved = false;
    boolean selectRecord = false;
    boolean isChecked_record = true;
    boolean isChecked_play = true;
    boolean isRecord = true;









    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);


        try {

            /** START
             Permission __________________________________________________________**/
            if((int)Build.VERSION.SDK_INT >= 23){

                if (ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    {
                        Toast.makeText(this, "write external storage permission is needed to create file", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "record audio permission is needed to record voice ", Toast.LENGTH_SHORT).show();

                    }

                    ActivityCompat.requestPermissions( this ,new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},REQUEST_PERMISSION);
                }


            }
            /** END
             Permission ________________________________________________________**/








            bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);


            recordbtn = (CircleButton) findViewById(R.id.record);
            nametext = (EditText) findViewById(R.id.audioname);
            playbtn = (ImageButton) findViewById(R.id.playbtn2);
            stopbtn = (ImageButton) findViewById(R.id.stopbtn2);
            myChr = (Chronometer) findViewById(R.id.chronometer2);
            save_btn=(Button)findViewById(R.id.save_btn);
            back_btn =(Button) findViewById(R.id.back_btn);

            stopbtn.setEnabled(false);
            playbtn.setEnabled(false);


            stopbtn.setImageTintList(getResources().getColorStateList(R.color.falseEnable));
            playbtn.setImageTintList(getResources().getColorStateList(R.color.falseEnable));






            /** START
             Alert Dialog __________________________________________________________**/

            AlertDialog.Builder builder = new AlertDialog.Builder(record.this);

            builder.setTitle("Save File ...");
            builder.setIcon(getResources().getDrawable(R.drawable.ic_save));
            builder.setMessage("Do you want to save this record ?");


            final Bundle Data = new Bundle();
            final Intent intent = new Intent();
            setResult(RESULT_OK, intent);

            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    intent.putExtras(Data);
                    Alert.show();




                }
            });
            builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if(!selectRecord) {
                        Data.putString("Uri", AudioDir.toString());
                        Data.putBoolean("isSaved", false);
                        intent.putExtras(Data);


                        record.super.onBackPressed();
                    }else{
                        deletefile(AudioDir.toString());
                        isSaved=true;
                        alert.cancel();




                    }


                }

                });



             alert = builder.create();

            /** END
             Alert Dialog ________________________________________________________**/



            /** START
             save Dialog __________________________________________________________**/

            try {
            View record_view = getLayoutInflater().inflate(R.layout.save,null);
            AlertDialog.Builder record_builder =new AlertDialog.Builder(record.this);
            record_builder.setView(record_view);
                record_builder.setTitle("Enter the name ..");

              Alert = record_builder.create();



            final EditText save_name = (EditText) record_view.findViewById(R.id.save_name);
            final Button savebtn=(Button) record_view.findViewById(R.id.savebtn);



                savebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {




                        if (!save_name.getText().toString().matches("")) {

                            File path = new File(Environment.getExternalStorageDirectory().getPath()+"/AudioRecorder");
                            File from = new File(path,nametext.getText().toString()+".wav");

                            File to = new File(path, save_name.getText() + ".wav");
                            from.renameTo(to);
                            deletefile(from.toString());
                            AudioDir = to.toURI();
                            deletefile(from.toString());

                      }

                        Toast.makeText(record.this, "Rename Record To " + save_name.getText(), Toast.LENGTH_SHORT).show();
                        if(!selectRecord) {
                            Data.putString("Uri", AudioDir.getPath());
                            Data.putBoolean("isSaved", true);
                            intent.putExtras(Data);

                            record.super.onBackPressed();

                        }else{
                            nametext.setText(save_name.getText().toString());
                            isSaved = true;
                            Alert.cancel();}

                        nametext.setEnabled(false);
                        nametext.setText("");
                        save_btn.setEnabled(false);


                    }




                });


            }catch(Exception e)
            {
                Toast.makeText(this,e.toString() , Toast.LENGTH_LONG).show();
            }


            /** END
             save Dialog ________________________________________________________**/

            back_btn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    onBackPressed();

                }
            });

            save_btn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {



                    if (nametext.getText().toString().matches("")) {



                        Toast.makeText(record.this, "Enter the audio name", Toast.LENGTH_SHORT).show();


                    }

                    else {

                        if(!nametext.getText().toString().matches(AudioName2)) {
                            File path = new File(Environment.getExternalStorageDirectory().getPath() + "/AudioRecorder");
                            File from = new File(path, AudioName);

                            File to = new File(path, nametext.getText() + ".wav");
                            from.renameTo(to);
                            deletefile(from.toString());
                            AudioDir = to.toURI();
                            deletefile(from.toString());
                        }

                        isSaved = true;
                        save_btn.setEnabled(false);
                        nametext.setEnabled(false);
                        stopbtn.performClick();




                        Toast.makeText(record.this, "Done " , Toast.LENGTH_SHORT).show();



                    }

                }

            });






            recordbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isChecked_record) {

                        isRecord = false;
                        isChecked_record =true;
                        selectRecord=false;
                        recordExist=true;
                        isSaved=false;

                        save_btn.setEnabled(true);
                        nametext.setEnabled(true);



                        recordbtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_record));

                        myChr.stop();

                        stopbtn.performClick();
                        playbtn.setEnabled(true);
                        playbtn.setImageTintList(getResources().getColorStateList(R.color.colorPrimary));

                        try {

                            stopRecording();


                        } catch (Exception e) {
                            e.printStackTrace();
                        }




                    } else {

                        isRecord = true;
                        selectRecord = true;

                        save_btn.setEnabled(false);
                        nametext.setEnabled(false);
                        nametext.setText("");


                        playbtn.setEnabled(false);
                        playbtn.setImageTintList(getResources().getColorStateList(R.color.falseEnable));
                        stopbtn.setEnabled(false);
                        stopbtn.setImageTintList(getResources().getColorStateList(R.color.falseEnable));




                        if (recordExist && !isSaved) {

                            alert.show();


                        }

                        if (!alert.isShowing())
                        {

                        recordbtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));
                        myChr.setBase(SystemClock.elapsedRealtime());
                        myChr.start();

                        try {
                            startRecording();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                            isChecked_record =false;

                    }else{isChecked_record=true;
                    }


                    }


                }
            });



            playbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                        if (isChecked_play) {
                            isChecked_play=false;

                            Toast.makeText(record.this,AudioDir.getPath(), Toast.LENGTH_SHORT).show();
                            if (AudioDir != null) {

                                playbtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_pause));
                                myChr.setBase(SystemClock.elapsedRealtime() - elapsed);
                                myChr.start();




                            }

                            try {

                                playMedia();


                                /** START
                                 Media Completed __________________________________________________________**/
                                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                    public boolean onError(MediaPlayer paramMediaPlayer, int paramInt1, int paramInt2) {
                                        // TODO Auto-generated method stub
                                        //your code if any error occurs while playing even you can show an alert to user
                                        return true;
                                    }
                                });

                                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {

                                        recordbtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_record));
                                        myChr.stop();
                                        stopbtn.performClick();
                                        playbtn.setEnabled(true);
                                        playbtn.setImageTintList(getResources().getColorStateList(R.color.colorPrimary));


                                        Toast.makeText(record.this, "Media Completed ", Toast.LENGTH_SHORT).show();

                                    }
                                });

                                /** END
                                 Media Completed ________________________________________________________**/


                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        } else {

                            isChecked_play=true;

                            playbtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play));
                            elapsed = SystemClock.elapsedRealtime() - myChr.getBase();
                            myChr.stop();
                            try {

                                if (mediaPlayer != null)

                                    mediaPlayer.pause();


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                }catch(Exception e)
                {
                    Toast.makeText(record.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }});



            stopbtn.setOnClickListener(new View.OnClickListener() {
                @Override

                public void onClick(View view) {


                    stopbtn.setEnabled(false);
                    stopbtn.setImageTintList(getResources().getColorStateList(R.color.falseEnable));


                    playbtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_media_play));


                    myChr.setBase(SystemClock.elapsedRealtime());
                    myChr.stop();
                    elapsed = 0;

                    try {

                        if (mediaPlayer != null)
                            mediaPlayer.stop();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

            });






        }catch (Exception e)
        {
            Toast.makeText(record.this, e.toString(), Toast.LENGTH_SHORT).show();
        }



    } // end onCreate Methode




    @Override
    public void onBackPressed()
    {
        final Bundle Data = new Bundle();
        final Intent intent = new Intent();


 if(recordExist && !isSaved)
 {
     alert.show();
 }
 else if (!recordExist)
 {
     setResult(RESULT_CANCELED, intent);

     finish();

 }
 else
 {

     setResult(RESULT_OK, intent);
     Data.putString("Uri", AudioDir.getPath());
     Data.putBoolean("isSaved", true);
     intent.putExtras(Data);

     finish();
 }
    }






    private void playMedia()throws Exception {


        if (AudioDir == null) {

            Toast.makeText(record.this, "not select audio", Toast.LENGTH_LONG).show();

        } else {


            if (!stopbtn.isEnabled()) {

                mediaPlayer = new MediaPlayer();
                Uri Dir_Uri = Uri.parse(AudioDir.toString()); // to conver for URI to Uri
                mediaPlayer.setDataSource(getApplicationContext(), Dir_Uri);


                stopbtn.setEnabled(true);
                stopbtn.setImageTintList(getResources().getColorStateList(R.color.trueEnable));

                myChr.setBase(SystemClock.elapsedRealtime());
                mediaPlayer.prepare();
            }


            mediaPlayer.start();




        }
    }












    private String getTempFilename(){




        String filepath = Environment.getExternalStorageDirectory().getPath();



     /*edited AUDIO_RECORDER_FILE_EXT_WAV ->  AUDIO_RECORDER_FOLDER*/
          final File  file = new File(filepath,AUDIO_RECORDER_FOLDER);



            if (!file.exists()) {

               file.mkdirs();


            }




        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);

        if(tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);


    }

    private URI getFilename(){



        File file = new File( Environment.getExternalStorageDirectory().getPath(),AUDIO_RECORDER_FOLDER);

        AudioName2 =  System.currentTimeMillis()+"";
        AudioName = AudioName2 + AUDIO_RECORDER_FILE_EXT_WAV;


        nametext.setText(AudioName2);

        if(!file.exists()){
            file.mkdirs();
        }

        AudioDir =URI.create(file.getAbsolutePath()+'/'+AudioName);
        return (AudioDir);

    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    private void deletefile(String uri) {
        File file = new File(uri);
        file.delete();
    }



    private void writeAudioDataToFile(){

        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;

        if(null != os){



            while(isRecord){
                read = recorder.read(data,0,bufferSize);


                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





    private void startRecording(){

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

        int i = recorder.getState();
        if(i==1)
            recorder.startRecording();

        recordingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                writeAudioDataToFile();
            }
        },"AudioRecorder Thread");

        recordingThread.start();
    }



    private void stopRecording(){
        if(null != recorder){
            int i = recorder.getState();
            if(i==1)
                recorder.stop();

            recorder.release();
            recorder = null;
            recordingThread = null;
        }

        copyWaveFile(getTempFilename(),getFilename());

        deleteTempFile();



    }




    private void copyWaveFile(String inFilename,URI outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
            long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 1;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename.toString());
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            AppLog.logString("File size: " + totalDataLen);

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);

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




}
