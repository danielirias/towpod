package net.towpod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

public class clsComentarioArrayAdapter extends ArrayAdapter<clsComentario> {

    public clsComentarioArrayAdapter(Context context, List<clsComentario> objects) {
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
                    R.layout.fragment_detalle_opinion_format,
                    parent,
                    false);
        }

        //Obteniendo instancia de la Categoria en la posición actual
        clsComentario item = getItem(position);

        //Obteniendo instancias de los elementos
        TextView txtNombrePersona = (TextView)listItemView.findViewById(R.id.txtNombrePersona);
        txtNombrePersona.setText(item.getNombrePersona());

        TextView txtComentario = (TextView)listItemView.findViewById(R.id.txtComentario);
        txtComentario.setText(item.getComentario());

        TextView txtFecha = (TextView)listItemView.findViewById(R.id.txtFecha);
        txtFecha.setText(item.getFecha());

        RatingBar Estrellas = (RatingBar) listItemView.findViewById(R.id.ratingBar);
        Estrellas.setRating(item.getIntRate());

        //Devolver al ListView la fila creada
        return listItemView;

    }

}