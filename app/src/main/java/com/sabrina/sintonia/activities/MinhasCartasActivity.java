package com.sabrina.sintonia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sabrina.sintonia.MatchesAdapter;
import com.sabrina.sintonia.MinhasCartasAdapter;
import com.sabrina.sintonia.R;
import com.sabrina.sintonia.Redirect;
import com.sabrina.sintonia.models.Carta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MinhasCartasActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MinhasCartasAdapter adapter;
    private List<Carta> listaCartas;
    private FirebaseFirestore db;
    private ImageButton mImgVoltar;
    private ImageButton btnConfig;

    // Adicione essas variáveis
    private String nomeContato;
    private String conexaoId;
    private String uidDois;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.act_minhas_cartas);
        // Recupera os extras recebidos da GameActivity
        Intent intent = getIntent();
        nomeContato = intent.getStringExtra("NOME_CONTATO");
        conexaoId = intent.getStringExtra("CONEXAO_ID");
        uidDois = intent.getStringExtra("UID_DOIS");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerViewMinhasCartas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mImgVoltar = findViewById(R.id.btn_voltar_minhas_cartas);
        mImgVoltar.setOnClickListener(v -> {
            Intent voltarIntent = new Intent(MinhasCartasActivity.this, GameActivity.class);
            voltarIntent.putExtra("NOME_CONTATO", nomeContato);
            voltarIntent.putExtra("CONEXAO_ID", conexaoId);
            voltarIntent.putExtra("UID_DOIS", uidDois);
            startActivity(voltarIntent);
            finish();
        });


        btnConfig = findViewById(R.id.btn_config_minhas_cartas);
        btnConfig.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(MinhasCartasActivity.this, btnConfig);
            popupMenu.getMenuInflater().inflate(R.menu.menu_config, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_perfil) {
                    Redirect.changeScreen(MinhasCartasActivity.this, PerfilActivity.class);
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Redirect.verifyAuthentication(MinhasCartasActivity.this, HomeActivity.class);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
        listaCartas = new ArrayList<>();
        adapter = new MinhasCartasAdapter(listaCartas,conexaoId);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        carregarCartas();
    }



    private void carregarCartas() {
        listaCartas.clear();
        String meuUid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // pega UID atual
        DocumentReference conexaoRef = db.collection("conexoes").document(conexaoId);

        conexaoRef.get().addOnSuccessListener(docSnapshot -> {
            if (docSnapshot.exists()) {
                List<Map<String, Object>> cartasMontante =
                        (List<Map<String, Object>>) docSnapshot.get("montante.cartas");

                if (cartasMontante != null) {
                    for (Map<String, Object> cartaMap : cartasMontante) {
                        String criadorId = (String) cartaMap.get("criadorId");
                        if (meuUid.equals(criadorId)) {
                            Carta carta = new Carta();
                            carta.setId((String) cartaMap.get("id"));
                            carta.setDescricao((String) cartaMap.get("descricao"));
                            carta.setCriadorId(criadorId);
                            listaCartas.add(carta);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Erro ao carregar suas cartas", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        });
    }
}