package net.towpod;

public class clsProveedorRegistrado {
    private String IdProveedor;
    private String nombreProveedor;
    private String direccionProveedor;
    private String ciudadProveedor;
    private Double LatProveedor;
    private Double LonProveedor;
    private Double distanciaProveedor;
    private String IconoCategoria;
    private String colorCategoria;
    private Integer idCategoria;

    public clsProveedorRegistrado(String IdProveedor, String nombreProveedor, String direccionProveedor, String ciudadProveedor, String IconoCategoria, String colorCategoria, Integer idCategoria){
        this.IdProveedor = IdProveedor;
        this.nombreProveedor = nombreProveedor;
        this.direccionProveedor = direccionProveedor;
        this.ciudadProveedor = ciudadProveedor;
        this.IconoCategoria = IconoCategoria;
        this.colorCategoria = colorCategoria;
        this.idCategoria = idCategoria;
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

    public void setIconoCategoria(String IconoCategoria){
        this.IconoCategoria = IconoCategoria;
    }

    public void setColorCategoria(String color){
        this.colorCategoria = color;
    }

    public void setIDCategoria(Integer idcategoria){
        this.idCategoria = idcategoria;
    }



    public String getIdProveedor(){return IdProveedor;}
    public String getNombre(){return nombreProveedor;}
    public String getDireccion(){return direccionProveedor;}
    public String getCiudad(){return ciudadProveedor;}
    public Double getLatitud(){return LatProveedor;}
    public Double getLongitud(){return LonProveedor;}
    public Double getDistancia(){return distanciaProveedor;}
    public String getIconoCategoria(){return IconoCategoria;}
    public String getColorCategoria(){return colorCategoria;}
    public Integer getIdCategoria(){return idCategoria;}

}