package escapadetechnologies.com.firebaseauthsexample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    LoginButton login_button;
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;

    Button sign_out;

    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private TextView mEmail;
    private TextView mPhoneNumber;
    private TextView mPhoto;
    private TextView mMetaData;
    ImageView image;
    String facebookUserId = "";

    private static final String TAG = "FacebookLogin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();


        login_button = findViewById(R.id.login_button);
        sign_out = findViewById(R.id.sign_out);
        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);
        mEmail = findViewById(R.id.email);
        mPhoneNumber = findViewById(R.id.phoneNumber);
        mPhoto = findViewById(R.id.photo);
        mMetaData = findViewById(R.id.metaData);
        image = findViewById(R.id.image);

        sign_out.setOnClickListener(this);

        mCallbackManager = CallbackManager.Factory.create();


       /* login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/

        login_button.setReadPermissions("email", "public_profile");
        login_button.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

                Log.d(TAG, "facebook:onCancel");
                // [START_EXCLUDE]
                updateUI(null);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // [START_EXCLUDE]
                updateUI(null);
            }
        });

        // Add code to print out the key hash (Important ---> without these we can't access fb)
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(
                    "escapadetechnologies.com.firebaseauthsexample",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));

                /*T135MRHb+YmICMWuhOh03aT2B6g=*/
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("error",e.getMessage());

        } catch (NoSuchAlgorithmException e) {
            Log.e("error",e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        updateUI(currentUser);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_out:

                signOut();
        }
    }

    private void updateUI(FirebaseUser user) {
        //hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(user.getDisplayName());
            mDetailTextView.setText(user.getUid());
            mEmail.setText(user.getEmail());
            mPhoneNumber.setText(user.getPhoneNumber());
            //blurred image with this commented code below
            /*Uri uri = user.getPhotoUrl();
            Picasso.get().load(uri).into(image);*/

            // find the Facebook profile and get the user's id
            for(UserInfo profile : user.getProviderData()) {
                // check if the provider id matches "facebook.com"
                if(FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                    facebookUserId = profile.getUid();
                }
            }

            // construct the URL to the profile picture, with a custom height
            // alternatively, use '?type=small|medium|large' instead of ?height=
            String photoUrl = "https://graph.facebook.com/" + facebookUserId + "/picture?height=500";
            //mPhoto.setText((CharSequence) user.getPhotoUrl());
           // mMetaData.setText((CharSequence) user.getMetadata());

            // (optional) use Picasso to download and show to image
            Picasso.get().load(photoUrl).into(image);

            login_button.setVisibility(View.GONE);
            sign_out.setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText("sign_out");
            mDetailTextView.setText(null);

            login_button.setVisibility(View.VISIBLE);
            sign_out.setVisibility(View.GONE);
        }
    }

    private void signOut() {

        mAuth.signOut();
        LoginManager.getInstance().logOut();

        updateUI(null);
    }
}
