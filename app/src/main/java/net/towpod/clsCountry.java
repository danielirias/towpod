package net.towpod;

/**
 * Created by Dani on 14/1/2016.
 */
public class clsCountry {

    private String country_name;
    private String country_iso_name;
    private String country_area_code;
    private String country_currency;

    public clsCountry(String country_name, String country_iso_name, String country_area_code, String country_currency)
    {
        this.country_name = country_name;
        this.country_iso_name = country_iso_name;
        this.country_area_code = country_area_code;
        this.country_currency = country_currency;
    }

    public String get_country_name(){return country_name;}
    public String get_country_iso_name(){return country_iso_name;}
    public String get_country_area_code(){return country_area_code;}
    public String get_country_currency(){return country_currency;}

}