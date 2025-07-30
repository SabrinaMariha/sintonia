package com.sabrina.sintonia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sabrina.sintonia.R;
import com.sabrina.sintonia.Redirect;

import java.util.Map;

public class PerfilActivity extends AppCompatActivity {
    private EditText mEditUsername;

    private Button mBtnSalvar;
    private ImageButton imgBtnVoltar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String  meuUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.act_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mEditUsername = findViewById(R.id.edit_nome_perfil);

        mBtnSalvar = findViewById(R.id.btn_salvar_nome);

        mBtnSalvar.setOnClickListener(v -> {
            mudarNomeDoUsuario();
        });
        imgBtnVoltar = findViewById(R.id.btn_voltar_perfil);
        imgBtnVoltar.setOnClickListener(v -> {
            Redirect.changeScreen(PerfilActivity.this, HomeActivity.class);
        });



    }

    private void mudarNomeDoUsuario() {
        String novoNome = mEditUsername.getText().toString();

        db.collection("users")
                .document(meuUid)
                .update("userName", novoNome)
                .addOnSuccessListener(aVoid -> {
                    atualizarNomeNasConexoes(novoNome);
                    atualizarNomeNosConvites(novoNome);
                    Toast.makeText(this, "Nome atualizado com sucesso", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao atualizar", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
    private void atualizarNomeNasConexoes(String novoNome) {
        db.collection("conexoes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        boolean atualizou = false;
                        var data = doc.getData();

                        var usuarioUm = (Map<String, Object>) data.get("usuarioUm");
                        var usuarioDois = (Map<String, Object>) data.get("usuarioDois");

                        if (usuarioUm != null && meuUid.equals(usuarioUm.get("uuid"))) {
                            usuarioUm.put("userName", novoNome);
                            atualizou = true;
                        }

                        if (usuarioDois != null && meuUid.equals(usuarioDois.get("uuid"))) {
                            usuarioDois.put("userName", novoNome);
                            atualizou = true;
                        }

                        if (atualizou) {
                            db.collection("conexoes").document(doc.getId())
                                    .update("usuarioUm", usuarioUm, "usuarioDois", usuarioDois);
                        }
                    }
                });
    }
    private void atualizarNomeNosConvites(String novoNome) {
        db.collection("convites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        boolean atualizou = false;
                        var data = doc.getData();

                        var remetente = (Map<String, Object>) data.get("remetente");
                        var destinatario = (Map<String, Object>) data.get("destinatario");

                        if (remetente != null && meuUid.equals(remetente.get("uuid"))) {
                            remetente.put("userName", novoNome);
                            atualizou = true;
                        }

                        if (destinatario != null && meuUid.equals(destinatario.get("uuid"))) {
                            destinatario.put("userName", novoNome);
                            atualizou = true;
                        }

                        if (atualizou) {
                            db.collection("convites").document(doc.getId())
                                    .update("remetente", remetente, "destinatario", destinatario);
                        }
                    }
                });
    }


}