package alarma.myenergymap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Cuenta extends AppCompatActivity {

    Context context;
    TextView txtCuenta;
    Button exit;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    String usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);

        // Cogemos preferencias
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();

        // Contexto
        context = this;

        // Usuario
        usuario = pref.getString("usuario_logeado", null);

        // Cogemos el TextView y el botón de desvincular
        txtCuenta = (TextView) findViewById(R.id.txtCuenta);
        exit = (Button) findViewById(R.id.exit);

        // Mensaje del usuario logeado
        if (usuario != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                txtCuenta.setText(Html.fromHtml("Cuenta vinculada a <big><strong>" + usuario+"</strong></big>", Html.FROM_HTML_MODE_COMPACT));
            }else{
                txtCuenta.setText(Html.fromHtml("Cuenta vinculada a <big><strong>" + usuario+"</strong></big>"));
            }
        }

        // Listener del botón de salir
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView message = new TextView(context);
                message.setText(R.string.usuario_salir);
                message.setTextSize(18);
                message.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                message.setGravity(View.TEXT_ALIGNMENT_CENTER);
                message.setPadding(20,20,20,20);

                new AlertDialog.Builder(context)
                        .setView(message)
                        .setIcon(android.R.mipmap.sym_def_app_icon)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                                // Si el usuario acepta y cierra sesión, lo borramos, pero sus preferencias y alarmas siguen guardadas
                                editor.putString("usuario_logeado", null);
                                editor.apply();

                                // Volvemos a abrir la ventana Login
                                Intent intent = new Intent(context, Login.class);
                                startActivity(intent);
                                Toast.makeText(Cuenta.this, "Sesión cerrada", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });


    }
}
