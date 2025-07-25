package com.sabrina.sintonia.models;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Montante {
    private String id;
    private List<Carta> cartas;

    public Montante() {
        this.cartas = new ArrayList<>();
    }

    public Montante(List<Carta> cartas) {
        this.cartas = cartas;
    }

    public List<Carta> getCartas() {
        return cartas;
    }

    // Método estático para carregar cartas padrão de forma assíncrona
    public static void carregarCartasPadrao(OnCartasCarregadasListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cartas")
                .whereEqualTo("criador", "padrao")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Carta> cartasCarregadas = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Carta carta = doc.toObject(Carta.class);
                        carta.setId(doc.getId());
                        cartasCarregadas.add(carta);
                    }
                    listener.onCartasCarregadas(cartasCarregadas);
                })
                .addOnFailureListener(e -> {
                    listener.onFalha(e);
                });
    }

    public interface OnCartasCarregadasListener {
        void onCartasCarregadas(List<Carta> cartas);
        void onFalha(Exception e);
    }
}

