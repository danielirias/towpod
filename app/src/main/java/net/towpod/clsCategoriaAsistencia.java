package net.towpod;

/**
 * Created by Dani on 14/1/2016.
 */
public class clsCategoriaAsistencia {
    private String IdCategoria;
    private String nombreCategoria;
    private Integer destination_required;
    private Integer DriverRequired;
    private Integer OnlyForSearch;

    public clsCategoriaAsistencia(String IdCategoria, String nombreCategoria, Integer destination_required, Integer DriverRequired, Integer OnlyForSearch){
        this.IdCategoria = IdCategoria;
        this.nombreCategoria = nombreCategoria;
        this.destination_required = destination_required;
        this.DriverRequired = DriverRequired;
        this.OnlyForSearch = OnlyForSearch;
    }

    public String getIdCategoria(){return IdCategoria;}
    public String getNombreCategoria(){return nombreCategoria;}
    public Integer getDestinationRequired(){return destination_required;}
    public Integer getDriverRequired(){return DriverRequired;}
	public Integer getOnlyForSearch(){return OnlyForSearch;}


}