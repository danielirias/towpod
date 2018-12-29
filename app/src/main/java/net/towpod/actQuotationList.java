package net.towpod;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class actQuotationList extends AppCompatActivity {

	public ExpandableListAdapter listAdapter;
	public ExpandableListView ListViewQuotation;

	public LinkedHashMap<String, ExpandableGroupInfo> subjects = new LinkedHashMap<String, ExpandableGroupInfo>();
	public ArrayList<ExpandableGroupInfo> GroupList = new ArrayList<ExpandableGroupInfo>();

	private RequestQueue requestQueue;
	private final static int JSON_REQUEST = 0;

	public String[] providerPhone = new String[3];

	public Integer nPhones;
	public String strNumberToDial = "";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_quotation_list);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		/*
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});
		*/

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);


		//Realizo el proceo en segundo plano y muestro el cuadro de espera
		ProgressDialog VentanaEspera = new ProgressDialog(this);
		VentanaEspera.setTitle("Obteniendo datos");
		VentanaEspera.setMessage("Espere un momento, por favor...");
		new actQuotationList.CargarListaEnSegundoPlano(VentanaEspera, this).execute();

	}

	//method to expand all groups
	private void expandAll()
	{
		int count = listAdapter.getGroupCount();
		for (int i = 0; i < count; i++){
			//ListViewQuotation.expandGroup(i);
		}
	}

	//method to collapse all groups
	private void collapseAll()
	{
		int count = listAdapter.getGroupCount();
		for (int i = 0; i < count; i++){
			ListViewQuotation.collapseGroup(i);
		}
	}

	private class CargarListaEnSegundoPlano extends AsyncTask<Void, Void, Void>
	{

		ProgressDialog progress;
		actQuotationList act;

		public CargarListaEnSegundoPlano(ProgressDialog progress, actQuotationList act) {
			this.progress = progress;
			this.act = act;
		}

		public void onPreExecute() {
			progress.show();
			//aquí se puede colocar código a ejecutarse previo
			//a la operación
		}

		public void onPostExecute(Void unused) {
			//aquí se puede colocar código que
			//se ejecutará tras finalizar el proceso y la espera adicional de 3 segundos
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					// acciones que se ejecutan tras los milisegundos
					progress.dismiss();
				}
			}, 3000);

		}

		protected Void doInBackground(Void... params) {
			//realizar la operación aquí
			CargarHistorial();
			return null;
		}

	}

	public void CargarHistorial()
	{

		try
		{
			//Abrimos la base de datos 'DBUsuarios' en modo escritura
			cls_SQL_MANAGER usdbh = new cls_SQL_MANAGER(actQuotationList.this, "USUARIOSHM", null, 2);
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
						Intent actMainLogin = new Intent(actQuotationList.this, actMainLogin.class);
						startActivity(actMainLogin);
						return;
					}
					else
					{

						//Si el USUARIO EXISTE LOCALMENTE voy a buscarlo al servidor web
						//Busco la direccion de correo entre los usarios registrados
						//Obtenemos la instancia única de la cola de peticiones
						requestQueue = RequestQueueSingleton.getRequestQueue(this);

						String url = "http://app.towpod.net/ws_get_part_request_history.php?usr_id=" + CursorDB.getString(0);
						url = url.replaceAll(" ", "%20");
						Log.i("TP_TAG", url);

						//AddItem(1001,"2018-10-08","Bujías","AutoRepuestos","Garantía de un año", 750.0,0.0,750.0);
						//AddItem(1002,"2018-10-08","Bujías","AutoRepuestos","Garantía de un año", 920.0,70.0,850.0);
						//AddItem(1003,"2018-10-08","Balineras","AutoRepuestos","Garantía de tres años", 1350.0,2000.0,1150.0);

						JsonArrayRequest request = new JsonArrayRequest(url,
								new Response.Listener<JSONArray>() {
									@Override
									public void onResponse(JSONArray response) {
										if (response.length() > 0)
										{
											for (int i = 0; i < response.length(); i++){
												try
												{
													response.getJSONObject(i).getInt("row_id");

													//listQuotation.add(response.getJSONObject(i).getString("request_time"));
													AddItem(response.getJSONObject(i).getInt("request_id"),response.getJSONObject(i).getString("request_time"), response.getJSONObject(i).getString("part_description"), response.getJSONObject(i).getInt("provider_id"),response.getJSONObject(i).getString("provider_name"), response.getJSONObject(i).getString("comment"), response.getJSONObject(i).getDouble("price"), response.getJSONObject(i).getDouble("discount"), response.getJSONObject(i).getDouble("total"), response.getJSONObject(i).getString("provider_address"), response.getJSONObject(i).getString("provider_city"), response.getJSONObject(i).getString("provider_phone"), response.getJSONObject(i).getString("provider_phone2"));
												}
												catch (JSONException e)
												{
													Toast popUpError = Toast.makeText(actQuotationList.this, e.toString() , Toast.LENGTH_LONG);
													popUpError.show();
												}
											}
											//get reference of the ExpandableListView
											ListViewQuotation = (ExpandableListView) findViewById(R.id.ListViewQuotation);
											// create the adapter by passing your ArrayList data
											listAdapter = new ExpandableListAdapter(actQuotationList.this, GroupList);
											// attach the adapter to the expandable list view
											ListViewQuotation.setAdapter(listAdapter);

											// setOnGroupClickListener listener for group heading click
											ListViewQuotation.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
												@Override
												public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
													//get the group header
													ExpandableGroupInfo headerInfo = GroupList.get(groupPosition);
													//display it or do something with it
													//Toast.makeText(getBaseContext(), "ID Request: " + headerInfo.getIdRequest(), Toast.LENGTH_LONG).show();
													return false;
												}
											});

											// setOnChildClickListener listener for child row click
											ListViewQuotation.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
												@Override
												public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
													//get the group header
													ExpandableGroupInfo headerInfo = GroupList.get(groupPosition);
													//get the child info
													ExpandableChildInfo detailInfo =  headerInfo.getItemList().get(childPosition);
													//display it or do something with it
													//Toast.makeText(getBaseContext(), "Provider: " + detailInfo.getProviderID()+", "+ detailInfo.getProviderName(), Toast.LENGTH_LONG).show();

													providerPhone[0] = detailInfo.getProviderPhone1();
													providerPhone[1] = detailInfo.getProviderPhone2();
													providerPhone[2] = "";
													DialogMostrarFormasDeContacto(detailInfo.getProviderName(), detailInfo.getProviderAddress(), detailInfo.getProviderCity());

													return false;
												}
											});
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
						request.setTag(JSON_REQUEST);
						//Al ponerle la política de reintento, le hacemos casting, ya que el metodo recibe un objeto de la clase padre
						request = (JsonArrayRequest) setRetryPolicy(request);
						//Iniciamos la petición añadiéndola a la cola
						requestQueue.add(request);

					}
				}
				catch (Exception e)
				{
					Toast popUpError = Toast.makeText(actQuotationList.this,  e.getMessage(), Toast.LENGTH_LONG);
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

	public Request setRetryPolicy(Request request)
	{
		return request.setRetryPolicy(new DefaultRetryPolicy(1000, 4, 1));
	}

	//load some initial data into out list
	private void loadData()
	{

	}

	//here we maintain our products in various departments
	public int AddItem(Integer intIdRequest, String DateRequest, String PartDescription, Integer ProviderID, String ProviderName, String ProviderComment, Double Price, Double Discount, Double Total, String ProviderAddress, String ProviderCity, String ProviderPhone1, String ProviderPhone2)
	{

		int groupPosition = 0;

		//check the hash map if the group already exists
		ExpandableGroupInfo headerInfo = subjects.get(String.valueOf(intIdRequest)); //Para agrupar
		//add the group if doesn't exists
		if(headerInfo == null)
		{
			headerInfo = new ExpandableGroupInfo();

			headerInfo.setDescription(PartDescription);
			headerInfo.setIdRequest(intIdRequest);
			headerInfo.setDateRequest(DateRequest);

			subjects.put(String.valueOf(intIdRequest), headerInfo); //Para agrupar
			GroupList.add(headerInfo);
		}

		//get the children for the group
		ArrayList<ExpandableChildInfo> ItemList = headerInfo.getItemList();
		//size of the children list
		int listSize = ItemList.size();
		//add to the counter
		listSize++;

		//create a new child and add that to the group
		ExpandableChildInfo ProviderInfo = new ExpandableChildInfo();

		ProviderInfo.setSequence(String.valueOf(listSize));
		ProviderInfo.setProviderID(ProviderID);
		ProviderInfo.setProviderName(ProviderName);
		ProviderInfo.setProviderAddress(ProviderAddress);
		ProviderInfo.setProviderCity(ProviderCity);
		ProviderInfo.setProviderPhone1(ProviderPhone1);
		ProviderInfo.setProviderPhone2(ProviderPhone2);
		ProviderInfo.setComment(ProviderComment);
		ProviderInfo.setPrice(Price);
		ProviderInfo.setDiscount(Discount);
		ProviderInfo.setTotal(Total);

		ItemList.add(ProviderInfo);
		headerInfo.setItemList(ItemList);

		//find the group position inside the list
		groupPosition = GroupList.indexOf(headerInfo);
		return groupPosition;
	}

	public void DialogMostrarFormasDeContacto(String strProviderName, String strProviderAddress, String strProviderCity)
	{

		nPhones = 1;

		try
		{
			if (CadenaVacia(providerPhone[0].toString()) == true) {
				Toast popUpError = Toast.makeText(actQuotationList.this, "El número telefónico no está disponible.", Toast.LENGTH_SHORT);
				popUpError.show();
				return;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(actQuotationList.this);
			LayoutInflater inflater = actQuotationList.this.getLayoutInflater();
			builder.setView(inflater.inflate(R.layout.dialog_show_info_make_call, null));


			if (CadenaVacia(providerPhone[1].toString()) == false) {
				nPhones = nPhones + 1;
			}

			if (CadenaVacia(providerPhone[2].toString()) == false) {
				nPhones = nPhones + 1;
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
                Toast popUp = Toast.makeText(actDestination.this, "Has marcado el número: " + items[wich].toString(), Toast.LENGTH_SHORT);
                //popUp.show();
                strNumberToDial = items[wich].toString();
            }
        });
        */

			// Add the buttons
			builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Toast popUp = Toast.makeText(actQuotationList.this, "Has cancelado tu llamada", Toast.LENGTH_SHORT);
					//popUp.show();
				}
			});

        /*
        builder.setPositiveButton("Llamar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast popUp = Toast.makeText(actDestination.this, "Llamando", Toast.LENGTH_SHORT);
                //popUp.show();
                //Intent callIntent = new Intent(Intent.ACTION_DIAL); //Escribe el número telefónico pero no lo marca
                Intent callIntent = new Intent(Intent.ACTION_CALL); //Marca el número inmediatamente
                callIntent.setData(Uri.parse("tel:" + strNumberToDial));
                if (ActivityCompat.checkSelfPermission(actDestination.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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

			TextView lblProvider = (TextView) dialog.findViewById(R.id.lblProvider);
			lblProvider.setText(strProviderName);

			TextView lblAddress = (TextView) dialog.findViewById(R.id.lblAddress);
			lblAddress.setText(strProviderAddress+"\n"+strProviderCity);

			GridView ListViewPhones = (GridView) dialog.findViewById(R.id.ListViewPhones);
			ListViewPhones.setAdapter(AdaptadorPhones);
			//Creo el evento Clic para cada objeto de la lista
			ListViewPhones.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> pariente, View view, int posicion, long id) {
					clsPhones itemSeleccionado = (clsPhones) pariente.getItemAtPosition(posicion);
					if (posicion < 2){
						Intent callIntent = new Intent(Intent.ACTION_CALL); //Marca el número inmediatamente
						callIntent.setData(Uri.parse("tel:"+itemSeleccionado.get_phone_number()));
						if (ActivityCompat.checkSelfPermission(actQuotationList.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
					else
					{
						//================
						Intent _intencion = new Intent("android.intent.action.MAIN");
						_intencion.setComponent(new ComponentName("com.whatsapp","com.whatsapp.Conversation"));
						_intencion.putExtra("jid", providerPhone[2]+"@s.whatsapp.net");
						startActivity(_intencion);
					}

				}
			});
		}
		catch (Exception e)
		{
			Toast popUp = Toast.makeText(actQuotationList.this, e.getMessage(), Toast.LENGTH_SHORT);
			popUp.show();
		}

	}

	private boolean CadenaVacia(String MyString)
	{
		return MyString.toString().trim().length() == 0;
	}



}
