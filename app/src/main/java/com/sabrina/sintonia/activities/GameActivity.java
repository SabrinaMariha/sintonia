package com.sabrina.sintonia.activities;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.sabrina.sintonia.CartaAdapter;
import com.sabrina.sintonia.R;
import com.sabrina.sintonia.Redirect;
import com.sabrina.sintonia.models.Carta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartaAdapter cartaAdapter;
    private TextView editNomeContato;
    private ImageButton imgVoltar;
    private ImageButton btnConfig ;
    private String conexaoId;
    private String meuUid;
    private String outroUid;

    private List<Carta> listaCartas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.act_game);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgVoltar = findViewById(R.id.btn_voltar_game);
        imgVoltar.setOnClickListener(v -> Redirect.changeScreen(GameActivity.this, HomeActivity.class));

        btnConfig = findViewById(R.id.btn_config);
        btnConfig.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(GameActivity.this, btnConfig);
            popupMenu.getMenuInflater().inflate(R.menu.menu_config, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_perfil) {
                    Toast.makeText(GameActivity.this, "Perfil selecionado", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Redirect.verifyAuthentication(GameActivity.this, HomeActivity.class);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });

        editNomeContato = findViewById(R.id.lbl_nome_contato);
        String nomeContato = getIntent().getStringExtra("NOME_CONTATO");
        conexaoId = getIntent().getStringExtra("CONEXAO_ID");
        outroUid = getIntent().getStringExtra("UID_DOIS");

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Redirect.verifyAuthentication(GameActivity.this, HomeActivity.class);
            return;
        }

        meuUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (nomeContato != null) {
            editNomeContato.setText(nomeContato);
        }

        recyclerView = findViewById(R.id.recyclerViewCartas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("interacoes")
                .document(conexaoId)
                .collection("cartas")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> cartasJaVistas = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot) {
                        if (doc.contains(meuUid)) {
                            cartasJaVistas.add(doc.getId());
                        }
                    }
                    carregarMontanteFiltrado(cartasJaVistas);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao buscar interações", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });

        // Swipe (like/dislike)
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Carta carta = listaCartas.get(position);
                String cartaId = carta.getid();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference interacaoRef = db
                        .collection("interacoes")
                        .document(conexaoId)
                        .collection("cartas")
                        .document(cartaId);

                boolean gostou = direction == ItemTouchHelper.RIGHT;

                Map<String, Object> update = new HashMap<>();
                update.put(meuUid, gostou);

                interacaoRef.set(update, SetOptions.merge())
                        .addOnSuccessListener(unused -> {
                            if (gostou) {
                                Toast.makeText(getApplicationContext(), "Like!", Toast.LENGTH_SHORT).show();

                                interacaoRef.get().addOnSuccessListener(doc -> {
                                    Map<String, Object> data = doc.getData();
                                    if (data != null) {
                                        Boolean likeMeu = (Boolean) data.get(meuUid);
                                        Boolean likeOutro = (Boolean) data.get(outroUid);

                                        if (Boolean.TRUE.equals(likeMeu) && Boolean.TRUE.equals(likeOutro)) {
                                            db.collection("matches")
                                                    .document(conexaoId)
                                                    .collection("cartas")
                                                    .document(cartaId)
                                                    .set(Map.of(
                                                            "match", true,
                                                            "timestamp", System.currentTimeMillis()
                                                    ));

                                            View fundoEscuro = findViewById(R.id.fundo_escuro);
                                            LinearLayout matchContainer = findViewById(R.id.match_container);
                                            ImageView heart = findViewById(R.id.heart_match);
                                            TextView textMatch = findViewById(R.id.text_match);

                                            fundoEscuro.setVisibility(View.VISIBLE);
                                            matchContainer.setVisibility(View.VISIBLE);

                                            ScaleAnimation scaleHeart = new ScaleAnimation(
                                                    0f, 1.5f, 0f, 1.5f,
                                                    Animation.RELATIVE_TO_SELF, 0.5f,
                                                    Animation.RELATIVE_TO_SELF, 0.5f);
                                            scaleHeart.setDuration(500);
                                            scaleHeart.setFillAfter(true);

                                            AlphaAnimation fadeInText = new AlphaAnimation(0.0f, 1.0f);
                                            fadeInText.setDuration(500);
                                            fadeInText.setStartOffset(300);

                                            heart.startAnimation(scaleHeart);
                                            textMatch.startAnimation(fadeInText);
                                            recyclerView.setVisibility(View.INVISIBLE);

                                            new android.os.Handler().postDelayed(() -> {
                                                heart.clearAnimation();
                                                textMatch.clearAnimation();
                                                matchContainer.setVisibility(View.GONE);
                                                fundoEscuro.setVisibility(View.GONE);
                                                recyclerView.setVisibility(View.VISIBLE);
                                            }, 2000);

                                            Toast.makeText(getApplicationContext(), "✨ MATCH! ✨", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), "Dislike!", Toast.LENGTH_SHORT).show();
                            }
                        });

                cartaAdapter.removeItem(position);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    private void carregarMontanteFiltrado(List<String> cartasJaVistas) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("conexoes")
                .document(conexaoId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        listaCartas.clear();

                        Map<String, Object> montanteMap = (Map<String, Object>) documentSnapshot.get("montante");
                        if (montanteMap != null) {
                            List<Map<String, Object>> cartasMap = (List<Map<String, Object>>) montanteMap.get("cartas");
                            if (cartasMap != null) {
                                for (Map<String, Object> cartaMap : cartasMap) {
                                    String cartaId = (String) cartaMap.get("id");

                                    if (!cartasJaVistas.contains(cartaId)
                                            && cartaMap.get("descricao") != null) {

                                        Carta carta = new Carta();
                                        carta.setId(cartaId);
                                        carta.setDescricao((String) cartaMap.get("descricao"));
                                        carta.setCriadorId((String) cartaMap.get("criadorId"));
                                        listaCartas.add(carta);
                                    }

                                }
                            }
                        }

                        cartaAdapter = new CartaAdapter(listaCartas);
                        recyclerView.setAdapter(cartaAdapter);
                    } else {
                        Toast.makeText(this, "Conexão não encontrada", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar cartas", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
