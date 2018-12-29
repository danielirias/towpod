package net.towpod;

/**
 * Created by Dani on 14/1/2016.
 */
public class clsPoliza {
    private String car_id;
    private String car_brand;
    private String car_model;
    private Integer car_year;

    private String policy_number;
    private String policy_name;
    private String policy_description;
    private String policy_card_number;
    private String policy_expire;
    private Double policy_value;
    private String bank_name;

    public clsPoliza(String car_id, String car_brand, String car_model, Integer car_year, String policy_number, String policy_name, String policy_description, String policy_card_number, Double policy_value, String bank_name){
        this.car_id = car_id;
        this.car_brand = car_brand;
        this.car_model = car_model;
        this.car_year = car_year;
        this.policy_number = policy_number;
        this.policy_name = policy_name;
        this.policy_description = policy_description;
        this.policy_card_number = policy_card_number;
        this.policy_value = policy_value;
        this.bank_name = bank_name;

    }

    public String get_car_id(){return car_id;}
    public String get_car_brand(){return car_brand;}
    public String get_car_model(){return car_model;}
    public Integer get_car_year(){return car_year;}

    public String get_policy_number(){return policy_number;}
    public String get_policy_name(){return policy_name;}
    public String get_policy_description(){return policy_description;}

    public String get_policy_card_number(){return policy_card_number;}

    public Double get_policy_value(){return policy_value;}

    public String get_policy_bank_name(){return bank_name;}

}