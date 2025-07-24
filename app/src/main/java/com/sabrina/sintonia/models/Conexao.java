package com.sabrina.sintonia.models;

public class Conexao {
    private  String id;
    private  Usuario usuarioUm;
    private  Usuario usuarioDois;
    private Montante montante;

    public Conexao(){
        this.montante = new Montante();
    }
    public Conexao(Usuario usuarioUm, Usuario usuarioDois) {
        this.usuarioUm = usuarioUm;
        this.usuarioDois = usuarioDois;
        this.montante = new Montante();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Usuario getUsuarioUm() {
        return usuarioUm;
    }

    public Usuario getUsuarioDois() {
        return usuarioDois;
    }

    public Montante getMontante() {
        return montante;
    }

    public void setMontante(Montante montante) {
        this.montante = montante;
    }
}
