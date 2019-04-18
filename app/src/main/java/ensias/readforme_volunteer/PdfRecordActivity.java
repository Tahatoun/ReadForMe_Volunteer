package ensias.readforme_volunteer;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import ensias.readforme_volunteer.lame.AndroidLame;
import ensias.readforme_volunteer.lame.LameBuilder;
import ensias.readforme_volunteer.model.File;
import es.voghdev.pdfviewpager.library.RemotePDFViewPager;
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter;
import es.voghdev.pdfviewpager.library.remote.DownloadFile;
import es.voghdev.pdfviewpager.library.util.FileUtil;
import ensias.readforme_volunteer.databinding.ActivityPdfRecordBinding;
public class PdfRecordActivity extends AppCompatActivity implements DownloadFile.Listener  {
    static {
        System.loadLibrary("wrapper");
    }
    private Activity activity=this;
    ActivityPdfRecordBinding binding;
    RemotePDFViewPager remotePDFViewPager;
    private UploadTrackService uploadTrackService;
    PDFPagerAdapter adapter;
    LinearLayout root;
    File file;
    String filePath;
    long currentTime;
    FloatingActionMenu menuButton;
    FloatingActionButton recordButton, stopButton, playButton;
    int minBuffer;
    int inSamplerate = 48000;
    AudioRecord audioRecord;
    AndroidLame androidLame;
    FileOutputStream outputStream;
    short[] buffer;
    byte[] mp3buffer;
    PlayAudio playTask;
    CountDownTimer t;
    TextView textViewTimer;
    private MediaPlayer mp;
    private int cnt=0;
    boolean fileRecorder = false,fileEmported = false;
    boolean isRecording = false,isPlaying = false;
    private  boolean serviceBound=false;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this,R.layout.activity_pdf_record);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pdf_record);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        textViewTimer=(TextView)findViewById(R.id.textViewTimer);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
       currentTime=System.currentTimeMillis();
        filePath = getFilesDir().getAbsolutePath() + "/r@"+currentTime+".mp3";

        minBuffer = AudioRecord.getMinBufferSize(inSamplerate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, inSamplerate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBuffer * 2);
        buffer = new short[inSamplerate * 2 * 5];
        mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];

        java.io.File path = new java.io.File(getFilesDir().getAbsolutePath());
        path.mkdirs();

        root = (LinearLayout) findViewById(R.id.remote_pdf_root);
        menuButton = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);

        menuButton.setVisibility(View.GONE);

        initView();

        file= (File) getIntent().getSerializableExtra("file");

        remotePDFViewPager= new RemotePDFViewPager(getBaseContext(), file.getUrlfile(), this);
        remotePDFViewPager.setId(R.id.pdfViewPager);

        t = new CountDownTimer( Long.MAX_VALUE , 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                cnt++;
                long seconds = cnt;
                int minutes = (int) (seconds / 60);
                seconds = seconds % 60;
                textViewTimer.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {}
        };

        binding.recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording) { // start recording audio
                    binding.recordButton.setLabelText(getString(R.string.Pause));
                    binding.recordButton.setImageResource(R.drawable.ic_pause_red_24dp);
                    isRecording=true;
                    new Thread() {
                        @Override
                        public void run() {
                            t.start();
                            startRecording();
                        }
                    }.start();
                    updateView();
                } else { // resume audio recording
                    isRecording = false;
                    binding.recordButton.setLabelText(getString(R.string.Record));
                    binding.recordButton.setImageResource(R.drawable.ic_fiber_manual_record_black_24dp);
                    //recordThread.interrupt();
                    t.cancel();
                }

                fileRecorder = true;
            }
        });

        binding.stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    stopRecording();
                t.cancel();

            }
        });

        binding.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlaying){
                    stopPlaying();
                }else{
                    play();
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.record_activity_bar, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_save : ;
                final LayoutInflater inflater = activity.getLayoutInflater();
                final AlertDialog.Builder newPlaylistBuilder = new AlertDialog.Builder(activity);
                final View deleteView = inflater.inflate(R.layout.add_track_dialog, null);
                newPlaylistBuilder.setView(deleteView)
                        .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                               Intent intent = new Intent( PdfRecordActivity.this , UploadTrackService.class);
                                intent.putExtra("file", file);
                                startService(intent);
                                bindService(intent, uploadTrackServiceConnection, Context.BIND_AUTO_CREATE);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}
                        });
                newPlaylistBuilder.create().show();


            break;

            default:
                ;

        }

        return true;
    }

    @Override
    public void onSuccess(String url, String destinationPath) {

        root.removeAllViewsInLayout();
        adapter = new PDFPagerAdapter(this, FileUtil.extractFileNameFromURL(url));
        remotePDFViewPager.setAdapter(adapter);
        root.addView(remotePDFViewPager,
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        menuButton.setVisibility(View.VISIBLE);

    }

    @Override
    public void onFailure(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onProgressUpdate(int progress, int total) {
        Log.d("progress FILE ",progress+"");

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initView(){
        binding.stopButton.setVisibility(View.GONE);
        binding.playButton.setVisibility(View.GONE);
        binding.playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        binding.recordButton.setImageResource(R.drawable.ic_fiber_manual_record_black_24dp);
        textViewTimer.setText("00:00");
        cnt=0;
    }

    private void updateView(){
        binding.stopButton.setVisibility(View.VISIBLE);
        binding.playButton.setVisibility(View.VISIBLE);
    }
    public void play() {
        if(filePath!=null){
            isPlaying = true;
            binding.recordButton.setEnabled(true);
            binding.stopButton.setEnabled(true);
            playTask = new PlayAudio();
            playTask.execute();
            binding.playButton.setImageResource(R.drawable.ic_pause_black_24dp);
        }else{
            Toast.makeText(this, R.string.no_file_imported, Toast.LENGTH_SHORT).show();
        }
    }

    public void stopPlaying() {
        if (isPlaying) {
            isPlaying = false;
            mp.stop();
            binding.playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }
    private void startRecording() {
        try {
            outputStream = new FileOutputStream(new java.io.File(filePath),true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        androidLame = new LameBuilder()
                .setInSampleRate(audioRecord.getSampleRate())
                .setOutChannels(1)
                .setOutBitrate(128)
                .setOutSampleRate(audioRecord.getSampleRate())
                .setQuality(5)
                .build();
        audioRecord.startRecording();

        int bytesRead = 0;
        while (isRecording) {
            bytesRead = audioRecord.read(buffer, 0, minBuffer);
            if (bytesRead > 0) {
                int bytesEncoded = androidLame.encode(buffer, buffer, bytesRead, mp3buffer);
                if (bytesEncoded > 0) {
                    try {
                        outputStream.write(mp3buffer, 0, bytesEncoded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        int outputMp3buf = androidLame.flush(mp3buffer);
        if (outputMp3buf > 0) {
            try {
                outputStream.write(mp3buffer, 0, outputMp3buf);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        audioRecord.stop();
      //  audioRecord.release();
        androidLame.close();
    }

    private void stopRecording(){


                try {
                    //outputStream.write(mp3buffer, 0, outputMp3buf);
                    outputStream = new FileOutputStream(new java.io.File(filePath));
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            //}
            audioRecord.stop();
            //audioRecord.release();
            androidLame.close();
            isRecording = false;
            fileRecorder = false;
            initView();
    }

    private class PlayAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            isPlaying = true;
            try {
                mp = new MediaPlayer();
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        binding.playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        isPlaying = false;
                    }
                });

                try {
                    mp.setDataSource(getApplicationContext(), Uri.parse(filePath));
                } catch (IllegalArgumentException | SecurityException | IOException | IllegalStateException e) {
                    e.printStackTrace();
                }

                try {
                    mp.prepare();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mp.start();
            } catch (Throwable t) {
                Log.e("AudioTrack", "Playback Failed");
            }
            return null;
        }
    }

    /*========================================================= SERVICE ================================*/
    private ServiceConnection uploadTrackServiceConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            UploadTrackService.UploadTrackServiceBinder binder = (UploadTrackService.UploadTrackServiceBinder)service;
            //get service
            uploadTrackService = binder.getService();

            if (uploadTrackService!=null){
                serviceBound=true;
                uploadTrackService.setUploadedFile(filePath);
                uploadTrackService.setFilePdf(file);
                //uploadTrackService.setName(binding.editTextTitle.getText().toString());
                //uploadTrackService.setDescription(binding.editTextDescription.getText().toString());
                uploadTrackService.setNOTIFICATION_ID(Integer.parseInt((""+currentTime).substring((""+currentTime).length()-9)));
                Intent intent=new Intent(UploadTrackService.START_UPLOAD);
                sendBroadcast(intent);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uploadTrackServiceConnection != null) {
            try {
                unbindService(uploadTrackServiceConnection);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}
