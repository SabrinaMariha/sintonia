package com.sabrina.sintonia.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.sabrina.sintonia.models.Usuario;

public class RegisterActivity extends AppCompatActivity {
    private EditText mEditEmail;
    private EditText mEditUsername;
    private EditText mEditSenha;
    private Button mBtnCadastrar;
    private ImageButton imgVoltar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.act_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mEditUsername = findViewById(R.id.Input_username);
        mEditEmail = findViewById(R.id.Input_codigo);
        mEditSenha = findViewById(R.id.Input_seu_codigo);
        mBtnCadastrar = findViewById(R.id.btn_cadastrar);

        imgVoltar = findViewById(R.id.btn_voltar_game);
        imgVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Redirect.changeScreen(RegisterActivity.this, MainActivity.class);
            }
        });

        mBtnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();            }
        });
    }



    private void createUser() {
        String email = mEditEmail.getText().toString();
        String senha = mEditSenha.getText().toString();
        String username = mEditUsername.getText().toString();
        if( username == null || username.isEmpty() || email == null || email.isEmpty() || senha ==null || senha.isEmpty()){
            Toast.makeText(this, "Nome, senha e e-mail devem ser preenchidos!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,senha)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.i("Teste", task.getResult().getUser().getUid());
                        }
                        saveUserInFirebase();
                        Redirect.changeScreen(RegisterActivity.this, HomeActivity.class);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Teste",e.getMessage());

                    }
                });
    }

    private void saveUserInFirebase() {
        String uuid = FirebaseAuth.getInstance().getUid();
        String username = mEditUsername.getText().toString();
        Usuario usuario = new Usuario(uuid, username);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uuid)
                .set(usuario)
                .addOnSuccessListener(unused -> {
                    Log.i("Teste", "Usuário salvo com ID = " + uuid);
                })
                .addOnFailureListener(e -> {
                    Log.e("Teste", "Erro ao salvar usuário: " + e.getMessage());
                });
    }
}