package com.sabrina.sintonia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sabrina.sintonia.R;
import com.sabrina.sintonia.Redirect;
import com.sabrina.sintonia.Redirect;
import com.sabrina.sintonia.models.Convite;
import com.sabrina.sintonia.models.Usuario;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ConectionActivity extends AppCompatActivity {
    private EditText inputCodigoUsuario;
    private ImageButton imgVoltar;
    private ImageButton btnConfig;
    private Button btnConvidar;
    private Button btnVerConvites;

    private EditText inputCodigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.act_conection);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        btnConfig = findViewById(R.id.btn_config);
        btnConfig.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(ConectionActivity.this, btnConfig);
            popupMenu.getMenuInflater().inflate(R.menu.menu_config, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_perfil) {
                    Toast.makeText(ConectionActivity.this, "Perfil selecionado", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Redirect.verifyAuthentication(ConectionActivity.this, HomeActivity.class);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        inputCodigoUsuario = findViewById(R.id.Input_seu_codigo);
        String uuid = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore.getInstance().collection("users")
                .document(uuid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String codigo = documentSnapshot.getString("codigo");
                        inputCodigoUsuario.setText(codigo); // Atribui o valor ao campo
                    } else {
                        inputCodigoUsuario.setText("Não encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    inputCodigoUsuario.setText("Erro");
                });


        imgVoltar = findViewById(R.id.btn_voltar);
        imgVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConectionActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });


        btnConvidar = findViewById(R.id.btn_convidar);
        btnConvidar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarConvite();
            }


        });

        btnVerConvites = findViewById(R.id.btn_convites_recebidos);
        btnVerConvites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Redirect.changeScreen(ConectionActivity.this, InviteActivity.class);
            }
        });

    }

    private void enviarConvite() {
        inputCodigo = findViewById(R.id.Input_codigo);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String codigoDigitado = inputCodigo.getText().toString();
        String meuUid = FirebaseAuth.getInstance().getUid();

        // Busca o destinatário pelo código digitado
        db.collection("users")
                .whereEqualTo("codigo", codigoDigitado)
                .get()
                .addOnSuccessListener(destinatarioSnapshot -> {
                    if (!destinatarioSnapshot.isEmpty()) {
                        DocumentSnapshot docDest = destinatarioSnapshot.getDocuments().get(0);
                        Usuario usuarioDestinatario = docDest.toObject(Usuario.class);

                        // Agora busca o remetente (usuário logado)
                        db.collection("users")
                                .document(meuUid)
                                .get()
                                .addOnSuccessListener(docRemetente -> {
                                    if (docRemetente.exists()) {
                                        Usuario usuarioRemetente = docRemetente.toObject(Usuario.class);

                                        // Agora sim, cria o objeto convite completo
                                        Convite convite = new Convite(
                                                usuarioRemetente,
                                                usuarioDestinatario,
                                                "pendente",
                                                new Date() // Ou FieldValue.serverTimestamp() se for usar como Map
                                        );

                                        // Salva no Firestore
                                        db.collection("convites")
                                                .add(convite)
                                                .addOnSuccessListener(documentReference -> {
                                                    // Após salvar, também coloca o ID dentro do objeto (se quiser)
                                                    db.collection("convites")
                                                            .document(documentReference.getId())
                                                            .update("id", documentReference.getId());

                                                    Toast.makeText(this, "Convite enviado com sucesso!", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Erro ao enviar convite", Toast.LENGTH_SHORT).show();
                                                    Log.e("Convite", "Erro: " + e.getMessage());
                                                });
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Código não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
//4e884