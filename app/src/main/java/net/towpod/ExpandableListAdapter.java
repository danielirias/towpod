package net.towpod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Gourav on 08-03-2016.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private Context context;
	private ArrayList<ExpandableGroupInfo> GroupList;

	public ExpandableListAdapter(Context context, ArrayList<ExpandableGroupInfo> GroupList) {
		this.context = context;
		this.GroupList = GroupList;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		ArrayList<ExpandableChildInfo> productList = GroupList.get(groupPosition).getItemList();
		return productList.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
	                         View view, ViewGroup parent) {

		ExpandableChildInfo detailInfo = (ExpandableChildInfo) getChild(groupPosition, childPosition);
		if (view == null) {
			LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = infalInflater.inflate(R.layout.act_quotation_list_child_item, null);
		}

		TextView lblProviderName = (TextView) view.findViewById(R.id.lblProviderName);
		lblProviderName.setText(detailInfo.getProviderName());

		TextView lblComment = (TextView) view.findViewById(R.id.lblComment);
		lblComment.setText(detailInfo.getComment().trim());


		TextView lblPrice = (TextView) view.findViewById(R.id.lblPrice);
		lblPrice.setText(String.valueOf(detailInfo.getPrice()));

		TextView lblDiscount = (TextView) view.findViewById(R.id.lblDiscount);
		lblDiscount.setText(String.valueOf(detailInfo.getDiscount()));

		TextView lblTotal = (TextView) view.findViewById(R.id.lblTotal);
		lblTotal.setText(String.valueOf(detailInfo.getTotal()));


		return view;
	}

	@Override
	public int getChildrenCount(int groupPosition) {

		ArrayList<ExpandableChildInfo> productList = GroupList.get(groupPosition).getItemList();
		return productList.size();

	}

	@Override
	public Object getGroup(int groupPosition) {
		return GroupList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return GroupList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isLastChild, View view,
	                         ViewGroup parent) {

		ExpandableGroupInfo headerInfo = (ExpandableGroupInfo) getGroup(groupPosition);
		if (view == null) {
			LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inf.inflate(R.layout.act_quotation_list_group_item, null);
		}

		TextView lblFecha = (TextView) view.findViewById(R.id.lblFecha);
		lblFecha.setText(headerInfo.getDateRequest());

		TextView lblIDRequest = (TextView) view.findViewById(R.id.lblIDRequest);
		lblIDRequest.setText(String.valueOf(headerInfo.getIdRequest()));

		TextView lblDescription = (TextView) view.findViewById(R.id.lblDescription);
		lblDescription.setText(headerInfo.getDescription().trim());

		return view;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}