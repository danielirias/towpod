package net.towpod;

/**
 * Created by Dani on 14/1/2016.
 */
public class clsCategoria {
    private String IdCategoria;
    private String nombreCategoria;
    private String colorCategoria;
    private String UrlIconoCategoria;

    public clsCategoria(String IdCategoria, String nombreCategoria, String colorCategoria, String UrlIconoCategoria){
        this.IdCategoria = IdCategoria;
        this.nombreCategoria = nombreCategoria;
        this.colorCategoria = colorCategoria;
        this.UrlIconoCategoria = UrlIconoCategoria;
    }

    public void setIdCategoria(String idCategoria){
        this.IdCategoria = idCategoria;
    }

    public void setNombreCategoria(String nombre){
        this.nombreCategoria = nombre;
    }

    public void setColorCategoria(String color){
        this.colorCategoria = color;
    }

    public void setUrlIconoCategoria(String urlicono){
        this.UrlIconoCategoria = urlicono;
    }


    public String getIdCategoria(){return IdCategoria;}
    public String getNombreCategoria(){return nombreCategoria;}
    public String getColorCategoria(){return colorCategoria;}
    public String getURLIconoCategoria(){return UrlIconoCategoria;}

}