package com.sabrina.sintonia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sabrina.sintonia.R;
import com.sabrina.sintonia.Redirect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private EditText mEditEmail;
    private EditText mEditSenha;
    private Button mBtnEnter;
    private TextView mLblAccount;

    private TextView mLblForgotPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mEditEmail = findViewById(R.id.email);
        mEditSenha = findViewById(R.id.senha);
        mBtnEnter = findViewById(R.id.btn_entrar);
        mLblAccount = findViewById(R.id.lbl_account);
        mLblForgotPassword = findViewById(R.id.lbl_forgot_password);

        mLblForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEditEmail.getText().toString();

                if (email.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, insira seu e-mail para recuperar a senha.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "E-mail de recuperação enviado.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Erro ao enviar e-mail de recuperação.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

                mBtnEnter.setOnClickListener(v -> {
                    String email = mEditEmail.getText().toString();
                    String senha = mEditSenha.getText().toString();

                    if (email == null || email.isEmpty() || senha == null || senha.isEmpty()) {
                        Toast.makeText(this, "Senha e e-mail devem ser preenchidos!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.i("Teste", task.getResult().getUser().getUid());
                                        Redirect.changeScreen(MainActivity.this, HomeActivity.class);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("Teste", e.getMessage());
                                }
                            });

                });
        mLblAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void adicionarCartas() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        List<String> descricoes = Arrays.asList(
//                "Roleplay: Devil & Angel",
//                "Drive-in: vamos entrar no carro e estacionar em algum lugar público, como um estacionamento ou a entrada de uma praia",
//                "Diz um número de 1 a 10. Vai ser o nível da ousadia hoje.",
//                "Você me descreve em 3 palavras. Depois eu faço o mesmo.",
//                "Vamos dormir de conchinha e agradecer pela gente?",
//                "Vamos dormir sem roupas intimas por uma noite",
//                "Vamos brincar com gelo",
//                "Vamos fazer no balcão da cozinha",
//                "Desafio: quem perder no jogo, dá um beijo onde o outro quiser.",
//                "Você manda: lugar, posição e intensidade.",
//                "Me ensina algo que você ama e eu nunca tentei?",
//                "Diz uma parte do meu corpo que você ama explorar.",
//                "Strip-tease com trilha sonora escolhida por você.",
//                "Vamos escrever uma carta um pro outro e trocar amanhã.",
//                "Sexo em um cômodo que nunca usamos.",
//                "Topa me ensinar algo que você ama fazer?",
//                "Sem julgamentos: cada um faz um pedido ousado hoje!",
//                "Você se sentiu visto(a) por mim essa semana?",
//                "Tem algo que eu faço que te incomoda e nunca falou?",
//                "Vamos criar uma tradição só nossa?",
//                "Troca de casais",
//                "Roleplay:  Vikings",
//                "Promessa de hoje: escutar sem interromper.",
//                "Finge que acabamos de nos conhecer. Como me seduziria?",
//                "Massagem com óleo e toques lentos. Sem pressa.",
//                "1 verdade + 1 mentira sobre nós. Adivinha qual é qual!",
//                "Sussurrar fantasias no ouvido um do outro.",
//                "Que tal criarmos uma tradição só nossa?",
//                "Faça um elogio safado, mas que me faça rir.",
//                "Hoje, nada de telas: só a gente, conversa e vinho.",
//                "Você está no comando. Me entrega o que quiser."
//        );
        List<String> descricoes = Arrays.asList(
                "Sexo com um grupo",
                "Vamos escrever uma lista de sonhos juntos?",
                "Eu adoro quando você está no comando. Me faz implorar pelo meu próximo orgasmo",
                "Roleplay: Devil & Angel",
                "Vamos revisar nossos combinados e renovar promessas?",
                "Vamos assistir a um vídeo de educação sexual em pompoarismo",
                "Sente na minha cara e me conte sobre o seu dia",
                "Drive-in: vamos entrar no carro e estacionar em algum lugar público, como um estacionamento ou a entrada de uma praia",
                "Fisting",
                "Me conta um medo seu que eu posso ajudar a acalmar.",
                "Roda da sorte: cara, carinho. Coroa, safadeza.",
                "Diz um número de 1 a 10. Vai ser o nível da ousadia hoje.",
                "Você me descreve em 3 palavras. Depois eu faço o mesmo.",
                "Explorar zonas erógenas que nunca exploramos.",
                "Vamos dormir de conchinha e agradecer pela gente?",
                "Sexo a três:  +1 mulher",
                "Vamos dormir sem roupas intimas por uma noite",
                "Vamos brincar com gelo",
                "Vamos fazer no balcão da cozinha",
                "Desafio: quem perder no jogo, dá um beijo onde o outro quiser.",
                "Massagem na próstata",
                "Você manda: lugar, posição e intensidade.",
                "Vamos fazer algo novo juntos: aula de dança ou culinária?",
                "Me ensina algo que você ama e eu nunca tentei?",
                "Leia um filme erótico para mim na cama e vamos ver quanto tempo duramos",
                "Diz uma parte do meu corpo que você ama explorar.",
                "Topa fazermos um roleplay sensual, bem criativo?",
                "Sexo na sauna",
                "Você vai me surpreender com cunilíngua",
                "Qual foi o momento em que mais se sentiu amado(a)?",
                "Hoje, só elogios: me diga 3, e eu te devolvo 3.",
                "Cartinha escrita à mão. Quem faz primeiro?",
                "Chupe meus lóbulos das orelhas",
                "Strip-tease com trilha sonora escolhida por você.",
                "Xixi no meu rosto",
                "Topa listar 3 coisas que você ama em mim e ouvir as minhas?",
                "Roleplay: Jardineiro e governanta",
                "Contar uma fantasia e realizar uma parte dela.",
                "Vamos escrever uma carta um pro outro e trocar amanhã.",
                "Sexo em um cômodo que nunca usamos.",
                "Topa me ensinar algo que você ama fazer?",
                "Sem julgamentos: cada um faz um pedido ousado hoje!",
                "No escuro, só toques. Sem palavras. Topa?",
                "Vamos conversar sobre nossos limites e desejos?",
                "Mostre meus nudes para seus amigos",
                "Banho juntos, com direito a massagem e carinho.",
                "Sexo a três:  +1 homem",
                "Vamos para a cama, você escolhe minha roupa e eu a sua",
                "Chupão no abdômen",
                "Tem algo que eu posso mudar pra sermos mais felizes?",
                "Imita minha cara quando tô excitado(a).",
                "Exame retal",
                "Você se sentiu visto(a) por mim essa semana?",
                "Tem algo que eu faço que te incomoda e nunca falou?",
                "Hoje, quero te provocar até você implorar por mais.",
                "Vamos transar, mas você não pode fazer nenhum som",
                "Vamos criar uma tradição só nossa?",
                "Sexo em câmera lenta. Sentir tudo. Olho no olho.",
                "Troca de casais",
                "Roleplay:  Vikings",
                "Hoje só vale dizer coisas que curam.",
                "Jantar à luz de velas e depois... sobremesa na cama.",
                "Chupe meus lóbulos das orelhas",
                "Trocar elogios sinceros antes de dormir.",
                "Promessa de hoje: escutar sem interromper.",
                "Mostre-me como você se masturba",
                "Jogo da verdade: 3 perguntas profundas cada um.",
                "Vamos para a cama, você escolhe minha roupa e eu a sua",
                "Ventosas eróticas",
                "Finge que acabamos de nos conhecer. Como me seduziria?",
                "Escreva em mim com os dedos o que quer que eu faça.",
                "Massagem com óleo e toques lentos. Sem pressa.",
                "1 verdade + 1 mentira sobre nós. Adivinha qual é qual!",
                "Sussurrar fantasias no ouvido um do outro.",
                "Wrist to thigh restraints",
                "Use uma máscara de diabo e me bata",
                "Me diga uma parte do meu corpo que você quer explorar mais.",
                "Que tal criarmos uma tradição só nossa?",
                "Como posso te amar melhor nos dias difíceis?",
                "Mostre-me como você se masturba",
                "Faça um elogio safado, mas que me faça rir.",
                "Hoje, nada de telas: só a gente, conversa e vinho.",
                "Você está no comando. Me entrega o que quiser."
        );

        for (String descricao : descricoes) {
            String id = UUID.randomUUID().toString(); // Será usado como nome do doc e como campo "id"

            Map<String, Object> carta = new HashMap<>();
            carta.put("criadorId", "padrao");
            carta.put("descricao", descricao);
            carta.put("id", id);

            db.collection("cartas")
                    .document(id) // mesmo valor usado dentro da carta
                    .set(carta)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Carta adicionada: " + descricao);
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Erro ao adicionar carta: " + descricao, e);
                    });
        }
    }
}