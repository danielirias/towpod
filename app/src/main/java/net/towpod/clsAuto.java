package net.towpod;

/**
 * Created by Dani on 14/1/2016.
 */
public class clsAuto {
    private String car_id;
    private String car_brand;
    private String car_model;
    private Integer car_year;


    public clsAuto(String car_id, String car_brand, String car_model, Integer car_year){
        this.car_id = car_id;
        this.car_brand = car_brand;
        this.car_model = car_model;
        this.car_year = car_year;
    }

    public void set_car_id(String car_id){
        this.car_id = car_id;
    }

    public void set_car_brand(String car_brand){
        this.car_brand = car_brand;
    }

    public void set_car_model(String car_model){
        this.car_model = car_model;
    }

    public void set_car_year(Integer car_year){
        this.car_year = car_year;
    }


    public String get_car_id(){return car_id;}
    public String get_car_brand(){return car_brand;}
    public String get_car_model(){return car_model;}
    public Integer get_car_year(){return car_year;}


}