package com.sabrina.sintonia;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sabrina.sintonia.models.Usuario;

import java.util.List;
import java.util.Random;
import android.graphics.drawable.GradientDrawable;
public class ConexaoAdapter extends RecyclerView.Adapter<ConexaoAdapter.ConexaoViewHolder> {
    private List<Usuario> listaUsuarios;
    private OnItemClickListener listener;
    private final int[] avatarColors = {
            R.color.avatar_color_1,
            R.color.avatar_color_2,
            R.color.avatar_color_3,
            R.color.avatar_color_4,
            R.color.avatar_color_5,
            R.color.avatar_color_6,
            R.color.avatar_color_7,
            R.color.avatar_color_8
    };

    public interface OnItemClickListener {
        void onItemClick(Usuario usuario);
    }

    public ConexaoAdapter(List<Usuario> listaUsuarios, OnItemClickListener listener) {
        this.listaUsuarios = listaUsuarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConexaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conexao, parent, false);

        return new ConexaoViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ConexaoViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);
        String nome = usuario.getUserName();

        holder.textViewNome.setText(nome);

        if (nome != null && !nome.isEmpty()) {
            String inicial = nome.substring(0, 1).toUpperCase();
            holder.avatarInicial.setText(inicial);
        } else {
            holder.avatarInicial.setText("?");
        }

        int index = Math.abs(usuario.getUuid().hashCode()) % avatarColors.length;
        int corResId = avatarColors[index];
        int cor = holder.itemView.getContext().getResources().getColor(corResId);

        // Cria um drawable circular com a cor sorteada
        GradientDrawable bgShape = new GradientDrawable();
        bgShape.setShape(GradientDrawable.OVAL);
        bgShape.setColor(cor);
        bgShape.setStroke(2, Color.WHITE);
        holder.avatarInicial.setBackground(bgShape);

        if ("ADD".equals(usuario.getUuid())) {
            holder.textViewNome.setTextColor(Color.WHITE);
            holder.textViewNome.setTypeface(null, Typeface.BOLD);
            holder.avatarInicial.setText("+");

            GradientDrawable addBgShape = new GradientDrawable();
            addBgShape.setShape(GradientDrawable.OVAL);
            addBgShape.setColor(Color.GRAY);
            holder.avatarInicial.setBackground(addBgShape);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(usuario);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class ConexaoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNome;
        TextView avatarInicial;
        public ConexaoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNome = itemView.findViewById(R.id.textViewNome);
            avatarInicial = itemView.findViewById(R.id.avatarInicial);
        }

    }
}
