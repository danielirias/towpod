package net.towpod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class clsPhonesArrayAdapter extends ArrayAdapter<clsPhones> {

    public clsPhonesArrayAdapter(Context context, List<clsPhones> objects) {
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
                    R.layout.dialog_make_call_item_format,
                    parent,
                    false);
        }

        //Obteniendo instancia de la clsPhones en la posición actual
        clsPhones item = getItem(position);

        if(position <2){
	        //Obteniendo instancias de los elementos
	        TextView lblPhoneNumber = (TextView)listItemView.findViewById(R.id.lblPhoneNumber);
	        lblPhoneNumber.setText(item.get_phone_number());
        }
        else
        {
	        //Obteniendo instancias de los elementos
	        TextView lblPhoneNumber = (TextView)listItemView.findViewById(R.id.lblPhoneNumber);
	        lblPhoneNumber.setText("Whatsapp");
        }


        return listItemView;

    }
}