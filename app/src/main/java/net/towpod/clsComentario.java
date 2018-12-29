package net.towpod;

/**
 * Created by Dani on 2/2/2016.
 */

public class clsComentario {
    private String idComentario;
    private String NombrePersona;
    private int IntRate;
    private String Comentario;
    private String Fecha;

    public clsComentario(String idComentario, String NombrePersona, int IntRate, String Comentario, String Fecha){
        this.idComentario = idComentario;
        this.NombrePersona = NombrePersona;
        this.IntRate = IntRate;
        this.Comentario = Comentario;
        this.Fecha = Fecha;
    }

    public void setidComentario(String idComentario){
        this.idComentario = idComentario;
    }

    public void setNombrePersona(String NombrePersona){
        this.NombrePersona = NombrePersona;
    }

    public void setIntRate(int IntRate){
        this.IntRate = IntRate;
    }

    public void setComentario(String Comentario){
        this.Comentario = Comentario;
    }

    public void setFecha(String Fecha){
        this.Fecha = Fecha;
    }


    public String getidComentario(){return idComentario;}
    public String getNombrePersona(){return NombrePersona;}
    public int getIntRate(){return IntRate;}
    public String getComentario(){return Comentario;}
    public String getFecha(){return Fecha;}

}