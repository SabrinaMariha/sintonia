package com.sabrina.sintonia.activities;

import android.os.Bundle;
import android.view.View;
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
        imgVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Redirect.changeScreen(GameActivity.this, HomeActivity.class);
            }
        });
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
        meuUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        outroUid = getIntent().getStringExtra("UID_DOIS");

        if (nomeContato != null) {
            editNomeContato.setText(nomeContato);
        }

        recyclerView = findViewById(R.id.recyclerViewCartas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseFirestore.getInstance().collection("cartas")
                .whereEqualTo("criador", "padrao")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        Carta carta = doc.toObject(Carta.class);
                        if (carta != null) {
                            carta.setId(doc.getId());
                            listaCartas.add(carta);
                        }
                    }
                    cartaAdapter = new CartaAdapter(listaCartas);
                    recyclerView.setAdapter(cartaAdapter);
                });

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

                                // Aguarda o merge concluir antes de verificar o match
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
}
