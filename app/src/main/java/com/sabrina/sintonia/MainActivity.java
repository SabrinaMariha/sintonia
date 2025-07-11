package com.sabrina.sintonia;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private EditText mEditEmail;
    private EditText mEditSenha;
    private Button mBtnEnter;
    private TextView mLblAccount;



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

        mEditEmail = findViewById(R.id.Input_email);
        mEditSenha = findViewById(R.id.Input_senha);
        mBtnEnter = findViewById(R.id.btn_entrar);
        mLblAccount = findViewById(R.id.lbl_account);
        mBtnEnter.setOnClickListener(v -> {
            Log.i("Teste",mEditEmail.getText().toString());
            Log.i("Teste",mEditSenha.getText().toString());
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