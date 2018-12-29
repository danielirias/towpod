package net.towpod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class clsCountryArrayAdapter extends ArrayAdapter<clsCountry> {

    public clsCountryArrayAdapter(Context context, List<clsCountry> objects) {
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
                    R.layout.act_registration_country_item_format,
                    parent,
                    false);
        }

        //Obteniendo instancia de la clsCategoria en la posición actual
        clsCountry item = getItem(position);

        TextView txtCountryName = (TextView)listItemView.findViewById(R.id.txtCountryName);
        txtCountryName.setText(item.get_country_name());

        TextView txtCountryAreaCode = (TextView)listItemView.findViewById(R.id.txtCountryAreaCode);
        txtCountryAreaCode.setText(item.get_country_area_code());


        /*TextView lblPlaca = (TextView)listItemView.findViewById(R.id.lblPlaca);
        lblPlaca.setText(item.get_car_id());

        TextView lblCarBrand = (TextView)listItemView.findViewById(R.id.lblCarBrand);
        lblCarBrand.setText(item.get_car_brand());

        TextView lblCarModel = (TextView)listItemView.findViewById(R.id.lblCarModel);
        lblCarModel.setText(item.get_car_model());

        TextView lblCarYear = (TextView)listItemView.findViewById(R.id.lblCarYear);
        lblCarYear.setText(String.valueOf(item.get_car_year()));

        DecimalFormat Formato = new DecimalFormat("#,###.00");
        String precioFormateado = Formato.format(item.get_service_value());

        TextView lblPrecioPagado = (TextView)listItemView.findViewById(R.id.lblPrecioPagado);
        lblPrecioPagado.setText("HNL " + String.valueOf(precioFormateado));*/

        return listItemView;

    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        /*
        Debido a que deseamos usar spinner_item.xml para inflar los
        items del Spinner en ambos casos, entonces llamamos a getView()
         */
        return getView(position, convertView, parent);
    }




}