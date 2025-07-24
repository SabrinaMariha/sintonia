package com.sabrina.sintonia.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Usuario {
    private String uuid;
    private String userName;
    private String codigo;
    private List<CartaoInteracao> interacoes;
    private List<Carta> cartasCriadas;

public Usuario(){}

    public Usuario(String uuid, String userName) {
        this.uuid = uuid;
        this.userName = userName;
        this.codigo = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        this.interacoes = new ArrayList<>();
        this.cartasCriadas = new ArrayList<>();
    }

    public String getUserName() {
        return userName;
    }

    public String getUuid() {
        return uuid;
    }

    public String getCodigo() {
        return codigo;
    }
}
