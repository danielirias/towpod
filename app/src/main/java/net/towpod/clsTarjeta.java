package net.towpod;

/**
 * Created by Dani on 14/1/2016.
 */
public class clsTarjeta {
    private String card_number;
    private String card_name;
    private String card_valid;
    private String card_code;


    public clsTarjeta(String card_number, String card_name, String card_valid, String card_code){
        this.card_number = card_number;
        this.card_name = card_name;
        this.card_valid = card_valid;
        this.card_code = card_code;
    }

    public void set_card_number(String card_number){
        this.card_number = card_number;
    }

    public void set_card_name(String card_name){
        this.card_name = card_name;
    }

    public void set_card_valid(String card_valid){
        this.card_valid = card_valid;
    }

    public void set_card_code(String card_code){
        this.card_code = card_code;
    }


    public String get_card_number(){return card_number;}
    public String get_card_name(){return card_name;}
    public String get_card_valid(){return card_valid;}
    public String get_card_code(){return card_code;}


}