package at.ac.tuwien.caa.docscan.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.volley.VolleyError;
import com.dropbox.core.android.Auth;

import at.ac.tuwien.caa.docscan.R;
import at.ac.tuwien.caa.docscan.rest.LoginRequest;
import at.ac.tuwien.caa.docscan.rest.RestRequest;
import at.ac.tuwien.caa.docscan.rest.User;
import at.ac.tuwien.caa.docscan.rest.UserHandler;
import at.ac.tuwien.caa.docscan.sync.DropboxUtils;

/**
 * Created by fabian on 23.08.2017.
 */

public class DropboxActivity extends BaseNoNavigationActivity implements LoginRequest.LoginCallback{

    private boolean mIsActitivyJustCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox);

        super.initToolbarTitle(R.string.dropbox_title);

        mIsActitivyJustCreated = true;

        Button authenticateButton = (Button) findViewById(R.id.dropbox_authenticate_button);
        authenticateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DropboxUtils.getInstance().startAuthentication(getApplicationContext());
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // The dropbox login is done in the onResume method, because if the user authenticates the
        // dropbox access via a web browser intent, this is the entry to the app after accepting in the
        // browser. However onResume is also called if the activity is created by an internal intent.
        if (!mIsActitivyJustCreated)
            loginToDropbox();

        mIsActitivyJustCreated = false;

    }

    @Override
    public void onLogin(User user) {

        // Start the CameraActivity and remove everything from the back stack:
        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    @Override
    public void onLoginError() {
        showLoadingLayout(false);
    }

    private void loginToDropbox() {

        String accessToken = Auth.getOAuth2Token();
//            Save the access token:
        if (accessToken != null) {
            showLoadingLayout(true);
            User.getInstance().setDropboxToken(accessToken);
            UserHandler.saveDropboxToken(this);
            DropboxUtils.getInstance().loginToDropbox(this, accessToken);
        }

//        showLoadingLayout(false);

    }

    private void showLoadingLayout(boolean showLoading) {

        View authenticationLayout = findViewById(R.id.dropbox_authentication_layout);
        View loadingLayout = findViewById(R.id.dropbox_loading_layout);


        if (showLoading) {
            authenticationLayout.setVisibility(View.INVISIBLE);
            loadingLayout.setVisibility(View.VISIBLE);
        }
        else {
            authenticationLayout.setVisibility(View.VISIBLE);
            loadingLayout.setVisibility(View.INVISIBLE);
        }

    }


    @Override
    public void handleRestError(RestRequest request, VolleyError error) {

    }
}