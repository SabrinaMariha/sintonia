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
import com.sabrina.sintonia.ConexaoAdapter;
import com.sabrina.sintonia.R;
import com.sabrina.sintonia.Redirect;
import com.sabrina.sintonia.models.Conexao;
import com.sabrina.sintonia.models.Usuario;
import com.sabrina.sintonia.Redirect;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

private RecyclerView recyclerView;
private ConexaoAdapter adapter;
private List<Usuario> listaUsuarios = new ArrayList<>();
private FirebaseFirestore db;
private String meuUid;
private int carregamentosPendentes = 2;

private ImageButton btnConfig ;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Redirect.verifyAuthentication(this, MainActivity.class);


    EdgeToEdge.enable(this);
    setContentView(R.layout.act_home);

    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        return insets;
    });


    btnConfig = findViewById(R.id.btn_config);
    btnConfig.setOnClickListener(view -> {
        PopupMenu popupMenu = new PopupMenu(HomeActivity.this, btnConfig);
        popupMenu.getMenuInflater().inflate(R.menu.menu_config, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_perfil) {
                Redirect.changeScreen(HomeActivity.this, PerfilActivity.class);
                return true;
            } else if (id == R.id.menu_logout) {
                FirebaseAuth.getInstance().signOut();
                Redirect.verifyAuthentication(HomeActivity.this, HomeActivity.class);
                return true;
            }
            return false;
        });

        popupMenu.show();
    });



    recyclerView = findViewById(R.id.recyclerViewConexoes);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    adapter = new ConexaoAdapter(listaUsuarios, usuario -> {
        if ("ADD".equals(usuario.getUuid())) {
            abrirDialogAdicionarAmigo();
        } else {
            abrirTelaJogo(usuario);
        }
    });

    recyclerView.setAdapter(adapter);

    meuUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    db = FirebaseFirestore.getInstance();

    carregarConexoes();
}




    private void carregarConexoes() {
        listaUsuarios.clear();
        carregamentosPendentes = 2;

        db.collection("conexoes")
                .whereEqualTo("usuarioUm.uuid", meuUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        Conexao conexao = doc.toObject(Conexao.class);
                        if (conexao != null) {
                            Usuario outro = conexao.getUsuarioDois();
                            if (outro != null) listaUsuarios.add(outro);
                        }
                    }
                    finalizarCarregamento();
                });

        db.collection("conexoes")
                .whereEqualTo("usuarioDois.uuid", meuUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot) {
                        Conexao conexao = doc.toObject(Conexao.class);
                        if (conexao != null) {
                            Usuario outro = conexao.getUsuarioUm();
                            if (outro != null) listaUsuarios.add(outro);
                        }
                    }
                    finalizarCarregamento();
                });
    }


    private void buscarUsuario(String uid) {
    db.collection("usuarios").document(uid).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    String nome = doc.getString("userName");
                    Usuario usuario = new Usuario(uid, nome);
                    listaUsuarios.add(usuario);
                    adapter.notifyDataSetChanged();
                }
            });
}

private void finalizarCarregamento() {
    carregamentosPendentes--;
    if (carregamentosPendentes == 0) {
        Usuario addItem = new Usuario("ADD", "ADD UM AMIGO");
        listaUsuarios.add(addItem);
        adapter.notifyDataSetChanged();
    }
}

private void abrirDialogAdicionarAmigo() {
    Intent intent = new Intent(this, ConectionActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
}

    private void abrirTelaJogo(Usuario usuario) {
        // Gera o conexaoId de forma ordenada para evitar duplicatas
        String outroUid = usuario.getUuid();
        String conexaoId;
        conexaoId = gerarChaveConexao(meuUid, outroUid);


        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("NOME_CONTATO", usuario.getUserName());
        intent.putExtra("CONEXAO_ID", conexaoId);
        intent.putExtra("UID_UM", meuUid);
        intent.putExtra("UID_DOIS", outroUid);
        startActivity(intent);
    }
    private String gerarChaveConexao(String uid1, String uid2) {
        return (uid1.compareTo(uid2) < 0) ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }


}