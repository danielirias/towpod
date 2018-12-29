package net.towpod;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.towpod.fragment_detalle_proveedor.fragment_detalle_info;
import net.towpod.fragment_detalle_proveedor.fragment_detalle_opinion;
import net.towpod.fragment_detalle_proveedor.fragment_detalle_servicios;


public class actInfoProveedor extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    public static String SESSION_USER_MAIL = "";

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private RequestQueue requestQueue;
    private final static int JSON_REQUEST = 0;

    public String prmIDProveedor;
    public String strLatitud;
    public String strLongitud;

    public Double strLatitudProveedor;
    public Double strLongitudProveedor;

    public String strWebsite;
    ListView ListaComentarios;
    ArrayAdapter AdaptadorComentarios;

    public static String EXTRA_ID_PROVEEDOR = "extra.id.categoria";

    public String[] providerPhone = new String[3];
    public String strProviderName = "";
    public Integer nPhones;
    public String strNumberToDial = "";
    public String strAreaCode = "";

    GoogleMap googleMap;
    MapView mapView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAbout:
                Intent actAbout = new Intent(actInfoProveedor.this, actAbout.class);
                startActivity(actAbout);
                return true;

            case R.id.menuMyRecord:
                Intent actRegistros = new Intent(actInfoProveedor.this, actMyAccountStores.class);
                startActivity(actRegistros);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_info_proveedor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Realizo el proceo en segundo plano y muestro el cuadro de espera
        ProgressDialog VentanaEspera = new ProgressDialog(this);
        VentanaEspera.setTitle("Obteniendo información");
        VentanaEspera.setMessage("Espere un momento, por favor...");
        new ProcesoSegundoPlano(VentanaEspera, this).execute();
        //PresentarInfoProveedor();
    }

    protected void onStart()
    {
        super.onStart();
    }

    private void PresentarInfoProveedor()
    {
        try{
            //============Inicio de construccion de los tabs===================================
            viewPager = (ViewPager) findViewById(R.id.pager);
            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

            Intent intent = getIntent();
            prmIDProveedor = intent.getStringExtra(actListaProveedores.EXTRA_ID_PROVEEDOR);
            strLatitud = intent.getStringExtra(actListaProveedores.EXTRA_LATITUD_USUARIO);
            strLongitud = intent.getStringExtra(actListaProveedores.EXTRA_LONGITUD_USUARIO);

            final Bundle bundle = new Bundle();

            bundle.putString("prmIDProveedor", prmIDProveedor);

            fragment_detalle_info DetalleInfo = new fragment_detalle_info();
            DetalleInfo.setArguments(bundle);
            adapter.addFrag(DetalleInfo, "Información");


            //Obtenemos la instancia única de la cola de peticiones
            requestQueue = RequestQueueSingleton.getRequestQueue(this);

            //URL del detalle del proveedor
            String url = "http://app.towpod.net/ws_get_provider_detail.php?id="+prmIDProveedor+"&lat="+strLatitud+"&lon="+strLongitud;
            //String url = "http://app.towpod.net/ws_get_provider_detail.php?id=" + prmIDProveedor + "&lat=14.0668&lon=-87.1949";
            url = url.replaceAll(" ", "%20");

            Toast popUpError = Toast.makeText(actInfoProveedor.this, url, Toast.LENGTH_LONG);
            popUpError.show();

            JsonArrayRequest request = new JsonArrayRequest(url,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                /*
                                ImageView imagenHeader = (ImageView) findViewById(R.id.imagenHeader);
                                if (CampoVacio(response.getJSONObject(0).getString("provider_logo")) == true) {
                                    //Aqui pongo la publicidad si no hay logo asignado
                                    //new AsyncTaskLoadImage(imagenHeader).execute("http://s427533673.onlinehome.us/helpmech/assets/img/bgheader.jpg");
                                } else {
                                    //En caso de haber un logo asignado lo presento aqui
                                    new AsyncTaskLoadImage(imagenHeader).execute("http://s427533673.onlinehome.us/helpmech/assets/img/logos/" + response.getJSONObject(0).getString("provider_logo"));
                                }
                                */

                                bundle.putString("prmUrlIDCategoria", response.getJSONObject(0).getString("category_icon"));
                                ImageView iconoCategoria = (ImageView) findViewById(R.id.imgIconoCategoria);

                                //Para cargar el ciono de la categoria localmente:
                                String strMyCatNumber = "";
                                if (Integer.parseInt(response.getJSONObject(0).getString("category_id")) < 10) {
                                    strMyCatNumber = "cat0" + response.getJSONObject(0).getString("category_id");
                                } else {
                                    strMyCatNumber = "cat" + response.getJSONObject(0).getString("category_id");
                                }
                                int resID = getResources().getIdentifier(strMyCatNumber, "drawable", getPackageName());
                                //iconoCategoria.setImageResource(resID);

                                //Para cargar el ciono de la categoria desde el servidor:
                                //new AsyncTaskLoadImage(iconoCategoria).execute(response.getJSONObject(0).getString("category_icon"));

                                bundle.putString("prmNombreProveedor", response.getJSONObject(0).getString("provider_name"));
                                TextView txtNombreProveedor = (TextView) findViewById(R.id.txtNombreProveedor);
                                txtNombreProveedor.setText(response.getJSONObject(0).getString("provider_name"));
                                setTitle(response.getJSONObject(0).getString("provider_name"));
                                strProviderName = response.getJSONObject(0).getString("provider_name");


                                bundle.putInt("prmRating", response.getJSONObject(0).getInt("provider_rate"));
                                RatingBar Estrellas = (RatingBar) findViewById(R.id.ratingBar);
                                Estrellas.setRating(response.getJSONObject(0).getInt("provider_rate"));

                                bundle.putString("prmNombreCategoria", response.getJSONObject(0).getString("provider_name"));
                                TextView txtNombreCategoria = (TextView) findViewById(R.id.txtNombreCategoria);
                                txtNombreCategoria.setText(response.getJSONObject(0).getString("category_name"));

                                bundle.putString("prmDireccionProveedor", response.getJSONObject(0).getString("provider_address"));
                                TextView txtDireccion = (TextView) findViewById(R.id.txtDireccion);
                                txtDireccion.setText(response.getJSONObject(0).getString("provider_address"));

                                bundle.putString("prmCiudadProveedor", response.getJSONObject(0).getString("provider_city"));
                                TextView txtCiudad = (TextView) findViewById(R.id.txtCiudad);
                                txtCiudad.setText(response.getJSONObject(0).getString("provider_city"));

                                bundle.putDouble("prmLatProveedor", Double.parseDouble(response.getJSONObject(0).getString("provider_lat")));
                                strLatitudProveedor = Double.parseDouble(response.getJSONObject(0).getString("provider_lat"));

                                bundle.putDouble("prmLonProveedor", Double.parseDouble(response.getJSONObject(0).getString("provider_lon")));
                                strLongitudProveedor = Double.parseDouble(response.getJSONObject(0).getString("provider_lon"));

                                TextView txtDistancia = (TextView) findViewById(R.id.txtDistancia);
                                double dblDistancia = Double.parseDouble(response.getJSONObject(0).getString("distance"));

                                if (dblDistancia < 1) {
                                    DecimalFormat Formato = new DecimalFormat("#,###");
                                    String distanciaFormateada = Formato.format(dblDistancia * 1000);
                                    bundle.putString("prmDistanciaProveedor", distanciaFormateada + " metros");
                                    txtDistancia.setText(distanciaFormateada + " metros");
                                } else {
                                    DecimalFormat Formato = new DecimalFormat("#,###.00");
                                    String distanciaFormateada = Formato.format(dblDistancia);
                                    bundle.putString("prmDistanciaProveedor", distanciaFormateada + " km");
                                    txtDistancia.setText(distanciaFormateada + " km");
                                }

                                if (dblDistancia > 5) {
                                    txtDistancia.setTextColor(Color.parseColor("#f44336"));
                                }
                                if (dblDistancia <= 5) {
                                    txtDistancia.setTextColor(Color.parseColor("#FF9800"));
                                }
                                if (dblDistancia <= 2.5) {
                                    txtDistancia.setTextColor(Color.parseColor("#108315"));
                                }

                                bundle.putString("prmIntro", response.getJSONObject(0).getString("provider_intro"));
                                TextView txtIntro = (TextView) findViewById(R.id.txtIntro);
                                txtIntro.setText(response.getJSONObject(0).getString("provider_intro"));

                                bundle.putString("prmHorarioLunesViernes", response.getJSONObject(0).getString("provider_time_mon_fri"));
                                TextView txtTimeMonFri = (TextView) findViewById(R.id.txtNumeroPoliza);
                                txtTimeMonFri.setText(response.getJSONObject(0).getString("provider_time_mon_fri"));

                                bundle.putString("prmHorarioSabado", response.getJSONObject(0).getString("provider_time_sat"));
                                TextView txtTimeSat = (TextView) findViewById(R.id.txtEstadoPoliza);
                                txtTimeSat.setText(response.getJSONObject(0).getString("provider_time_sat"));

                                bundle.putString("prmHorarioDomingo", response.getJSONObject(0).getString("provider_time_sun"));
                                TextView txtTimeSun = (TextView) findViewById(R.id.txtTimeSun);
                                txtTimeSun.setText(response.getJSONObject(0).getString("provider_time_sun"));

                                if (Integer.parseInt(response.getJSONObject(0).getString("payment_credit_debit_card")) == 1) {
                                    bundle.putString("prmPayCard", "1");
                                    RelativeLayout loCard = (RelativeLayout) findViewById(R.id.loCard);
                                    loCard.setVisibility(View.VISIBLE);
                                }
                                if (Integer.parseInt(response.getJSONObject(0).getString("payment_cash")) == 1) {
                                    bundle.putString("prmPayCash", "1");
                                    RelativeLayout loCash = (RelativeLayout) findViewById(R.id.loCash);
                                    loCash.setVisibility(View.VISIBLE);
                                }
                                if (Integer.parseInt(response.getJSONObject(0).getString("payment_check")) == 1) {
                                    bundle.putString("prmPayCheck", "1");
                                    RelativeLayout loCheck = (RelativeLayout) findViewById(R.id.loCheck);
                                    loCheck.setVisibility(View.VISIBLE);
                                }

                                providerPhone[0] = response.getJSONObject(0).getString("provider_phone");
                                providerPhone[1] = response.getJSONObject(0).getString("provider_phone2");
                                providerPhone[2] = response.getJSONObject(0).getString("whatsapp_number");

                                strAreaCode = response.getJSONObject(0).getString("country_area_code");

                                strWebsite = response.getJSONObject(0).getString("provider_website");

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //Si recibimos un error, mostraremos la causa
                            Toast popUpError = Toast.makeText(actInfoProveedor.this, error.getMessage(), Toast.LENGTH_LONG);
                            //popUpError.show();
                        }
                    }
            );

            //Le ponemos un tag que servirá para identificarla si la queremos cancelar
            request.setTag(JSON_REQUEST);
            //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
            request = (JsonArrayRequest) setRetryPolicy(request);
            //Iniciamos la petición añadiéndola a la cola
            requestQueue.add(request);

            //==================================================================================================

            fragment_detalle_servicios DetalleServicios = new fragment_detalle_servicios();
            DetalleServicios.setArguments(bundle);
            adapter.addFrag(DetalleServicios, "Servicios");

            /*fragment_detalle_servicios DetalleGaleria = new fragment_detalle_servicios();
            DetalleGaleria.setArguments(bundle);
            adapter.addFrag(DetalleGaleria, "Galería");

            fragment_detalle_mapa DetalleMapa = new fragment_detalle_mapa();
            DetalleMapa.setArguments(bundle);
            adapter.addFrag(DetalleMapa, "Mapa");*/

            fragment_detalle_opinion DetalleOpinion = new fragment_detalle_opinion();
            DetalleOpinion.setArguments(bundle);
            adapter.addFrag(DetalleOpinion, "Opiniones");


            viewPager.setAdapter(adapter);

            tabLayout = (TabLayout) findViewById(R.id.MyTabs);
            tabLayout.setupWithViewPager(viewPager);//Esto hace que el TabLayout se enlace al viewPager y cambien de Tab al mismo tiempo

            tabLayout.getTabAt(0).setIcon(R.drawable.inforound);
            tabLayout.getTabAt(1).setIcon(R.drawable.services);
            //tabLayout.getTabAt(2).setIcon(R.drawable.gallery);
            //tabLayout.getTabAt(2).setIcon(R.drawable.map);
            tabLayout.getTabAt(2).setIcon(R.drawable.chat);
        }
        catch (Exception e)
        {

        }


    }

    class ViewPagerAdapter extends FragmentPagerAdapter
    {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private boolean CampoVacio(String cadena) {
        return cadena.toString().trim().length() == 0;
    }

    public class AsyncTaskLoadImage extends AsyncTask<String, String, Bitmap> {
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

    //Le pone una política de 4 reintentos con 4 segundos entre cada uno y la devuelve.
    public Request setRetryPolicy(Request request) {
        return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
    }

    public void ShowProviderLocation(View view)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(actInfoProveedor.this);
        LayoutInflater inflater = actInfoProveedor.this.getLayoutInflater();
        //builder.setView(inflater.inflate(R.layout.dialog_provider_location, null));

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                /*
                Solicitudes de intenciones

                Para iniciar Google Maps con una intención, primero debes crear un objeto Intent y especificar su acción, su URI y el paquete.

                Acción: todas las intenciones de Google Maps se llaman como una acción View: ACTION_VIEW.

                URI: Google Maps intenta usar cadenas con codificación de URI en las que se especifique una acción y datos con los cuales se realice la acción.

                Paquete: Llamar a setPackage("com.google.android.apps.maps") garantizará que la aplicación de Google Maps para Android administre la intención.

                Si no se configura el paquete, el sistema determinará las aplicaciones que pueden administrar la Intent.
                Si se encuentran disponibles varias aplicaciones, se puede solicitar al usuario que especifique la que desea usar.
                Después de crear la Intent, puedes solicitar que el sistema inicie la aplicación relacionada de varias maneras.
                Una práctica común consiste en pasar la Intent al método startActivity().
                El sistema lanzará la aplicación necesaria (en este caso, Google Maps) e iniciará la Activity correspondiente.
                */


                // Create a Uri from an intent string. Use the result to create an Intent.
                //Uri gmmIntentUri = Uri.parse("geo:37.7749,-122.4194"); //Muestra un punto en el mapa
                //Uri gmmIntentUri = Uri.parse("google.navigation:daddr=14.0639596,-87.1912505&mode=d"); //Inicia la navegación
                //Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=14.0639596,-87.1912505"); //Muestra las rutas disponibles para llegar
                Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr="+strLatitudProveedor+", "+strLongitudProveedor); //Muestra las rutas disponibles para llegar



                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");

                // Attempt to start an activity that can handle the Intent
                startActivity(mapIntent);

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("Ver cómo llegar");
        alertDialog.setMessage("Esta acción abrirá Google Maps y te mostrará las rutas disponibles.\n\n¿Deseas continuar?");
        alertDialog.show();

    }

    public void MakePhoneCall(View v)
    {

        nPhones = 1;

        if (CadenaVacia(providerPhone[0].toString()) == true) {
            Toast popUpError = Toast.makeText(actInfoProveedor.this, "El número telefónico no está disponible.", Toast.LENGTH_SHORT);
            popUpError.show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(actInfoProveedor.this);
        LayoutInflater inflater = actInfoProveedor.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_make_call_from_provider_info, null));

        if (CadenaVacia(providerPhone[1].toString()) == false) {
            nPhones = nPhones + 1;
        }

        if (CadenaVacia(providerPhone[2].toString()) == false) {
            //Omito el numero de whastapp
            //nPhones = nPhones + 1;
        }

        final CharSequence[] items = new CharSequence[nPhones];

        for (int i = 1; i <= nPhones; i++) {
            items[i - 1] = providerPhone[i - 1];
        }

        //Asigno el número por default, o sea el primero
        strNumberToDial = items[0].toString();

        /*
        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int wich) {
                Toast popUp = Toast.makeText(actGetHelp.this, "Has marcado el número: " + items[wich].toString(), Toast.LENGTH_SHORT);
                //popUp.show();
                strNumberToDial = items[wich].toString();
            }
        });
        */

        // Add the buttons
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast popUp = Toast.makeText(actInfoProveedor.this, "Has cancelado tu llamada", Toast.LENGTH_SHORT);
                //popUp.show();
            }
        });

        /*
        builder.setPositiveButton("Llamar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast popUp = Toast.makeText(actGetHelp.this, "Llamando", Toast.LENGTH_SHORT);
                //popUp.show();
                //Intent callIntent = new Intent(Intent.ACTION_DIAL); //Escribe el número telefónico pero no lo marca
                Intent callIntent = new Intent(Intent.ACTION_CALL); //Marca el número inmediatamente
                callIntent.setData(Uri.parse("tel:" + strNumberToDial));
                if (ActivityCompat.checkSelfPermission(actGetHelp.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent);

            }
        });
        */

        AlertDialog dialog = builder.create();
        //dialog.setTitle("Selecciona un número telefónico");
        dialog.show();

        //String[] sistemas = {"Ubuntu", "Android", "iOS", "Windows", "Mac OSX", "Google Chrome OS", "Debian", "Mandriva", "Solaris", "Unix"};


        List<clsPhones> Phones = new ArrayList<clsPhones>();
        ArrayAdapter AdaptadorPhones;
        AdaptadorPhones = new clsPhonesArrayAdapter(this, Phones);
        AdaptadorPhones.clear();


        for(int i = 1; i <= nPhones; i++)
        {
            AdaptadorPhones.add(new clsPhones(providerPhone[i-1]));
        }


        ListView ListViewPhones = (ListView) dialog.findViewById(R.id.ListViewPhones);
        ListViewPhones.setAdapter(AdaptadorPhones);
        //Creo el evento Clic para cada objeto de la lista
        ListViewPhones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
                clsPhones itemSeleccionado = (clsPhones) pariente.getItemAtPosition(posicion);

                Intent callIntent = new Intent(Intent.ACTION_CALL); //Marca el número inmediatamente
                callIntent.setData(Uri.parse("tel:" + itemSeleccionado.get_phone_number()));
                if (ActivityCompat.checkSelfPermission(actInfoProveedor.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent);

            }
        });

    }

    public void openWhatsApp(View view)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(actInfoProveedor.this);
        LayoutInflater inflater = actInfoProveedor.this.getLayoutInflater();
        //builder.setView(inflater.inflate(R.layout.dialog_provider_location, null));

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {

                // Check the SDK version and whether the permission is already granted or not.
                if (checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                    //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
                }
                else
                {

                    // Get android phone contact content provider uri.
                    //Uri addContactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                    // Below uri can avoid java.lang.UnsupportedOperationException: URI: content://com.android.contacts/data/phones error.
                    Uri addContactsUri = ContactsContract.Data.CONTENT_URI;

                    // Add an empty contact and get the generated id.
                    long rowContactId = getRawContactId();

                    // Add contact name data.
                    insertContactDisplayName(addContactsUri, rowContactId, strProviderName);

                    // Add contact phone data.
                    insertContactPhoneNumber(addContactsUri, rowContactId,  strAreaCode+providerPhone[0], ContactsContract.CommonDataKinds.Phone.TYPE_WORK);

                    if(providerPhone[1].trim() != "")
                    {
                        insertContactPhoneNumber(addContactsUri, rowContactId,  strAreaCode+providerPhone[1], ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE);
                    }

                    if(providerPhone[2].trim() != "")
                    {
                        insertContactPhoneNumber(addContactsUri, rowContactId,  strAreaCode+providerPhone[2], ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                        //================
                        Intent _intencion = new Intent("android.intent.action.MAIN");
                        _intencion.setComponent(new ComponentName("com.whatsapp","com.whatsapp.Conversation"));
                        _intencion.putExtra("jid", strAreaCode+providerPhone[2]+"@s.whatsapp.net");
                        startActivity(_intencion);
                    }
                    else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(actInfoProveedor.this);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setTitle("Chatear en WhatsApp");
                        alertDialog.setMessage("Este proveedor no ha configurado un número telefónico para WhatsApp.");
                        alertDialog.show();
                    }

                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("Chatear en WhatsApp");
        alertDialog.setMessage("Esta acción abrirá un chat de WhatsApp con el proveedor de servicios.\n\n¿Deseas continuar?");
        alertDialog.show();

    }

    // This method will only insert an empty data to RawContacts.CONTENT_URI
    // The purpose is to get a system generated raw contact id.
    private long getRawContactId()
    {
        // Inser an empty contact.
        ContentValues contentValues = new ContentValues();
        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
        // Get the newly created contact raw id.
        long ret = ContentUris.parseId(rawContactUri);
        return ret;
    }


    // Insert newly created contact display name.
    private void insertContactDisplayName(Uri addContactsUri, long rawContactId, String displayName)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

        // Each contact must has an mime type to avoid java.lang.IllegalArgumentException: mimetype is required error.
        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

        // Put contact display name value.
        contentValues.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, displayName);

        getContentResolver().insert(addContactsUri, contentValues);

    }

    private void insertContactPhoneNumber(Uri addContactsUri, long rawContactId, String phoneNumber, int phoneContactType)
    {
        // Create a ContentValues object.
        ContentValues contentValues = new ContentValues();

        // Each contact must has an id to avoid java.lang.IllegalArgumentException: raw_contact_id is required error.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

        // Each contact must has an mime type to avoid java.lang.IllegalArgumentException: mimetype is required error.
        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

        // Put phone number value.
        contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);

        // Put phone type value.
        contentValues.put(ContactsContract.CommonDataKinds.Phone.TYPE, phoneContactType);

        // Insert new contact data into phone contact list.
        getContentResolver().insert(addContactsUri, contentValues);

    }



    public void GoToWebSite(View v)
    {
        if (CampoVacio(strWebsite) == true) {
            Toast popUpError = Toast.makeText(actInfoProveedor.this, "No hay un sitio web disponible.", Toast.LENGTH_SHORT);
            popUpError.show();
            return;
        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse("http://" + strWebsite));
        startActivity(browserIntent);
    }

    public void AddComment(View v)
    {


        AlertDialog.Builder builder = new AlertDialog.Builder(actInfoProveedor.this);
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_add_comment, null));

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Nada para hacer aquí
            }
        });

        builder.setPositiveButton("Enviar comentario", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //No pasa nada... el código se trasladó a la clase CustomListener
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new CustomListener(alertDialog));

    }

    class CustomListener implements View.OnClickListener
    {
        private final Dialog dialog;

        public CustomListener(Dialog MyDialog) {
            this.dialog = MyDialog;
        }

        @Override
        public void onClick(View v) {

            try
            {
                //Abrimos la base de datos 'DBUsuarios' en modo escritura
                cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actInfoProveedor.this, "USUARIOSHM", null, 2);
                SQLiteDatabase db = usdbh.getWritableDatabase();

                //Si hemos abierto correctamente la base de datos
                if(db != null)
                {
                    String[] campos = new String[] {"user_id", "user_email", "user_password"};
                    final Cursor CursorDB = db.query("USUARIO", campos, null, null, null, null, null);

                    try
                    {
                        CursorDB.moveToFirst();
                        if (CursorDB.getCount() == 0)
                        {
                            //Si NO EXISTE LOCALMENTE lo envio al Login para que inicie sesion o se registre
                            Intent actMainLogin = new Intent(actInfoProveedor.this, actMainLogin.class);
                            startActivity(actMainLogin);
                            return;

                        }
                        else
                        {
                            //Si el USUARIO EXISTE LOCALMENTE voy a buscarlo al servidor web
                            //Busco la direccion de correo entre los usarios registrados
                            //Obtenemos la instancia única de la cola de peticiones
                            requestQueue = RequestQueueSingleton.getRequestQueue(actInfoProveedor.this);

                            //Con esta URL verifico si se han creado registros con este usuario.
                            String urlUser = "http://app.towpod.net/ws_get_login_data_customer.php?usr="+CursorDB.getString(1)+"&pwd="+CursorDB.getString(2);;
                            urlUser = urlUser.replaceAll(" ", "%20");

                            //Creamos la petición
                            JsonArrayRequest requestUser = new JsonArrayRequest(urlUser,
                                    new Response.Listener<JSONArray>() {
                                        @Override
                                        public void onResponse(JSONArray response) {

                                            if (response.length() > 0)
                                            {
                                                try
                                                {
                                                    response.getJSONObject(0).getInt("customer_id");

                                                    final EditText txtMyComment = (EditText) dialog.findViewById(R.id.txtMyComment);
                                                    if (CampoVacio(txtMyComment) == true) {
                                                        txtMyComment.requestFocus();
                                                        InputMethodManager IMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                        IMM.showSoftInput(txtMyComment, InputMethodManager.SHOW_IMPLICIT);

                                                        Toast.makeText(
                                                                actInfoProveedor.this,
                                                                "Debes escribir un comentario.",
                                                                Toast.LENGTH_LONG)
                                                                .show();
                                                        return;

                                                    }


                                                    RatingBar rateProveedor = (RatingBar) dialog.findViewById(R.id.rateBar);
                                                    if (rateProveedor.getRating() == 0) {
                                                        Toast.makeText(
                                                                actInfoProveedor.this,
                                                                "Danos tu calificación.",
                                                                Toast.LENGTH_LONG)
                                                                .show();
                                                        return;
                                                    }


                                                    //Obtenemos la instancia única de la cola de peticiones
                                                    requestQueue = RequestQueueSingleton.getRequestQueue(actInfoProveedor.this);

                                                    //URL para guardar datos
                                                    //http://app.towpod.net/ws_add_comment.php?idp=7&txt=Esto%20siempre%20es%20as%C3%AD&rate=2&email=danielirias@gmail.com
                                                    String url = "http://app.towpod.net/ws_add_comment.php?" +
                                                            "idp=" + prmIDProveedor +
                                                            "&txt=" + txtMyComment.getText() +
                                                            "&rate=" + rateProveedor.getRating() +
                                                            "&email=" + CursorDB.getString(1);

                                                    url = url.replaceAll(" ", "%20");

                                                    //Creamos la petición
                                                    JsonArrayRequest request = new JsonArrayRequest(url,
                                                            new Response.Listener<JSONArray>() {

                                                                @Override
                                                                public void onResponse(JSONArray response)
                                                                {


                                                                }
                                                            },
                                                            new Response.ErrorListener() {
                                                                @Override
                                                                public void onErrorResponse(VolleyError error)
                                                                {
                                                                    dialog.dismiss();
                                                                    //==========================================================================================
                                                                    //=======CARGA DE COMENTARIOS DE LOS USUARIOS===============================================
                                                                    //==========================================================================================

                                                                    //Instancia del ListView
                                                                    ListaComentarios = (ListView) findViewById(R.id.ListViewComentarios);

                                                                    //Obtenemos la instancia única de la cola de peticiones
                                                                    requestQueue = RequestQueueSingleton.getRequestQueue(actInfoProveedor.this);

                                                                    //Inicializar el adaptador con la fuente de datos
                                                                    List<clsComentario> Comentarios = new ArrayList<clsComentario>();
                                                                    AdaptadorComentarios = new clsComentarioArrayAdapter(actInfoProveedor.this, Comentarios);
                                                                    AdaptadorComentarios.clear();

                                                                    //URL del detalle del proveedor
                                                                    String urlComment = "http://app.towpod.net/ws_get_comments.php?idp=" + prmIDProveedor;
                                                                    urlComment = urlComment.replaceAll(" ", "%20");

                                                                    JsonArrayRequest requestComment = new JsonArrayRequest(urlComment,
                                                                            new Response.Listener<JSONArray>() {
                                                                                @Override
                                                                                public void onResponse(JSONArray responseComment) {
                                                                                    for (int i = 0; i < responseComment.length(); i++) {
                                                                                        try {
                                                                                            AdaptadorComentarios.add(new clsComentario(
                                                                                                    responseComment.getJSONObject(i).getString("comment_id"),
                                                                                                    responseComment.getJSONObject(i).getString("customer_name"),
                                                                                                    responseComment.getJSONObject(i).getInt("comment_rate"),
                                                                                                    responseComment.getJSONObject(i).getString("comment_text"),
                                                                                                    responseComment.getJSONObject(i).getString("comment_date")));
                                                                                        } catch (JSONException e) {
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
                                                                    AdaptadorComentarios = new clsComentarioArrayAdapter(actInfoProveedor.this, Comentarios);

                                                                    //Relacionando la lista con el adaptador
                                                                    ListaComentarios.setAdapter(AdaptadorComentarios);

                                                                    //==========================================================================================
                                                                    //=======FIN DE CARGA DE COMENTARIOS DE LOS USUARIOS========================================
                                                                    //==========================================================================================

                                                                }
                                                            }
                                                    );

                                                    //Le ponemos un tag que servirá para identificarla si la queremos cancelar
                                                    request.setTag(JSON_REQUEST);
                                                    //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe
                                                    //un objeto de la clase padre
                                                    request = (JsonArrayRequest) setRetryPolicy(request);
                                                    //Iniciamos la petición añadiéndola a la cola
                                                    requestQueue.add(request);

                                                    final AlertDialog.Builder builder = new AlertDialog.Builder(actInfoProveedor.this);
                                                    //LayoutInflater inflater = actInfoProveedor.this.getLayoutInflater();
                                                    //builder.setView(inflater.inflate(R.layout.dialog_set_policy, null));

                                                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener()
                                                    {
                                                        public void onClick(DialogInterface dialog, int id) {

                                                        }
                                                    });

                                                    AlertDialog alertDialog = builder.create();
                                                    alertDialog.setTitle("¡Muchas gracias!");
                                                    alertDialog.setMessage("Tu comentario ha sido recibido.");
                                                    alertDialog.show();

                                                }
                                                catch (JSONException e)
                                                {

                                                    Toast popUpError = Toast.makeText(actInfoProveedor.this, e.getMessage(), Toast.LENGTH_LONG);
                                                    popUpError.show();
                                                }
                                            }
                                            else
                                            {
                                                //Si NO EXISTE LOCALMENTE NI EN LA WEB lo envio al Login para que inicie sesion o se registre
                                                Intent actMainLogin = new Intent(actInfoProveedor.this, actMainLogin.class);
                                                startActivity(actMainLogin);
                                                return;
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            //Si recibimos un error, mostraremos la causa

                                        }
                                    }
                            );

                            //Le ponemos un tag que servirá para identificarla si la queremos cancelar
                            requestUser.setTag(JSON_REQUEST);
                            //Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
                            requestUser = (JsonArrayRequest) setRetryPolicy(requestUser);
                            //Iniciamos la petición añadiéndola a la cola
                            requestQueue.add(requestUser);
                        }
                    }
                    catch (Exception e)
                    {
                        Toast popUpError = Toast.makeText(actInfoProveedor.this,  e.getMessage(), Toast.LENGTH_LONG);
                        popUpError.show();
                    }

                    //Cerramos la base de datos
                    db.close();

                }
            }
            catch (Exception e)
            {

                return;
            }
        }


    }

    private boolean CampoVacio(EditText myEditText) {
        return myEditText.getText().toString().trim().length() == 0;
    }

    private boolean CadenaVacia(String MyString)
    {
        return MyString.toString().trim().length() == 0;
    }

    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        }
        return account;
    }

    private class ProcesoSegundoPlano extends AsyncTask<Void, Void, Void> {

        ProgressDialog progress;
        actInfoProveedor act;

        public ProcesoSegundoPlano(ProgressDialog progress, actInfoProveedor act) {
            this.progress = progress;
            this.act = act;
        }

        public void onPreExecute() {
            progress.show();
            //aquí se puede colocar código a ejecutarse previo
            //a la operación
        }

        public void onPostExecute(Void unused) {
            //progress.dismiss();
            //aquí se puede colocar código que
            //se ejecutará tras finalizar el proceso y la espera adicional de 3 segundos
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // acciones que se ejecutan tras los milisegundos
                    progress.dismiss();
                }
            }, 1500);


        }

        protected Void doInBackground(Void... params) {
            //realizar la operación aquí
            PresentarInfoProveedor();
            return null;
        }

    }
}
