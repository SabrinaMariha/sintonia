package com.sabrina.sintonia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sabrina.sintonia.R;
import com.sabrina.sintonia.Redirect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private EditText mEditEmail;
    private EditText mEditSenha;
    private Button mBtnEnter;
    private TextView mLblAccount;

    private TextView mLblForgotPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mEditEmail = findViewById(R.id.email);
        mEditSenha = findViewById(R.id.senha);
        mBtnEnter = findViewById(R.id.btn_entrar);
        mLblAccount = findViewById(R.id.lbl_account);
        mLblForgotPassword = findViewById(R.id.lbl_forgot_password);

        mLblForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEditEmail.getText().toString();

                if (email.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, insira seu e-mail para recuperar a senha.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "E-mail de recuperação enviado.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Erro ao enviar e-mail de recuperação.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

                mBtnEnter.setOnClickListener(v -> {
                    String email = mEditEmail.getText().toString();
                    String senha = mEditSenha.getText().toString();

                    if (email == null || email.isEmpty() || senha == null || senha.isEmpty()) {
                        Toast.makeText(this, "Senha e e-mail devem ser preenchidos!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.i("Teste", task.getResult().getUser().getUid());
                                        Redirect.changeScreen(MainActivity.this, HomeActivity.class);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("Teste", e.getMessage());
                                }
                            });

                });
        mLblAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

}