package ensias.readforme_volunteer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import ensias.readforme_volunteer.dao.Util;
import ensias.readforme_volunteer.model.File;
import ensias.readforme_volunteer.model.Track;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadTrackService  extends Service {


    public int NOTIFICATION_ID;
    public static final String START_UPLOAD = "start_upload";
    private final IBinder editorBind = new UploadTrackServiceBinder();
    private String uploadedFile;
    private File filePdf;
    private String name="track";
    private String description="";
    private java.io.File file;


    public int getNOTIFICATION_ID() {
        return NOTIFICATION_ID;
    }

    public void setNOTIFICATION_ID(int NOTIFICATION_ID) {
        this.NOTIFICATION_ID = NOTIFICATION_ID;
    }

    public File getFilePdf() {
        return filePdf;
    }

    public void setFilePdf(File filePdf) {
        this.filePdf = filePdf;
    }

    public String getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(String uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return editorBind;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(START_UPLOAD);
        registerReceiver(receiver,filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;

        // return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case START_UPLOAD:
                    //file = (java.io.File) intent.getExtras().get("file");
                    startUpload();
                    break;
            }

        }
    };

    NotificationManager manager;
    public void startUpload(){

        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this,"CH_ID_"+NOTIFICATION_ID)
                        .setSmallIcon(R.mipmap.ic_logo)
                        .setContentTitle(name)
                        .setContentText(getString(R.string.uploading))
                        .setOngoing(true)
                        .setAutoCancel(true);

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, mBuilder.build());

        final Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        final Intent intentSame = new Intent(this,PdfRecordActivity.class);
        intentSame.putExtra("path", uploadedFile);
        intentSame.putExtra("file", filePdf);

        java.io.File file = new java.io.File(uploadedFile);
        RequestBody requestFile = RequestBody.create(MediaType.parse("form-data"), file);
        MultipartBody.Part bodyFile = MultipartBody.Part.createFormData("mp3", file.getName(), requestFile);
        RequestBody bodyDescription = RequestBody.create(okhttp3.MultipartBody.FORM, description);
        RequestBody bodyName = RequestBody.create(okhttp3.MultipartBody.FORM, name);
        RequestBody fileId = RequestBody.create(MultipartBody.DIGEST, filePdf.getId()+"");
        Call<Track> call;

        call = Util.getTrackService(getApplicationContext()).postTrack(fileId,bodyName,bodyDescription,bodyFile);

        call.enqueue(new Callback<Track>() {
            @Override
            public void onResponse(Call<Track> call, Response<Track> response) {
                Log.v("Upload", "Code : " + response.code());
                long[] vibrate = { 0, 100, 200, 300 };
                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setVibrate(vibrate);
                intent.putExtra("path", uploadedFile);
                if(response.isSuccessful()){
                    intent.putExtra("track",response.body());
                    final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(pendingIntent);
                    mBuilder.setContentText(getString(R.string.file_uploaded));
                    Log.v("Upload", "success");
                }else{
                    final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0, intentSame,PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentText(" tt "+ response.code()+getString(R.string.error_uploading_track));
                    mBuilder.setContentIntent(pendingIntent);
                    Log.v("Upload", "Error : " + response.code());
                }
                manager.notify(NOTIFICATION_ID, mBuilder.build());
            }
            @Override
            public void onFailure(Call<Track> call, Throwable t) {
                Util.printThrowable(t);
                t.printStackTrace();
                final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0, intentSame,PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentText(getString(R.string.error_uploading_track));
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setOngoing(false);
                mBuilder.setAutoCancel(true);
                manager.notify(NOTIFICATION_ID, mBuilder.build());

            }
        });


    }
    /*========================= Binder ===============================================*/
    public class UploadTrackServiceBinder extends Binder {
        UploadTrackService getService() {
            return UploadTrackService.this;
        }
    }

}
