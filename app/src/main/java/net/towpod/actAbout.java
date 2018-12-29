package net.towpod;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class actAbout extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_about);

    }

    public void GoToWebSite (View v)
    {
        Intent navegador = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.danielirias.com/resumen/"));
        startActivity(navegador);
    }
}
