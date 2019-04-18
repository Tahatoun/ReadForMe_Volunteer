package ensias.readforme_volunteer.dao;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.io.Serializable;

import ensias.readforme_volunteer.model.Volunteer;
import ensias.readforme_volunteer.R;

public class SharedData {
    public static void saveVolunteerInSharedPref(String Volunteer,Context context){
        SharedPreferences pref = context.getSharedPreferences(context.getResources().getString(R.string.volunteerKey), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(context.getString(R.string.volunteerKey), Volunteer);
        editor.commit();
    }
    public static void removeVolunteerInSharedPref(Context context){
        SharedPreferences pref = context.getSharedPreferences(context.getResources().getString(R.string.volunteerKey), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(context.getResources().getString(R.string.volunteerKey));
        editor.commit();
    }
    public static Volunteer getVolunteerFromSharedPref(Context context){
        SharedPreferences pref = context.getSharedPreferences(context.getResources().getString(R.string.volunteerKey), Context.MODE_PRIVATE);
        String key = context.getResources().getString(R.string.volunteerKey);
        if(pref.contains(key)){
            Volunteer Volunteer = new Volunteer();
            Volunteer = new Gson().fromJson(pref.getString(context.getResources().getString(R.string.volunteerKey),"undefined"), Volunteer.class);
            return  new Gson().fromJson(pref.getString(context.getResources().getString(R.string.volunteerKey),"undefined"), Volunteer.class);
        }
        return null;
    }

    public static Serializable getVolunteerFromExtra(Activity activity){
        if(activity.getIntent().hasExtra("Volunteer")){
            if(activity.getIntent().getExtras().getSerializable("Volunteer") != null){
                return activity.getIntent().getExtras().getSerializable("Volunteer");
            }else return  null;
        }
        return null;
    }
    public static Volunteer getVolunteer(Activity activity){
        Volunteer Volunteer = new Volunteer();
        Volunteer = (Volunteer) SharedData.getVolunteerFromExtra(activity);
        if(Volunteer == null)
            Volunteer = getVolunteerFromSharedPref(activity);
        return Volunteer;
    }
}
