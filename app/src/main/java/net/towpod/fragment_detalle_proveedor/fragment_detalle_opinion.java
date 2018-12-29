package net.towpod.fragment_detalle_proveedor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import net.towpod.clsComentario;
import net.towpod.clsComentarioArrayAdapter;
import net.towpod.R;
import net.towpod.RequestQueueSingleton;

public class fragment_detalle_opinion extends Fragment
{
    ListView ListaComentarios;
    ArrayAdapter AdaptadorComentarios;
    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    public fragment_detalle_opinion() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View MyView = inflater.inflate(R.layout.fragment_detalle_opinion, container, false);

        //==========================================================================================
        //=======CARGA DE COMENTARIOS DE LOS USUARIOS===============================================
        //==========================================================================================

        //Instancia del ListView
        ListaComentarios = (ListView) MyView.findViewById(R.id.ListViewComentarios);

        //Obtenemos la instancia única de la cola de peticiones
        requestQueue = RequestQueueSingleton.getRequestQueue(this.getActivity());

        //Inicializar el adaptador con la fuente de datos
        List<clsComentario> Comentarios = new ArrayList<clsComentario>();
        AdaptadorComentarios = new clsComentarioArrayAdapter(this.getActivity(), Comentarios);
        AdaptadorComentarios.clear();

        //URL del detalle del proveedor
        String urlComment = "http://app.towpod.net/ws_get_comments.php?idp="+ getArguments().getString("prmIDProveedor");
        urlComment = urlComment.replaceAll(" ", "%20");

        JsonArrayRequest requestComment = new JsonArrayRequest(urlComment,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray responseComment) {
                        for (int i = 0; i < responseComment.length(); i++){
                            try
                            {
                                AdaptadorComentarios.add(new clsComentario(
                                        responseComment.getJSONObject(i).getString("comment_id"),
                                        responseComment.getJSONObject(i).getString("customer_name"),
                                        responseComment.getJSONObject(i).getInt("comment_rate"),
                                        responseComment.getJSONObject(i).getString("comment_text"),
                                        responseComment.getJSONObject(i).getString("comment_date")));
                            } catch (JSONException e)
                            {
                                //Toast popUpError = Toast.makeText(actProviderData.this, e.toString() , Toast.LENGTH_LONG);
                                //popUpError.show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Si recibimos un error, mostraremos la causa
                        //Toast popUpError = Toast.makeText(actProviderData.this, error.getMessage(), Toast.LENGTH_LONG);
                        //popUpError.show();
                    }
                }
        );

        //Le ponemos un tag que servirá para identificarla si la queremos cancelar
        requestComment.setTag(JSON_REQUEST);
        //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
        requestComment = (JsonArrayRequest) setRetryPolicy(requestComment);
        //Iniciamos la petición añadiéndola a la cola
        requestQueue.add(requestComment);

        //Inicializar el adaptador con la fuente de datos
        AdaptadorComentarios = new clsComentarioArrayAdapter(this.getActivity(), Comentarios);

        //Relacionando la lista con el adaptador
        ListaComentarios.setAdapter(AdaptadorComentarios);

        //==========================================================================================
        //=======FIN DE CARGA DE COMENTARIOS DE LOS USUARIOS========================================
        //==========================================================================================

        return  MyView;

    }

    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request){
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }


}