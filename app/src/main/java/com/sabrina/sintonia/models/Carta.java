package com.sabrina.sintonia.models;

public class Carta {
    private  String id;
    private String criadorId;
    private String descricao;

    public Carta(){}
    public Carta(String criadorId, String descricao) {
        this.criadorId = criadorId;
        this.descricao = descricao;
    }

    public String getid() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getCriadorId() {
        return criadorId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setCriadorId(String criadorId){
        this.criadorId = criadorId;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
