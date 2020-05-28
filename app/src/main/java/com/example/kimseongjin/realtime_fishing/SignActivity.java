package com.example.kimseongjin.realtime_fishing;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Created by kimseongjin on 2017. 5. 23..
 */

public class SignActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private FirebaseAuth mFirebaseAuth;
    private Button google_sign, btn_register;
    private EditText edit_email, edit_pwd;
    private int RC_SIGN_IN =20;
    FirebaseAuth.AuthStateListener mailAuthListener;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_activity);
        mFirebaseAuth = FirebaseAuth.getInstance();
        google_sign = (Button)findViewById(R.id.signWithGoogle);
        btn_register = (Button)findViewById(R.id.btn_register);
        edit_email = (EditText)findViewById(R.id.edit_email);
        edit_pwd = (EditText)findViewById(R.id.edit_pwd);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mailAuthListener!=null){
            mFirebaseAuth.removeAuthStateListener(mailAuthListener);
        }
    }

    public void onSignClick(View v){
        switch (v.getId()){
            case R.id.signWithGoogle:
                signIn_withGoggle();
                break;
            case R.id.signWithEmail:
                signIn_withEmail();
                break;
            case R.id.btn_register:
                new RegisterDialog(this).show();
                break;
        }
    }

    private void signIn_withEmail(){
        if(edit_email.getText() == null || (""+edit_email.getText()).equals("") || edit_pwd.getText() == null || (""+edit_pwd.getText()).equals("")){
            Toast.makeText(getApplicationContext(), "ID 와 Password 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }


        mFirebaseAuth.signInWithEmailAndPassword(""+edit_email.getText(),  ""+edit_pwd.getText()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.e("SIGNINActivity", "SignInWithEmail is Complete?:" + task.isSuccessful());
                if(!task.isSuccessful()){
                    Log.e("SignInActivity", "SignWithEmail is failed", task.getException());
                    Toast.makeText(SignActivity.this, "ID 혹은 Password가 일치 하지 않습니다", Toast.LENGTH_SHORT).show();
                }else {
                    startActivity(new Intent(SignActivity.this, MainActivity.class));
                    finish();
                }
            }
        });



        mailAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    Log.e("SIGNINActivity", "LoginSuccess: ");
                }else {
                    Log.e("SIGNINActivity", "LoginFail");

                }
            }
        };
        mFirebaseAuth.addAuthStateListener(mailAuthListener);


    }


    private void signIn_withGoggle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        Intent SignIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(SignIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("Sign Activity", "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("Sign Activity", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("Sign Activity", "signInWithCredential", task.getException());
                            Toast.makeText(SignActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        } else {
                            DB_Manager db_manager = new DB_Manager();
                            db_manager.checkUser(mFirebaseAuth, null);
                            startActivity(new Intent(SignActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign-In failed
                Log.e("Sign Activity", "Google Sign-In failed.");
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void createEmailAccount(final String name, String email, String pwd){
        mFirebaseAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.e("SIGNINActivity", "SignInWithEmail is Complete?:" + task.isSuccessful());
                if(!task.isSuccessful()){
                    Log.e("SignInActivity", "SignWithEmail is failed", task.getException());
                    Toast.makeText(SignActivity.this, "계정 생성에 실패 했습니다.", Toast.LENGTH_SHORT).show();
                }else {
                    DB_Manager db_manager = new DB_Manager();
                    db_manager.checkUser(mFirebaseAuth, name);
                    startActivity(new Intent(SignActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
    }


    class RegisterDialog extends Dialog implements View.OnClickListener{
        Button btn_register_ok, btn_register_cancle;
        EditText edit_register_name, edit_register_email, edit_register_pwd;
        public RegisterDialog(@NonNull Context context) {
            super(context, android.R.style.Theme_Translucent_NoTitleBar);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
            lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lpWindow.dimAmount = 0.8f;
            getWindow().setAttributes(lpWindow);
            setContentView(R.layout.register_dialog);

            btn_register_ok = (Button)findViewById(R.id.btn_register_ok);
            btn_register_ok.setOnClickListener(this);
            btn_register_cancle = (Button)findViewById(R.id.btn_register_cancle);
            btn_register_cancle.setOnClickListener(this);
            edit_register_email = (EditText)findViewById(R.id.edit_register_email);
            edit_register_name = (EditText)findViewById(R.id.edit_register_name);
            edit_register_pwd = (EditText)findViewById(R.id.edit_register_pwd);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_register_ok:
                    if(edit_register_name.getText() == null || edit_register_email.getText() == null || edit_register_pwd.getText() == null){
                        return;
                    }
                    createEmailAccount(edit_register_name.getText().toString(), edit_register_email.getText().toString(), edit_register_pwd.getText().toString());
                    dismiss();
                    break;
                case R.id.btn_register_cancle:
                    dismiss();
                    break;
            }
        }
    }
}
