package com.sabrina.sintonia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sabrina.sintonia.MatchesAdapter;
import com.sabrina.sintonia.R;
import com.sabrina.sintonia.Redirect;
import com.sabrina.sintonia.models.Carta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MatchesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MatchesAdapter adapter;
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
        setContentView(R.layout.act_matches);

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

        recyclerView = findViewById(R.id.recyclerViewMacthes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mImgVoltar = findViewById(R.id.btn_voltar_macthes);
        mImgVoltar.setOnClickListener(v -> {
            Intent voltarIntent = new Intent(MatchesActivity.this, GameActivity.class);
            voltarIntent.putExtra("NOME_CONTATO", nomeContato);
            voltarIntent.putExtra("CONEXAO_ID", conexaoId);
            voltarIntent.putExtra("UID_DOIS", uidDois);
            startActivity(voltarIntent);
            finish();
        });


        btnConfig = findViewById(R.id.btn_config_macthes);
        btnConfig.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(MatchesActivity.this, btnConfig);
            popupMenu.getMenuInflater().inflate(R.menu.menu_config, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_perfil) {
                    Toast.makeText(MatchesActivity.this, "Perfil selecionado", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Redirect.verifyAuthentication(MatchesActivity.this, HomeActivity.class);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
        listaCartas = new ArrayList<>();
        adapter = new MatchesAdapter(listaCartas);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        carregarCartas();
    }

    private void carregarCartas() {
        listaCartas.clear();

        db.collection("interacoes")
                .document(conexaoId)
                .collection("cartas")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    List<String> cartaIdsMatch = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot) {
                        Boolean likeA = doc.getBoolean(FirebaseAuth.getInstance().getUid());
                        Boolean likeB = doc.getBoolean(uidDois);

                        if (Boolean.TRUE.equals(likeA) && Boolean.TRUE.equals(likeB)) {
                            cartaIdsMatch.add(doc.getId());
                            Log.d("MATCH_DEBUG", "CartaId encontrada: " + doc.getId());

                        }
                    }

                    if (cartaIdsMatch.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    List<Carta> cartasTemp = new ArrayList<>();

                    for (String cartaId : cartaIdsMatch) {
                        db.collection("cartas")
                                .document(cartaId)
                                .get()
                                .addOnSuccessListener(cartaSnap -> {
                                    Carta carta = cartaSnap.toObject(Carta.class);
                                    if (carta != null) {
                                        carta.setId(cartaSnap.getId());
                                        cartasTemp.add(carta);
                                    } else {
                                        // fallback no montante
                                        db.collection("conexoes")
                                                .document(conexaoId)
                                                .get()
                                                .addOnSuccessListener(conexaoSnap -> {
                                                    if (conexaoSnap.exists()) {
                                                        Map<String, Object> montante = (Map<String, Object>) conexaoSnap.get("montante");
                                                        if (montante != null) {
                                                            List<Map<String, Object>> cartasMontante = (List<Map<String, Object>>) montante.get("cartas");
                                                            if (cartasMontante != null) {
                                                                for (Map<String, Object> c : cartasMontante) {
                                                                    if (cartaId.equals(c.get("id"))) {
                                                                        Carta fallback = new Carta();
                                                                        fallback.setId(cartaId);
                                                                        fallback.setDescricao((String) c.get("descricao"));
                                                                        fallback.setCriadorId((String) c.get("criadorId"));
                                                                        cartasTemp.add(fallback);
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    Log.d("MATCH_DEBUG", "Carta carregada: " + carta.getDescricao());


                                                    // verifica se terminou todas
                                                    if (cartasTemp.size() == cartaIdsMatch.size()) {
                                                        listaCartas.clear();
                                                        listaCartas.addAll(cartasTemp);
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                });
                                        return;
                                    }

                                    // verifica se terminou todas
                                    if (cartasTemp.size() == cartaIdsMatch.size()) {
                                        listaCartas.clear();
                                        listaCartas.addAll(cartasTemp);
                                        adapter.notifyDataSetChanged();
                                    }

                                })
                                .addOnFailureListener(e -> Log.e("Firebase", "Erro ao buscar carta", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Erro ao buscar interações", e));
    }


}
