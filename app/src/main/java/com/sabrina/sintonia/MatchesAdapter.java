package com.sabrina.sintonia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sabrina.sintonia.models.Carta;
import com.sabrina.sintonia.models.Usuario;

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
