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
import com.sabrina.sintonia.ConviteAdapter;
import com.sabrina.sintonia.R;
import com.sabrina.sintonia.Redirect;
import com.sabrina.sintonia.models.Conexao;
import com.sabrina.sintonia.models.Convite;
import com.sabrina.sintonia.models.Montante;
import com.sabrina.sintonia.models.Carta;
import com.sabrina.sintonia.models.Usuario;

import java.util.ArrayList;
import java.util.List;

public class InviteActivity extends AppCompatActivity implements ConviteAdapter.OnConviteClickListener {
    private RecyclerView recyclerView;
    private ConviteAdapter adapter;
    private final List<Convite> listaConvites = new ArrayList<>();
    private FirebaseFirestore db;
    private String meuUid;
    private ImageButton imgVoltar;
    private ImageButton btnConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.act_invite);

        // Ajuste dos Insets para evitar que conteúdo fique atrás da status bar / navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerViewConvites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConviteAdapter(listaConvites, this);
        recyclerView.setAdapter(adapter);

        meuUid = FirebaseAuth.getInstance().getUid();
        db = FirebaseFirestore.getInstance();

        carregarConvites();

        btnConfig = findViewById(R.id.btn_config);
        btnConfig.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(InviteActivity.this, btnConfig);
            popupMenu.getMenuInflater().inflate(R.menu.menu_config, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_perfil) {
                    Toast.makeText(InviteActivity.this, "Perfil selecionado", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    Redirect.verifyAuthentication(InviteActivity.this, HomeActivity.class);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        imgVoltar = findViewById(R.id.btn_voltar);
        imgVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(InviteActivity.this, ConectionActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onAceitarClick(Convite convite) {
        db.collection("convites")
                .document(convite.getId())
                .update("status", "aceito")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Convite aceito!", Toast.LENGTH_SHORT).show();
                    saveConnectionUsers(convite);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao aceitar convite", Toast.LENGTH_SHORT).show());
        carregarConvites();
    }

    private void saveConnectionUsers(Convite convite) {
        String uidRemetente = convite.getRemetente().getUuid();

        db.collection("users").document(meuUid).get()
                .addOnSuccessListener(documentSnapshotMeu -> {
                    Usuario usuarioUm = documentSnapshotMeu.toObject(Usuario.class);

                    db.collection("users").document(uidRemetente).get()
                            .addOnSuccessListener(documentSnapshotRemetente -> {
                                Usuario usuarioDois = documentSnapshotRemetente.toObject(Usuario.class);

                                Montante.carregarCartasPadrao(new Montante.OnCartasCarregadasListener() {
                                    @Override
                                    public void onCartasCarregadas(List<Carta> cartas) {
                                        Montante montante = new Montante(cartas);
                                        Conexao conexao = new Conexao(usuarioUm, usuarioDois);
                                        conexao.setMontante(montante);
                                        String conexaoId;
                                        conexaoId = gerarChaveConexao(usuarioUm.getUuid(), usuarioDois.getUuid());

                                        db.collection("conexoes")
                                                .document(conexaoId) // <-- define o ID customizado
                                                .set(conexao)        // <-- envia o objeto Conexao
                                                .addOnSuccessListener(unused -> Log.i("InviteActivity", "Conexão salva com cartas padrão com sucesso!"))
                                                .addOnFailureListener(e -> Log.e("InviteActivity", "Erro ao salvar conexão: " + e.getMessage()));
                                        conexao.setId(conexaoId);
                                    }

                                    @Override
                                    public void onFalha(Exception e) {
                                        Log.e("InviteActivity", "Erro ao carregar cartas padrão: " + e.getMessage());
                                        // Opcional: tratar falha, ex: salvar conexão sem cartas ou avisar o usuário
                                    }
                                });
                            })
                            .addOnFailureListener(e -> Log.e("InviteActivity", "Erro ao buscar remetente: " + e.getMessage()));
                })
                .addOnFailureListener(e -> Log.e("InviteActivity", "Erro ao buscar usuário logado: " + e.getMessage()));
    }

    @Override
    public void onRecusarClick(Convite convite) {
        db.collection("convites")
                .document(convite.getId())
                .update("status", "recusado")
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Convite recusado!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao recusar convite", Toast.LENGTH_SHORT).show());
        carregarConvites();
    }
    private String gerarChaveConexao(String uid1, String uid2) {
        return (uid1.compareTo(uid2) < 0) ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }
    private void carregarConvites() {
        listaConvites.clear();

        db.collection("convites")
                .whereEqualTo("destinatario.uuid", meuUid)
                .whereEqualTo("status", "pendente")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        Convite convite = doc.toObject(Convite.class);
                        convite.setId(doc.getId());
                        listaConvites.add(convite);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao carregar convites", Toast.LENGTH_SHORT).show());
    }
}
