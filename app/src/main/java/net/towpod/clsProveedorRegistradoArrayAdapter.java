package net.towpod;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class clsProveedorRegistradoArrayAdapter extends ArrayAdapter<clsProveedorRegistrado> {

    public clsProveedorRegistradoArrayAdapter(Context context, List<clsProveedorRegistrado> objects) {
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
                    R.layout.act_my_account_stores_item_format,
                    parent,
                    false);
        }

        //Obteniendo instancias de los elementos
        TextView txtNombre = (TextView)listItemView.findViewById(R.id.textoNombre);
        TextView txtDireccion = (TextView)listItemView.findViewById(R.id.textoDireccion);
        TextView txtCiudad = (TextView)listItemView.findViewById(R.id.textoCiudad);


        //Obteniendo instancia de la clsProveedor en la posición actual
        clsProveedorRegistrado item = getItem(position);

        txtNombre.setText(item.getNombre());
        txtDireccion.setText(item.getDireccion());
        txtCiudad.setText(item.getCiudad());



        //ImageView icono = (ImageView) listItemView.findViewById(R.id.iconoCategoria);
        //new AsyncTaskLoadImage(icono).execute(item.getIconoCategoria());

        ImageView iconoCategoria = (ImageView) listItemView.findViewById(R.id.iconoCategoria);

        //línea para cargar el icono local
        //iconoCategoria.setImageResource(R.drawable.mainicon);


        String strMyCatNumber = "";
        if ((item.getIdCategoria()) < 10)
        {
            strMyCatNumber = "cat0" + item.getIdCategoria();
        }
        else
        {
            strMyCatNumber = "cat" + item.getIdCategoria();
        }
        int resID = getContext().getResources().getIdentifier(strMyCatNumber, "drawable", getContext().getPackageName());
        iconoCategoria.setImageResource(resID);
        //Devolver al ListView la fila creada
        return listItemView;

    }

    public class AsyncTaskLoadImage  extends AsyncTask<String, String, Bitmap> {
        private final static String TAG = "AsyncTaskLoadImage";
        private ImageView imageView;
        public AsyncTaskLoadImage(ImageView imageView) {
            this.imageView = imageView;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(params[0]);
                bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return bitmap;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }
}