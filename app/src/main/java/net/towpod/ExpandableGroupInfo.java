package net.towpod;

import java.util.ArrayList;

public class ExpandableGroupInfo {

	private String strDescription;
	private Integer intRequestID;
	private String strDate;

	private ArrayList<ExpandableChildInfo> list = new ArrayList<ExpandableChildInfo>();

	public void setDescription(String name) {
		this.strDescription = name;
	}
	public String getDescription() {
		return strDescription;
	}


	public void setIdRequest(Integer intRequestID) {
		this.intRequestID = intRequestID;
	}
	public Integer getIdRequest() {
		return intRequestID;
	}

	public void setDateRequest(String strDate) {
		this.strDate = strDate;
	}
	public String getDateRequest() {
		return strDate;
	}

	public ArrayList<ExpandableChildInfo> getItemList() {
		return list;
	}

	public void setItemList(ArrayList<ExpandableChildInfo> productList) {
		this.list = productList;
	}

}