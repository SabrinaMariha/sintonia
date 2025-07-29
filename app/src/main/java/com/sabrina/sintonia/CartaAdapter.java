package com.sabrina.sintonia;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sabrina.sintonia.activities.GameActivity;
import com.sabrina.sintonia.activities.MatchesActivity;
import com.sabrina.sintonia.activities.NovaCartaActivity;
import com.sabrina.sintonia.models.Carta;

import java.util.List;

public class CartaAdapter extends RecyclerView.Adapter<CartaAdapter.CartaViewHolder> {

    private List<Carta> cartas;
    private String conexaoId;
    private String uidDois;
    private String nomeContato;

    public CartaAdapter(List<Carta> cartas, String conexaoId, String uidDois, String nomeContato) {
        this.cartas = cartas;
        this.conexaoId = conexaoId;
        this.uidDois = uidDois;
        this.nomeContato = nomeContato;
    }

    public interface OnLikeDislikeListener {
        void onLikeDislike(Carta carta, boolean gostou, int position);
    }

    private OnLikeDislikeListener listener;

    public void setOnLikeDislikeListener(OnLikeDislikeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carta, parent, false);
        return new CartaViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull CartaViewHolder holder, int position) {
        Carta carta = cartas.get(position);
        holder.textDescricao.setText(carta.getDescricao());
        // Aqui o clique no botão opções dentro de cada carta:

        // Aplica o gradiente depois que a view for desenhada
        holder.textDescricao.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        holder.textDescricao.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        TextPaint paint = holder.textDescricao.getPaint();
                        Shader shader = new LinearGradient(
                                0, 0, holder.textDescricao.getHeight(), 0, // vertical
                                new int[]{
                                        Color.parseColor("#C20853"),
                                        Color.parseColor("#E02E3E"),
                                        Color.parseColor("#FF5328")
                                },
                                null,
                                Shader.TileMode.CLAMP
                        );
                        paint.setShader(shader);
                        holder.textDescricao.invalidate();
                    }
                }
        );
        holder.btnLike.setOnClickListener(v -> {
            handleLikeDislike(holder.getAdapterPosition(), true); // true = like
        });

        holder.btnDislike.setOnClickListener(v -> {
            handleLikeDislike(holder.getAdapterPosition(), false); // false = dislike
        });

    }
    private void handleLikeDislike(int position, boolean gostou) {
        if (listener != null && position >= 0 && position < cartas.size()) {
            listener.onLikeDislike(cartas.get(position), gostou, position);
        }
    }


    public void removeItem(int position) {
        cartas.remove(position);
        notifyItemRemoved(position);
    }
    @Override
    public int getItemCount() {
        return Math.min(cartas.size(), 1);
    }

    static class CartaViewHolder extends RecyclerView.ViewHolder {
        TextView textDescricao;
        ImageButton btnLike, btnDislike;

        public CartaViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescricao = itemView.findViewById(R.id.text_descricao);
            btnLike = itemView.findViewById(R.id.btn_like);
            btnDislike = itemView.findViewById(R.id.btn_dislike);
        }
    }
}
