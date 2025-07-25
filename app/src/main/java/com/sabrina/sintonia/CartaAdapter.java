package com.sabrina.sintonia;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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
        // Aqui o clique no botão opções dentro de cada carta:
        holder.opcoes.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_opcoes, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_adicionar_carta) {
                    Toast.makeText(view.getContext(), "Adicionar nova carta selecionado", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.menu_ver_matches) {
                    Toast.makeText(view.getContext(), "Ver matches selecionado", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

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
        ImageView opcoes;
        public CartaViewHolder(@NonNull View itemView) {
            super(itemView);
            textDescricao = itemView.findViewById(R.id.text_descricao);
            opcoes = itemView.findViewById(R.id.opcoes);
        }
    }
}
