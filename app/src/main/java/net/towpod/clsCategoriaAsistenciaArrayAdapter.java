package net.towpod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class clsCategoriaAsistenciaArrayAdapter extends ArrayAdapter<clsCategoriaAsistencia> {

    public clsCategoriaAsistenciaArrayAdapter(Context context, List<clsCategoriaAsistencia> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        //Obteniendo una instancia del inflater
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Salvando la referencia del View de la fila
        View listItemView = convertView;

        //Comprobando si el View no existe
       listItemView = inflater.inflate(
                    R.layout.act_get_help_item_format,
                    parent,
                    false);


        //Obteniendo instancia de la clsCategoria en la posición actual
        clsCategoriaAsistencia item = getItem(position);

        TextView txtNombreCategoria = (TextView)listItemView.findViewById(R.id.txtNombreCategoria);
        txtNombreCategoria.setText(item.getNombreCategoria());

        return listItemView;

    }

}