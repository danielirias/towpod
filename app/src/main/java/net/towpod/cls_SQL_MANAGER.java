package net.towpod;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by WebMaster on 1/30/17.
 */

public class cls_SQL_MANAGER extends SQLiteOpenHelper {

    public cls_SQL_MANAGER(Context contexto, String nombre, SQLiteDatabase.CursorFactory factory, int version)
    {
        //1. Si la base de datos ya existe y su versión actual coincide con la solicitada simplemente se realizará la conexión con ella.
        //2. Si la base de datos existe pero su versión actual es anterior a la solicitada, se llamará automáticamente al método onUpgrade() para convertir la base de datos a la nueva versión y se conectará con la base de datos convertida.
        //3. Si la base de datos no existe, se llamará automáticamente al método onCreate() para crearla y se conectará con la base de datos creada.

        super(contexto, nombre, factory, version);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        // Comandos SQL
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS USUARIO (row_id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, user_name TEXT, user_email TEXT, user_password TEXT, user_keep_session INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // No hay operaciones
    }
}
