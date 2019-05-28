package ensias.readforme_volunteer.listAdapter;


        import android.app.Activity;
        import android.content.Intent;
        import android.media.MediaPlayer;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.AbsListView;
        import android.widget.ArrayAdapter;
        import android.widget.ImageView;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.like.LikeButton;
        import com.like.OnLikeListener;
        import com.squareup.picasso.Picasso;

        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.LinkedList;
        import java.util.List;
        import java.util.Locale;

        import ensias.readforme_volunteer.ProfileActivity;
        import ensias.readforme_volunteer.R;
        import ensias.readforme_volunteer.dao.ImageTransformation;
        import ensias.readforme_volunteer.dao.Util;
        import ensias.readforme_volunteer.model.File;
        import ensias.readforme_volunteer.model.Playlist;
        import ensias.readforme_volunteer.model.Volunteer;
        import retrofit2.Call;
        import retrofit2.Callback;
        import retrofit2.Response;

public class FileAdapter extends ArrayAdapter<File>  {
    private  ArrayList<File> suggestions;
    String imagePath;
    List<File> posts;
    List<File> Suggestions;
    Volunteer volunteer;
    boolean suggestionsB;
    List<Integer> suggestionPositions;
    List<File> files;
    Activity context;
    public MediaPlayer player;

    public MediaPlayer getPlayer() {
        return player;
    }

    public void setPlayer(MediaPlayer player) {
        this.player = player;
    }
    public FileAdapter(Activity context, int resource, Volunteer volunteer, List<File> items, ArrayList<File> suggestions) {
        super(context, resource, items);
        posts=items;
        files=new ArrayList<>();
        files.addAll(items);
        Suggestions = suggestions;
        suggestionsB = true;
        this.context = context;
        this.volunteer = volunteer;
        this.suggestions =  suggestions;
        suggestionPositions = new LinkedList<>();
        player = new MediaPlayer();
        try {
            imagePath = Util.getImagesHost(getContext())+ Util.getProperty("Post",this.getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View v = convertView;
        final LayoutInflater vi = LayoutInflater.from(getContext());
        final File post= getItem(position);

        if (v == null) {
            v = vi.inflate(R.layout.file_item, null);

        }
        if( (Math.random() >= 1.0 - 0.05 && suggestionsB ) || (volunteer.getSuggestions().size() < 10  && suggestionsB ) || suggestionPositions.contains(position) ){
            v = vi.inflate(R.layout.post_suggestion_item, null);
            if( !(posts.contains(post))) posts.add(posts.size(),post);
            notifyDataSetChanged();
            suggestionPositions.add(position);
            suggestionsB = false;
            final ViewGroup insertPoint = (ViewGroup) v.findViewById(R.id.suggestionItems);
            insertPoint.removeAllViews();
            for(final File suggestion : suggestions){
                final View suggestionView = vi.inflate(R.layout.post_suggestion_file_item, null);
                TextView name = (TextView) suggestionView.findViewById(R.id.textViewName);
                ImageView image = (ImageView) suggestionView.findViewById(R.id.circleImageViewPicture);
                name.setText(suggestion.getName());
                Picasso.with(getContext()).load(suggestion.getImage() )
                        .transform(new ImageTransformation())
                        .placeholder(R.mipmap.ic_camera)
                        .error(R.mipmap.ic_camera)
                        .into(image);
                insertPoint.addView(suggestionView, insertPoint.getChildCount(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                suggestionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra("currentVolunteer",volunteer);
                        intent.putExtra("File", suggestion);
                        context.startActivityForResult(intent, 1);*/
                    }
                });
            }


        }else {
            v = vi.inflate(R.layout.file_item, null);
            ImageView picture = (ImageView) v.findViewById(R.id.imageViewImage);
            TextView name = (TextView) v.findViewById(R.id.textViewName);
            TextView desc = (TextView) v.findViewById(R.id.textViewType);
            TextView by = (TextView) v.findViewById(R.id.textViewBy);
            final TextView content = (TextView) v.findViewById(R.id.textViewContent);
            //ImageView trackImage = (ImageView) v.findViewById(R.id.imageViewImageTrack);
            final LikeButton like = (LikeButton) v.findViewById(R.id.imageViewLike);

            like.setLiked(false);
            like.setEnabled(true);
            name.setText(post.getBlind().getFirstName()+" "+post.getBlind().getLastName());

            desc.setText(R.string.shared);
            desc.setText(desc.getText() + " " + post.getName());
            if(post.getBlind().getId() != post.getBlind().getId() ){
                by.setText(R.string.by);
                by.setText(by.getText() + " " +  post.getBlind().getFirstName() + " " + post.getBlind().getLastName());
            }

            content.setText(post.getDescription());
            if(post.getBlind().getImage() != null){
                System.out.println(post.getBlind().getImage() );
                Picasso.with(getContext()).load(post.getBlind().getImage() )
                        .transform(new ImageTransformation())
                        .placeholder(R.mipmap.ic_camera)
                        .error(R.mipmap.ic_camera)
                        .into(picture);
                picture.setImageResource(R.drawable.livre);
            }else {
                picture.setImageResource(R.mipmap.ic_camera);
                picture.setImageResource(R.drawable.livre);
            }
            /*if(post.getImage() == null) trackImage.setVisibility(View.GONE);
            else{
                Picasso.with(getContext()).load(post.getImage() )
                        .placeholder(R.mipmap.ic_camera)
                        .error(R.mipmap.ic_camera)
                        .into(trackImage);
            }*/
  /*          if(volunteer.getFavourites().contains(post)){
                like.setLiked(true);
            }else{
                like.setLiked(false);
            }*/
            final LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            like.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    Call favCall = Util.getTrackService(getContext()).favouriteTrack(posts.get(position).getId(), volunteer.getId());
                    favCall.enqueue(new Callback() {
                        @Override
                        public void onResponse(Call call, Response response) {
                            if(response.code() == 200){
                                Toast.makeText(getContext(), R.string.track_added_favourite, Toast.LENGTH_SHORT).show();
                                like.setLiked(true);
                                volunteer.getFavourites().add(post);
                            }else {
                                like.setLiked(false);
                                Toast.makeText(getContext(), R.string.application_error, Toast.LENGTH_SHORT).show();
                            }
                            Util.printResponse(response);
                        }

                        @Override
                        public void onFailure(Call call, Throwable t) {
                            like.setLiked(false);
                            Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    Call ufavCall = Util.getTrackService(getContext()).unFavouriteTrack(posts.get(position).getId(), volunteer.getId());
                    ufavCall.enqueue(new Callback() {
                        @Override
                        public void onResponse(Call call, Response response) {
                            if(response.code() == 200){
                                like.setLiked(false);
                                volunteer.getFavourites().remove(post);
                                Toast.makeText(getContext(), R.string.track_removed_favourite, Toast.LENGTH_SHORT).show();
                            }else {
                                like.setLiked(true);
                                Toast.makeText(getContext(), R.string.application_error, Toast.LENGTH_SHORT).show();
                            }
                            Util.printResponse(response);
                        }

                        @Override
                        public void onFailure(Call call, Throwable t) {
                            like.setLiked(true);
                            Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
        return v;
    }


    public CharSequence[] getPlaylists(ArrayList<Playlist> playlists){
        ArrayList<CharSequence> playlistsString = new ArrayList<>();
        for(Playlist p : playlists)
            playlistsString.add(p.getName());
        return playlistsString.toArray(new CharSequence[playlistsString.size()]);
    }

    public void filter(String query) {
        query = query.toLowerCase(Locale.getDefault());
        if (posts.size()>=files.size()){
            files.clear();
            files.addAll(posts);
        }
        posts.clear();
        if (query.length()==0){
            posts.addAll(files);
        }
        else {
            for (File file : files){
                if (file.getName().toLowerCase(Locale.getDefault())
                        .contains(query)){
                    posts.add(file);
                }
            }
        }
        Log.d("array size files",files.size()+"");
        Log.d("array size posts",posts.size()+"");
        notifyDataSetChanged();
    }
}
