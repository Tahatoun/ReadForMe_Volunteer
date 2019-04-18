package ensias.readforme_volunteer.dao;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class Util {
    static String configFileName = "config.properties";
    public static void requestPermission(Activity activity, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, 0);
        }
    }
    public static String getProperty(String key,Context context) throws IOException {
        Properties properties = new Properties();;
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(configFileName);
        properties.load(inputStream);
        return properties.getProperty(key);
    }
    public static String getApiHost(Context context) throws IOException {
        Properties properties = new Properties();;
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(configFileName);
        properties.load(inputStream);
        return properties.getProperty("Host")+properties.getProperty("Api");
    }
    public static String getImagesHost(Context context) throws IOException {
        Properties properties = new Properties();;
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(configFileName);
        properties.load(inputStream);
        return properties.getProperty("Host")+properties.getProperty("Images");
    }
    public static VolunteerInterface getVolunteerService (Context context){
        VolunteerInterface artistInterface = null;  // crash
        try {
            System.out.println("HOST : "+getApiHost(context));
            artistInterface = RetrofitBuilder.createServiceWithAuth(VolunteerInterface.class,Auth.getInstance(context.getSharedPreferences("token_prefs", MODE_PRIVATE)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  artistInterface;
    }
    public static PlaylistIterface getPlaylistService(Context context){
        PlaylistIterface playlistIterface = null;
        try {
            System.out.println("HOST : "+getApiHost(context));

            playlistIterface = RetrofitBuilder.createServiceWithAuth(PlaylistIterface.class,Auth.getInstance(context.getSharedPreferences("token_prefs", MODE_PRIVATE)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return playlistIterface;
    }
    public static TrackInterface getTrackService(Context context){
        TrackInterface trackInterface = null;
        try {
            System.out.println("HOST : "+getApiHost(context));
            trackInterface = RetrofitBuilder.createServiceWithAuth(TrackInterface.class,Auth.getInstance(context.getSharedPreferences("token_prefs", MODE_PRIVATE)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trackInterface;
    }

    private static char[] c = new char[]{'k', 'm', 'b', 't'};
    public static String coolFormat(double n, int iteration) {
        double d = ((long) n / 100) / 10.0;
        boolean isRound = (d * 10) %10 == 0;//true if the decimal part is equal to 0 (then it's trimmed anyway)
        return (d < 1000? //this determines the class, i.e. 'k', 'm' etc
                ((d > 99.9 || isRound || (!isRound && d > 9.99)? //this decides whether to trim the decimals
                        (int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
                ) + "" + c[iteration])
                : coolFormat(d, iteration+1));
    }
    public static void printResponse(Response response){
        System.out.println("Response code : "+response.code());
        System.out.println("Response message : "+response.message());
        try {
            System.out.println("Response body : "+((okhttp3.ResponseBody)response.body()).string());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void printThrowable(Throwable throwable){
        try {
            System.out.println("Localized Message : "+throwable.getLocalizedMessage());
            System.out.println("Message : "+throwable.getMessage());
            System.out.println("Stack Trace : "+throwable.getStackTrace().toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     * @param currentDuration
     * @param totalDuration
     * */
    public static int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     * @param progress -
     * @param totalDuration
     * returns current duration in milliseconds
     * */
    public static  int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }
}