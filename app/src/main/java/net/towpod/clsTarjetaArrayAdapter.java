package net.towpod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class clsTarjetaArrayAdapter extends ArrayAdapter<clsTarjeta> {

    public clsTarjetaArrayAdapter(Context context, List<clsTarjeta> objects) {
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
                    R.layout.act_my_account_my_cards_item_format,
                    parent,
                    false);
        }

        //Obteniendo instancia de la clsCategoria en la posición actual
        clsTarjeta item = getItem(position);

        String strCardType = "";

        if (item.get_card_number().toString().substring(0, 1).equals("4"))
        {
            strCardType = "VISA";
        }

        if (item.get_card_number().toString().substring(0, 2).equals("51"))
        {
            strCardType = "MASTERCARD";
        }
        if (item.get_card_number().toString().substring(0, 2).equals("52"))
        {
            strCardType = "MASTERCARD";
        }
        if (item.get_card_number().toString().substring(0, 2).equals("53"))
        {
            strCardType = "MASTERCARD";
        }
        if (item.get_card_number().toString().substring(0, 2).equals("54"))
        {
            strCardType = "MASTERCARD";
        }
        if (item.get_card_number().toString().substring(0, 2).equals("55"))
        {
            strCardType = "MASTERCARD";
        }


        //Obteniendo instancias de los elementos
        TextView txtCardType = (TextView)listItemView.findViewById(R.id.txtCardType);
        txtCardType.setText(strCardType);

        TextView txtCardNumber = (TextView)listItemView.findViewById(R.id.txtCardNumber);
        txtCardNumber.setText(item.get_card_number());

        TextView txtNameOnCard = (TextView)listItemView.findViewById(R.id.txtNameOnCard);
        txtNameOnCard.setText(item.get_card_name());

        TextView txtCardValid = (TextView)listItemView.findViewById(R.id.txtCardValid);
        txtCardValid.setText(item.get_card_valid());

        return listItemView;

    }



}