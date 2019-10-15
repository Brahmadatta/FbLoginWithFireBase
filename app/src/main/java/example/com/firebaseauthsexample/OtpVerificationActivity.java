package example.com.firebaseauthsexample;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OtpVerificationActivity extends AppCompatActivity implements View.OnClickListener {

    EditText mobileNumber,otpText;
    Button sendOtp,verifyOtp;
    FirebaseAuth mAuth;
    String codeSent;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    //String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        mobileNumber = findViewById(R.id.mobileNumber);
        otpText = findViewById(R.id.otpText);
        sendOtp = findViewById(R.id.sendOtp);
        verifyOtp = findViewById(R.id.verifyOtp);

        sendOtp.setOnClickListener(this);
        verifyOtp.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
            }
        };


    }

    @Override
    public void onResume() {
        super.onResume();
        //receiveOtp(phoneNumber,null);
        /*if (phoneAuthDialog != null && phoneAuthDialog.isShowing()) {
            phoneAuthDialog.resumeProcess();
        }*/
    }

    public void resumeProcess(){
        /*if(isReceivingOtpSms){
            receiveOtp(phoneNumber,null);
        }

        if(isVerifyingOtp){
            verifyOtp();
        }*/

    }

    private void receiveOtp(String phoneNumber,PhoneAuthProvider.ForceResendingToken forceResendingToken) {


            setPhoneVerificationCallback();
            //isReceivingOtpSms =true;
            //showProgress();

            //for receiving otp for the first time
            if(forceResendingToken==null){
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        this,               // Activity (for callback binding)
                        mCallbacks);        // OnVerificationStateChangedCallbacks
            }

            //for resending otp
            else {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,        // Phone number to verify
                        60,                 // Timeout duration
                        TimeUnit.SECONDS,   // Unit of timeout
                        this,               // Activity (for callback binding)
                        mCallbacks,          // OnVerificationStateChangedCallbacks
                        forceResendingToken);
            }

        }
        //showToast(activity, Constants.MESSAGE_NO_CONNECTION);



    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.sendOtp:

                sendVerificationCode();
                break;

            case R.id.verifyOtp:

                verifyCodeSent();
                break;
        }
    }

    private void verifyCodeSent() {

        String code = otpText.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent,code);
        verifyCredentials(credential);
    }


    private void setPhoneVerificationCallback() {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks  = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                //hideProgress();               //to hide progressbar.
                //isReceivingOtpSms=false;
                //some ui process....
                verifyCredentials(phoneAuthCredential);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                e.printStackTrace();
                //hideProgress();
                //isReceivingOtpSms=false;

                if (e instanceof FirebaseNetworkException) {
                    //showToast(activity, activity.getString(R.string.err_noconnection_message));
                } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    e.printStackTrace();
                    //showToast(activity, "Incorrect phone number format. Check your mobile number and country code twice.");
                } else {
                    //showToast(activity, e.getMessage());
                }
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                //hideProgress();
                //isReceivingOtpSms=false;
                codeSent = verificationId;

                //some ui process ...

                //showToast(activity, "code sent to your number");
            }
        };
    }

    private void verifyCredentials(PhoneAuthCredential credential) {
        //isVerifyingOtp=true;
        //showProgress();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("verifyCode", "signInWithCredential:success");

                            Toast.makeText(OtpVerificationActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                            //FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("verifyCode", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(OtpVerificationActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void sendVerificationCode() {

        String phoneNumber = mobileNumber.getText().toString();

        if (phoneNumber.isEmpty()){
            mobileNumber.setError("mobile number cannot be empty");
            mobileNumber.requestFocus();
        }

        if (phoneNumber.length() < 10){
            mobileNumber.setError("Please enter a valid phone");
            mobileNumber.requestFocus();
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);
    }
}


/*public class OtpVerificationActivity extends AppCompatActivity implements View.OnClickListener {

    EditText mobileNumber,otpText;
    Button sendOtp,verifyOtp;
    FirebaseAuth mAuth;
    String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        mobileNumber = findViewById(R.id.mobileNumber);
        otpText = findViewById(R.id.otpText);
        sendOtp = findViewById(R.id.sendOtp);
        verifyOtp = findViewById(R.id.verifyOtp);

        sendOtp.setOnClickListener(this);
        verifyOtp.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.sendOtp:

                sendVerificationCode();
                break;

            case R.id.verifyOtp:

                verifyCodeSent();
                break;
        }

    }

    private void verifyCodeSent() {

        String code = otpText.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent,code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("verifyCode", "signInWithCredential:success");

                            Toast.makeText(OtpVerificationActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                            //FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("verifyCode", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(OtpVerificationActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void sendVerificationCode() {

        String phoneNumber = mobileNumber.getText().toString();

        if (phoneNumber.isEmpty()){
            mobileNumber.setError("mobile number cannot be empty");
            mobileNumber.requestFocus();
        }

        if (phoneNumber.length() < 10){
            mobileNumber.setError("Please enter a valid phone");
            mobileNumber.requestFocus();
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            codeSent = s;
        }
    };
}*/
