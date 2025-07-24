package com.sabrina.sintonia.models;

public class CartaoInteracao {
    private  String id;
    private  Usuario usuario;
    private  Carta carta;
    private String gostou;
    private  Conexao conexao;
    public CartaoInteracao(){}
    public CartaoInteracao(Usuario usuario, Carta carta, Conexao conexao) {
        this.usuario = usuario;
        this.carta = carta;
        this.conexao = conexao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Carta getCarta() {
        return carta;
    }

    public String getGostou() {
        return gostou;
    }

    public void setGostou(String gostou) {
        this.gostou = gostou;
    }

    public Conexao getConexao() {
        return conexao;
    }
}
