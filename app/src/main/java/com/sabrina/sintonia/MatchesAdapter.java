package com.sabrina.sintonia;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sabrina.sintonia.models.Carta;

import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchesViewHolder>{
    private List<Carta> listaCartas;



    public MatchesAdapter(List<Carta> listaCartas) {
        this.listaCartas = listaCartas;

    }
    @NonNull
    @Override
    public MatchesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carta_match, parent, false);

        return new MatchesAdapter.MatchesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchesAdapter.MatchesViewHolder holder, int position) {
        Carta carta = listaCartas.get(position);
        String descricao = carta.getDescricao();
        holder.textDescricaoMatch.setText(descricao);
        holder.itemView.setOnClickListener(v -> {
            Dialog dialog = new Dialog(v.getContext());
            dialog.setContentView(R.layout.dialog_detalhe_carta);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // Deixa fundo arredondado se quiser

            TextView descricaoView = dialog.findViewById(R.id.textDescricaoDetalhe);
            ImageButton btnFechar = dialog.findViewById(R.id.btnFechar);

            descricaoView.setText(carta.getDescricao());
            btnFechar.setOnClickListener(view -> dialog.dismiss());

            dialog.show();
        });

    }

    @Override
    public int getItemCount() {
        return listaCartas.size(); // em vez de retornar 0
    }
    public static class MatchesViewHolder extends RecyclerView.ViewHolder {
        TextView textDescricaoMatch;

        public MatchesViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescricaoMatch = itemView.findViewById(R.id.text_descricao_match);

        }
    }
}
