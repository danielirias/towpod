package net.towpod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class clsAutoArrayAdapter extends ArrayAdapter<clsAuto> {

    public clsAutoArrayAdapter(Context context, List<clsAuto> objects) {
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
                    R.layout.act_my_account_cars_item_format,
                    parent,
                    false);
        }

        //Obteniendo instancia de la clsCategoria en la posición actual
        clsAuto item = getItem(position);

        //Obteniendo instancias de los elementos
        TextView txtIDVehicular = (TextView)listItemView.findViewById(R.id.txtIDVehicular);
        txtIDVehicular.setText(item.get_car_id());

        TextView txtMarca = (TextView)listItemView.findViewById(R.id.txtMarca);
        txtMarca.setText(item.get_car_brand());

        TextView txtModelo = (TextView)listItemView.findViewById(R.id.txtModelo);
        txtModelo.setText(item.get_car_model());

        TextView txtYear = (TextView)listItemView.findViewById(R.id.txtYear);
        txtYear.setText(item.get_car_year().toString());


        return listItemView;

    }




}