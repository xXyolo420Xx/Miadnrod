package alarma.myenergymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


// Esta es la Activity que se iniciara al abrir la aplicación
public class RedirectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences datos = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editorDatos = datos.edit();

        String usuario = datos.getString("usuario_logeado", null);


        // Si el usuario esta logeado, al abrir la aplicación abrimos el MainActivity, sino, el login.
        if(usuario == null){
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }





    }
}
