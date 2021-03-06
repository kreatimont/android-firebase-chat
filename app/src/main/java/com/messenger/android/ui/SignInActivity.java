package com.messenger.android.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseError;
import com.messenger.android.R;
import com.messenger.android.database.FBDatabaseManager;
import com.messenger.android.model.User;
import com.messenger.android.utils.SharedData;

import java.util.ArrayList;
import java.util.List;

import static com.messenger.android.ui.PostMessageActivity.REQUIRED;

public class SignInActivity extends AppCompatActivity {

    public static final String TAG = "SignInActivity";

    private FirebaseAuth mAuth;

    private EditText mEmailField, mPassField;
    private Button mSignInButton, mSignUpButton;
    private ProgressBar mProgressBar;
    private LinearLayout mSignInLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initUI();

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {
            showProgressBar(true);
////            AuthCredential credential = EmailAuthProvider.getCredential("lenovo@phone.com","12345678");
////            FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
////                @Override
////                public void onComplete(@NonNull Task<Void> task) {
////                    FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
////                        @Override
////                        public void onComplete(@NonNull Task<Void> task) {
////                            if(task.isSuccessful()) {
////                                Log.e(TAG, "MEIZU deleted");
////                            }
////                        }
////                    });
////                }
//            });
            final List<User> userList = new ArrayList<User>();
            FBDatabaseManager.getInstance().subscribeForUsers(userList, new FBDatabaseManager.FBDatabasePostListener() {
                @Override
                public void postSuccessful() {
                    showProgressBar(false);
                    for(User user : userList) {
                        if(user.getFirebaseId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            SharedData.currentName = user.getName();
                            startListActivityWithUser(mAuth.getCurrentUser());
                            return;
                        }
                    }
                    Log.w(TAG, "loginInWithEmail: failure");
                    Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void postWithError(DatabaseError error) {
                    showProgressBar(false);
                    Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void initUI() {
        mEmailField = (EditText) findViewById(R.id.loginEmail);
        mPassField = (EditText) findViewById(R.id.loginPassword);
        mSignInButton = (Button) findViewById(R.id.buttonLogin);
        mSignUpButton = (Button) findViewById(R.id.buttonSign);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mSignInLayout = (LinearLayout) findViewById(R.id.loginForm);

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(mEmailField.getText().toString())) {
                    mEmailField.setError(REQUIRED);
                    return;
                }
                if (TextUtils.isEmpty(mPassField.getText().toString())) {
                    mPassField.setError(REQUIRED);
                    return;
                }

                showProgressBar(true);
                signIn(mEmailField.getText().toString(), mPassField.getText().toString());

                View view = getCurrentFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromInputMethod(view != null ? view.getWindowToken() : null, 0);
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    Log.d(TAG, "loginUserWithEmail:success");
                    final List<User> userList = new ArrayList<User>();
                    FBDatabaseManager.getInstance().subscribeForUsers(userList, new FBDatabaseManager.FBDatabasePostListener() {
                        @Override
                        public void postSuccessful() {
                            showProgressBar(false);
                            for(User user : userList) {
                                if(user.getFirebaseId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    SharedData.currentName = user.getName();
                                    startListActivityWithUser(mAuth.getCurrentUser());
                                    return;
                                }
                            }
                            Log.w(TAG, "loginInWithEmail: failure");
                            Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void postWithError(DatabaseError error) {
                            showProgressBar(false);
                            Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    showProgressBar(false);
                    Log.w(TAG, "loginInWithEmail: failure");
                    Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void startListActivityWithUser(FirebaseUser user) {
        startActivity(new Intent(this, ListChannelsActivity.class));
        finish();
    }

    private void showProgressBar(boolean state) {

        if (state) {
            mProgressBar.setVisibility(View.VISIBLE);
            mSignInLayout.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mSignInLayout.setVisibility(View.VISIBLE);
        }

    }

}
