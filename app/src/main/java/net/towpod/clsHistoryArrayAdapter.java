package net.towpod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class clsHistoryArrayAdapter extends ArrayAdapter<clsHistory> {

    public clsHistoryArrayAdapter(Context context, List<clsHistory> objects) {
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
                    R.layout.act_my_account_history_item_format,
                    parent,
                    false);
        }

        //Obteniendo instancia de la clsCategoria en la posición actual
        clsHistory item = getItem(position);

        TextView lblFecha = (TextView)listItemView.findViewById(R.id.lblFecha);
        lblFecha.setText(item.get_request_date() + " - " + item.get_request_time());

        TextView lblTitulo = (TextView)listItemView.findViewById(R.id.lblTitulo);
        lblTitulo.setText(item.get_assistance_name());

        TextView lblComentario = (TextView)listItemView.findViewById(R.id.lblComentario);
        lblComentario.setText(item.get_customer_comment());

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




}