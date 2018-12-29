package net.towpod;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;

public class clsProveedorCercanoDestinoArrayAdapter extends ArrayAdapter<clsProveedorCercanoDestino> {

    public clsProveedorCercanoDestinoArrayAdapter(Context context, List<clsProveedorCercanoDestino> objects) {
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
                    R.layout.dialog_proveedores_disponibles_extended_item_format,
                    parent,
                    false);
        }

        //Obteniendo instancias de los elementos
        TextView txtNombre = (TextView)listItemView.findViewById(R.id.textoNombre);
        //TextView txtDireccion = (TextView)listItemView.findViewById(R.id.textoDireccion);
        //TextView txtCiudad = (TextView)listItemView.findViewById(R.id.textoCiudad);
        TextView txtDistancia = (TextView)listItemView.findViewById(R.id.textoDistancia);

        //Obteniendo instancia de la clsProveedor en la posición actual
        clsProveedorCercanoDestino item = getItem(position);

        txtNombre.setText(item.get_nombre_proveedor());
        //txtDireccion.setText(item.getDireccion());
        //txtCiudad.setText(item.getCiudad());

        String distanciaObtenida;

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

        RatingBar Estrellas = (RatingBar) listItemView.findViewById(R.id.ratingBar);
        Estrellas.setRating(item.get_provider_rate());

        DecimalFormat Formato = new DecimalFormat("#,###.00");
        int price = (int) item.get_service_value_fixed();
	    String precioFormateado = Formato.format(price);
        /*
		String strPrice = String.valueOf(price);
		if(Integer.parseInt(strPrice.substring(strPrice.length()-1, strPrice.length())) > 5 )
		{
			price = price + 5;
		}
	    strPrice = String.valueOf(price);
	    Log.i ("TP_TAG", strPrice);
	    String strFixedprice = strPrice.substring(0, strPrice.length()-1)+"0";
	    try {

		    Log.i ("TP_TAG", strFixedprice);
	    }
	    catch (Exception e)
	    {
		    Log.i ("TP_TAG", e.getMessage());
	    }
	    String precioFormateado = Formato.format(Integer.parseInt(strFixedprice));
		*/

        TextView lblPrecioEstimado = (TextView) listItemView.findViewById(R.id.lblPrecioEstimado);
        lblPrecioEstimado.setText(item.get_country_currency() + " " + String.valueOf(precioFormateado));

        TextView lblCobertura = (TextView) listItemView.findViewById(R.id.lblCobertura);
        lblCobertura.setText("Aprox. a " + distanciaObtenida + ", en " + item.get_area_name() + "(" +item.get_aprox_time() + " mins)");
        lblCobertura.setText("Aprox. a " + distanciaObtenida + ", en " + item.get_area_name());

        TextView lblTotalDistance = (TextView) listItemView.findViewById(R.id.lblTotalDistance);
        lblTotalDistance.setText(item.getDistanceUserToDestiny()+" km");

        LinearLayout loCarInfo = (LinearLayout) listItemView.findViewById(R.id.loCarInfo);
        if (item.getCarbrand().trim() != "")
        {
            loCarInfo.setVisibility(View.VISIBLE);
            TextView lblCarInfo = (TextView) listItemView.findViewById(R.id.lblCarInfo);
            lblCarInfo.setText(item.getCarbrand()+" "+item.getCarModel()+" "+ String.valueOf(item.getCarYear()));
        }
        else
        {
            loCarInfo.setVisibility(View.GONE);
        }

        //Devolver al ListView la fila creada
        return listItemView;

    }
}