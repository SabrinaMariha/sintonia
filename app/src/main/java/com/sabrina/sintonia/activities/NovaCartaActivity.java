package com.sabrina.sintonia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sabrina.sintonia.R;
import com.sabrina.sintonia.Redirect;
import com.sabrina.sintonia.models.Carta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NovaCartaActivity extends AppCompatActivity {
    private EditText mEditDescricaoNova;
    private Button mBtnSalvarCarta;
    private ImageButton mImgTrash;
    private ImageButton mImgVoltar;
    private ImageButton btnConfig;
    private TextView editNomeContato;
    private String nomeContato;
    private String conexaoId;
    private String uidDois;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.act_nova_carta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Recupera os extras recebidos da GameActivity
        Intent intent = getIntent();
        editNomeContato = findViewById(R.id.lbl_nome_contato);
        nomeContato = intent.getStringExtra("NOME_CONTATO");
        conexaoId = intent.getStringExtra("CONEXAO_ID");
        uidDois = intent.getStringExtra("UID_DOIS");

        if (nomeContato != null) {
            editNomeContato.setText(nomeContato);
        }

        mEditDescricaoNova = findViewById(R.id.edit_descricao_nova_carta);

        mBtnSalvarCarta = findViewById(R.id.salvar_nova_carta);
        mImgTrash = findViewById(R.id.trash);
        mImgVoltar = findViewById(R.id.btn_voltar_nvCarta);
        mImgVoltar.setOnClickListener(v -> {
            Intent voltarIntent = new Intent(NovaCartaActivity.this, GameActivity.class);
            voltarIntent.putExtra("NOME_CONTATO", nomeContato);
            voltarIntent.putExtra("CONEXAO_ID", conexaoId);
            voltarIntent.putExtra("UID_DOIS", uidDois);
            startActivity(voltarIntent);
            finish();
        });
        btnConfig = findViewById(R.id.btn_config);
        btnConfig.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(NovaCartaActivity.this, btnConfig);
            popupMenu.getMenuInflater().inflate(R.menu.menu_config, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_perfil) {
                    Redirect.changeScreen(NovaCartaActivity.this, PerfilActivity.class);
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Redirect.verifyAuthentication(NovaCartaActivity.this, HomeActivity.class);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        mBtnSalvarCarta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCarta();
                limparCampos();
            }
        });
        mImgTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditDescricaoNova.setText("");
            }
        });
    }

    private void limparCampos() {
        mEditDescricaoNova.setText("");
    }

    private void createCarta() {
        String descricao = mEditDescricaoNova.getText().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String meuUid = FirebaseAuth.getInstance().getUid();

        if (descricao == null || descricao.isEmpty()) {
            Toast.makeText(this, "A descrição deve ser preenchida!", Toast.LENGTH_SHORT).show();
            return;
        }

        Carta carta = new Carta(meuUid, descricao);

        // 1. Adiciona na coleção 'cartas'
        db.collection("cartas")
                .add(carta)
                .addOnSuccessListener(documentReference -> {
                    String cartaId = documentReference.getId();
                    carta.setId(cartaId);

                    // Atualiza o campo "id" da carta salva
                    documentReference.update("id", cartaId)
                            .addOnSuccessListener(aVoid -> {
                                // 2. Adiciona no 'montante.cartas' da conexão
                                DocumentReference conexaoRef = db.collection("conexoes").document(conexaoId);

                                conexaoRef.get().addOnSuccessListener(docSnapshot -> {
                                    if (docSnapshot.exists()) {
                                        List<Map<String, Object>> cartasMontante =
                                                (List<Map<String, Object>>) docSnapshot.get("montante.cartas");

                                        if (cartasMontante == null) cartasMontante = new ArrayList<>();

                                        Map<String, Object> novaCarta = new HashMap<>();
                                        novaCarta.put("id", cartaId);
                                        novaCarta.put("descricao", descricao);
                                        novaCarta.put("criadorId", meuUid);

                                        cartasMontante.add(novaCarta);

                                        Map<String, Object> montanteUpdate = new HashMap<>();
                                        montanteUpdate.put("montante", Map.of("cartas", cartasMontante));

                                        conexaoRef.update(montanteUpdate)
                                                .addOnSuccessListener(v -> {
                                                    Toast.makeText(this, "Carta salva com sucesso!", Toast.LENGTH_SHORT).show();
                                                    // Volta para GameActivity
                                                    Intent intent = new Intent(NovaCartaActivity.this, GameActivity.class);
                                                    intent.putExtra("CONEXAO_ID", conexaoId);
                                                    intent.putExtra("UID_DOIS", uidDois);
                                                    intent.putExtra("NOME_CONTATO", nomeContato);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("MontanteUpdate", "Erro ao atualizar montante", e);
                                                    Toast.makeText(this, "Carta salva, mas não foi adicionada ao montante", Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                });
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e("CreateCarta", "Erro ao salvar carta", e);
                    Toast.makeText(this, "Erro inesperado ao salvar a carta!", Toast.LENGTH_SHORT).show();
                });
    }

}