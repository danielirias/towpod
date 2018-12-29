package net.towpod;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;

public class clsProveedorCercano  {
    private String IdProveedor;
    private String nombreProveedor;
    private Integer assistance_id;
    private Double service_value;
    private Integer provider_rate;
    private Double distanciaProveedor;
    private Double additional_value;
    private String strPhone_1;
    private String strPhone_2;
    private String strPhone_3;
    private Integer area_id;
    private String area_name;
    private String aprox_time;
    private String country_currency;
    private double lat;
    private double lon;
    private Integer use_policy;
    private Context contexto;

    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    private Double fixed_service_value;
    private Double total_distance;
    private Double distanceUserToDestiny;


    public clsProveedorCercano(String IdProveedor, String nombreProveedor, Integer assistance_id, Double service_value, Integer provider_rate,
                               Double distanciaProveedor, String strPhone_1, String strPhone_2, String strPhone_3, Integer area_id, String area_name, String aprox_time, String country_currency, Double fixed_service_value)
    {
        this.IdProveedor = IdProveedor;
        this.nombreProveedor = nombreProveedor;
        this.service_value = service_value;
        this.provider_rate = provider_rate;
        this.distanciaProveedor = distanciaProveedor;
        this.additional_value = additional_value;
        this.strPhone_1 = strPhone_1;
        this.strPhone_2 = strPhone_2;
        this.strPhone_3 = strPhone_3;
        this.area_id = area_id;
        this.area_name = area_name;
        this.aprox_time = aprox_time;
        this.country_currency = country_currency;
        this.assistance_id = assistance_id;
        this.lat = lat;
        this.lon = lon;
        this.use_policy = use_policy;
        this.contexto = contexto;
        this.fixed_service_value = fixed_service_value;
        this.total_distance = total_distance;
        this.distanceUserToDestiny = distanceUserToDestiny;

    }

    public String get_id_proveedor(){return IdProveedor;}
    public String get_nombre_proveedor(){return nombreProveedor;}

    public double get_service_value()
    {
        return service_value;
    }

    public double get_service_value_fixed()
    {
        return fixed_service_value;
    }
    public double get_total_distance()
    {
        return total_distance;
    }

    public double getDistanceUserToDestiny(){return distanceUserToDestiny;}

    public Integer get_provider_rate(){return provider_rate;}
    public Double get_distancia(){return distanciaProveedor;}

    public String get_Phone_1(){return strPhone_1;}
    public String get_Phone_2(){return strPhone_2;}
    public String get_Phone_3(){return strPhone_3;}

    public Integer get_area_id(){return area_id;}
    public String get_area_name(){return area_name;}
    public String get_aprox_time(){return aprox_time;}
    public String get_country_currency(){return country_currency;}


    public Request setRetryPolicy(Request request)
    {
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }




}