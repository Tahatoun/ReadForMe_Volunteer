package ensias.readforme_volunteer.dao;



import java.util.ArrayList;

import ensias.readforme_volunteer.model.AccessToken;
import ensias.readforme_volunteer.model.File;
import ensias.readforme_volunteer.model.Home;
import ensias.readforme_volunteer.model.Volunteer;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface VolunteerInterface {
    @GET("volunteers/{id}?include=playlists,favouritesTracks,tracks,posts,followers,following,notifications,likedTracks,reportedTracks,commentedTracks")
    Call<Volunteer> getVolunteerAllInfo(@Path("id") int user);

    @GET("volunteers/auth_volunteer")
    Call<Volunteer> getAuthVolunteer(@Query("include") String include);

    @GET("volunteers/{id}?include=playlists")
    Call<Volunteer> getVolunteerWithPlaylists(@Path("id") int user);

    @GET("volunteers/{id}?include=tracks")
    Call<Volunteer> getVolunteerWithTracks(@Path("id") int user);

    @GET("volunteers/{id}?include=favouritesTracks")
    Call<Volunteer> getVolunteerWithFavouritesTracks(@Path("id") int user);

    @GET("volunteers/{id}?include=posts")
    Call<Volunteer> getVolunteerWithPosts(@Path("id") int user);

    @GET("volunteers/{id}?include=followers")
    Call<Volunteer> getVolunteerWithFollowers(@Path("id") int user);

    @GET("volunteers/{id}?include=followings")
    Call<Volunteer> getVolunteerWithFollowing(@Path("id") int user);

    @GET("volunteers/{id}?include=notifications")
    Call<Volunteer> getVolunteerWithNotifications(@Path("id") int user);

    @GET("volunteers/{id}?include=likedTracks")
    Call<Volunteer> getVolunteerWithlikedTracks(@Path("id") int user);

    @GET("volunteers/{id}?include=reportedTracks")
    Call<Volunteer> getVolunteerWithReportedTracks(@Path("id") int user);

    @GET("volunteers/{id}?include=commentedTracks")
    Call<Volunteer> getVolunteerWithCommentedTracks(@Path("id") int user);

    @GET("volunteers/{id}")
    Call<Volunteer> getVolunteerWithIncludes(@Path("id") int user, @Query("include") String include);

    @GET("volunteers/home")
    Call<Home> getHomeContent(@Query("page") int pageIndex);

    @FormUrlEncoded
    @POST("volunteers/login")
    Call<AccessToken> Login(@Field("username") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("volunteers/signup")
    Call<AccessToken> signUp(@Field("firstName") String fname, @Field("lastName") String lname, @Field("username") String username, @Field("email") String email, @Field("password") String password, @Field("gender") int gender);

    @FormUrlEncoded
    @POST("volunteers/signup")
    Call<AccessToken> signupEmail(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("volunteers/refresh")
    Call<AccessToken> refresh(@Field("refresh_token") String refreshToken);

    @POST("volunteers/logout")
    Call<ResponseBody> logout();

    @FormUrlEncoded
    @POST("volunteers/{id}/follow")
    Call<Volunteer> follow(@Path("id") int Volunteer_id, @Field("followed_id") int track_id);

    @FormUrlEncoded
    @POST("volunteers/{id}/unfollow")
    Call<Volunteer> unfollow(@Path("id") int Volunteer_id, @Field("followed_id") int followed_id);

    @FormUrlEncoded
    @POST("volunteers/{id}/postTrack")
    Call<ResponseBody> post(@Path("id") int Volunteer_id, @Field("track_id") int track_id, @Field("content") String content);

    @Multipart
    @POST("volunteers/updateProfile")
    Call<Volunteer> updateProfile(@Part("firstName") RequestBody firstName, @Part("lastName") RequestBody lastName,  @Part("email") RequestBody email, @Part("gender") RequestBody gender, @Part MultipartBody.Part image);
    @Multipart
    @POST("volunteers/updateProfile")
    Call<Volunteer> updateProfileWithoutPic(@Part("firstName") RequestBody firstName, @Part("lastName") RequestBody lastName, @Part("email") RequestBody email, @Part("gender") RequestBody gender);

    @GET("volunteers/auth_Volunteer")
    Call<ArrayList<File>> getSuggestions();
}
