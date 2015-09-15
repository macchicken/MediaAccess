package comp5216.sydney.edu.au.mediaaccess;

import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class RecordAudioActivity extends ActionBarActivity {

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String audioFileName;
    private int MAX_DURATION=60000;
    private ProgressBar pb;
    private Timer timer;
    private boolean stopped;
    private Button recordBtn;
    private Button stopRecordBtn;
    private Button playBackBtn;
    private Button stopPlayBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);
        pb=(ProgressBar)findViewById(R.id.recordProgressBar);
        recordBtn= (Button) findViewById(R.id.captureAudio);
        stopRecordBtn= (Button) findViewById(R.id.stopRecord);
        playBackBtn= (Button) findViewById(R.id.playbackAudio);
        stopPlayBackBtn= (Button) findViewById(R.id.stopPlaybackAudio);
        pb.setVisibility(View.GONE);
        recordBtn.setEnabled(true);
        stopRecordBtn.setEnabled(true);
        playBackBtn.setEnabled(false);
        stopPlayBackBtn.setEnabled(false);
        audioFileName=getIntent().getStringExtra("audioFileName");
        if (audioFileName==null||"".equals(audioFileName.trim())){
            audioFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            audioFileName += "/audiorecordtest.3gp";
        }
        stopped=true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record_audio, menu);
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

    /**
     * start record audio
     * @param v
     */
    public void onCapaureAudio(View v){
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            // Create the recorder
            mediaRecorder = new MediaRecorder();
            // Set the audio format and encoder
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // Setup the output location
            mediaRecorder.setOutputFile(audioFileName);
            mediaRecorder.setMaxDuration(MAX_DURATION);
            pb.setMax(MAX_DURATION);
            pb.setProgress(0);
            timer = new Timer();
            pb.setVisibility(View.VISIBLE);
            startProgress();
            // Start the recording
            try {
                playBackBtn.setEnabled(false);
                stopPlayBackBtn.setEnabled(false);
                mediaRecorder.prepare();
                stopped=false;
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else { // no mic on device
            Toast.makeText(this, "This device doesn't have a mic!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Stop the recording of the audio
     * @param v
     */
    public void onStopRecording(View v){
        if (mediaRecorder!=null){
            mediaRecorder.stop();
            stopped=true;
            pb.setProgress(0); // if u want to reset
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder=null;
            timer.cancel();
            timer=null;
            pb.setVisibility(View.GONE);
            playBackBtn.setEnabled(true);
            stopPlayBackBtn.setEnabled(true);
        }
    }

    public void onPlayBack(View v){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(audioFileName);
            recordBtn.setEnabled(false);
            stopRecordBtn.setEnabled(false);
            mediaPlayer.prepare(); // must call prepare first
            pb.setMax(mediaPlayer.getDuration());
            pb.setProgress(0);
            timer = new Timer();
            pb.setVisibility(View.VISIBLE);
            startProgress();
            stopped=false;
            mediaPlayer.start(); // then start
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onStopPlayBack(View v){
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            pb.setProgress(0); // if u want to reset
            pb.setVisibility(View.GONE);
            stopped=true;
            mediaPlayer=null;
            timer.cancel();
            timer=null;
            recordBtn.setEnabled(true);
            stopRecordBtn.setEnabled(true);
        }
    }

    private void startProgress()
    {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!stopped)  // call ui only when  the progress is not stopped
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                pb.setProgress(pb.getProgress()+1000);
                            } catch (Exception e) {e.printStackTrace();}
                        }
                    });
                }
            }
        }, 1, 1000);
    }


}
