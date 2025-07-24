package com.sabrina.sintonia;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sabrina.sintonia.models.Carta;

import java.util.List;

public class CartaAdapter extends RecyclerView.Adapter<CartaAdapter.CartaViewHolder> {

    private List<Carta> cartas;

    public CartaAdapter(List<Carta> cartas) {
        this.cartas = cartas;
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
    }

    @Override
    public int getItemCount() {
        return cartas.size();
    }

    public void removeItem(int position) {
        cartas.remove(position);
        notifyItemRemoved(position);
    }

    static class CartaViewHolder extends RecyclerView.ViewHolder {
        TextView textDescricao;

        public CartaViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescricao = itemView.findViewById(R.id.text_descricao);
        }
    }
}
