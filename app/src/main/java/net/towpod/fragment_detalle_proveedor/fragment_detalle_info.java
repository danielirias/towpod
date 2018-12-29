package net.towpod.fragment_detalle_proveedor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


import net.towpod.R;


public class fragment_detalle_info extends  Fragment
{

    public fragment_detalle_info()
    {
        //Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View MyView = inflater.inflate(R.layout.fragment_detalle_info, container, false);

        ImageView iconoCategoria = (ImageView) MyView.findViewById(R.id.imgIconoCategoria);
        //Para cargar el ciono de la categoria localmente:
        String strMyCatNumber = "";
        if (getArguments().getInt("prmIdCategoria") < 10)
        {
            strMyCatNumber = "cat0" + getArguments().getInt("prmIdCategoria");
        }
        else
        {
            strMyCatNumber = "cat" + getArguments().getInt("prmIdCategoria");
        }
        int resID = getResources().getIdentifier(strMyCatNumber, "drawable", getContext().getPackageName());
        //iconoCategoria.setImageResource(resID);

        //Para cargar el ciono de la categoria desde el servidor:
        //new AsyncTaskLoadImage(iconoCategoria).execute(getArguments().getString("prmUrlIDCategoria"));

        //DATOS GENERALES
        TextView txtTituloProveedor = (TextView) MyView.findViewById(R.id.txtNombreProveedor);
        txtTituloProveedor.setText(getArguments().getString("prmNombreProveedor"));

        RatingBar Estrellas = (RatingBar) MyView.findViewById(R.id.ratingBar);
        Estrellas.setRating(getArguments().getInt("prmRating"));

        TextView txtNombreCategoria = (TextView) MyView.findViewById(R.id.txtNombreCategoria);
        txtNombreCategoria.setText(getArguments().getString("prmNombreCategoria"));

        TextView txtDireccion = (TextView) MyView.findViewById(R.id.txtDireccion);
        txtDireccion.setText(getArguments().getString("prmDireccionProveedor"));

        TextView txtCiudad = (TextView) MyView.findViewById(R.id.txtCiudad);
        txtCiudad.setText(getArguments().getString("prmCiudadProveedor"));

        TextView txtDistancia = (TextView) MyView.findViewById(R.id.txtDistancia);
        txtDistancia.setText(getArguments().getString("prmDistanciaProveedor"));


        //HORARIOS
        TextView txtTimeMonFri = (TextView) MyView.findViewById(R.id.txtNumeroPoliza);
        txtTimeMonFri.setText(getArguments().getString("prmHorarioLunesViernes"));

        TextView txtTimeSat = (TextView) MyView.findViewById(R.id.txtEstadoPoliza);
        txtTimeSat.setText(getArguments().getString("prmHorarioSabado"));

        TextView txtTimeSun = (TextView) MyView.findViewById(R.id.txtTimeSun);
        txtTimeSun.setText(getArguments().getString("prmHorarioDomingo"));

        TextView txtIntro = (TextView) MyView.findViewById(R.id.txtIntro);
        txtIntro.setText(getArguments().getString("prmIntro"));



        //SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.mapaProveedor);
        //mapFragment.getMapAsync(this);

        return MyView;
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