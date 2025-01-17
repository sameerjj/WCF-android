package com.android.wcf.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.wcf.R;
import com.android.wcf.application.WCFApplication;
import com.android.wcf.base.BaseFragment;
import com.android.wcf.helper.SharedPreferencesUtil;
import com.android.wcf.model.Constants;
import com.android.wcf.network.WCFClient;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.Date;

public class LoginFragment extends BaseFragment implements LoginMvp.View {

    private static final String TAG = LoginFragment.class.getSimpleName();
    private static final String PUBLIC_PROFILE = "public_profile";

    LoginMvp.Host host;
    LoginMvp.Presenter loginPesenter;
    private CallbackManager callbackManager;
    LoginButton loginButton;
    View termsTv;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LoginMvp.Host) {
            host = (LoginMvp.Host) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement LoginMvp.Host");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_login, container, false);
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupView(view);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        loginPesenter = new LoginPresenter(this);
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        host.hideToolbar();
    }

    @Override
    public void onStop() {
        super.onStop();
        loginPesenter.onStop();
    }

    private void setupView(View view) {
        loginButton = view.findViewById(R.id.login_button);
        termsTv = view.findViewById(R.id.term_conditions);
        termsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                host.showTermsAndConditions();
            }
        });
        TextView appVersionTv = view.findViewById(R.id.app_version);
        appVersionTv.setText(WCFApplication.instance.getAppVersion());

        appVersionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchServerForTestingTeam();
            }
        });

        TextView hashKeyTV = view.findViewById(R.id.hash_key);
        hashKeyTV.setText(WCFApplication.instance.getHashKey());

        loginButton.setFragment(this);
        loginButton.setPermissions(Arrays.asList(PUBLIC_PROFILE));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("LoginPresenter", "onSuccess called");
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {

                                String userName = "", userId = "", userProfileUrl = "";
                                try {
                                    AccessToken token = AccessToken.getCurrentAccessToken();
                                    Log.d("access only Token is", String.valueOf(token.getToken()));

                                    userId = object.getString("id");
                                    userName = object.getString("name");
                                    userProfileUrl = response.getJSONObject().getJSONObject("picture").getJSONObject("data").getString("url");

                                    String savedParticipantId = SharedPreferencesUtil.getMyParticipantId();
                                    if (!userId.equals(savedParticipantId)){
                                        SharedPreferencesUtil.clearMyStepsCommitted();
                                        SharedPreferencesUtil.clearMyTeamId();
                                        SharedPreferencesUtil.clearAkfProfileCreated();
                                        cacheParticipantTeam(null);
                                    }

                                    SharedPreferencesUtil.saveMyLoginId(userId, Constants.AUTH_FACEBOOK);
                                    SharedPreferencesUtil.saveUserLoggedIn(true);
                                    SharedPreferencesUtil.saveUserFullName(userName);
                                    SharedPreferencesUtil.saveUserProfilePhotoUrl(userProfileUrl);
                                    joinFBGroup(userId);

                                    loginPesenter.onLoginSuccess();

                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing Facebook Login response\n" + e);
                                    e.printStackTrace();
                                    loginPesenter.onLoginError(null);
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,picture.type(large)");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel called");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "Facebook Login error: " + error.getMessage());
                if (error.getMessage().contains("CONNECTION_FAILURE")) {
                    showNetworkErrorMessage(R.string.login_title);
                }
                else {
                    loginPesenter.onLoginError(error.getMessage());
                }
            }
        });
    }

    void joinFBGroup( String userId) {

        //THIS IS NO LONGER SUPPORTED BY FB

//        new GraphRequest(
//                AccessToken.getCurrentAccessToken(),
//                "/466564417434674/members/"+ userId,
//                null,
//                HttpMethod.POST,
//                new GraphRequest.Callback() {
//                    public void onCompleted(GraphResponse response) {
//                        if (response.getError() == null) {
//                            Log.d(TAG, "Facebook Group join response" + response.toString());
//                        } else {
//                            Log.e(TAG, "Facebook Group join error: " + response.getError().getException().getMessage());
//                        }
//                    }
//                }
//        ).executeAsync();

    }

    @Override
    public void showMessage(String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void loginComplete() {
        LoginHelper.loginIsValid();
        host.loginComplete();

    }
    int switchRequestClickCount = 0;
    long lastSwitchClickAt = new Date().getTime();
    private void switchServerForTestingTeam(){
        long now = new Date().getTime();
        Log.d(TAG, "Click count=" + switchRequestClickCount + " timeDelta=" + (now - lastSwitchClickAt));
        if (now - lastSwitchClickAt  < 1000) { //upto .1 sec gap allowed between clicks
            switchRequestClickCount++;
            if (switchRequestClickCount >= 2) {
                switchRequestClickCount = 0;
                confirmServerSwitch();
            }
        }
        else {
            switchRequestClickCount = 0;
        }
        lastSwitchClickAt = now;
    }

    private void confirmServerSwitch() {

        final AlertDialog dialogBuilder = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_password_entry, null);

        final TextView connectionMessageTv = dialogView.findViewById(R.id.current_connection_message);
        if (WCFClient.isProdBackend()) {
            connectionMessageTv.setText(getString(R.string.connected_to_prod_server));
        }
        else {
            connectionMessageTv.setText(getString(R.string.connected_to_test_server));
        }

        final EditText editText = dialogView.findViewById(R.id.password);
        Button okBtn = dialogView.findViewById(R.id.ok);
        Button cancelBtn = dialogView.findViewById(R.id.cancel);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
                closeKeyboard();
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBuilder.dismiss();
                closeKeyboard();
                if (editText.getText().toString().equals("akftester1")) {
                    host.switchServerForTestingTeam();
                }
                else {
                    Toast.makeText(getContext(), getString(R.string.invalid_password), Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }

}

