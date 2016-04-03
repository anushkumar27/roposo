package com.ansu.roposo.roposo;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MUX";
    private int AUDIO_CODE = 0;
    private int VIDEO_CODE = 1;

    Button audioButton, videoButton, save;
    private VideoView videoPreview;
    private MediaController ctlr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        videoPreview = (VideoView) findViewById(R.id.preview);
        save = (Button) findViewById(R.id.saveButton);
        audioButton = (Button) findViewById(R.id.audioSelect);
        videoButton = (Button) findViewById(R.id.videoSelect);
    }

    public void audioPicker(View view) {

        Intent i = new Intent(this, FilePickerActivity.class);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

        startActivityForResult(i, AUDIO_CODE);
    }

    public void videoPicker(View view) {

        Intent i = new Intent(this, FilePickerActivity.class);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

        startActivityForResult(i, VIDEO_CODE);
    }

    public void videoPreview(String input) {
        System.out.println("*************Video File: " + input.toString());
        videoPreview.setVideoPath(input);
        ctlr = new MediaController(this);
        ctlr.setMediaPlayer(videoPreview);
        videoPreview.setMediaController(ctlr);
        videoPreview.requestFocus();
        videoPreview.start();
    }

    String videoPath = "";
    String audioPath = "";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUDIO_CODE && resultCode == Activity.RESULT_OK) {
            Uri audioUri = data.getData();
            audioPath = audioUri.getPath();
            audioButton.setBackgroundColor(Color.parseColor("#FF4081"));
            Toast.makeText(getApplicationContext(), "Audio : "+audioPath, Toast.LENGTH_SHORT).show();
        }
        else if(requestCode == VIDEO_CODE && resultCode == Activity.RESULT_OK) {
            Uri videoUri = data.getData();
            videoPath = videoUri.getPath();
            videoButton.setBackgroundColor(Color.parseColor("#FF4081"));
            Toast.makeText(getApplicationContext(), "Video : "+videoPath, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
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


    public void saveMux(View view){
        if(videoPath != "" && audioPath != ""){
            muxing(audioPath, videoPath);

        }
        else {
            Toast.makeText(getApplicationContext(), " Select Audio/Video File ", Toast.LENGTH_SHORT).show();
        }

    }
    public void muxing(String audioFilePath, String videoFilePath) {
            String outputFile = "";

            try {
                /*String root = Environment.getExternalStorageDirectory().toString();
                String audioFilePath = root + "/ringtone.aac";
                String videoFilePath = root + "/intel.mp4";
                */
                Toast.makeText(getApplicationContext(), audioFilePath,Toast.LENGTH_SHORT).show();
                File file = new File(Environment.getExternalStorageDirectory() + File.separator + "output.mp4");
                file.createNewFile();
                outputFile = file.getAbsolutePath();

                MediaExtractor videoExtractor = new MediaExtractor();
                videoExtractor.setDataSource(videoFilePath);

                MediaExtractor audioExtractor = new MediaExtractor();
                audioExtractor.setDataSource(audioFilePath);

                Log.d(TAG, "Video Extractor Track Count " + videoExtractor.getTrackCount());
                Log.d(TAG, "Audio Extractor Track Count " + audioExtractor.getTrackCount());

                MediaMuxer muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

                videoExtractor.selectTrack(0);
                MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
                int videoTrack = muxer.addTrack(videoFormat);

                audioExtractor.selectTrack(0);
                MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
                int audioTrack = muxer.addTrack(audioFormat);

                Log.d(TAG, "Video Format " + videoFormat.toString());
                Log.d(TAG, "Audio Format " + audioFormat.toString());

                boolean sawEOS = false;
                int frameCount = 0;
                int offset = 100;
                int sampleSize = 256 * 1024;

                ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
                ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);

                MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
                MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();


                videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

                muxer.start();

                while (!sawEOS) {
                    videoBufferInfo.offset = offset;
                    videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


                    if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                        Log.d(TAG, "saw input EOS.");
                        sawEOS = true;
                        videoBufferInfo.size = 0;

                    } else {
                        videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                        videoBufferInfo.flags = videoExtractor.getSampleFlags();
                        muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                        videoExtractor.advance();


                        frameCount++;
                        Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs + " Flags:" + videoBufferInfo.flags + " Size(KB) " + videoBufferInfo.size / 1024);
                        Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs + " Flags:" + audioBufferInfo.flags + " Size(KB) " + audioBufferInfo.size / 1024);

                    }
                }

                Toast.makeText(getApplicationContext(), "frame:" + frameCount, Toast.LENGTH_SHORT).show();


                boolean sawEOS2 = false;
                int frameCount2 = 0;
                while (!sawEOS2) {
                    frameCount2++;

                    audioBufferInfo.offset = offset;
                    audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                    if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                        Log.d(TAG, "saw input EOS.");
                        sawEOS2 = true;
                        audioBufferInfo.size = 0;
                    } else {
                        audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                        audioBufferInfo.flags = audioExtractor.getSampleFlags();
                        muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                        audioExtractor.advance();


                        Log.d(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs + " Flags:" + videoBufferInfo.flags + " Size(KB) " + videoBufferInfo.size / 1024);
                        Log.d(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs + " Flags:" + audioBufferInfo.flags + " Size(KB) " + audioBufferInfo.size / 1024);

                    }
                }

                muxer.stop();
                muxer.release();
                Toast.makeText(getApplicationContext(), "Output: "+outputFile , Toast.LENGTH_LONG).show();
                videoPreview(outputFile);
            } catch (IOException e) {
                Log.d(TAG, "Mixer Error 1 " + e.getMessage());
            } catch (Exception e) {
                Log.d(TAG, "Mixer Error 2 " + e.getMessage());
            }
    }

}
