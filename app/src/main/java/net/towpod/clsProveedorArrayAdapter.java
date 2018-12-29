package net.towpod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class clsProveedorArrayAdapter extends ArrayAdapter<clsProveedor> {

    public clsProveedorArrayAdapter(Context context, List<clsProveedor> objects) {
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
                    R.layout.act_lista_proveedores_item_format,
                    parent,
                    false);
        }

        //Obteniendo instancias de los elementos
        TextView txtNombre = (TextView)listItemView.findViewById(R.id.textoNombre);
        TextView txtDireccion = (TextView)listItemView.findViewById(R.id.textoDireccion);
        TextView txtCiudad = (TextView)listItemView.findViewById(R.id.textoCiudad);
        TextView txtDistancia = (TextView)listItemView.findViewById(R.id.textoDistancia);

        //Obteniendo instancia de la clsProveedor en la posición actual
        clsProveedor item = getItem(position);

        txtNombre.setText(item.getNombre());
        txtDireccion.setText(item.getDireccion());
        txtCiudad.setText(item.getCiudad());

        if (item.getDistancia() < 1) {
            DecimalFormat Formato = new DecimalFormat("#,###");
            String distanciaFormateada = Formato.format(item.getDistancia()*1000);
            txtDistancia.setText(distanciaFormateada + " mts");
        }
        else
        {
            DecimalFormat Formato = new DecimalFormat("#,###.00");
            String distanciaFormateada = Formato.format(item.getDistancia());
            txtDistancia.setText(distanciaFormateada + " km");
        }

        RatingBar Estrellas = (RatingBar) listItemView.findViewById(R.id.ratingBar);
        Estrellas.setRating(item.get_provider_rate());

        //Devolver al ListView la fila creada
        return listItemView;

    }
}