package net.towpod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class clsTipoPolizaArrayAdapter extends ArrayAdapter<clsTipoPoliza> {

    public clsTipoPolizaArrayAdapter(Context context, List<clsTipoPoliza> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        //Obteniendo una instancia del inflater
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Salvando la referencia del View de la fila
        View listItemView = convertView;

        //Comprobando si el View no existe
        if (null == convertView) {
            //Si no existe, entonces inflarlo con image_list_view.xml
            listItemView = inflater.inflate(
                    R.layout.act_policy_type_format,
                    parent,
                    false);
        }

        //Obteniendo instancia de la clsCategoria en la posición actual
        clsTipoPoliza item = getItem(position);

        //Obteniendo instancias de los elementos

        TextView txtPolicyName = (TextView)listItemView.findViewById(R.id.txtPolicyName);
        txtPolicyName.setText(item.get_policy_type_name());

        TextView txtPolicyDescription = (TextView)listItemView.findViewById(R.id.txtPolicyDescription);
        txtPolicyDescription.setText(item.get_policy_type_description());

        DecimalFormat Formato = new DecimalFormat("#,###.00");
        String precioFormateado = Formato.format(item.get_policy_type_month_price());

        TextView txtPolicyPrice = (TextView)listItemView.findViewById(R.id.txtPolicyPrice);
        txtPolicyPrice.setText("HNL "+String.valueOf(precioFormateado) +"/mes");

        TextView lblNombreBanco = (TextView)listItemView.findViewById(R.id.lblNombreBanco);
        lblNombreBanco.setText(item.get_policy_bank_name());

        return listItemView;

    }




}