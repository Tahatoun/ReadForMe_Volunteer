package ensias.readforme_volunteer;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ensias.readforme_volunteer.dao.ImageTransformation;
import ensias.readforme_volunteer.dao.SharedData;
import ensias.readforme_volunteer.dao.Util;
import ensias.readforme_volunteer.databinding.ActivityProfileBinding;
import ensias.readforme_volunteer.model.Volunteer;
import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity implements OnMenuItemClickListener {

    ActivityProfileBinding binding;
    Activity activity;
    Volunteer volunteer;
    File dest;
    private FragmentManager fragmentManager;
    private ContextMenuDialogFragment mMenuDialogFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        volunteer = SharedData.getVolunteer(this);
        DataBindingUtil.setContentView(this,R.layout.activity_profile);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        initMenuFragment();
        fragmentManager = getSupportFragmentManager();
        binding.setVolunteer(volunteer);

        TagView tagview =  (TagView) findViewById(R.id.tagview);

        Tag tag = new Tag("Tag Text");
        tag.tagTextColor = Color.parseColor("#FFFFFF");
        tag.layoutColor =  Color.parseColor("#fd456d");
        tag.layoutColorPress = Color.parseColor("#555555");
//or tag.background = this.getResources().getDrawable(R.drawable.custom_bg);
        tag.radius = 20f;
        tag.tagTextSize = 14f;
        tag.layoutBorderSize = 1f;
        tag.layoutBorderColor = Color.parseColor("#FFFFFF");
        tag.isDeletable = true;
        tagview.addTag(tag);
        tagview.addTag(tag);
        tagview.addTag(tag);
        tagview.addTag(tag);
        tagview.addTag(tag);
        tagview.addTag(tag);
        tagview.addTag(tag);
        tagview.addTag(tag);
        tagview.addTag(tag);
        tagview.addTag(tag);
        tagview.addTag(tag);
        tagview.addTag(tag);

        if(volunteer==null){
            volunteer=new Volunteer();
        }


        setGender();
        String emptyString = "";
        if(volunteer.getFirstName() == null) {
            volunteer.setFirstName(emptyString);
        }
        if(volunteer.getLastName() == null) {
            volunteer.setLastName(emptyString);
        }

        Picasso.with(ProfileActivity.this)
                .load(volunteer.getImage())
                .transform(new ImageTransformation()).
                placeholder(R.mipmap.ic_camera).error(R.mipmap.ic_camera).into(binding.imageViewAddPicture);

        binding.imageViewAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crop.pickImage(activity);
            }
        });

        binding.imageViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestBody fistName = RequestBody.create(MultipartBody.FORM, binding.EditTextFname.getText().toString());
                RequestBody lastName = RequestBody.create(MultipartBody.FORM, binding.EditTextLname.getText().toString());
                RequestBody email = RequestBody.create(MultipartBody.FORM, binding.EditTextEmail.getText().toString());
                RequestBody gender = RequestBody.create(MultipartBody.FORM, ""+getGender());

                Call<Volunteer> call;
                if( dest  != null){
                    RequestBody requestFileImage = RequestBody.create(MediaType.parse("form-data"), dest);
                    MultipartBody.Part bodyImage = MultipartBody.Part.createFormData("image", dest.getName(), requestFileImage);
                    call = Util.getVolunteerService(getApplicationContext()).updateProfile(fistName,lastName,email,gender,bodyImage);
                }else{
                    call = Util.getVolunteerService(getApplicationContext()).updateProfileWithoutPic(fistName,lastName,email,gender);
                }
                call.enqueue(new Callback<Volunteer>() {
                    @Override
                    public void onResponse(Call<Volunteer> call, Response<Volunteer> response) {
                        if(response.isSuccessful()){
                            Log.v("update profile", "success");
                            Toast.makeText(ProfileActivity.this, R.string.profile_updating,Toast.LENGTH_LONG).show();
                            SharedData.saveVolunteerInSharedPref(response.body().toString(), ProfileActivity.this);
                            startActivity(new Intent(ProfileActivity.this,MainActivity.class));
                            finish();
                        }else{
                            Toast.makeText(ProfileActivity.this, R.string.error_update_profile,Toast.LENGTH_LONG).show();
                            Log.v("update profile", "Error");
                        }
                    }
                    @Override
                    public void onFailure(Call<Volunteer> call, Throwable t) {
                        Log.e("update profile error:", t.getMessage());
                        Toast.makeText(ProfileActivity.this,R.string.network_error,Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        binding.imageViewMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null)
                    mMenuDialogFragment.show(fragmentManager, ContextMenuDialogFragment.TAG);
            }
        });
    }

    private void setGender() {
        binding.radioButtonNot.setChecked(false);
        binding.radioButtonMale.setChecked(false);
        binding.radioButtonFemale.setChecked(false);
        if(new Integer(volunteer.getGender()) == null) {
            binding.radioButtonNot.toggle();
            return;
        }
        switch (volunteer.getGender()){
            case 0 : binding.radioButtonNot.toggle();break;
            case 1 : binding.radioButtonMale.toggle();break;
            case 2 : binding.radioButtonFemale.toggle();break;
            default : binding.radioButtonNot.toggle();break;
        }
    }

    private int getGender() {
        if(binding.radioButtonNot.isChecked())
            return 0;
        else if (binding.radioButtonMale.isChecked())
            return  1;
        else if (binding.radioButtonFemale.isChecked())
            return 2;
        else return  0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);
        }
    }

    private void beginCrop(Uri source) {
        dest = new File(getCacheDir(), System.currentTimeMillis()+"cropped");
        if(!dest.exists()) try {
            dest.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri destination = Uri.fromFile(dest);
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            binding.imageViewAddPicture.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void initMenuFragment() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.menu_icon_size));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(true);
        menuParams.setAnimationDuration(40);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        mMenuDialogFragment.setItemClickListener(this);
    }
    private List<MenuObject> getMenuObjects() {
        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject();
        close.setResource(R.drawable.ic_close);

        MenuObject myProfile = new MenuObject("Profile");
        myProfile.setResource(R.drawable.ic_profile);

        MenuObject addFav = new MenuObject("Favourites");
        addFav.setResource(R.drawable.ic_heart_colored);

        MenuObject home = new MenuObject("HomeActivity");
        home.setResource(R.drawable.ic_home);

        menuObjects.add(close);
        menuObjects.add(myProfile);
        menuObjects.add(addFav);
        menuObjects.add(home);
        return menuObjects;
    }
    @Override
    public void onMenuItemClick(View clickedView, int position) {
        Intent intent = null;
        switch (position){
            case 1 :intent = new Intent(getApplicationContext(), ProfileActivity.class);break;
            case 2 :intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("Volunteer",volunteer);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
            default: break;
        }
        if(position > 0 )
        {

            intent.putExtra("Volunteer", volunteer);
            startActivityForResult(intent, 1);
        }
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}
