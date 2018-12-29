package net.towpod;

/**
 * Created by Dani on 14/1/2016.
 */
public class clsHistory {
    private Integer row_id;
    private Integer customer_id;
    private String request_date;
    private String request_time;
    private Integer assistance_id;
    private String assistance_name;
    private String customer_comment;
    private String policy_number;

    private String Latitud;
    private String Longitud;

    private String car_id;
    private String car_brand;
    private String car_model;
    private Integer car_year;

    private String card_number;
    private Double service_value;
    private String strConfirmationCode;
    private Integer intStatus;

    private Integer intDriverRequired;

    public clsHistory(Integer row_id, Integer customer_id, String request_date, String request_time, Integer assistance_id, String assistance_name, String customer_comment, String policy_number, String Latitud, String Longitud, String car_id, String car_brand, String car_model, Integer car_year,  String card_number, Double service_value, String strConfirmationCode, Integer intStatus, Integer intDriverRequired)
    {
        this.row_id = row_id;
        this.customer_id = customer_id;
        this.request_date = request_date;
        this.request_time = request_time;
        this.assistance_id = assistance_id;
        this.assistance_name = assistance_name;
        this.customer_comment = customer_comment;
        this.policy_number = policy_number;

        this.Latitud = Latitud;
        this.Longitud = Longitud;

        this.car_id = car_id;
        this.car_brand = car_brand;
        this.car_model = car_model;
        this.car_year = car_year;

        this.card_number = card_number;
        this.service_value = service_value;
        this.strConfirmationCode = strConfirmationCode;
        this.intStatus = intStatus;

        this.intDriverRequired = intDriverRequired;
    }


    public Integer get_row_id(){return row_id;}
    public Integer get_customer_id(){return customer_id;}
    public String get_request_date(){return request_date;}
    public String get_request_time(){return request_time;}
    public Integer get_assistance_id(){return assistance_id;}
    public String get_assistance_name(){return assistance_name;}
    public String get_customer_comment(){return customer_comment;}
    public String get_policy_number(){return policy_number;}

    public String get_latitud(){return Latitud;}
    public String get_longitud(){return Longitud;}

    public String get_car_id(){return car_id;}
    public String get_car_brand(){return car_brand;}
    public String get_car_model(){return car_model;}
    public Integer get_car_year(){return car_year;}

    public String get_card_number(){return card_number;}
    public Double get_service_value(){return service_value;}

    public String get_confirmation_code(){return strConfirmationCode;}
    public Integer get_status(){return intStatus;}

    public Integer getDriverRequired(){return intDriverRequired;}



}