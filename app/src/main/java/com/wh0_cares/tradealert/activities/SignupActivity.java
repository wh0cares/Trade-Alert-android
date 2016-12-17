package com.wh0_cares.tradealert.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.wh0_cares.tradealert.R;
import com.wh0_cares.tradealert.utils.SaveSharedPreference;

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

public class SignupActivity extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    @Bind(R.id.link_login)
    AppCompatButton hasAccount;
    @Bind(R.id.signup)
    AppCompatButton signup;
    @Bind(R.id.username)
    EditText usernameET;
    @Bind(R.id.password)
    EditText passwordET;
    @Bind(R.id.email)
    EditText emailET;
    String username, email, password;
    private ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setupSignup();
        pDialog = new ProgressDialog(this);
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
    }

    private void setupSignup() {
        hasAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hasAccount.setEnabled(false);
                signup.setEnabled(false);
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup.setEnabled(false);
                hasAccount.setEnabled(false);
                try {
                    signup();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void signup() throws Exception {
        pDialog.setMessage(getString(R.string.Creating_account));
        pDialog.show();
        if (!validate()) {
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
                            response.body().close();
                            error(message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    throw new IOException("Unexpected code " + response.body());
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        pDialog.dismiss();
                        try {
                            login();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void login() throws Exception {
        pDialog.setMessage(getString(R.string.Authenticating));
        pDialog.show();

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
            public void onResponse(Call call, final Response response) throws IOException {
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
                            response.body().close();
                            SaveSharedPreference.setToken(SignupActivity.this, access_token);
                            SaveSharedPreference.setSetup(SignupActivity.this, 1);
                            pDialog.dismiss();
                            signup.setEnabled(true);
                            hasAccount.setEnabled(true);
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
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

    public boolean validate() {
        boolean valid = true;
        String username = usernameET.getText().toString();
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();

        if (username.isEmpty()) {
            usernameET.setError(getString(R.string.Enter_a_username));
            valid = false;
        } else {
            usernameET.setError(null);
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailET.setError(getString(R.string.Enter_a_valid_email_address));
            valid = false;
        } else {
            emailET.setError(null);
        }
        if (password.isEmpty()) {
            passwordET.setError(getString(R.string.Enter_a_password));
            valid = false;
        } else {
            passwordET.setError(null);
        }

        this.username = username;
        this.email = email;
        this.password = password;
        return valid;
    }

    public void error(final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                pDialog.dismiss();
                signup.setEnabled(true);
                hasAccount.setEnabled(true);
                Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
