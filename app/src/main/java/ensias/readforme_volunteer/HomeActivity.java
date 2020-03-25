package ensias.readforme_volunteer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;


import ensias.readforme_volunteer.dao.Auth;
import ensias.readforme_volunteer.dao.SharedData;
import ensias.readforme_volunteer.dao.Util;
import ensias.readforme_volunteer.databinding.ActivityHomeBinding;

import com.google.gson.Gson;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ensias.readforme_volunteer.listAdapter.FileAdapter;
import ensias.readforme_volunteer.model.File;
import ensias.readforme_volunteer.model.Home;
import ensias.readforme_volunteer.model.Volunteer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements OnMenuItemClickListener {
    ActivityHomeBinding binding;
    Call<Home> profileCall;
    Call<Volunteer> profileCall2;
    ArrayList<File> posts;
    ArrayList<Integer> addedByMe;
    Home home;
    Volunteer volunteer;
    FileAdapter adapter;
    Activity activity;
    Auth authManager;
    public View ftView;
    boolean loadingMore=false;
    boolean search=false;
    ArrayList<File> suggestions;
    private FragmentManager fragmentManager;
    private ContextMenuDialogFragment mMenuDialogFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this,R.layout.activity_home);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initMenuFragment();
        fragmentManager = getSupportFragmentManager();
        home=new Home();
        LayoutInflater li = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ftView = li.inflate(R.layout.footer_view, null);

        posts=new ArrayList<>();
        activity = this;
        authManager=Auth.getInstance(getSharedPreferences("token_prefs", MODE_PRIVATE));
        volunteer = (Volunteer) getIntent().getSerializableExtra("volunteer");
        if(volunteer == null){
            volunteer = SharedData.getVolunteer(this);
            if(volunteer == null) volunteer = new Volunteer();
        }
        if(volunteer.getFirstName() == null) volunteer.setFirstName("  ");
        if(volunteer.getLastName() == null) volunteer.setLastName("  ");
        if(authManager.getToken() == null){
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }

        suggestions= new ArrayList<>();
        Call<ArrayList<File>> profileCall3 = Util.getVolunteerService(activity).getSuggestions();
        profileCall3.enqueue(new Callback<ArrayList<File>>() {
            @Override
            public void onResponse(Call<ArrayList<File>> call, Response<ArrayList<File>> response) {
                if(response.isSuccessful()){
                    Log.v("postadapter Suggestions", String.valueOf(response.body().size()));
                    suggestions.addAll(response.body());
                }else{
                    Util.printResponse(response);
                    Log.v("FileAdapter suggestion",response.code()+" "+response.toString());
                }
            }
            @Override
            public void onFailure(Call<ArrayList<File>> call, Throwable t) {
                Toast.makeText(activity, R.string.network_error, Toast.LENGTH_LONG).show();
                Util.printThrowable(t);
                t.printStackTrace();
                System.out.println(call.toString());
            }
        });
        adapter = new FileAdapter(activity,R.layout.file_item,volunteer,posts,posts);
        binding.listViewPosts.setAdapter(adapter);
        profileCall2 = Util.getVolunteerService(this).getAuthVolunteer("favouritesFiles,playlists,followings");

        profileCall2.enqueue(new Callback<Volunteer>() {
            @Override
            public void onResponse(Call<Volunteer> call, Response<Volunteer> response) {
                if(response.isSuccessful()){
                    try {
                        volunteer.setId(response.body().getId());
                        volunteer.setLastName(response.body().getLastName());
                        volunteer.setFirstName(response.body().getFirstName());
                        volunteer.setEmail(response.body().getEmail());
                        volunteer.setBirthdate(response.body().getBirthdate());
                        volunteer.getFavourites().addAll(response.body().getFavourites());
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(HomeActivity.this, "Application Error "+response.code(), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    authManager.deleteToken();
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                    finish();
                }
            }
            @Override
            public void onFailure(Call<Volunteer> call, Throwable t) {
                Toast.makeText(HomeActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                t.printStackTrace();
                System.out.println(call.toString());
            }
        });

        binding.listViewPosts.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //what is the bottom item that is visible
                int lastInScreen = firstVisibleItem + visibleItemCount;

                //is the bottom item file visible & not loading more ? Load more !

                if ((lastInScreen == totalItemCount) && !(loadingMore)) {
                    //if the last page equal to current page ? no more data else load more
                    if (!(search)) {
                        if (home.getCurrent_page() == home.getLast_page()) {
                            Toast.makeText(HomeActivity.this, "No More Files ", Toast.LENGTH_SHORT).show();
                        } else {
                            loadingMore = true;
                            binding.listViewPosts.addFooterView(ftView);
                            homeContent(home.getNextPage());
                        }
                    } else {

                    }
                }
            }
        });

        binding.listViewPosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                final LayoutInflater inflater = activity.getLayoutInflater();
                final AlertDialog.Builder newPlaylistBuilder = new AlertDialog.Builder(activity);
                final View deleteView = inflater.inflate(R.layout.add_record_dialog, null);
                newPlaylistBuilder.setView(deleteView)
                        .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                Intent intent= new Intent(getApplicationContext(), PdfRecordActivity.class);
                                intent.putExtra("file", posts.get(i));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {}
                        });
                newPlaylistBuilder.create().show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_bar, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);


        SearchView searchView = (SearchView)searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                binding.listViewPosts.addFooterView(ftView);
                Toast.makeText(HomeActivity.this, "No text ", Toast.LENGTH_SHORT).show();
                if (TextUtils.isEmpty(s)){
                    adapter.filter("");
                    binding.listViewPosts.clearTextFilter();
                }
                else {
                    adapter.filter(s);
                }
                binding.listViewPosts.removeFooterView(ftView);
                adapter.notifyDataSetChanged();
                return true;
            }
        });
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                search=true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                search=false;
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                if (fragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null)
                    mMenuDialogFragment.show(fragmentManager, ContextMenuDialogFragment.TAG);
                return true;
            case R.id.action_search:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void homeContent(int pageIndex){
        profileCall = Util.getVolunteerService(this).getHomeContent(pageIndex);
        profileCall.enqueue(new Callback<Home>() {
            @Override
            public void onResponse(Call<Home> call, Response<Home> response) {
                if(response.isSuccessful()){
                    try {
                        posts.addAll(posts.size(),response.body().getData());
                        home=response.body();
                        adapter.notifyDataSetChanged();
                        loadingMore=false;
                        binding.listViewPosts.removeFooterView(ftView);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(HomeActivity.this, "Application Error "+response.code(), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Log.v("posts home",response.code()+" "+response.toString());
                }
            }
            @Override
            public void onFailure(Call<Home> call, Throwable t) {
                Toast.makeText(HomeActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                t.printStackTrace();
                System.out.println(call.toString());
            }
        });

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

        MenuObject send = new MenuObject("Playlist");
        send.setResource(R.drawable.ic_playlist_colored);



        menuObjects.add(close);
        menuObjects.add(myProfile);
        menuObjects.add(send);

        return menuObjects;
    }
    @Override
    public void onMenuItemClick(View clickedView, int position) {
        Intent intent = null;
        switch (position){
            case 1 :intent = new Intent(getApplicationContext(), ProfileActivity.class);break;
            case 2 :intent = new Intent(getApplicationContext(), MainActivity.class);
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
 /*   @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        adapter.notifyDataSetChanged();
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.hasExtra("currentVolunteer")) {
                    Volunteer currentVolunteer = (Volunteer) data.getExtras().getSerializable("currentVolunteer");
                    for (File in : currentVolunteer.getFavourites())
                        if (!volunteer.getFavourites().contains(in)) volunteer.getFavourites().add(in);
                    ArrayList<File> tracksToRemove = new ArrayList<>();
                    for (File in : volunteer.getFavourites())
                        if (!currentVolunteer.getFavourites().contains(in)) tracksToRemove.add(in);
                    volunteer.getFavourites().removeAll(tracksToRemove);


                    adapter.notifyDataSetChanged();
                }
                Call<Home> profileCall = Util.getVolunteerService(this).getHomeContent();
                profileCall.enqueue(new Callback<Home>() {
                    @Override
                    public void onResponse(Call<Home> call, Response<Home> response) {

                        if(response.isSuccessful()){
                            try {
                                for(File p : response.body().getData()){
                                    if(!posts.contains(p)) posts.add(p);
                                }
                                adapter.notifyDataSetChanged();
                                binding.progressBar.setVisibility(View.GONE);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(HomeActivity.this, "Application Error "+response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Log.v("posts home",response.code()+" "+response.toString());
                        }
                    }
                    @Override
                    public void onFailure(Call<Home> call, Throwable t) {
                        Toast.makeText(HomeActivity.this, R.string.network_error, Toast.LENGTH_LONG).show();
                        t.printStackTrace();
                        System.out.println(call.toString());
                    }
                });
            }

        }
    }*/
}
