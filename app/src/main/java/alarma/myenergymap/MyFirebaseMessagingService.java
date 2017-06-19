package alarma.myenergymap;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    SharedPreferences datos;
    SharedPreferences.Editor editorDatos;

    //Metodo que se ejecuta al recibir un mensaje
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (!remoteMessage.getData().isEmpty()) {

            datos = PreferenceManager.getDefaultSharedPreferences(this);
            editorDatos = datos.edit();
            String usuario = datos.getString("usuario_logeado", null);

            if (usuario != null) {

                // Llamamos a sendNotification pasandole los datos
                sendNotification(remoteMessage.getData());
            }
        }
    }

    private void sendNotification(Map<String, String> data) {

        // Campos de la alarma enviada comunes
        String usuario = datos.getString("usuario_logeado", null);
        String tipoAlarma = data.get("tipoAlarma");
        String tituloNotificacion = data.get("tituloNotificacion");
        String nombreAlarma = data.get("nombreAlarma");
        String prioridad = "Baja";

        // Como el campo prioridad no esta en la alarma de Configuración hacemos try catch
        try {
            prioridad = data.get("prioridad");
        } catch (Exception ignored) {

        }

        // Umbral
        if (tipoAlarma.equals("Umbral")) {

            // Lo guardamos en las sharedpreferences

            // Alarmas viejas
            String oldAlarmas = datos.getString(usuario, null);

            JSONArray jsonArray;

            // Campos de la alarma
            String empresa = data.get("empresa");
            String sede = data.get("sede");
            String fechaActivada = data.get("fechaActivada");
            String horaActivada = data.get("horaActivada");
            String fechaDesactivada = data.get("fechaDesactivada");
            String horaDesactivada = data.get("horaDesactivada");
            String urlAlarma = data.get("urlAlarma");
            String comentario = data.get("comentario");
            String responsable = data.get("responsable");
            JSONArray activacion = null;

            // Campos de activaciones
            try {
                activacion = new JSONArray(data.get("activacion"));
            } catch (JSONException e) {
                e.printStackTrace();
            }


            try {
                if (oldAlarmas == null) {
                    jsonArray = new JSONArray();
                } else {
                    jsonArray = new JSONArray(oldAlarmas);
                }


                //JSONObject de la alarma
                JSONObject alarma = new JSONObject("{" +
                        "sede : '" + sede + "'," +
                        "empresa: '" + empresa + "'," +
                        "titulo : '" + tituloNotificacion + "'," +
                        "comentario : '" + comentario + "'," +
                        "tipoAlarma: '" + tipoAlarma + "'," +
                        "nombreAlarma : '" + nombreAlarma + "'," +
                        "urlAlarma : '" + urlAlarma + "'," +
                        "fechaActivada : '" + fechaActivada + "'," +
                        "horaActivada : '" + horaActivada + "'," +
                        "fechaDesactivada : '" + fechaDesactivada + "'," +
                        "horaDesactivada : '" + horaDesactivada + "'," +
                        "prioridad : '" + prioridad + "'," +
                        "responsable : '" + responsable + "'," +
                        "activacion : '" + activacion + "'" +
                        "}"
                );

                // Guardamos
                jsonArray.put(alarma);
                editorDatos.putString(usuario, String.valueOf(jsonArray));
                editorDatos.apply();

                System.out.println(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // OEE
            // Igual que la anterior pero sin activaciones y diferentes campos
        } else if (tipoAlarma.equals("OEE")) {

            //Lo guardamos en las sharedpreferences

            String oldAlarmas = datos.getString(usuario, null);

            JSONArray jsonArray;

            String empresa = data.get("empresa");
            String sede = data.get("sede");
            String proceso = data.get("proceso");
            String turno = data.get("turno");
            String urlAlarma = data.get("urlAlarma");
            String oeeActivado = data.get("oeeActivado");
            String rendimiento = data.get("rendimiento");
            String disponibilidad = data.get("disponibilidad");
            String fechaActivada = data.get("fechaActivada");
            String horasActivada = data.get("horasActivada");
            String oeeDesactivado = data.get("oeeDesactivado");
            String horasDesactivada = data.get("horasDesactivada");
            String comentario = data.get("comentario");
            String responsable = data.get("responsable");


            try {
                if (oldAlarmas == null) {
                    jsonArray = new JSONArray();
                } else {
                    jsonArray = new JSONArray(oldAlarmas);
                }

                JSONObject alarma = new JSONObject("{" +
                        "titulo : '" + tituloNotificacion + "'," +
                        "tipoAlarma: '" + tipoAlarma + "'," +
                        "comentario : '" + comentario + "'," +
                        "nombreAlarma : '" + nombreAlarma + "'," +
                        "prioridad : '" + prioridad + "'," +
                        "urlAlarma : '" + urlAlarma + "'," +
                        "sede : '" + sede + "'," +
                        "empresa: '" + empresa + "'," +
                        "proceso: '" + proceso + "'," +
                        "turno : '" + turno + "'," +
                        "oeeActivado : '" + oeeActivado + "'," +
                        "rendimiento : '" + rendimiento + "'," +
                        "disponibilidad : '" + disponibilidad + "'," +
                        "fechaActivada : '" + fechaActivada + "'," +
                        "horasActivada : '" + horasActivada + "'," +
                        "oeeDesactivado : '" + oeeDesactivado + "'," +
                        "horasDesactivada : '" + horasDesactivada + "'," +
                        "responsable : '" + responsable + "'" +
                        "}"
                );

                jsonArray.put(alarma);
                editorDatos.putString(usuario, String.valueOf(jsonArray));
                editorDatos.apply();

                System.out.println(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Igual que la anterior
        } else if (tipoAlarma.equals("Comunicación")) {

            //Lo guardamos en las sharedpreferences
            String oldAlarmas = datos.getString(usuario, null);

            JSONArray jsonArray;


            String empresa = data.get("empresa");
            String sede = data.get("sede");
            String comentario = data.get("comentario");
            String urlAlarma = data.get("urlAlarma");
            String medidor = data.get("medidor");
            String fallo = data.get("fallo");
            String recuperacion = data.get("recuperacion");

            try {
                if (oldAlarmas == null) {
                    jsonArray = new JSONArray();
                } else {
                    jsonArray = new JSONArray(oldAlarmas);
                }

                JSONObject alarma = new JSONObject("{" +
                        "titulo : '" + tituloNotificacion + "'," +
                        "tipoAlarma: '" + tipoAlarma + "'," +
                        "nombreAlarma : '" + nombreAlarma + "'," +
                        "urlAlarma : '" + urlAlarma + "'," +
                        "comentario : '" + comentario + "'," +
                        "prioridad : '" + prioridad + "'," +
                        "sede : '" + sede + "'," +
                        "empresa: '" + empresa + "'," +
                        "medidor: '" + medidor + "'," +
                        "fallo : '" + fallo + "'," +
                        "recuperacion : '" + recuperacion + "'" +
                        "}"
                );

                jsonArray.put(alarma);
                editorDatos.putString(usuario, String.valueOf(jsonArray));
                editorDatos.apply();

                System.out.println(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Igual que la anterior
        } else if (tipoAlarma.equals("Exceso potencia")) {

            //Lo guardamos en las sharedpreferences

            String oldAlarmas = datos.getString(usuario, null);

            JSONArray jsonArray;


            String comentario = data.get("comentario");
            String empresa = data.get("empresa");
            String sede = data.get("sede");
            String maximetro = data.get("maximetro");
            String exceso = data.get("exceso");
            String urlAlarma = data.get("urlAlarma");
            String coste = data.get("coste");
            String excesoPrevisto = data.get("excesoPrevisto");
            String periodoTarifario = data.get("periodoTarifario");
            String costePrevisto = data.get("costePrevisto");
            String fechaIncidencia = data.get("fechaIncidencia");
            String responsable = data.get("responsable");

            try {
                if (oldAlarmas == null) {
                    jsonArray = new JSONArray();
                } else {
                    jsonArray = new JSONArray(oldAlarmas);
                }

                JSONObject alarma = new JSONObject("{" +
                        "titulo : '" + tituloNotificacion + "'," +
                        "tipoAlarma: '" + tipoAlarma + "'," +
                        "nombreAlarma : '" + nombreAlarma + "'," +
                        "sede : '" + sede + "'," +
                        "empresa: '" + empresa + "'," +
                        "urlAlarma : '" + urlAlarma + "'," +
                        "comentario : '" + comentario + "'," +
                        "maximetro: '" + maximetro + "'," +
                        "exceso : '" + exceso + "'," +
                        "coste : '" + coste + "'," +
                        "excesoPrevisto : '" + excesoPrevisto + "'," +
                        "periodoTarifario : '" + periodoTarifario + "'," +
                        "costePrevisto : '" + costePrevisto + "'," +
                        "fechaIncidencia : '" + fechaIncidencia + "'," +
                        "responsable : '" + responsable + "'," +
                        "prioridad : '" + prioridad + "'" +
                        "}"
                );

                jsonArray.put(alarma);
                editorDatos.putString(usuario, String.valueOf(jsonArray));
                editorDatos.apply();

                System.out.println(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            // Igual que la anterior
        } else if (tipoAlarma.equals("Predicción gas")) {

            //Lo guardamos en las sharedpreferences

            String oldAlarmas = datos.getString(usuario, null);

            JSONArray jsonArray;

            String comentario = data.get("comentario");
            String empresa = data.get("empresa");
            String sede = data.get("sede");
            String contratada = data.get("contratada");
            String prediccion = data.get("prediccion");
            String urlAlarma = data.get("urlAlarma");
            String costePenalizacion = data.get("costePenalizacion");
            String maximoMesPrevisto = data.get("maximoMesPrevisto");
            String costeMesPrevisto = data.get("costeMesPrevisto");
            String fechaIncidencia = data.get("fechaIncidencia");
            String responsable = data.get("responsable");

            try {
                if (oldAlarmas == null) {
                    jsonArray = new JSONArray();
                } else {
                    jsonArray = new JSONArray(oldAlarmas);
                }

                JSONObject alarma = new JSONObject("{" +
                        "titulo : '" + tituloNotificacion + "'," +
                        "tipoAlarma: '" + tipoAlarma + "'," +
                        "nombreAlarma : '" + nombreAlarma + "'," +
                        "sede : '" + sede + "'," +
                        "empresa: '" + empresa + "'," +
                        "urlAlarma : '" + urlAlarma + "'," +
                        "comentario : '" + comentario + "'," +
                        "contratada: '" + contratada + "'," +
                        "prediccion : '" + prediccion + "'," +
                        "costePenalizacion : '" + costePenalizacion + "'," +
                        "maximoMesPrevisto : '" + maximoMesPrevisto + "'," +
                        "costeMesPrevisto : '" + costeMesPrevisto + "'," +
                        "fechaIncidencia : '" + fechaIncidencia + "'," +
                        "responsable : '" + responsable + "'," +
                        "prioridad : '" + prioridad + "'" +
                        "}"
                );

                jsonArray.put(alarma);
                editorDatos.putString(usuario, String.valueOf(jsonArray));
                editorDatos.apply();

                System.out.println(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        // Notificación flotante para el usuario
        if (!tipoAlarma.equals("Configuración")) {

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            if (usuario != null) {

                // Sonido por defecto de notificación
                String pref = "Por defecto";
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                //Boolean si esta silenciado en los Ajustes
                Boolean silencio = datos.getBoolean(usuario + "_silencio", false);


                // Según prioridad cogemos un sonido de notificación o otro
                switch (prioridad) {
                    case "Alta":
                        pref = datos.getString(usuario + "_prefAlta", "Por defecto");
                        break;
                    case "Media":
                        pref = datos.getString(usuario + "_prefMedia", "Por defecto");
                        break;
                    case "Baja":
                        pref = datos.getString(usuario + "_prefBaja", "Por defecto");
                }

                // Según las preferencias hacemos sonar el sonido que toque o ninguno
                if (!pref.equals("Por defecto") && !pref.equals("Silencio")) {
                    soundUri = Uri.parse("android.resource://" + this.getPackageName() + "/" + getResources().getIdentifier(pref, "raw", this.getPackageName()));
                }

                if (pref.equals("Silencio") || silencio) {
                    soundUri = null;
                }


                // Según preferencias vibra o no
                Boolean vibracion = datos.getBoolean(usuario + "_vibracion", false);
                if (vibracion) {
                    Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(500);
                }

                if(tituloNotificacion == null){
                    tituloNotificacion = nombreAlarma;
                }

                // Parámetros notificación emergente
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_notificacion) // Icono
                        .setContentTitle(tituloNotificacion)    // Título
                        .setContentText("Prioridad " + prioridad) // Prioridad
                        .setAutoCancel(true)                    // Para que se cierra al abrir la app
                        .setSound(soundUri)                     // Sonido elegido
                        .setContentIntent(pendingIntent);       // Intent

                // Construimos notificación
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(0, notificationBuilder.build());
            }


            // Si el tipo de alarma es de Configuración
        } else if (tipoAlarma.equals("Configuración")) {

            //{'Mercadona' : ['Cedro', 'Plaza España', 'Plaza Honduras'], 'Consum' : ['El Carmen', 'El Cabanyal', 'San Marcelino']}

            // Guardamos los datos en las preferencias de usuario
            if (usuario != null) {
                editorDatos.putString(usuario + "_empresasede", data.get("conf"));
                editorDatos.apply();
            }
        }
    }
}
