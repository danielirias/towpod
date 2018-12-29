package net.towpod;

public class ExpandableChildInfo {

	private String sequence = "";
	private String Comment = "";

	private Integer ProviderID;
	private String ProviderName = "";
	private String ProviderAddress = "";
	private String ProviderCity = "";
	private String ProviderPhone1 = "";
	private String ProviderPhone2 = "";


	private Double Price = 0.0;
	private Double Discount = 0.0;
	private Double Total = 0.0;

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	public String getSequence() {
		return sequence;
	}


	public void setProviderID(Integer ProviderID) {
		this.ProviderID = ProviderID;
	}
	public Integer getProviderID() {
		return ProviderID;
	}


	public void setProviderName(String ProviderName) {
		this.ProviderName = ProviderName;
	}
	public String getProviderName() {
		return ProviderName;
	}

	public void setProviderAddress(String ProviderAddress) {
		this.ProviderAddress = ProviderAddress;
	}
	public String getProviderAddress() {
		return ProviderAddress;
	}

	public void setProviderCity(String ProviderCity) {
		this.ProviderCity = ProviderCity;
	}
	public String getProviderCity() {
		return ProviderCity;
	}

	public void setProviderPhone1(String ProviderPhone1) {
		this.ProviderPhone1 = ProviderPhone1;
	}
	public String getProviderPhone1() {
		return ProviderPhone1;
	}

	public void setProviderPhone2(String ProviderPhone2) {
		this.ProviderPhone2 = ProviderPhone2;
	}
	public String getProviderPhone2() {
		return ProviderPhone2;
	}


	public void setComment(String Comment) {
		this.Comment = Comment;
	}
	public String getComment() {
		return Comment;
	}


	public void setPrice(Double Price) {
		this.Price = Price;
	}
	public Double getPrice() {
		return Price;
	}


	public void setDiscount(Double Discount) {
		this.Discount = Discount;
	}
	public Double getDiscount() {
		return Discount;
	}


	public void setTotal(Double Total) {
		this.Total = Total;
	}
	public Double getTotal() {
		return Total;
	}
}