package com.example.justnotes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    TextView haveAccount;
    Button btnRegistrar;
    EditText txtInputEmail, txtInputPassword, txtInputConfirmPassword;
    private ProgressDialog mProgressBar;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        txtInputEmail = findViewById(R.id.editText_email);
        txtInputPassword = findViewById(R.id.editText_Password);
        txtInputConfirmPassword = findViewById(R.id.editText_Confirm_Password);

        btnRegistrar = findViewById(R.id.btn_SignUp);
        haveAccount =findViewById(R.id.textView_already_Have_Account);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCredentials();
            }
        });

        haveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            }
        });
        mAuth = FirebaseAuth.getInstance();
        mProgressBar = new ProgressDialog(SignUpActivity.this);
    }

    public void verifyCredentials(){
        String email = txtInputEmail.getText().toString();
        String password = txtInputPassword.getText().toString();
        String confirmPass = txtInputConfirmPassword.getText().toString();
        if (email.isEmpty() || !email.contains("@")){
            showError(txtInputEmail,"Invalid email");
        }else if (password.isEmpty() || password.length() < 6){
            showError(txtInputPassword,"Invalid key, minimum 6 characters");
        }else if (confirmPass.isEmpty() || !confirmPass.equals(password)){
            showError(txtInputConfirmPassword,"Invalid key, doesn't match.");
        }else{
            //Mostrar ProgressBar
            mProgressBar.setTitle("Sign Up process");
            mProgressBar.setMessage("Signing up a user, wait a minute");
            mProgressBar.setCanceledOnTouchOutside(false);
            mProgressBar.show();
            //Registrar usuario
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        mProgressBar.dismiss();
                        //redireccionar - intent a login
                        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getApplicationContext(), "Could not be registered", Toast.LENGTH_LONG).show();
                        mProgressBar.dismiss();
                    }
                }
            });
        }
    }

    private void showError(EditText input, String s){
        input.setError(s);
        input.requestFocus();
    }
}

