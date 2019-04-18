package ensias.readforme_volunteer;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.dd.processbutton.iml.ActionProcessButton;

import ensias.readforme_volunteer.dao.Auth;
import ensias.readforme_volunteer.dao.RetrofitBuilder;
import ensias.readforme_volunteer.dao.Util;
import ensias.readforme_volunteer.dao.VolunteerInterface;
import ensias.readforme_volunteer.model.AccessToken;
import ensias.readforme_volunteer.model.Volunteer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ensias.readforme_volunteer.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    AwesomeValidation validator;
    Call<AccessToken> tokenService;
    VolunteerInterface retrofitService;
    Auth authToken;
    Volunteer volunteer;

    ActionProcessButton signinB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this,R.layout.activity_login);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        retrofitService= RetrofitBuilder.createService(VolunteerInterface.class);
        validator = new AwesomeValidation(ValidationStyle.BASIC);
        authToken = Auth.getInstance(getSharedPreferences("token_prefs", MODE_PRIVATE));
        Rules();
        signinB = (ActionProcessButton) findViewById(R.id.signinButton);
        signinB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.editTextLogin.setError(null);
                binding.editTextPassword.setError(null);
                validator.clear();
                if(validator.validate()) {
                    signinB.setProgress(50);
                    tokenService=retrofitService.Login(binding.editTextLogin.getText().toString(),binding.editTextPassword.getText().toString());
                    tokenService.enqueue(new Callback<AccessToken>() {
                        @Override
                        public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                            signinB.setProgress(100);
                            if (response.isSuccessful()) {
                                try {
                                    authToken.saveToken(response.body());
                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    finish();
                                }catch (Exception e){
                                    signinB.setProgress(-1);
                                    e.printStackTrace();
                                    Toast.makeText(LoginActivity.this, "Application Error", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                signinB.setProgress(-1);
                                Toast.makeText(LoginActivity.this, "Network Error " + response.code(), Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onFailure(Call<AccessToken> call, Throwable t) {
                            signinB.setProgress(-1);
                            Toast.makeText(LoginActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                            Util.printThrowable(t);
                        }
                    });
                }
            }
        });

        if(authToken.getToken().getAccessToken() != null){
            startActivity(new Intent(LoginActivity.this,HomeActivity.class));
            finish();
        }

        binding.textViewSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class));
                finish();
            }
        });

    }

    public void Rules(){
        validator.addValidation(this, binding.editTextLogin.getId(), RegexTemplate.NOT_EMPTY, R.string.volunteername_error);
        validator.addValidation(this, binding.editTextPassword.getId(), "[a-zA-Z0-9]{6,}", R.string.password_error);
    }


    class volunteerCallback  implements Callback<Volunteer>{
        @Override
        public void onResponse(Call<Volunteer> call, Response<Volunteer> response) {
            System.out.println();
        }
        @Override
        public void onFailure(Call<Volunteer> call, Throwable t) {
            System.out.println(t.toString());
        }
    }
}
