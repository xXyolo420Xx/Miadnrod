package alarma.myenergymap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import static android.text.Html.FROM_HTML_MODE_COMPACT;

public class MainActivity extends AppCompatActivity {

    private Context context;
    LinearLayout layout;
    private String filtroTipo;
    private String filtroPrioridad;
    private String filtroEmpresa;
    private String filtroSede;

    private final String TAG = "EMPRESASEDE";
    private JSONObject jsonEmpresaSede;
    private ArrayAdapter<String> adapterEmpresa;
    private ArrayAdapter<String> adapterSede;
    private String usuario;
    private String prefTipo;
    private String prefPrioridad;
    private String prefEmpresa;
    private String prefSede;
    private String prefUmbral;
    private String prefOEE;
    private String prefComunicacion;
    private String prefExceso;
    private String prefGas;
    private Spinner selectTipo;
    private Spinner selectPrioridad;
    private Spinner selectEmpresa;
    private Spinner selectSede;
    private JSONArray jsonArray;
    private SharedPreferences datos;
    private SharedPreferences.Editor editor;
    private int alertasImprimidas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // En onCreate ponemos la toolbar

        context = this;
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(t);

        layout = (LinearLayout) findViewById(R.id.layout);

        FirebaseMessaging.getInstance().subscribeToTopic("MyEnergyMap");
        FirebaseInstanceId.getInstance().getToken();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflamos el menu de la toolbar
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {

        //Opciones del menu, cogemos el id del botón del menu que se haya pulsado y según cual se ejecuta una función

        if (menu != null) {

            int menuItemId = 0;
            try {
                menuItemId = menu.getItemId();
            } catch (Exception ignored) {
            }

            if (menuItemId == R.id.borrar) {

                TextView message = new TextView(this);
                message.setText(R.string.borrar_las_alarmas);
                message.setTextSize(18);
                message.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    message.setGravity(View.TEXT_ALIGNMENT_CENTER);
                }
                message.setPadding(20, 20, 20, 20);

                AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
                        .setView(message)
                        .setIcon(android.R.mipmap.sym_def_app_icon)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(MainActivity.this, "Alarmas borradas", Toast.LENGTH_LONG).show();
                                String usuario = datos.getString("usuario_logeado", null);
                                editor.putString(usuario, null);
                                editor.apply();
                                pintaAlarmas();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();

                TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                if (textView != null) {
                    textView.setTextSize(18);
                }


            }

            if (menuItemId == R.id.actualizar) {
                pintaAlarmas();
            }

            if (menuItemId == R.id.ajustes) {
                Intent i = new Intent(this, Ajustes.class);
                startActivity(i);
            }

            if (menuItemId == R.id.cuenta) {
                Intent i = new Intent(this, Cuenta.class);

                startActivity(i);
            }
        }

        return true;
    }


    public void onResume() {
        super.onResume();

        // Al resumir la app primero borramos todos los views de alarmas para voverlos a imprimir
        layout.removeAllViews();


        // Guardamos referencia a las sharedPreferences y al usuario
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = pref.edit();
        final String usuario = pref.getString("usuario_logeado", null);

        //Filtros

        // Capturamos los spinners de cada filtro
        selectTipo = (Spinner) findViewById(R.id.selectTipoAlarma);
        selectPrioridad = (Spinner) findViewById(R.id.selectPrioridad);
        selectEmpresa = (Spinner) findViewById(R.id.selectEmpresa);
        selectSede = (Spinner) findViewById(R.id.selectSede);
        context = this;

        if (usuario != null) {

            // Capturamos los filtros anteriores del usuario
            prefTipo = pref.getString(usuario + "_filtroTipo", "Mostrar todo");
            prefPrioridad = pref.getString(usuario + "_filtroPrioridad", "Mostrar todo");
            prefEmpresa = pref.getString(usuario + "_filtroEmpresa", "Mostrar todo");
            prefSede = pref.getString(usuario + "_filtroSede", "Mostrar todo");

            prefUmbral = pref.getString(usuario + "_prefUmbral", "Por defecto");
            prefOEE = pref.getString(usuario + "_prefOEE", "Por defecto");
            prefComunicacion = pref.getString(usuario + "_prefComunicacion", "Por defecto");
            prefExceso = pref.getString(usuario + "_prefExceso", "Por defecto");
            prefGas = pref.getString(usuario + "_prefGas", "Por defecto");


            // Capturamos los filtros de empresa y sede del usuario
            String empresasede = pref.getString(usuario + "_empresasede", null);


            // Los tipos de alarmas
            ArrayList<String> arrayTipos = new ArrayList<>();

            arrayTipos.add("Mostrar todo");
            arrayTipos.add("Umbral");
            arrayTipos.add("OEE");
            arrayTipos.add("Comunicación");
            arrayTipos.add("Exceso potencia");
            arrayTipos.add("Predicción gas");

            // Los convertimos a ArrayAdapter para poder insertarlos en el spinner
            ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this, R.layout.spinner, arrayTipos);

            selectTipo.setAdapter(adapterTipo);

            // Listener del filtro de tipo
            selectTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    String item = (String) parent.getItemAtPosition(position);
                    editor.putString(usuario + "_filtroTipo", item);
                    editor.apply();
                    pintaAlarmas();

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            // Filtro prioridad
            ArrayList<String> arayPrioridad = new ArrayList<>();

            arayPrioridad.add("Mostrar todo");
            arayPrioridad.add("Alta");
            arayPrioridad.add("Media");
            arayPrioridad.add("Baja");


            // Los convertimos a ArrayAdapter para poder insertarlos en el spinner
            ArrayAdapter<String> adapterPrioridad = new ArrayAdapter<>(this, R.layout.spinner, arayPrioridad);
            selectPrioridad.setAdapter(adapterPrioridad);

            // Listener del filtro de prioridad
            selectPrioridad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    String item = (String) parent.getItemAtPosition(position);
                    editor.putString(usuario + "_filtroPrioridad", item);
                    editor.apply();
                    pintaAlarmas();


                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            // Si el usuario tiene datos del filtro de empresa y sede:
            if (empresasede != null) {

                try {
                    jsonEmpresaSede = new JSONObject(empresasede);

                    ArrayList<String> empresa = new ArrayList<>();
                    empresa.add("Mostrar todo");

                    for (Iterator<String> iter = jsonEmpresaSede.keys(); iter.hasNext(); ) {

                        String key = iter.next();
                        JSONArray sedes = (JSONArray) jsonEmpresaSede.get(key);
                        empresa.add(key);
                        ArrayList<String> arraySede = new ArrayList<>();

                        for (int i = 0; i < sedes.length(); i++) {

                            String sede = (String) sedes.get(i);
                            arraySede.add((String) sedes.get(i));

                        }

                        adapterEmpresa = new ArrayAdapter<>(this, R.layout.spinner, empresa);
                        selectEmpresa.setAdapter(adapterEmpresa);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                // Listener del filtro de empresa
                selectEmpresa.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String item = (String) parent.getItemAtPosition(position);

                        try {
                            editor.putString(usuario + "_filtroEmpresa", item);
                            editor.apply();

                            if (item.equals("Mostrar todo")) {

                                editor.putString(usuario + "_filtroSede", "Mostrar todo");
                                editor.apply();

                                try {
                                    ArrayList<String> sedesNull = new ArrayList<String>();

                                    ArrayAdapter adapterNull = new ArrayAdapter<String>(context, R.layout.spinner, sedesNull);

                                    selectSede.setAdapter(adapterNull);

                                } catch (Exception ignored) {
                                    Log.d("ERROR", "ERROR");
                                }

                            } else {
                                JSONArray sedes = (JSONArray) jsonEmpresaSede.get(item);

                                ArrayList<String> arraySedes = new ArrayList<String>();
                                arraySedes.add("Mostrar todo");


                                for (int i = 0; i < sedes.length(); i++) {
                                    arraySedes.add(sedes.getString(i));
                                }

                                adapterSede = new ArrayAdapter<String>(context, R.layout.spinner, arraySedes);

                                selectSede.setAdapter(adapterSede);

                            }

                            pintaAlarmas();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                // Listener del filtro de sede
                selectSede.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String item = (String) parent.getItemAtPosition(position);

                        editor.putString(usuario + "_filtroSede", item);

                        editor.apply();

                        pintaAlarmas();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

            }


            // Para cada filtro al resumir la aplicación le ponemos las preferencias del usuario
            selectTipo.setSelection(getIndex(selectTipo, prefTipo));
            selectPrioridad.setSelection(getIndex(selectPrioridad, prefPrioridad));
            selectEmpresa.setSelection(getIndex(selectEmpresa, prefEmpresa));

            // Como el filtro sede depende del de empresa (es dinámico) añadimos un timer de 100ms para dejar que pueda cargar primero la empresa
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            selectSede.setSelection(getIndex(selectSede, prefSede));
                        }
                    },
                    100);
        }

        // Por último llamamos a la función pintaAlarmas que plasmará todo en la pantalla.
        pintaAlarmas();
    }

    //FinFiltros

    // Función que pinta las alarmas en la pantalla
    public void pintaAlarmas() {
        layout.removeAllViews();

        // Variables de las preferencias
        datos = PreferenceManager.getDefaultSharedPreferences(this);
        editor = datos.edit();

        // Variable para saber si hay alertas imprimidas en pantalla
        alertasImprimidas = 0;

        // Usuario
        usuario = datos.getString("usuario_logeado", null);

        // Preferencias del usuario
        if (usuario != null) {
            filtroTipo = datos.getString(usuario + "_filtroTipo", "Mostrar todo");
            filtroPrioridad = datos.getString(usuario + "_filtroPrioridad", "Mostrar todo");
            filtroEmpresa = datos.getString(usuario + "_filtroEmpresa", "Mostrar todo");
            filtroSede = datos.getString(usuario + "_filtroSede", "Mostrar todo");
        }

        // El string (json) de las alarmas del usuario se guarda en las shared preferences con la clave del nombre de usuario
        jsonArray = null;

        // JSON de las alarmas del usuario
        String json = datos.getString(usuario, null);

        // En caso de que tenga alarmas creamos un nuevo JSONArray con sus alarmas, sino creamos un nuevo JSONArray vacío
        if (json != null) {
            try {
                jsonArray = new JSONArray(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            jsonArray = new JSONArray();
        }

        // Si sí que hay alarmas:
        try {
            if (jsonArray.length() >= 1) {

                // Parametros para el separador final
                LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
                separatorParams.setMargins(5, 50, 5, 50);

                // Parámetros para los TextView
                LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                textViewParams.setMargins(5, 5, 5, 15);
                textViewParams.weight = 1;

                // Para cada alarma:
                for (int i = jsonArray.length() - 1; i >= 0; i--) {

                    // Cada alarma es objetoAlarma
                    final JSONObject objetoAlarma = (JSONObject) jsonArray.get(i);

                    // Sacamos de la alarma el tipo, prioridad, empresa y sede para luego poder filtrar
                    String tipo = String.valueOf(objetoAlarma.get("tipoAlarma"));
                    String prioridad = String.valueOf(objetoAlarma.get("prioridad"));
                    String empresa = String.valueOf(objetoAlarma.get("empresa"));
                    String sede = String.valueOf(objetoAlarma.get("sede"));

                    // Si la alarma cumple los filtros del usuario se pinta:
                    if ((filtroTipo.equals(tipo) || filtroTipo.equals("Mostrar todo")) && (filtroPrioridad.equals(prioridad) || filtroPrioridad.equals("Mostrar todo")) &&
                            (filtroEmpresa.equals(empresa) || filtroEmpresa.equals("Mostrar todo")) &&
                            (filtroSede.equals(sede) || filtroSede.equals("Mostrar todo"))) {


                        // Creamos el TextView que desplegará la alarma
                        final TextView txtViewAlarma = new TextView(MainActivity.this);
                        txtViewAlarma.setLayoutParams(textViewParams);
                        txtViewAlarma.setPadding(19, 8, 8, 12);
                        txtViewAlarma.setTextSize(20);
                        txtViewAlarma.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                        txtViewAlarma.setLayoutParams(textViewParams);
                        txtViewAlarma.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

                        // Variables donde se guarda el nombre de la alarma y si esta leida o no
                        String nombre;
                        boolean leida;


                        // Según tipo de alarma:
                        switch (tipo) {
                            case "Umbral":

                                // Nombre con el que guardaremos la alarma
                                nombre = String.valueOf(objetoAlarma.get("nombreAlarma")) + "\n" +
                                        "Prioridad: " + String.valueOf(objetoAlarma.get("prioridad")) + "\n" +
                                        String.valueOf(objetoAlarma.get("fechaActivada")) + " - " +
                                        String.valueOf(objetoAlarma.get("horaActivada"));
                                txtViewAlarma.setText(nombre);

                                // Fondo de la alarma con el color de las preferencias del usuario
                                if (prefUmbral.equals("Por defecto")) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        GradientDrawable shape = new GradientDrawable();
                                        shape.setCornerRadius(8);
                                        shape.setColor(ContextCompat.getColor(this, R.color.colorUmbral));
                                        txtViewAlarma.setBackground(shape);
                                    } else {
                                        txtViewAlarma.setBackgroundColor(ContextCompat.getColor(this, R.color.colorUmbral));
                                    }
                                } else if (!prefUmbral.equals("blanco")) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        GradientDrawable shape = new GradientDrawable();
                                        shape.setCornerRadius(8);
                                        int fondo = getResources().getIdentifier(prefUmbral, "color", this.getPackageName());
                                        shape.setColor(ContextCompat.getColor(this, fondo));
                                        txtViewAlarma.setBackground(shape);
                                    } else {
                                        txtViewAlarma.setBackgroundResource(getResources().getIdentifier(prefUmbral, "color", this.getPackageName()));
                                    }
                                }

                                nombre = nombre.replace("\n", "");
                                leida = datos.getBoolean(usuario + "_alarmaLeida_" + nombre, false);

                                if (leida) {
                                    txtViewAlarma.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_open, 0, 0, 0);
                                } else {
                                    txtViewAlarma.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_closed, 0, 0, 0);
                                }

                                break;
                            case "OEE":

                                // Nombre con el que guardaremos la alarma
                                nombre = String.valueOf(objetoAlarma.get("nombreAlarma")) + "\n" +
                                        "Prioridad: " + String.valueOf(objetoAlarma.get("prioridad")) + "\n" +
                                        String.valueOf(objetoAlarma.get("fechaActivada")) + " - " +
                                        "Turno: " + String.valueOf(objetoAlarma.get("turno"));
                                txtViewAlarma.setText(nombre);


                                // Fondo de la alarma con el color de las preferencias del usuario
                                if (prefOEE.equals("Por defecto")) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        GradientDrawable shape = new GradientDrawable();
                                        shape.setCornerRadius(8);
                                        shape.setColor(ContextCompat.getColor(this, R.color.colorOEE));
                                        txtViewAlarma.setBackground(shape);
                                    } else {
                                        txtViewAlarma.setBackgroundColor(ContextCompat.getColor(this, R.color.colorOEE));
                                    }
                                } else if (!prefOEE.equals("blanco")) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        GradientDrawable shape = new GradientDrawable();
                                        shape.setCornerRadius(8);
                                        int fondo = getResources().getIdentifier(prefOEE, "color", this.getPackageName());
                                        shape.setColor(ContextCompat.getColor(this, fondo));
                                        txtViewAlarma.setBackground(shape);
                                    } else {
                                        txtViewAlarma.setBackgroundResource(getResources().getIdentifier(prefOEE, "color", this.getPackageName()));
                                    }
                                }

                                nombre = nombre.replace("\n", "");
                                leida = datos.getBoolean(usuario + "_alarmaLeida_" + nombre, false);

                                if (leida) {
                                    txtViewAlarma.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_open, 0, 0, 0);
                                } else {
                                    txtViewAlarma.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_closed, 0, 0, 0);
                                }

                                break;
                            case "Comunicación":

                                // Nombre con el que guardaremos la alarma
                                nombre = String.valueOf(objetoAlarma.get("nombreAlarma")) + "\n" +
                                        "Prioridad: " + String.valueOf(objetoAlarma.get("prioridad")) + "\n" +
                                        String.valueOf(objetoAlarma.get("fallo"));
                                txtViewAlarma.setText(nombre);


                                // Fondo de la alarma con el color de las preferencias del usuario
                                if (prefComunicacion.equals("Por defecto")) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        GradientDrawable shape = new GradientDrawable();
                                        shape.setCornerRadius(8);
                                        shape.setColor(ContextCompat.getColor(this, R.color.colorComunicacion));
                                        txtViewAlarma.setBackground(shape);
                                    } else {
                                        txtViewAlarma.setBackgroundColor(ContextCompat.getColor(this, R.color.colorComunicacion));
                                    }
                                } else if (!prefComunicacion.equals("blanco")) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        GradientDrawable shape = new GradientDrawable();
                                        shape.setCornerRadius(8);
                                        int fondo = getResources().getIdentifier(prefComunicacion, "color", this.getPackageName());
                                        shape.setColor(ContextCompat.getColor(this, fondo));
                                        txtViewAlarma.setBackground(shape);
                                    } else {
                                        txtViewAlarma.setBackgroundResource(getResources().getIdentifier(prefComunicacion, "color", this.getPackageName()));
                                    }
                                }

                                nombre = nombre.replace("\n", "");
                                leida = datos.getBoolean(usuario + "_alarmaLeida_" + nombre, false);

                                if (leida) {
                                    txtViewAlarma.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_open, 0, 0, 0);
                                } else {
                                    txtViewAlarma.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_closed, 0, 0, 0);
                                }
                                break;
                            case "Exceso potencia":

                                // Nombre con el que guardaremos la alarma
                                nombre = String.valueOf(objetoAlarma.get("nombreAlarma")) + "\n" +
                                        "Prioridad: " + String.valueOf(objetoAlarma.get("prioridad")) + "\n" +
                                        String.valueOf(objetoAlarma.get("fechaIncidencia"));
                                txtViewAlarma.setText(nombre);

                                // Fondo de la alarma con el color de las preferencias del usuario
                                if (prefExceso.equals("Por defecto")) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        GradientDrawable shape = new GradientDrawable();
                                        shape.setCornerRadius(8);
                                        shape.setColor(ContextCompat.getColor(this, R.color.colorExceso));
                                        txtViewAlarma.setBackground(shape);
                                    } else {
                                        txtViewAlarma.setBackgroundColor(ContextCompat.getColor(this, R.color.colorExceso));
                                    }
                                } else if (!prefExceso.equals("blanco")) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        GradientDrawable shape = new GradientDrawable();
                                        shape.setCornerRadius(8);
                                        int fondo = getResources().getIdentifier(prefExceso, "color", this.getPackageName());
                                        shape.setColor(ContextCompat.getColor(this, fondo));
                                        txtViewAlarma.setBackground(shape);
                                    } else {
                                        txtViewAlarma.setBackgroundResource(getResources().getIdentifier(prefExceso, "color", this.getPackageName()));
                                    }
                                }

                                // Para poder guardar si esta leida o no la alarma debemos quitarle al nombre los saltos de línea
                                nombre = nombre.replace("\n", "");

                                // Vemos si esta leida o no
                                leida = datos.getBoolean(usuario + "_alarmaLeida_" + nombre, false);


                                // Si esta leida sobre abierto, sino sobre cerrado
                                if (leida) {
                                    txtViewAlarma.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_open, 0, 0, 0);
                                } else {
                                    txtViewAlarma.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_closed, 0, 0, 0);
                                }

                                break;

                            case "Predicción gas":
                                // Nombre con el que guardaremos la alarma
                                nombre = String.valueOf(objetoAlarma.get("nombreAlarma")) + "\n" +
                                        "Prioridad: " + String.valueOf(objetoAlarma.get("prioridad")) + "\n" +
                                        String.valueOf(objetoAlarma.get("fechaIncidencia"));
                                txtViewAlarma.setText(nombre);

                                // Fondo de la alarma con el color de las preferencias del usuario
                                if (prefGas.equals("Por defecto")) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        GradientDrawable shape = new GradientDrawable();
                                        shape.setCornerRadius(8);
                                        shape.setColor(ContextCompat.getColor(this, R.color.colorGas));
                                        txtViewAlarma.setBackground(shape);
                                    } else {
                                        txtViewAlarma.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGas));
                                    }
                                } else if (!prefGas.equals("blanco")) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        GradientDrawable shape = new GradientDrawable();
                                        shape.setCornerRadius(8);
                                        int fondo = getResources().getIdentifier(prefGas, "color", this.getPackageName());
                                        shape.setColor(ContextCompat.getColor(this, fondo));
                                        txtViewAlarma.setBackground(shape);
                                    } else {
                                        txtViewAlarma.setBackgroundResource(getResources().getIdentifier(prefGas, "color", this.getPackageName()));
                                    }
                                }

                                nombre = nombre.replace("\n", "");
                                leida = datos.getBoolean(usuario + "_alarmaLeida_" + nombre, false);

                                if (leida) {
                                    txtViewAlarma.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_open, 0, 0, 0);
                                } else {
                                    txtViewAlarma.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_closed, 0, 0, 0);
                                }


                        }


                        // Aqui va el contenido dentro de cada alarma
                        // Lo metemos todo dentro de un container para luego poder alternar su visibilidad
                        final LinearLayout container = new LinearLayout(this);
                        container.setOrientation(LinearLayout.VERTICAL);

                        //Por lo tanto a la alarma le añadimos un listener que cambie de visible a invisibile y viceversa
                        txtViewAlarma.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (container.getVisibility() == View.GONE) {
                                    container.setVisibility(View.VISIBLE);
                                } else {
                                    container.setVisibility(View.GONE);
                                }

                                // El nombre de la alarma al dar click quitándole los saltos de linea para poder guardar
                                String nombre = (String) txtViewAlarma.getText();
                                nombre = nombre.replace("\n", "");

                                // Al abrirla la damos por leida
                                editor.putBoolean(usuario + "_alarmaLeida_" + nombre, true);
                                editor.apply();

                                // Ponemos el icono de sobre abierto
                                txtViewAlarma.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_open, 0, 0, 0);

                            }
                        });

                        // Añadimos la alarma a la pantalla
                        layout.addView(txtViewAlarma);


                        // Rellenando el container con los detalles de la alarma según el tipo:
                        if (tipo.equals("Umbral")) {

                            for (int j = objetoAlarma.length() - 3; j >= 0; j--) {

                                // Siempre 2 campos: clave -> valor
                                LinearLayout linea = new LinearLayout(this);
                                final TextView txt1 = new TextView(this);
                                final TextView txt2 = new TextView(this);


                                // Aplicamos los parámetros
                                txt1.setLayoutParams(textViewParams);
                                txt2.setLayoutParams(textViewParams);
                                txt2.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

                                // Booleano para saber si es el enlace web de la alarma
                                Boolean link = false;
                                switch (j) {

                                    // Campo comentario
                                    case 0:
                                        txt1.setText("Comentario");
                                        if (String.valueOf(objetoAlarma.get("comentario")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {

                                            // Variable con el texto entero
                                            final String fullTxt = String.valueOf(objetoAlarma.get("comentario"));


                                            // Si el texto ocupa mas de 50 caracteres
                                            if (fullTxt.length() > 50) {

                                                // Variable con los 50 primeros caracteres + mensaje para desplegarla
                                                final String partTxt = fullTxt.substring(0, 50) + "...<br><br><b>Pulsa para desplegar</b><br>";

                                                if (Build.VERSION.SDK_INT >= 24) {
                                                    txt2.setText(Html.fromHtml(partTxt, FROM_HTML_MODE_COMPACT)); // for 24 api and more
                                                } else {
                                                    txt2.setText(Html.fromHtml(partTxt));
                                                }


                                                // Listener para desplegarla
                                                txt2.setOnClickListener(new View.OnClickListener() {

                                                    Boolean desplegado = false;
                                                    String txtDesplegado = fullTxt + "<br><br><b>Pulsa para plegar</b><br>";

                                                    @Override
                                                    public void onClick(View v) {

                                                        if (!desplegado) {
                                                            txt1.setVisibility(View.GONE);
                                                            if (Build.VERSION.SDK_INT >= 24) {
                                                                txt2.setText(Html.fromHtml(txtDesplegado, FROM_HTML_MODE_COMPACT));
                                                            } else {
                                                                txt2.setText(Html.fromHtml(txtDesplegado));
                                                            }

                                                            txt2.setPadding(11, 11, 11, 11);
                                                            desplegado = true;
                                                        } else {
                                                            txt1.setVisibility(View.VISIBLE);

                                                            if (Build.VERSION.SDK_INT >= 24) {
                                                                txt2.setText(Html.fromHtml(partTxt, FROM_HTML_MODE_COMPACT));
                                                            } else {
                                                                txt2.setText(Html.fromHtml(partTxt));
                                                            }
                                                            desplegado = false;
                                                        }
                                                    }
                                                });

                                            } else {
                                                txt2.setText(fullTxt);
                                            }
                                        }
                                        break;
                                    case 1:
                                        txt1.setText("Responsable");
                                        if (String.valueOf(objetoAlarma.get("responsable")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("responsable")));
                                        }
                                        break;
                                    case 2:
                                        txt1.setText("Prioridad");
                                        if (String.valueOf(objetoAlarma.get("prioridad")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("prioridad")));
                                        }
                                        break;
                                    case 3:
                                        txt1.setText("Hora desactivada");
                                        if (String.valueOf(objetoAlarma.get("horaDesactivada")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("horaDesactivada")));
                                        }
                                        break;
                                    case 4:
                                        txt1.setText("Fecha desactivada");
                                        if (String.valueOf(objetoAlarma.get("fechaDesactivada")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("fechaDesactivada")));
                                        }
                                        break;
                                    case 5:
                                        txt1.setText("Hora activada");
                                        if (String.valueOf(objetoAlarma.get("horaActivada")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("horaActivada")));
                                        }
                                        break;
                                    case 6:
                                        txt1.setText("Fecha activada");
                                        if (String.valueOf(objetoAlarma.get("fechaActivada")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("fechaActivada")));
                                        }
                                        break;
                                    case 7:
                                        if (String.valueOf(objetoAlarma.get("urlAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {

                                            ImageView imageView = new ImageView(this);

                                            imageView.setImageResource(R.drawable.ic_graph);
                                            imageView.setScaleX(1.5f);
                                            imageView.setScaleY(1.5f);

                                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                            imageParams.setMargins(5, 5, 5, 15);
                                            imageParams.weight = 1;

                                            imageView.setLayoutParams(imageParams);

                                            imageView.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Intent.ACTION_VIEW);
                                                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                                    try {
                                                        intent.setData(Uri.parse(String.valueOf(objetoAlarma.get("urlAlarma"))));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    startActivity(intent);
                                                }
                                            });

                                            txt1.setText("Enlace");
                                            linea.addView(txt1);
                                            linea.addView(imageView);

                                            link = true;
                                        }
                                        break;
                                    case 8:
                                        txt1.setText("Nombre");
                                        if (String.valueOf(objetoAlarma.get("nombreAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("nombreAlarma")));
                                        }
                                        break;
                                    case 9:
                                        txt1.setText("Tipo de Alarma");
                                        if (String.valueOf(objetoAlarma.get("tipoAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("tipoAlarma")));
                                        }
                                        break;
                                    case 10:
                                        txt1.setText("Empresa");
                                        if (String.valueOf(objetoAlarma.get("empresa")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("empresa")));
                                        }
                                        break;
                                    case 11:
                                        txt1.setText("Sede");
                                        if (String.valueOf(objetoAlarma.get("sede")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("sede")));
                                        }
                                }

                                if (!link) {
                                    linea.addView(txt1);
                                    linea.addView(txt2);
                                } else {
                                    link = false;
                                }
                                container.addView(linea);
                                alertasImprimidas++;
                            }


                            // Activaciones de la alarma umbral
                            // Cogemos el String
                            String strActivaciones = String.valueOf(objetoAlarma.get("activacion"));

                            // Creamos el JSONArray del String
                            JSONArray activaciones = new JSONArray(strActivaciones);

                            // Empezamos por la primera activación, luego vamos sumando
                            int numAct = 1;
                            for (int q = activaciones.length() - 1; q >= 0; q--) {

                                JSONObject activacion = (JSONObject) activaciones.get(q);

                                // Titulo activación
                                TextView act1 = new TextView(MainActivity.this);
                                act1.setLayoutParams(textViewParams);
                                act1.setPadding(19, 8, 8, 12);
                                act1.setTextSize(20);
                                act1.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                                act1.setLayoutParams(textViewParams);
                                act1.setText("Activacion (" + numAct + ")");
                                numAct++;
                                container.addView(act1);

                                // Para cada campo de la activación
                                for (int w = 0; w < activacion.length(); w++) {
                                    LinearLayout linea = new LinearLayout(this);
                                    TextView txt1 = new TextView(this);
                                    TextView txt2 = new TextView(this);

                                    txt1.setLayoutParams(textViewParams);
                                    txt2.setLayoutParams(textViewParams);

                                    txt2.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

                                    switch (w) {
                                        case 0:
                                            txt1.setText("Recurso");
                                            if (String.valueOf(activacion.get("recurso")).equals("null")) {
                                                txt2.setText("No hay datos");
                                            } else {
                                                txt2.setText(String.valueOf(activacion.get("recurso")));
                                            }
                                            break;
                                        case 1:
                                            txt1.setText("Proceso");
                                            if (String.valueOf(activacion.get("proceso")).equals("null")) {
                                                txt2.setText("No hay datos");
                                            } else {
                                                txt2.setText(String.valueOf(activacion.get("proceso")));
                                            }
                                            break;
                                        case 2:
                                            txt1.setText("Variable");
                                            if (String.valueOf(activacion.get("variable")).equals("null")) {
                                                txt2.setText("No hay datos");
                                            } else {
                                                txt2.setText(String.valueOf(activacion.get("variable")));
                                            }
                                            break;
                                        case 3:
                                            txt1.setText("Valor de Activacion");
                                            if (String.valueOf(activacion.get("valorActivacion")).equals("null")) {
                                                txt2.setText("No hay datos");
                                            } else {
                                                txt2.setText(String.valueOf(activacion.get("valorActivacion")));
                                            }
                                    }
                                    linea.addView(txt1);
                                    linea.addView(txt2);
                                    container.addView(linea);
                                    alertasImprimidas++;
                                }


                            }

                            // Tipo OEE, igual que la anterior
                        } else if (tipo.equals("OEE")) {

                            for (int j = objetoAlarma.length() - 2; j >= 0; j--) {
                                LinearLayout linea = new LinearLayout(this);
                                final TextView txt1 = new TextView(this);
                                final TextView txt2 = new TextView(this);

                                txt1.setLayoutParams(textViewParams);
                                txt2.setLayoutParams(textViewParams);

                                txt2.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

                                Boolean link = false;

                                switch (j) {
                                    case 0:
                                        txt1.setText("Comentario");
                                        if (String.valueOf(objetoAlarma.get("comentario")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            final String fullTxt = String.valueOf(objetoAlarma.get("comentario"));

                                            if (fullTxt.length() > 50) {
                                                final String partTxt = fullTxt.substring(0, 50) + "...<br><br><b>Pulsa para desplegar</b><br>";

                                                if (Build.VERSION.SDK_INT >= 24) {
                                                    txt2.setText(Html.fromHtml(partTxt, FROM_HTML_MODE_COMPACT)); // for 24 api and more
                                                } else {
                                                    txt2.setText(Html.fromHtml(partTxt));
                                                }
                                                //txt2.setText(partTxt);

                                                txt2.setOnClickListener(new View.OnClickListener() {

                                                    Boolean desplegado = false;
                                                    String txtDesplegado = fullTxt + "<br><br><b>Pulsa para plegar</b><br>";

                                                    @Override
                                                    public void onClick(View v) {

                                                        if (!desplegado) {
                                                            txt1.setVisibility(View.GONE);
                                                            if (Build.VERSION.SDK_INT >= 24) {
                                                                txt2.setText(Html.fromHtml(txtDesplegado, FROM_HTML_MODE_COMPACT)); // for 24 api and more
                                                            } else {
                                                                txt2.setText(Html.fromHtml(txtDesplegado)); // or for older api
                                                            }

                                                            txt2.setPadding(11, 11, 11, 11);
                                                            desplegado = true;
                                                        } else {
                                                            txt1.setVisibility(View.VISIBLE);

                                                            if (Build.VERSION.SDK_INT >= 24) {
                                                                txt2.setText(Html.fromHtml(partTxt, FROM_HTML_MODE_COMPACT)); // for 24 api and more
                                                            } else {
                                                                txt2.setText(Html.fromHtml(partTxt)); // or for older api
                                                            }
                                                            desplegado = false;
                                                        }
                                                    }
                                                });

                                            } else {
                                                txt2.setText(fullTxt);
                                            }
                                        }
                                        break;
                                    case 1:
                                        txt1.setText("Responsable");
                                        if (String.valueOf(objetoAlarma.get("responsable")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("responsable")));
                                        }
                                        break;
                                    case 2:
                                        txt1.setText("Prioridad");
                                        if (String.valueOf(objetoAlarma.get("prioridad")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("prioridad")));
                                        }
                                        break;
                                    case 3:
                                        txt1.setText("H. transcurridas desactivada");
                                        if (String.valueOf(objetoAlarma.get("horasDesactivada")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("horasDesactivada")));
                                        }
                                        break;
                                    case 4:
                                        txt1.setText("OEE desactivado");
                                        if (String.valueOf(objetoAlarma.get("oeeDesactivado")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("oeeDesactivado")));
                                        }
                                        break;
                                    case 5:
                                        txt1.setText("H. transcurridas activada");
                                        if (String.valueOf(objetoAlarma.get("horasActivada")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("horasActivada")));
                                        }
                                        break;
                                    case 6:
                                        txt1.setText("Fecha activada");
                                        if (String.valueOf(objetoAlarma.get("fechaActivada")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("fechaActivada")));
                                        }
                                        break;
                                    case 7:
                                        txt1.setText("Disponibilidad");
                                        if (String.valueOf(objetoAlarma.get("disponibilidad")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("disponibilidad")));
                                        }
                                        break;
                                    case 8:
                                        txt1.setText("Rendimiento");
                                        if (String.valueOf(objetoAlarma.get("rendimiento")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("rendimiento")));
                                        }
                                        break;
                                    case 9:
                                        txt1.setText("OEE Activado");
                                        if (String.valueOf(objetoAlarma.get("oeeActivado")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("oeeActivado")));
                                        }
                                        break;
                                    case 10:
                                        txt1.setText("Turno");
                                        if (String.valueOf(objetoAlarma.get("turno")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("turno")));
                                        }
                                        break;
                                    case 11:
                                        txt1.setText("Proceso");
                                        if (String.valueOf(objetoAlarma.get("proceso")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("proceso")));
                                        }
                                        break;
                                    case 12:
                                        txt1.setText("Sede");
                                        if (String.valueOf(objetoAlarma.get("sede")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("sede")));
                                        }
                                        break;
                                    case 13:
                                        txt1.setText("Enlace");
                                        if (String.valueOf(objetoAlarma.get("urlAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {


                                            ImageView imageView = new ImageView(this);

                                            imageView.setImageResource(R.drawable.ic_graph);
                                            imageView.setScaleX(1.5f);
                                            imageView.setScaleY(1.5f);

                                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                            imageParams.setMargins(5, 5, 5, 15);
                                            imageParams.weight = 1;

                                            imageView.setLayoutParams(imageParams);

                                            imageView.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Intent.ACTION_VIEW);
                                                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                                    try {
                                                        intent.setData(Uri.parse(String.valueOf(objetoAlarma.get("urlAlarma"))));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    startActivity(intent);
                                                }
                                            });

                                            txt1.setText("Enlace");
                                            linea.addView(txt1);
                                            linea.addView(imageView);

                                            link = true;
                                        }
                                        break;
                                    case 14:
                                        txt1.setText("Nombre de alarma");
                                        if (String.valueOf(objetoAlarma.get("nombreAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("nombreAlarma")));
                                        }
                                        break;
                                    case 15:
                                        txt1.setText("Tipo de Alarma");
                                        if (String.valueOf(objetoAlarma.get("tipoAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("tipoAlarma")));
                                        }
                                        break;
                                    case 16:
                                        txt1.setText("Empresa");
                                        if (String.valueOf(objetoAlarma.get("sede")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("empresa")));
                                        }
                                        break;
                                    case 17:
                                        txt1.setText("Sede");
                                        if (String.valueOf(objetoAlarma.get("sede")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("sede")));
                                        }

                                }

                                if (!link) {
                                    linea.addView(txt1);
                                    linea.addView(txt2);
                                }
                                container.addView(linea);
                                alertasImprimidas++;
                            }

                            // Tipo Comunicación, igual que la anterior
                        } else if (tipo.equals("Comunicación")) {
                            for (int j = objetoAlarma.length() - 2; j >= 0; j--) {
                                LinearLayout linea = new LinearLayout(this);
                                final TextView txt1 = new TextView(this);
                                final TextView txt2 = new TextView(this);

                                txt1.setLayoutParams(textViewParams);
                                txt2.setLayoutParams(textViewParams);

                                txt2.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

                                Boolean link = false;

                                switch (j) {
                                    case 0:
                                        txt1.setText("Comentario");
                                        if (String.valueOf(objetoAlarma.get("comentario")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            final String fullTxt = String.valueOf(objetoAlarma.get("comentario"));

                                            if (fullTxt.length() > 50) {
                                                final String partTxt = fullTxt.substring(0, 50) + "...<br><br><b>Pulsa para desplegar</b><br>";

                                                if (Build.VERSION.SDK_INT >= 24) {
                                                    txt2.setText(Html.fromHtml(partTxt, FROM_HTML_MODE_COMPACT)); // for 24 api and more
                                                } else {
                                                    txt2.setText(Html.fromHtml(partTxt));
                                                }
                                                //txt2.setText(partTxt);

                                                txt2.setOnClickListener(new View.OnClickListener() {

                                                    Boolean desplegado = false;
                                                    String txtDesplegado = fullTxt + "<br><br><b>Pulsa para plegar</b><br>";

                                                    @Override
                                                    public void onClick(View v) {

                                                        if (!desplegado) {
                                                            txt1.setVisibility(View.GONE);
                                                            if (Build.VERSION.SDK_INT >= 24) {
                                                                txt2.setText(Html.fromHtml(txtDesplegado, FROM_HTML_MODE_COMPACT)); // for 24 api and more
                                                            } else {
                                                                txt2.setText(Html.fromHtml(txtDesplegado)); // or for older api
                                                            }

                                                            txt2.setPadding(11, 11, 11, 11);
                                                            desplegado = true;
                                                        } else {
                                                            txt1.setVisibility(View.VISIBLE);

                                                            if (Build.VERSION.SDK_INT >= 24) {
                                                                txt2.setText(Html.fromHtml(partTxt, FROM_HTML_MODE_COMPACT)); // for 24 api and more
                                                            } else {
                                                                txt2.setText(Html.fromHtml(partTxt)); // or for older api
                                                            }
                                                            desplegado = false;
                                                        }
                                                    }
                                                });

                                            } else {
                                                txt2.setText(fullTxt);
                                            }
                                        }
                                        break;
                                    case 1:
                                        txt1.setText("Recuperación");
                                        if (String.valueOf(objetoAlarma.get("recuperacion")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("recuperacion")));
                                        }
                                        break;
                                    case 2:
                                        txt1.setText("Prioridad");
                                        if (String.valueOf(objetoAlarma.get("prioridad")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("prioridad")));
                                        }
                                        break;
                                    case 3:
                                        txt1.setText("Fallo");
                                        if (String.valueOf(objetoAlarma.get("fallo")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("fallo")));
                                        }
                                        break;
                                    case 4:
                                        txt1.setText("Medidor");
                                        if (String.valueOf(objetoAlarma.get("medidor")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("medidor")));
                                        }
                                        break;
                                    case 5:
                                        txt1.setText("Sede");
                                        if (String.valueOf(objetoAlarma.get("sede")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("sede")));
                                        }
                                        break;
                                    case 6:
                                        txt1.setText("Enlace");
                                        if (String.valueOf(objetoAlarma.get("urlAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {


                                            ImageView imageView = new ImageView(this);

                                            imageView.setImageResource(R.drawable.ic_graph);
                                            imageView.setScaleX(1.5f);
                                            imageView.setScaleY(1.5f);

                                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                            imageParams.setMargins(5, 5, 5, 15);
                                            imageParams.weight = 1;

                                            imageView.setLayoutParams(imageParams);

                                            imageView.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Intent.ACTION_VIEW);
                                                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                                    try {
                                                        intent.setData(Uri.parse(String.valueOf(objetoAlarma.get("urlAlarma"))));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    startActivity(intent);
                                                }
                                            });

                                            txt1.setText("Enlace");
                                            linea.addView(txt1);
                                            linea.addView(imageView);

                                            link = true;
                                        }
                                        break;
                                    case 7:
                                        txt1.setText("Nombre de alarma");
                                        if (String.valueOf(objetoAlarma.get("nombreAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("nombreAlarma")));
                                        }
                                        break;
                                    case 8:
                                        txt1.setText("Tipo de Alarma");
                                        if (String.valueOf(objetoAlarma.get("tipoAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("tipoAlarma")));
                                        }
                                        break;
                                    case 9:
                                        txt1.setText("Empresa");
                                        if (String.valueOf(objetoAlarma.get("empresa")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("empresa")));
                                        }
                                        break;
                                    case 10:
                                        txt1.setText("sede");
                                        if (String.valueOf(objetoAlarma.get("sede")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("sede")));
                                        }

                                }

                                if (!link) {
                                    linea.addView(txt1);
                                    linea.addView(txt2);
                                }
                                container.addView(linea);
                                alertasImprimidas++;

                            }

                            // Tipo Exceso potencia, igual que la anterior
                        } else if (tipo.equals("Exceso potencia")) {


                            for (int j = objetoAlarma.length() - 2; j >= 0; j--) {
                                LinearLayout linea = new LinearLayout(this);
                                final TextView txt1 = new TextView(this);
                                final TextView txt2 = new TextView(this);

                                txt1.setLayoutParams(textViewParams);
                                txt2.setLayoutParams(textViewParams);

                                txt2.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

                                Boolean link = false;


                                switch (j) {
                                    case 0:
                                        txt1.setText("Comentario");
                                        if (String.valueOf(objetoAlarma.get("comentario")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            final String fullTxt = String.valueOf(objetoAlarma.get("comentario"));

                                            if (fullTxt.length() > 50) {
                                                final String partTxt = fullTxt.substring(0, 50) + "...<br><br><b>Pulsa para desplegar</b><br>";

                                                if (Build.VERSION.SDK_INT >= 24) {
                                                    txt2.setText(Html.fromHtml(partTxt, FROM_HTML_MODE_COMPACT)); // for 24 api and more
                                                } else {
                                                    txt2.setText(Html.fromHtml(partTxt));
                                                }
                                                //txt2.setText(partTxt);

                                                txt2.setOnClickListener(new View.OnClickListener() {

                                                    Boolean desplegado = false;
                                                    String txtDesplegado = fullTxt + "<br><br><b>Pulsa para plegar</b><br>";

                                                    @Override
                                                    public void onClick(View v) {

                                                        if (!desplegado) {
                                                            txt1.setVisibility(View.GONE);
                                                            if (Build.VERSION.SDK_INT >= 24) {
                                                                txt2.setText(Html.fromHtml(txtDesplegado, FROM_HTML_MODE_COMPACT)); // for 24 api and more
                                                            } else {
                                                                txt2.setText(Html.fromHtml(txtDesplegado)); // or for older api
                                                            }

                                                            txt2.setPadding(11, 11, 11, 11);
                                                            desplegado = true;
                                                        } else {
                                                            txt1.setVisibility(View.VISIBLE);

                                                            if (Build.VERSION.SDK_INT >= 24) {
                                                                txt2.setText(Html.fromHtml(partTxt, FROM_HTML_MODE_COMPACT)); // for 24 api and more
                                                            } else {
                                                                txt2.setText(Html.fromHtml(partTxt)); // or for older api
                                                            }
                                                            desplegado = false;
                                                        }
                                                    }
                                                });

                                            } else {
                                                txt2.setText(fullTxt);
                                            }
                                        }
                                        break;
                                    case 1:
                                        txt1.setText("Responsable");
                                        if (String.valueOf(objetoAlarma.get("responsable")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("responsable")));
                                        }
                                        break;
                                    case 2:
                                        txt1.setText("Prioridad");
                                        if (String.valueOf(objetoAlarma.get("prioridad")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("prioridad")));
                                        }
                                        break;
                                    case 3:
                                        txt1.setText("Fecha");
                                        if (String.valueOf(objetoAlarma.get("fechaIncidencia")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("fechaIncidencia")));
                                        }
                                        break;
                                    case 4:
                                        txt1.setText("Coste previsto");
                                        if (String.valueOf(objetoAlarma.get("costePrevisto")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("costePrevisto")));
                                        }
                                        break;
                                    case 5:
                                        txt1.setText("Exceso previsto");
                                        if (String.valueOf(objetoAlarma.get("excesoPrevisto")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("excesoPrevisto")));
                                        }
                                        break;
                                    case 6:
                                        txt1.setText("Periodo tarifario");
                                        if (String.valueOf(objetoAlarma.get("periodoTarifario")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("periodoTarifario")));
                                        }
                                        break;
                                    case 7:
                                        txt1.setText("Coste");
                                        if (String.valueOf(objetoAlarma.get("coste")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("coste")));
                                        }
                                        break;
                                    case 8:
                                        txt1.setText("Exceso");
                                        if (String.valueOf(objetoAlarma.get("exceso")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("exceso")));
                                        }
                                        break;
                                    case 9:
                                        txt1.setText("Maxímetro");
                                        if (String.valueOf(objetoAlarma.get("maximetro")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {

                                            txt2.setText(String.valueOf(objetoAlarma.get("maximetro")));
                                        }
                                        break;
                                    case 10:
                                        txt1.setText("Enlace");
                                        if (String.valueOf(objetoAlarma.get("urlAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {


                                            ImageView imageView = new ImageView(this);

                                            imageView.setImageResource(R.drawable.ic_graph);
                                            imageView.setScaleX(1.5f);
                                            imageView.setScaleY(1.5f);

                                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                            imageParams.setMargins(5, 5, 5, 15);
                                            imageParams.weight = 1;

                                            imageView.setLayoutParams(imageParams);

                                            imageView.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Intent.ACTION_VIEW);
                                                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                                    try {
                                                        intent.setData(Uri.parse(String.valueOf(objetoAlarma.get("urlAlarma"))));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    startActivity(intent);
                                                }
                                            });

                                            txt1.setText("Enlace");
                                            linea.addView(txt1);
                                            linea.addView(imageView);

                                            link = true;
                                        }
                                        break;
                                    case 11:
                                        txt1.setText("Nombre alarma");
                                        if (String.valueOf(objetoAlarma.get("nombreAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {

                                            txt2.setText(String.valueOf(objetoAlarma.get("nombreAlarma")));
                                        }
                                        break;
                                    case 12:
                                        txt1.setText("Tipo de alarma");
                                        if (String.valueOf(objetoAlarma.get("tipoAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {

                                            txt2.setText(String.valueOf(objetoAlarma.get("tipoAlarma")));
                                        }
                                        break;
                                    case 13:
                                        txt1.setText("Empresa");
                                        if (String.valueOf(objetoAlarma.get("empresa")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("empresa")));
                                        }
                                        break;
                                    case 14:
                                        txt1.setText("Sede");
                                        if (String.valueOf(objetoAlarma.get("sede")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("sede")));
                                        }
                                }

                                if (!link) {
                                    linea.addView(txt1);
                                    linea.addView(txt2);
                                }
                                container.addView(linea);
                                alertasImprimidas++;
                            }
                        } else if (tipo.equals("Predicción gas")) {

                            for (int j = objetoAlarma.length() - 2; j >= 0; j--) {
                                LinearLayout linea = new LinearLayout(this);
                                final TextView txt1 = new TextView(this);
                                final TextView txt2 = new TextView(this);

                                txt1.setLayoutParams(textViewParams);
                                txt2.setLayoutParams(textViewParams);

                                txt2.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));

                                Boolean link = false;


                                switch (j) {
                                    case 0:
                                        txt1.setText("Comentario");
                                        if (String.valueOf(objetoAlarma.get("comentario")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            final String fullTxt = String.valueOf(objetoAlarma.get("comentario"));

                                            if (fullTxt.length() > 50) {
                                                final String partTxt = fullTxt.substring(0, 50) + "...<br><br><b>Pulsa para desplegar</b><br>";

                                                if (Build.VERSION.SDK_INT >= 24) {
                                                    txt2.setText(Html.fromHtml(partTxt, FROM_HTML_MODE_COMPACT)); // for 24 api and more
                                                } else {
                                                    txt2.setText(Html.fromHtml(partTxt));
                                                }
                                                //txt2.setText(partTxt);

                                                txt2.setOnClickListener(new View.OnClickListener() {

                                                    Boolean desplegado = false;
                                                    String txtDesplegado = fullTxt + "<br><br><b>Pulsa para plegar</b><br>";

                                                    @Override
                                                    public void onClick(View v) {

                                                        if (!desplegado) {
                                                            txt1.setVisibility(View.GONE);
                                                            if (Build.VERSION.SDK_INT >= 24) {
                                                                txt2.setText(Html.fromHtml(txtDesplegado, FROM_HTML_MODE_COMPACT)); // for 24 api and more
                                                            } else {
                                                                txt2.setText(Html.fromHtml(txtDesplegado)); // or for older api
                                                            }

                                                            txt2.setPadding(11, 11, 11, 11);
                                                            desplegado = true;
                                                        } else {
                                                            txt1.setVisibility(View.VISIBLE);

                                                            if (Build.VERSION.SDK_INT >= 24) {
                                                                txt2.setText(Html.fromHtml(partTxt, FROM_HTML_MODE_COMPACT)); // for 24 api and more
                                                            } else {
                                                                txt2.setText(Html.fromHtml(partTxt)); // or for older api
                                                            }
                                                            desplegado = false;
                                                        }
                                                    }
                                                });

                                            } else {
                                                txt2.setText(fullTxt);
                                            }
                                        }
                                        break;
                                    case 1:
                                        txt1.setText("Responsable");
                                        if (String.valueOf(objetoAlarma.get("responsable")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("responsable")));
                                        }
                                        break;
                                    case 2:
                                        txt1.setText("Prioridad");
                                        if (String.valueOf(objetoAlarma.get("prioridad")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("prioridad")));
                                        }
                                        break;
                                    case 3:
                                        txt1.setText("Fecha/Hora");
                                        if (String.valueOf(objetoAlarma.get("fechaIncidencia")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("fechaIncidencia")));
                                        }
                                        break;
                                    case 4:
                                        txt1.setText("Contratada");
                                        if (String.valueOf(objetoAlarma.get("contratada")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("contratada")));
                                        }
                                        break;
                                    case 5:
                                        txt1.setText("Predicción");
                                        if (String.valueOf(objetoAlarma.get("prediccion")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("prediccion")));
                                        }
                                        break;
                                    case 6:
                                        txt1.setText("Coste penalización");
                                        if (String.valueOf(objetoAlarma.get("costePenalizacion")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("costePenalizacion")));
                                        }
                                        break;
                                    case 7:
                                        txt1.setText("Máximo mes previsto");
                                        if (String.valueOf(objetoAlarma.get("maximoMesPrevisto")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("maximoMesPrevisto")));
                                        }
                                        break;
                                    case 8:
                                        txt1.setText("Coste mes previsto");
                                        if (String.valueOf(objetoAlarma.get("costeMesPrevisto")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("costeMesPrevisto")));
                                        }
                                        break;
                                    case 9:
                                        txt1.setText("Enlace");
                                        if (String.valueOf(objetoAlarma.get("urlAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {


                                            ImageView imageView = new ImageView(this);

                                            imageView.setImageResource(R.drawable.ic_graph);
                                            imageView.setScaleX(1.5f);
                                            imageView.setScaleY(1.5f);

                                            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                            imageParams.setMargins(5, 5, 5, 15);
                                            imageParams.weight = 1;

                                            imageView.setLayoutParams(imageParams);

                                            imageView.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Intent.ACTION_VIEW);
                                                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                                    try {
                                                        intent.setData(Uri.parse(String.valueOf(objetoAlarma.get("urlAlarma"))));
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    startActivity(intent);
                                                }
                                            });

                                            txt1.setText("Enlace");
                                            linea.addView(txt1);
                                            linea.addView(imageView);

                                            link = true;
                                        }
                                        break;
                                    case 10:
                                        txt1.setText("Nombre alarma");
                                        if (String.valueOf(objetoAlarma.get("nombreAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {

                                            txt2.setText(String.valueOf(objetoAlarma.get("nombreAlarma")));
                                        }
                                        break;
                                    case 11:
                                        txt1.setText("Tipo de alarma");
                                        if (String.valueOf(objetoAlarma.get("tipoAlarma")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {

                                            txt2.setText(String.valueOf(objetoAlarma.get("tipoAlarma")));
                                        }
                                        break;
                                    case 12:
                                        txt1.setText("Empresa");
                                        if (String.valueOf(objetoAlarma.get("empresa")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("empresa")));
                                        }
                                        break;
                                    case 13:
                                        txt1.setText("Sede");
                                        if (String.valueOf(objetoAlarma.get("sede")).equals("null")) {
                                            txt2.setText("No hay datos");
                                        } else {
                                            txt2.setText(String.valueOf(objetoAlarma.get("sede")));
                                        }
                                }

                                if (!link) {
                                    linea.addView(txt1);
                                    linea.addView(txt2);
                                }
                                container.addView(linea);
                                alertasImprimidas++;
                            }
                        }


                        container.setVisibility(View.GONE);
                        layout.addView(container);

                        alertasImprimidas++;


                    }

                    // Cuando imprimimos la última de las alarmas añadimos al final un espacio en blanco para que se vea al desplegar la ultima alarma
                    if (i == jsonArray.length() - 1) {
                        TextView separator = new TextView(this);
                        separator.setLayoutParams(separatorParams);

                    }

                }

                // Si el usuario tiene alarmas pero por los filtros no se le muestran
                if (alertasImprimidas == 0) {
                    TextView txtCero = new TextView(this);
                    txtCero.setText("No se encontraron alarmas con estos filtros");

                    Button btnCero = new Button(this);
                    btnCero.setText("Quitar filtros");

                    txtCero.setPadding(15, 15, 15, 15);
                    txtCero.setTextSize(17);
                    btnCero.setPadding(15, 15, 15, 15);
                    btnCero.setTextSize(17);
                    txtCero.setLayoutParams(textViewParams);

                    LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    btnParams.setMargins(5, 5, 5, 15);
                    btnParams.gravity = Gravity.CENTER_HORIZONTAL;
                    btnParams.weight = 1;
                    btnCero.setLayoutParams(btnParams);

                    // Listener para  restablecer los filtros
                    btnCero.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editor.putString(usuario + "_filtroTipo", "Mostrar todo");
                            editor.putString(usuario + "_filtroPrioridad", "Mostrar todo");
                            editor.putString(usuario + "_filtroEmpresa", "Mostrar todo");
                            editor.putString(usuario + "_filtroSede", "Mostrar todo");
                            editor.apply();

                            Toast.makeText(MainActivity.this, "Filtros restablecidos", Toast.LENGTH_LONG).show();

                            selectTipo.setSelection(getIndex(selectTipo, "Mostrar todo"));
                            selectPrioridad.setSelection(getIndex(selectPrioridad, "Mostrar todo"));
                            selectEmpresa.setSelection(getIndex(selectEmpresa, "Mostrar todo"));

                            try {
                                ArrayList<String> sedesNull = new ArrayList<String>();

                                ArrayAdapter adapterNull = new ArrayAdapter<String>(context, R.layout.spinner, sedesNull);

                                selectSede.setAdapter(adapterNull);

                            } catch (Exception ignored) {
                                Log.d("ERROR", "ERROR");
                            }

                            pintaAlarmas();


                        }
                    });

                    layout.addView(txtCero);
                    layout.addView(btnCero);
                }

                // En caso de que el usuario no tenga alarmas
            } else {
                TextView v = new TextView(MainActivity.this);
                v.setText("No hay nuevas alarmas");
                v.setPadding(19, 8, 8, 12);
                v.setTextSize(25);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ShapeDrawable sd = new ShapeDrawable();

                    // Specify the shape of ShapeDrawable
                    sd.setShape(new RectShape());

                    // Specify the border color of shape
                    sd.getPaint().setColor(Color.GRAY);

                    // Set the border width
                    sd.getPaint().setStrokeWidth(1f);

                    // Specify the style is a Stroke
                    sd.getPaint().setStyle(Paint.Style.STROKE);

                    // Finally, add the drawable background to TextView
                    v.setBackground(sd);

                    //Ponemos el texto
                    v.setText("No hay alarmas");
                }

                layout.addView(v);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView space = new TextView(this);
        space.setPadding(7, 7, 7, 7);
        layout.addView(space);
    }


    // Función para sacar el índice del spinner mediante su texto
    private int getIndex(Spinner spnr, String value) {

        int index = 0;
        try {
            for (int i = 0; i < spnr.getCount(); i++) {


                if (spnr.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                    index = i;
                    break;
                }
            }

        } catch (Exception ignored) {

        }
        return index;
    }

}
