package com.sabrina.sintonia;

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sabrina.sintonia.models.Carta;

import java.util.List;
import java.util.Map;

public class MinhasCartasAdapter extends RecyclerView.Adapter<MinhasCartasAdapter.MinhasCartasViewHolder> {
    private List<Carta> listaCartas;
    private String conexaoId;

    public MinhasCartasAdapter(List<Carta> listaCartas, String conexaoId) {
        this.listaCartas = listaCartas;
        this.conexaoId = conexaoId;
    }

    @NonNull
    @Override
    public MinhasCartasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_minhas_cartas, parent, false);
        return new MinhasCartasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MinhasCartasAdapter.MinhasCartasViewHolder holder, int position) {
        Carta carta = listaCartas.get(position);
        String descricao = carta.getDescricao();
        holder.textDescricaoMatch.setText(descricao);

        holder.itemView.setOnClickListener(v -> {
            Dialog dialog = new Dialog(v.getContext());
            dialog.setContentView(R.layout.dialog_minha_carta_editavel);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());

            int marginPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16,
                    v.getContext().getResources().getDisplayMetrics()
            );
            int screenWidth = v.getContext().getResources().getDisplayMetrics().widthPixels;
            layoutParams.width = screenWidth - (marginPx * 2);
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

            EditText editDescricao = dialog.findViewById(R.id.editDescricaoDetalhe);
            ImageButton btnFechar = dialog.findViewById(R.id.btnFechar);
            Button btnSalvar = dialog.findViewById(R.id.salvar_nova_carta);

            editDescricao.setText(carta.getDescricao());

            btnFechar.setOnClickListener(view -> dialog.dismiss());

            btnSalvar.setOnClickListener(view -> {
                String novaDescricao = editDescricao.getText().toString().trim();

                if (!novaDescricao.isEmpty()) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) return;

                    carta.setDescricao(novaDescricao);
                    notifyItemChanged(adapterPosition);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("conexoes")
                            .document(conexaoId)
                            .get()
                            .addOnSuccessListener(doc -> {
                                List<Map<String, Object>> cartas = (List<Map<String, Object>>) doc.get("montante.cartas");
                                if (cartas != null) {
                                    for (Map<String, Object> cartaMap : cartas) {
                                        if (carta.getId().equals(cartaMap.get("id"))) {
                                            cartaMap.put("descricao", novaDescricao);
                                            limparInteracoes(carta.getId(), conexaoId, view);
                                            break;
                                        }
                                    }

                                    db.collection("conexoes")
                                            .document(conexaoId)
                                            .update("montante.cartas", cartas)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(view.getContext(), "Descrição atualizada com sucesso", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(view.getContext(), "Erro ao atualizar no banco", Toast.LENGTH_SHORT).show();
                                                e.printStackTrace();
                                            });
                                }
                            });
                }
            });
            dialog.getWindow().setAttributes(layoutParams);
            dialog.show();
        });

        holder.btnTrashMinhaCarta.setOnClickListener(v -> {
            excluirCarta(carta.getId(), conexaoId, holder.getAdapterPosition(), v);
        });
    }

    private void excluirCarta(String idCarta, String conexaoId, int position, View view) {
        // Mostrar diálogo de confirmação
        new android.app.AlertDialog.Builder(view.getContext())
                .setTitle("Confirmar exclusão")
                .setMessage("Tem certeza de que deseja excluir esta carta?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    DocumentReference conexaoRef = db.collection("conexoes").document(conexaoId);

                    conexaoRef.get().addOnSuccessListener(docSnapshot -> {
                        if (docSnapshot.exists()) {
                            List<Map<String, Object>> cartasMontante =
                                    (List<Map<String, Object>>) docSnapshot.get("montante.cartas");

                            if (cartasMontante != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    cartasMontante.removeIf(cartaMap ->
                                            idCarta.equals(cartaMap.get("id")));
                                }

                                conexaoRef.update("montante.cartas", cartasMontante)
                                        .addOnSuccessListener(aVoid -> {
                                            listaCartas.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, listaCartas.size());
                                            Toast.makeText(view.getContext(), "Carta excluída com sucesso", Toast.LENGTH_SHORT).show();
                                            limparInteracoes(idCarta, conexaoId, view);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(view.getContext(), "Erro ao atualizar montante", Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        });
                            }
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(view.getContext(), "Erro ao acessar conexão", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }


    private void limparInteracoes(String cartaId, String conexaoId, View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("interacoes")
                .document(conexaoId)
                .collection("cartas")
                .document(cartaId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(view.getContext(), "A carta retornou ao montante de vocês!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(view.getContext(), "Erro inesperado ao voltar a carta ao montante", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }



    @Override
    public int getItemCount() {
        return listaCartas.size();
    }

    public static class MinhasCartasViewHolder extends RecyclerView.ViewHolder {
        TextView textDescricaoMatch;
        ImageButton btnTrashMinhaCarta;

        public MinhasCartasViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescricaoMatch = itemView.findViewById(R.id.text_descricao_match);
            btnTrashMinhaCarta = itemView.findViewById(R.id.trashMinhaCarta);
        }
    }
}
