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

public class LoginActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();
    @Bind(R.id.link_signup)
    AppCompatButton noAccount;
    @Bind(R.id.login)
    AppCompatButton login;
    @Bind(R.id.username)
    EditText usernameET;
    @Bind(R.id.password)
    EditText passwordET;
    String username, password;
    private ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setupLogin();
        pDialog = new ProgressDialog(this);
        pDialog.setIndeterminate(true);
        pDialog.setCanceledOnTouchOutside(false);
    }

    private void setupLogin() {
        noAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noAccount.setEnabled(false);
                login.setEnabled(false);
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.setEnabled(false);
                noAccount.setEnabled(false);
                try {
                    login();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void login() throws Exception {
        pDialog.setMessage(getString(R.string.Authenticating));
        pDialog.show();
        if (!validate()) {
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
                            SaveSharedPreference.setToken(LoginActivity.this, access_token);
                            SaveSharedPreference.setSetup(LoginActivity.this, 1);
                            pDialog.dismiss();
                            login.setEnabled(true);
                            noAccount.setEnabled(true);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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
        String password = passwordET.getText().toString();

        if (username.isEmpty()) {
            usernameET.setError(getString(R.string.Enter_a_username));
            valid = false;
        } else {
            usernameET.setError(null);
        }

        if (password.isEmpty()) {
            passwordET.setError(getString(R.string.Enter_a_password));
            valid = false;
        } else {
            passwordET.setError(null);
        }
        this.username = username;
        this.password = password;
        return valid;
    }

    public void error(final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                pDialog.dismiss();
                login.setEnabled(true);
                noAccount.setEnabled(true);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
