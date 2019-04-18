package ensias.readforme_volunteer.dao;



import ensias.readforme_volunteer.model.Playlist;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PlaylistIterface {
    @GET("playlists/{id}")
    Call<Playlist> getPlaylistWithIncludes(@Path("id") int user, @Query("include") String include);

    @FormUrlEncoded
    @POST("playlists")
    Call<ResponseBody> addPlaylist(@Field("artist_id") int id, @Field("name") String name);

    @DELETE("playlists/{id}")
    Call<ResponseBody> deletePlaylist(@Path("id") int playlist_id);
}
