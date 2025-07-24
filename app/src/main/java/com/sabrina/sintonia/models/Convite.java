package com.sabrina.sintonia.models;
import java.util.Date;

public class Convite {
    private String id;
    private Usuario remetente;
    private Usuario destinatario;
    private String status;
    private Date timestamp;

    public Convite() {}// Necessário para Firebase

    public Convite(Usuario remetente, Usuario destinatario, String status, Date timestamp) {
        this.remetente = remetente;
        this.destinatario = destinatario;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public Usuario getRemetente() {
        return remetente;
    }

    public void setRemetente(Usuario remetente) {
        this.remetente = remetente;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Usuario destinatario) {
        this.destinatario = destinatario;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}

