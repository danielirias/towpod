package net.towpod;

/**
 * Created by Dani on 14/1/2016.
 */
public class clsTipoPoliza {
    private Integer policy_type_id;
    private String policy_type_name;
    private String policy_type_description;
    private Double policy_type_month_price;
    private String bank_name;



    public clsTipoPoliza(Integer policy_type_id, String policy_type_name, String policy_type_description, Double policy_type_month_price, String bank_name){
        this.policy_type_id = policy_type_id;
        this.policy_type_name = policy_type_name;
        this.policy_type_description = policy_type_description;
        this.policy_type_month_price = policy_type_month_price;
        this.bank_name = bank_name;
    }

    public void set_policy_type_id(Integer policy_type_id){
        this.policy_type_id = policy_type_id;
    }
    public void set_policy_type_name(String policy_type_name){this.policy_type_name = policy_type_name;}
    public void set_policy_type_description(String policy_type_description){this.policy_type_description = policy_type_description;}
    public void set_policy_type_month_price(Double policy_type_month_price){this.policy_type_month_price = policy_type_month_price;}

    public Integer get_policy_type_id(){return policy_type_id;}
    public String get_policy_type_name(){return policy_type_name;}
    public String get_policy_type_description(){return policy_type_description;}
    public Double get_policy_type_month_price(){return policy_type_month_price;}
    public String get_policy_bank_name(){return bank_name;}

}