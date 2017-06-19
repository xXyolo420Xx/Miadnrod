package alarma.myenergymap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class Login extends AppCompatActivity {
    private static final String urlServidor = "https://www.myenergymap.com/energia/appAndroid/login.php";
    //private static final String urlServidor = "http://192.168.0.163/login.php";

    Context context;
    EditText txtUser;
    EditText txtPass;
    TextView txtError;
    Button btnEntrar;
    String usuario;
    String pass;
    BufferedReader br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Cogemos los campos y botones
        txtUser = (EditText) findViewById(R.id.txtUser);
        txtPass = (EditText) findViewById(R.id.pass);
        txtError = (TextView) findViewById(R.id.error);
        btnEntrar = (Button) findViewById(R.id.btnEntrar);

        // El contexto
        context = this;


        // Listener del botón entrar
        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Cogemos los datos introducidos
                usuario = txtUser.getText().toString();
                pass = txtPass.getText().toString();

                // Para comprobar los datos mediante PHP hay que ejecutar otro hilo
                AsyncConnection a = new AsyncConnection();
                a.execute();

            }
        });

    }

    // Hilo para comprobar el login
    private class AsyncConnection extends AsyncTask<Object, Object, String> {

        @Override
        protected String doInBackground(Object... params) {
            try {

                // Establecemos conexión
                URL url = new URL(urlServidor);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");

                String token = FirebaseInstanceId.getInstance().getToken();

                // Establecemos las variables que vamos a enviar al servidor
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write("user=" + usuario);
                writer.write("&pass=" + pass);
                writer.write("&token=" + token);
                writer.close();

                // Si no hay error y obtenemos respuesta del servidor:
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();

                    String line = null;
                    while ((line = br.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    // String de la respuesta del servidor
                    String respuesta = stringBuilder.toString();

                    return respuesta;

                } else {
                    String respuesta;
                    respuesta = "Error";
                    return respuesta;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String respuesta) {
            if (respuesta == null) {

                // Si no obtenemos respuesta
                txtError.setText("Error de conexión");

            } else {

                // Si algo falla, imprimimos por pantalla la respuseta de servidor
                if (!respuesta.equals("1")) {
                    txtError.setText(respuesta);
                } else {

                    // Si todo esta correcto guardamos el inicio de sesión del usuario
                    txtError.setText("");

                    SharedPreferences datos = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editorDatos = datos.edit();

                    editorDatos.putString("usuario_logeado", usuario);
                    editorDatos.apply();

                    // Iniciamos la MainActivity
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);

                    // Cerramos la Activity de Login
                    finish();
                }
            }
        }
    }
}
