package net.towpod;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class actPaypalPayment extends AppCompatActivity {

	private Integer intTotalPrice;
	private static final int PAYPAL_REQUEST_CODE = 7171;

	//Para usar el Sandbox y testear
	private static PayPalConfiguration config = new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX).clientId(PAYPAL_CONFIG.PAYPAL_APP_ID);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_paypal_payment);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);


		//Iniciar servicio Paypal
		Intent thisService = new Intent(actPaypalPayment.this, PayPalService.class);
		thisService.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
		startService(thisService);



		TextView lblTotalPrice = (TextView) findViewById(R.id.lblTotalPrice);

		intTotalPrice = 4;

		Button btnPayNow = (Button) findViewById(R.id.btnPayNow);
		btnPayNow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view)
			{
				PayPalPayment thisPayment = new PayPalPayment(new BigDecimal(String.valueOf("4.57")),"USD", "Pago por servicio de grúa", PayPalPayment.PAYMENT_INTENT_SALE);

				//Enviar parametros a Paypal e iniciar su servicio
				Intent paypalActivity = new Intent(actPaypalPayment.this, PaymentActivity.class);
				paypalActivity.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
				paypalActivity.putExtra(PaymentActivity.EXTRA_PAYMENT, thisPayment);
				startActivityForResult(paypalActivity, PAYPAL_REQUEST_CODE);
			}
		});
	}

	@Override
	protected void onDestroy(){
		stopService(new Intent(this, PayPalService.class));
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == PAYPAL_REQUEST_CODE)
		{
			if(resultCode == RESULT_OK)
			{
				PaymentConfirmation thisConfirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

				if(thisConfirmation != null)
				{
					JSONObject paymentDetail = thisConfirmation.toJSONObject();
					try {
						Toast.makeText(this, paymentDetail.getString("id")+"\n"+paymentDetail.getString("status")+"\n"+intTotalPrice+"\n"+paymentDetail.getString("status"), Toast.LENGTH_SHORT);
					} catch (JSONException e) {
						Log.i("TP_TAG", e.getMessage());
					}

				}
			}
			else if(resultCode == Activity.RESULT_CANCELED)
			{
				Toast.makeText(this,"Proceso de pago cancelado por el usuario", Toast.LENGTH_SHORT);
			}
		}

		//super.onActivityResult(requestCode,  resultCode,  data);
	}
}
