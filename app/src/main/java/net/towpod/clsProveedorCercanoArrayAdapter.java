package net.towpod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class clsProveedorCercanoArrayAdapter extends ArrayAdapter<clsProveedorCercano> {

    public clsProveedorCercanoArrayAdapter(Context context, List<clsProveedorCercano> objects) {
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
                    R.layout.dialog_proveedores_disponibles_item_format,
                    parent,
                    false);
        }




        //Obteniendo instancia de la clsProveedor en la posición actual
        clsProveedorCercano item = getItem(position);

        TextView lblNombre = (TextView)listItemView.findViewById(R.id.lblNombre);
        lblNombre.setText(item.get_nombre_proveedor());

	    RatingBar Estrellas = (RatingBar) listItemView.findViewById(R.id.ratingBar);
	    Estrellas.setRating(item.get_provider_rate());

        String distanciaObtenida;
        TextView txtDistancia = (TextView)listItemView.findViewById(R.id.textoDistancia);

        if (item.get_distancia() < 1) {
            DecimalFormat Formato = new DecimalFormat("#,###");
            String distanciaFormateada = Formato.format(item.get_distancia()*1000);
            txtDistancia.setText(distanciaFormateada + " mts");
            distanciaObtenida = distanciaFormateada + " metros";
        }
        else
        {
            DecimalFormat Formato = new DecimalFormat("#,###.00");
            String distanciaFormateada = Formato.format(item.get_distancia());
            txtDistancia.setText(distanciaFormateada + " km");
            distanciaObtenida = distanciaFormateada + " km";
        }

        DecimalFormat Formato = new DecimalFormat("#,###.00");
        int price = (int) item.get_service_value_fixed();
        String precioFormateado = Formato.format(price);

        TextView lblPrecioEstimado = (TextView) listItemView.findViewById(R.id.lblPrecioEstimado);
        lblPrecioEstimado.setText(item.get_country_currency() + " " + String.valueOf(precioFormateado));

        TextView lblCobertura = (TextView) listItemView.findViewById(R.id.lblCobertura);
        lblCobertura.setText("Aprox. a " + distanciaObtenida + ", en " + item.get_area_name() + "(" +item.get_aprox_time() + " mins)");
        lblCobertura.setText("Aprox. a " + distanciaObtenida + ", en " + item.get_area_name());

        //Devolver al ListView la fila creada
        return listItemView;

    }
}