package com.sabrina.sintonia;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sabrina.sintonia.models.Convite;
import com.sabrina.sintonia.models.Usuario;

import java.util.List;

public class ConviteAdapter extends RecyclerView.Adapter<ConviteAdapter.ConviteViewHolder> {
    private List<Convite> listaConvites;
    private OnConviteClickListener listener;
    public ConviteAdapter(List<Convite> listaConvites, OnConviteClickListener listener) {
        this.listaConvites = listaConvites;
        this.listener = listener;
    }
    public interface OnConviteClickListener {
        void onAceitarClick(Convite convite);
        void onRecusarClick(Convite convite);
    }
    @NonNull
    @Override
    public ConviteAdapter.ConviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_convite, parent, false);
        return new ConviteAdapter.ConviteViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ConviteAdapter.ConviteViewHolder holder, int position) {
        Convite convite = listaConvites.get(position);

        // Nome do remetente diretamente do objeto
        Usuario remetente = convite.getRemetente();
        if (remetente != null && remetente.getUserName() != null) {
            holder.textViewNome.setText(remetente.getUserName());
        } else {
            holder.textViewNome.setText("Usuário desconhecido");
        }

        // Botões
        holder.btnAceitar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAceitarClick(convite);
            }
        });

        holder.btnRecusar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecusarClick(convite);
            }
        });
    }


    @Override
    public int getItemCount() {
        return listaConvites.size();

    }

    public static class ConviteViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNome;
        MaterialButton btnAceitar, btnRecusar;
        public ConviteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNome = itemView.findViewById(R.id.textViewNome);
            btnAceitar = itemView.findViewById(R.id.btn_aceitar);
            btnRecusar = itemView.findViewById(R.id.btn_recusar);
        }
    }
}
