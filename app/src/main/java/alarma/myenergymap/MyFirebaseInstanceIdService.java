package alarma.myenergymap;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        SharedPreferences datos = PreferenceManager.getDefaultSharedPreferences(this);;
        SharedPreferences.Editor editor = datos.edit();

        // Cada vez que se refresque el token (No suele pasar a no ser que el usuario reinstale la app) lo deslogeamos
        editor.putString("usuario_logeado", null);
        editor.apply();
    }



}
