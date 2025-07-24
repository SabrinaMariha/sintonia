package com.sabrina.sintonia.models;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Montante {
    private  String id;
    private List<Carta> cartas;


    public Montante() {
        cartas = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("cartas")
                .whereEqualTo("criador", "padrao")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String id = document.getId();
                        String criadorId = document.getString("criador");
                        String descricao = document.getString("descricao");

                        Carta carta = new Carta(criadorId, descricao);
                        carta.setId(id); // atribui o id do documento à carta
                        cartas.add(carta);
                    }

                    System.out.println("Cartas carregadas: " + cartas.size());
                })
                .addOnFailureListener(e -> {
                    System.err.println("Erro ao buscar cartas: " + e.getMessage());
                });
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    public List<Carta> getCartas() {
        return cartas;
    }

    public void setCartas(List<Carta> cartas) {
        this.cartas = cartas;
    }
}
