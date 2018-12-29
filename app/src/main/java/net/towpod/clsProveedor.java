package net.towpod;

public class clsProveedor {
    private String IdProveedor;
    private String nombreProveedor;
    private String direccionProveedor;
    private String ciudadProveedor;
    private Double LatProveedor;
    private Double LonProveedor;
    private Double distanciaProveedor;
    private Integer provider_rate;

    public clsProveedor(String IdProveedor, String nombreProveedor, String direccionProveedor, String ciudadProveedor, Double LatProveedor, Double LonProveedor, Double distanciaProveedor, Integer provider_rate){
        this.IdProveedor = IdProveedor;
        this.nombreProveedor = nombreProveedor;
        this.direccionProveedor = direccionProveedor;
        this.ciudadProveedor = ciudadProveedor;
        this.LatProveedor = LatProveedor;
        this.LonProveedor = LonProveedor;
        this.distanciaProveedor = distanciaProveedor;
        this.provider_rate = provider_rate;
    }

    public void setIdProveedor(String idproveedor){
        this.IdProveedor = idproveedor;
    }

    public void setNombre(String nombre){
        this.nombreProveedor = nombre;
    }

    public void setDireccion(String direccion){
        this.direccionProveedor = direccion;
    }

    public void setCiudad(String ciudad){
        this.ciudadProveedor = ciudad;
    }

    public void setLatitud(Double latitud){
        this.distanciaProveedor = latitud;
    }

    public void setLongitud(Double longitud){
        this.distanciaProveedor = longitud;
    }

    public void setDistancia(Double distancia){
        this.distanciaProveedor = distancia;
    }



    public String getIdProveedor(){return IdProveedor;}
    public String getNombre(){return nombreProveedor;}
    public String getDireccion(){return direccionProveedor;}
    public String getCiudad(){return ciudadProveedor;}
    public Double getLatitud(){return LatProveedor;}
    public Double getLongitud(){return LonProveedor;}
    public Double getDistancia(){return distanciaProveedor;}
    public Integer get_provider_rate(){return provider_rate;}

}