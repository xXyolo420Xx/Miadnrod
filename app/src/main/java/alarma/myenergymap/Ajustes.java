package alarma.myenergymap;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class Ajustes extends AppCompatActivity {

    Spinner alta;
    Spinner media;
    Spinner baja;
    Spinner selectUmbral;
    Spinner selectOEE;
    Spinner selectComunicacion;
    Spinner selectExceso;
    Spinner selectGas;
    SharedPreferences datos;
    SharedPreferences.Editor editor;
    Context context;
    String prefAlta;
    String prefMedia;
    String prefBaja;
    String prefUmbral;
    String prefOEE;
    String prefComunicacion;
    String prefExceso;
    String prefGas;

    Uri soundUri;
    String usuario;

    LinearLayout layout;

    Boolean silencio;

    Button btnSilenciar;
    Button btnVolver;

    CheckBox checkVibracion;

    Boolean open;
    ArrayList<String> sonidos;
    ArrayList<String> colores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustes);

        // Capturamos todos los elementos del layout
        btnSilenciar = (Button) findViewById(R.id.btnSilenciar);
        btnVolver  = (Button) findViewById(R.id.btnVolver);
        soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        open = true;
        datos = PreferenceManager.getDefaultSharedPreferences(this);
        editor = datos.edit();
        context = this;
        usuario = datos.getString("usuario_logeado", null);
        silencio = datos.getBoolean(usuario + "_silencio", false);
        layout = (LinearLayout) findViewById(R.id.layout);
        alta = (Spinner) findViewById(R.id.selectAlta);
        media = (Spinner) findViewById(R.id.selectMedia);
        baja = (Spinner) findViewById(R.id.selectBaja);
        selectUmbral = (Spinner) findViewById(R.id.selectUmbral);
        selectOEE = (Spinner) findViewById(R.id.selectOEE);
        selectComunicacion = (Spinner) findViewById(R.id.selectComunicacion);
        selectExceso = (Spinner) findViewById(R.id.selectExceso);
        selectGas = (Spinner) findViewById(R.id.selectGas);
        checkVibracion = (CheckBox) findViewById(R.id.checkVibracion);

        // Listener del check de vibración
        checkVibracion.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if ( ((CheckBox)v).isChecked() ) {
                    editor.putBoolean(usuario+"_vibracion", true);
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(500);
                }else{
                    editor.putBoolean(usuario+"_vibracion", false);
                }
                editor.apply();
            }
        });



        if (usuario != null) {

            // Preferencias del usuario guardadas
            prefAlta = datos.getString(usuario + "_prefAlta", "Por defecto");
            prefMedia = datos.getString(usuario + "_prefMedia", "Por defecto");
            prefBaja = datos.getString(usuario + "_prefBaja", "Por defecto");

            prefUmbral = datos.getString(usuario + "_prefUmbral", "Por defecto");
            prefOEE = datos.getString(usuario + "_prefOEE", "Por defecto");
            prefComunicacion = datos.getString(usuario + "_prefComunicacion", "Por defecto");
            prefExceso = datos.getString(usuario + "_prefExceso", "Por defecto");
            prefGas = datos.getString(usuario + "_prefGas", "Por defecto");

        }

        // ArrayList con los sonidos
        sonidos = new ArrayList<>();
        sonidos.add("Por defecto");

        // Añadimos todos los que esten en la carpeta raw
        Field[] fields = R.raw.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            sonidos.add(name);
        }
        sonidos.add("Silencio");


        // Los convertimos a ArrayAdapter
        ArrayAdapter<String> adapterSonidos = new ArrayAdapter<>(this, R.layout.spinner, sonidos);

        // Los aplicamos a cada uno de los spinners
        alta.setAdapter(adapterSonidos);
        media.setAdapter(adapterSonidos);
        baja.setAdapter(adapterSonidos);



        // ArrayList de colores
        colores = new ArrayList<>();

        colores.add("Por defecto");
        colores.add("morado");
        colores.add("marron");
        colores.add("naranja");
        colores.add("gris");
        colores.add("amarillo");
        colores.add("rosa");
        colores.add("verde");
        colores.add("rojo");
        colores.add("blanco");

        // Lo pasamos a ArrayAdapter
        ArrayAdapter<String> adapterColores = new ArrayAdapter<>(this, R.layout.spinner, colores);

        // Lo aplicamos a cada uno de los spinners
        selectUmbral.setAdapter(adapterColores);
        selectOEE.setAdapter(adapterColores);
        selectComunicacion.setAdapter(adapterColores);
        selectExceso.setAdapter(adapterColores);
        selectGas.setAdapter(adapterColores);

        // Listener de prioridad Alta
        alta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                editor.putString(usuario + "_prefAlta", item);
                editor.apply();

                if (!open) {
                    if (!item.equals("Silencio") && !item.equals("Por defecto")) {
                        int resID = getResources().getIdentifier(item, "raw", getPackageName());
                        MediaPlayer mediaPlayer = MediaPlayer.create(context, resID);
                        mediaPlayer.start();
                    } else if (item.equals("Por defecto")) {
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), soundUri);
                        r.play();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Listener de prioridad Media
        media.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                editor.putString(usuario + "_prefMedia", item);
                editor.apply();

                if (!open) {
                    if (!item.equals("Silencio") && !item.equals("Por defecto")) {
                        int resID = getResources().getIdentifier(item, "raw", getPackageName());
                        MediaPlayer mediaPlayer = MediaPlayer.create(context, resID);
                        mediaPlayer.start();
                    } else if (item.equals("Por defecto")) {
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), soundUri);
                        r.play();
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Listener de prioridad Baja
        baja.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                editor.putString(usuario + "_prefBaja", item);
                editor.apply();

                if (!open) {
                    if (!item.equals("Silencio") && !item.equals("Por defecto")) {
                        int resID = getResources().getIdentifier(item, "raw", getPackageName());
                        MediaPlayer mediaPlayer = MediaPlayer.create(context, resID);
                        mediaPlayer.start();
                    } else if (item.equals("Por defecto")) {
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), soundUri);
                        r.play();
                    }
                }
                open = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Ponemos las selecciones de las preferencias del usuario guardadas
        alta.setSelection(getIndex(alta, prefAlta));
        media.setSelection(getIndex(media, prefMedia));
        baja.setSelection(getIndex(baja, prefBaja));


        // Listener para tipo Umbral
        selectUmbral.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                editor.putString(usuario + "_prefUmbral", item);
                editor.apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Listener para tipo OEE
        selectOEE.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                editor.putString(usuario + "_prefOEE", item);
                editor.apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Listener para tipo Comunicación
        selectComunicacion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                editor.putString(usuario + "_prefComunicacion", item);
                editor.apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Listener para tipo Exceso
        selectExceso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                editor.putString(usuario + "_prefExceso", item);
                editor.apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Listener para tipo Predicción gas
        selectGas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                editor.putString(usuario + "_prefGas", item);
                editor.apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        selectUmbral.setSelection(getIndex(selectUmbral, prefUmbral));
        selectOEE.setSelection(getIndex(selectOEE, prefOEE));
        selectComunicacion.setSelection(getIndex(selectComunicacion, prefComunicacion));
        selectExceso.setSelection(getIndex(selectExceso, prefExceso));
        selectGas.setSelection(getIndex(selectGas, prefGas));

        // Botón para silenciar todo
        if (!silencio) {
            btnSilenciar.setText(R.string.silenciar_todo);
        } else {
            alta.setEnabled(false);
            media.setEnabled(false);
            baja.setEnabled(false);
            btnSilenciar.setText(R.string.txtActivar);
        }


        // Listener del botón silenciar
        btnSilenciar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Boolean silencioClick = datos.getBoolean(usuario + "_silencio", false);

                if (!silencioClick) {
                    editor.putBoolean(usuario + "_silencio", true);
                    editor.apply();

                    alta.setEnabled(false);
                    media.setEnabled(false);
                    baja.setEnabled(false);

                    btnSilenciar.setText(R.string.txtActivar);

                } else {
                    editor.putBoolean(usuario + "_silencio", false);
                    editor.apply();

                    alta.setEnabled(true);
                    media.setEnabled(true);
                    baja.setEnabled(true);

                    btnSilenciar.setText(R.string.silenciar_todo);
                }
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               onBackPressed();
            }
        });



    }

    // Método para sacar el indice del spinner segun su texto
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
