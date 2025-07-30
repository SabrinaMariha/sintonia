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
                    Redirect.changeScreen(ConectionActivity.this, PerfilActivity.class);
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
        String codigoDigitado = inputCodigo.getText().toString().trim();
        String meuUid = FirebaseAuth.getInstance().getUid();

        if (codigoDigitado.isEmpty()) {
            Toast.makeText(this, "Digite um código", Toast.LENGTH_SHORT).show();
            return;
        }

        if (codigoDigitado.length() < 4) {
            Toast.makeText(this, "Código inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Buscar usuário pelo código
        db.collection("users")
                .whereEqualTo("codigo", codigoDigitado)
                .get()
                .addOnSuccessListener(destinatarioSnapshot -> {
                    if (destinatarioSnapshot.isEmpty()) {
                        Toast.makeText(this, "Código não encontrado.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot docDest = destinatarioSnapshot.getDocuments().get(0);
                    Usuario usuarioDestinatario = docDest.toObject(Usuario.class);
                    String uidDestinatario = usuarioDestinatario.getUuid();

                    if (meuUid.equals(uidDestinatario)) {
                        Toast.makeText(this, "Você não pode convidar a si mesmo", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String conexaoId = gerarChaveConexao(meuUid, uidDestinatario); // mesma lógica de chave

                    // 2. Verificar se já existe uma conexão entre os dois
                    db.collection("conexoes")
                            .document(conexaoId)
                            .get()
                            .addOnSuccessListener(conexaoDoc -> {
                                if (conexaoDoc.exists()) {
                                    Toast.makeText(this, "Vocês já estão conectados!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // 3. Verificar se já existe um convite pendente com mesmo chaveConexao
                                db.collection("convites")
                                        .document(conexaoId)
                                        .get()
                                        .addOnSuccessListener(conviteDoc -> {
                                            if (conviteDoc.exists()) {
                                                String status = conviteDoc.getString("status");
                                                if ("pendente".equals(status)) {
                                                    Toast.makeText(this, "Já existe um convite pendente entre vocês", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                            }

                                            // 4. Criar e enviar convite
                                            db.collection("users").document(meuUid)
                                                    .get()
                                                    .addOnSuccessListener(docRemetente -> {
                                                        Usuario usuarioRemetente = docRemetente.toObject(Usuario.class);

                                                        Convite convite = new Convite(
                                                                usuarioRemetente,
                                                                usuarioDestinatario,
                                                                "pendente",
                                                                new Date()
                                                        );
                                                        convite.setId(conexaoId); // define id no objeto

                                                        db.collection("convites")
                                                                .document(conexaoId)
                                                                .set(convite)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Toast.makeText(this, "Convite enviado com sucesso!", Toast.LENGTH_SHORT).show();
                                                                    limparCampos();
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Toast.makeText(this, "Erro ao enviar convite", Toast.LENGTH_SHORT).show();
                                                                    Log.e("Convite", "Erro: " + e.getMessage());
                                                                });
                                                    });

                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Erro ao verificar convites", Toast.LENGTH_SHORT).show();
                                            Log.e("Convite", "Erro: " + e.getMessage());
                                        });

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erro ao verificar conexões", Toast.LENGTH_SHORT).show();
                                Log.e("Conexoes", "Erro: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao buscar usuário", Toast.LENGTH_SHORT).show();
                    Log.e("Usuário", "Erro: " + e.getMessage());
                });
    }


    private String gerarChaveConexao(String uid1, String uid2) {
        return (uid1.compareTo(uid2) < 0) ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }



    private void limparCampos() {
        inputCodigo.setText("");
    }



}
//4e884