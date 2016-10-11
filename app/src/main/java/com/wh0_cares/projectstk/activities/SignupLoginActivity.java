package com.wh0_cares.projectstk.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.quinny898.library.persistentsearch.SearchResult;
import com.wh0_cares.projectstk.R;
import com.wh0_cares.projectstk.utils.SaveSharedPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupLoginActivity extends AppCompatActivity {

    @Bind(R.id.CardView_login)
    CardView cvLogin;
    @Bind(R.id.CardView_signup)
    CardView cvSignup;
    @Bind(R.id.button_dont_have_account)
    AppCompatButton noAccount;
    @Bind(R.id.button_already_have_account)
    AppCompatButton hasAccount;
    @Bind(R.id.button_login)
    AppCompatButton login;
    @Bind(R.id.button_signup)
    AppCompatButton signup;
    @Bind(R.id.input_username_login)
    EditText loginUsername;
    @Bind(R.id.input_password_login)
    EditText loginPassword;
    @Bind(R.id.input_username_signup)
    EditText signupUsername;
    @Bind(R.id.input_email_signup)
    EditText signupEmail;
    @Bind(R.id.input_password_signup)
    EditText signupPassword;
    String username, email, password;
    private final OkHttpClient client = new OkHttpClient();
    private ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_login);
        ButterKnife.bind(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setupSignupLogin();
        pDialog = new ProgressDialog(this);
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
    }

    private void setupSignupLogin() {
        noAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cvLogin.setVisibility(View.GONE);
                cvSignup.setVisibility(View.VISIBLE);
            }
        });
        hasAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cvSignup.setVisibility(View.GONE);
                cvLogin.setVisibility(View.VISIBLE);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.setEnabled(false);
                try {
                    login(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup.setEnabled(false);
                try {
                    signup();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void login(int id) throws Exception {
        pDialog.setMessage(getString(R.string.Authenticating));
        pDialog.show();
        if (id == 0 && !validate(0)) {
            return;
        }
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url(getString(R.string.login_url))
                .addHeader("Content-Type", "application/json")
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                error(getString(R.string.Error_getting_data));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    error(getString(R.string.Error_getting_data));
                    throw new IOException("Unexpected code " + response.body());
                }
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    JSONObject dataObj = obj.getJSONObject("data");
                    final String access_token = dataObj.getString("access_token");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            SaveSharedPreference.setToken(SignupLoginActivity.this, access_token);
                            SaveSharedPreference.setSetup(SignupLoginActivity.this, 1);
                            pDialog.dismiss();
                            login.setEnabled(true);
                            Intent intent = new Intent(SignupLoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } catch (JSONException e) {
                    error(getString(R.string.Invalid_response));
                    e.printStackTrace();
                }
            }
        });
    }

    public void signup() throws Exception {
        pDialog.setMessage(getString(R.string.Creating_account));
        pDialog.show();
        if (!validate(1)) {
            return;
        }
        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("email", email)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url(getString(R.string.signup_url))
                .addHeader("Content-Type", "application/json")
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                error(getString(R.string.Error_getting_data));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (response.code() == 422){
                        try {
                            JSONObject obj = new JSONObject(response.body().string());
                            String message = obj.getString("message");
                            error(message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    throw new IOException("Unexpected code " + response.body());
                }
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            pDialog.dismiss();
                            signup.setEnabled(true);
                            try {
                                login(1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    error(getString(R.string.Invalid_response));
                    e.printStackTrace();
                }
            }
        });
    }

    public void error(final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                pDialog.dismiss();
                login.setEnabled(true);
                signup.setEnabled(true);
                Toast.makeText(SignupLoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean validate(int id) {
        boolean valid = true;
        if (id == 0) {
            String username = loginUsername.getText().toString();
            String password = loginPassword.getText().toString();

            if (username.isEmpty()) {
                loginUsername.setError(getString(R.string.Enter_a_username));
                valid = false;
            } else {
                loginUsername.setError(null);
            }

            if (password.isEmpty()) {
                loginPassword.setError(getString(R.string.Enter_a_password));
                valid = false;
            } else {
                loginPassword.setError(null);
            }
            this.username = username;
            this.password = password;
        } else if (id == 1) {
            String username = signupUsername.getText().toString();
            String email = signupEmail.getText().toString();
            String password = signupPassword.getText().toString();

            if (username.isEmpty()) {
                signupUsername.setError(getString(R.string.Enter_a_username));
                valid = false;
            } else {
                signupUsername.setError(null);
            }

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                signupEmail.setError(getString(R.string.Enter_a_valid_email_address));
                valid = false;
            } else {
                signupEmail.setError(null);
            }

            if (password.isEmpty()) {
                signupPassword.setError(getString(R.string.Enter_a_password));
                valid = false;
            } else {
                signupPassword.setError(null);
            }
            this.username = username;
            this.email = email;
            this.password = password;
        }
        return valid;
    }
}
