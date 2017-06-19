<?php

function sendNotification ($tokens, $mensaje){
	
	$url = "https://fcm.googleapis.com/fcm/send";

	$fields = array(
		'registration_ids' => $tokens,
		'data' => $mensaje
		);

	$headers = array(
		'Authorization:key = AAAA_eqDgmE:APA91bFAitlpJQRd1QrSoOoTc4KmJsFFLadBYy1myEYJkmfouJx5M-4_gocrcX0ojMXgTOJMvqsPB8JpZZD_WGNzLX9MQRtXvj2596RyC6AW_gOYCE-R1tH6UT1UFN-XFJ_1f-Qrm86f',
		'Content-Type : application/json'
		);

	$ch = curl_init();
	curl_setopt($ch, CURLOPT_URL, $url);
	curl_setopt($ch, CURLOPT_POST, true);
	curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	curl_setopt ($ch, CURLOPT_SSL_VERIFYHOST, 0);  
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
	curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
	$result = curl_exec($ch);           
	if ($result === FALSE) {
	   die('Ha ocurrido un error: ' . curl_error($ch));
	}
	curl_close($ch);
	return $result;

	}

/*

// TOKENS MEDIANTE CONSULTA SQL

	$conn = mysqli_connect("localhost", "root", "", "firebase");
	$query = "SELECT token FROM z_demo_usuarios  WHERE usuario = 'invitado'  ";
	$result = mysqli_query($conn, $query);
	mysqli_close($conn);

	while($row = mysqli_fetch_row($result)){
		if($row[0] != null){
			$token_array[] = $row[0];
		}
	}
	
*/

//TOKENS MANUALMENTE
	$token_array = Array(
	    0 => "f6n-h5r07vE:APA91bHNwCir-sn_1El4ZZpCkbsnC3vdpOYRAikU3r5RTuckCgZxYA1CVYjvyJPPF0UGvGGvGrc57vZVnGEbzeCUyp3JHTr1X44lXxgwKRKrAqfk9Ay5DuarsOiZD53gdpaL108pOyvN",
	    1 => "cmToSeu88sU:APA91bH4K9yNJCnoReO780sKbB_y3XzxpySvZ0x8YdcuPaQopBAFbYJqyQJ2x_hmpqca3_SxIcJZaFA3xNBe1FEbFvh3iYiZu7Enz5nboAXqJLKB5DLJ8GKAraAwGwfB79uMGQRzgKRR"
	);


	define("UMBRAL", "Umbral");
	define("OEE", "OEE");
	define("COM", "Comunicación");
	define("EXCESO", "Exceso potencia");
	define("CONF", "Configuración");
	define("GAS", "Predicción gas");



	//ALARMA PREDICCIÓN GAS
	$empresa = "Mercadona";
	$sede = "Plaza Honduras";
	$tituloNotificacion = "Alarma";
	$tipoAlarma = GAS;
	$prioridad = "Media";
	$nombreAlarma = "Alarma para demo";
	$urlAlarma = "https://developer.android.com/reference/java/util/HashMap.html";
	$fechaIncidencia = date("d/m/Y - H:i:s");
	$prediccion = "Te arruinas";
	$contratada = "2 litros de gas solo";
	$costePenalizacion = "5 mil leuros";
	$maximoMesPrevisto = "20 mil";
	$costeMesPrevisto = "35 mil";
	$responsable = "Cristian Garcia Lopez";

	$comentario = "Una mañana, tras un sueño intranquilo, Gregorio Samsa se despertó convertido en un monstruoso insecto. Estaba echado de espaldas sobre un duro caparazón y, al alzar la cabeza, vio su vientre convexo y oscuro, surcado por curvadas callosidades, sobre el que casi no se aguantaba la colcha, que estaba a punto de escurrirse hasta el suelo. Numerosas patas, penosamente delgadas en comparación con el grosor normal de sus piernas, se agitaban sin concierto. - ¿Qué me ha ocurrido? No estaba soñando. Su habitación, una habitación normal, aunque muy pequeña, tenía el aspecto habitual. Sobre la mesa había desparramado un muestrario de paños - Samsa era viajante de comercio-, y de la pared colgaba una estampa recientemente recortada de una revista ilustrada y puesta en un marco dorado. La estampa mostraba a una mujer tocada con un gorro de pieles, envuelta en una estola también de pieles, y que, muy erguida, esgrimía un amplio manguito, asimismo de piel, que ocultaba todo su antebrazo. Gregorio miró hacia la ventana; estaba nublado, y sobre el cinc del alféizar repiqueteaban las gotas de lluvia, lo que le hizo sentir una gran melancolía. «Bueno -pensó-; ¿y si siguiese durmiendo un rato y me olvidase de";



/*
	//ALARMA UMBRAL
	$empresa = "Mercadona";
	$sede = "Plaza Honduras";
	$tituloNotificacion = "Alarma";
	$tipoAlarma = UMBRAL;
	$nombreAlarma = "Alarma para demo";
	$urlAlarma = "https://developer.android.com/reference/java/util/HashMap.html";

	$fechaActivada = date("d/m/Y");
	$horaActivada = date("H:i:s");

	$fechaDesactivada = date("d/m/Y");
	$horaDesactivada = date("H:i:s");

	$prioridad = "Baja";
	$responsable = "John Doe";

	$comentario = "Una mañana, tras un sueño intranquilo, Gregorio Samsa se despertó convertido en un monstruoso insecto. Estaba echado de espaldas sobre un duro caparazón y, al alzar la cabeza, vio su vientre convexo y oscuro, surcado por curvadas callosidades, sobre el que casi no se aguantaba la colcha, que estaba a punto de escurrirse hasta el suelo. Numerosas patas, penosamente delgadas en comparación con el grosor normal de sus piernas, se agitaban sin concierto. - ¿Qué me ha ocurrido? No estaba soñando. Su habitación, una habitación normal, aunque muy pequeña, tenía el aspecto habitual. Sobre la mesa había desparramado un muestrario de paños - Samsa era viajante de comercio-, y de la pared colgaba una estampa recientemente recortada de una revista ilustrada y puesta en un marco dorado. La estampa mostraba a una mujer tocada con un gorro de pieles, envuelta en una estola también de pieles, y que, muy erguida, esgrimía un amplio manguito, asimismo de piel, que ocultaba todo su antebrazo. Gregorio miró hacia la ventana; estaba nublado, y sobre el cinc del alféizar repiqueteaban las gotas de lluvia, lo que le hizo sentir una gran melancolía. «Bueno -pensó-; ¿y si siguiese durmiendo un rato y me olvidase de";


	$activacion = array();

	$activacion[0]["recurso"] = "Electricidad";
	$activacion[0]["proceso"] = "Pinturas S.A.";
	$activacion[0]["variable"] = "Potencia activa total";
	$activacion[0]["valorActivacion"] = "895,70 kW";

	$activacion[1]["recurso"] = "Gas";
	$activacion[1]["proceso"] = "Restaurante S.A.";
	$activacion[1]["variable"] = "Fuga de gas";
	$activacion[1]["valorActivacion"] = "3425,70 kW";

 */
 
/*
	//ALARMA EXCESO POTENCIA
	$empresa = "Plaza del carmen";
	$sede = "Cedro";
	$tituloNotificacion = "Alarma Exceso demo";
	$tipoAlarma = EXCESO;
	$nombreAlarma = "Alarma Exceso";
	$maximetro = "Máximetro 21";
	$urlAlarma = "https://developer.android.com/reference/java/util/HashMap.html";
	$exceso = "Se fue";
	$coste = "2 mil € al menos";
	$periodoTarifario = "Hora punta";
	$costePrevisto = "1.5k €";
	$fechaIncidencia = date("d/m/Y - H:i:s");
	$prioridad = "Baja";
	$responsable = "John Doe";

	$comentario = "Una mañana, tras un sueño intranquilo, Gregorio Samsa se despertó convertido en un monstruoso insecto. Estaba echado de espaldas sobre un duro caparazón y, al alzar la cabeza, vio su vientre convexo y oscuro, surcado por curvadas callosidades, sobre el que casi no se aguantaba la colcha, que estaba a punto de escurrirse hasta el suelo. Numerosas patas, penosamente delgadas en comparación con el grosor normal de sus piernas, se agitaban sin concierto. - ¿Qué me ha ocurrido? No estaba soñando. Su habitación, una habitación normal, aunque muy pequeña, tenía el aspecto habitual. Sobre la mesa había desparramado un muestrario de paños - Samsa era viajante de comercio-, y de la pared colgaba una estampa recientemente recortada de una revista ilustrada y puesta en un marco dorado. La estampa mostraba a una mujer tocada con un gorro de pieles, envuelta en una estola también de pieles, y que, muy erguida, esgrimía un amplio manguito, asimismo de piel, que ocultaba todo su antebrazo. Gregorio miró hacia la ventana; estaba nublado, y sobre el cinc del alféizar repiqueteaban las gotas de lluvia, lo que le hizo sentir una gran melancolía. «Bueno -pensó-; ¿y si siguiese durmiendo un rato y me olvidase de";
*/


/*
	//ENVIO DE CONFIGURACIÓN
	$tipoAlarma = CONF;
	$conf = "{'Plaza del carmen' : ['Cedro', 'Plaza España', 'Plaza Honduras'], 'Consum' : ['El Carmen', 'El Cabanyal', 'San Marcelino'], 'Chovar' : ['ASD', 'weqw gggeg', 'Plaza qweqweqweqwe'], 'QWEQWE del carmen' : ['gfgf', 'erere España', 'asd zz']}";

//$conf = "{'Mercadona' : ['Cedro', 'Plaza España', 'Plaza Honduras'], 'Consum' : ['El Carmen', 'El Cabanyal', 'San Marcelino']}";

*/



/*

	//ALARMA OEE
	$empresa = "Mercadona";
	$sede = "Cedro";
	$tituloNotificacion = "Alarma OEE demo";
	$tipoAlarma = OEE;
	$urlAlarma = "https://stackoverflow.com/questions/8049620/how-to-set-layout-gravity-programmatically";
	$nombreAlarma = "Alarma OEE";
	$proceso = "Soplado";
	$turno = "Mañana";
	$oeeActivado = "77,3";
	$rendimiento = "89,3";
	$disponibilidad = "93,85";
	$fechaActivada = date("d/m/Y");
	$prioridad = "Media";
	$horasActivada = "1,5";
	$oeeDesactivado = "70,39";
	$horasDesactivada = "4,3";
	$responsable = "John Doe";

	$comentario = "Una mañana, tras un sueño intranquilo, Gregorio Samsa se despertó convertido en un monstruoso insecto. Estaba echado de espaldas sobre un duro caparazón y, al alzar la cabeza, vio su vientre convexo y oscuro, surcado por curvadas callosidades, sobre el que casi no se aguantaba la colcha, que estaba a punto de escurrirse hasta el suelo. Numerosas patas, penosamente delgadas en comparación con el grosor normal de sus piernas, se agitaban sin concierto. - ¿Qué me ha ocurrido? No estaba soñando. Su habitación, una habitación normal, aunque muy pequeña, tenía el aspecto habitual. Sobre la mesa había desparramado un muestrario de paños - Samsa era viajante de comercio-, y de la pared colgaba una estampa recientemente recortada de una revista ilustrada y puesta en un marco dorado. La estampa mostraba a una mujer tocada con un gorro de pieles, envuelta en una estola también de pieles, y que, muy erguida, esgrimía un amplio manguito, asimismo de piel, que ocultaba todo su antebrazo. Gregorio miró hacia la ventana; estaba nublado, y sobre el cinc del alféizar repiqueteaban las gotas de lluvia, lo que le hizo sentir una gran melancolía. «Bueno -pensó-; ¿y si siguiese durmiendo un rato y me olvidase de";

*/

/*

	//ALARMA COMUNICACION
	$empresa = "Consum";
	$sede = "San Marcelino";
	$tituloNotificacion = "Alarma Comunicación demo";
	$tipoAlarma = COM;
	$nombreAlarma = "Alarma COM demo";
	$urlAlarma = "https://stackoverflow.com/questions/8049620/how-to-set-layout-gravity-programmatically";
	$sede = "Manantiales";
	$medidor = "IEC102";
	$prioridad = "Baja";
	$fallo = date("d/m/Y - H:i:s");
	$recuperacion = date("d/m/Y - H:i:s");

	$comentario = "Una mañana, tras un sueño intranquilo, Gregorio Samsa se despertó convertido en un monstruoso insecto. Estaba echado de espaldas sobre un duro caparazón y, al alzar la cabeza, vio su vientre convexo y oscuro, surcado por curvadas callosidades, sobre el que casi no se aguantaba la colcha, que estaba a punto de escurrirse hasta el suelo. Numerosas patas, penosamente delgadas en comparación con el grosor normal de sus piernas, se agitaban sin concierto. - ¿Qué me ha ocurrido? No estaba soñando. Su habitación, una habitación normal, aunque muy pequeña, tenía el aspecto habitual. Sobre la mesa había desparramado un muestrario de paños - Samsa era viajante de comercio-, y de la pared colgaba una estampa recientemente recortada de una revista ilustrada y puesta en un marco dorado. La estampa mostraba a una mujer tocada con un gorro de pieles, envuelta en una estola también de pieles, y que, muy erguida, esgrimía un amplio manguito, asimismo de piel, que ocultaba todo su antebrazo. Gregorio miró hacia la ventana; estaba nublado, y sobre el cinc del alféizar repiqueteaban las gotas de lluvia, lo que le hizo sentir una gran melancolía. «Bueno -pensó-; ¿y si siguiese durmiendo un rato y me olvidase de";

*/

	if($tipoAlarma == UMBRAL){
	$mensaje = 	array(
					"empresa" => $empresa,
					"sede" => $sede,
					"tituloNotificacion" => $tituloNotificacion,
					"tipoAlarma" => $tipoAlarma,
					"nombreAlarma" => $nombreAlarma,
					"urlAlarma" => $urlAlarma,
					"fechaActivada" => $fechaActivada,
					"horaActivada" => $horaActivada,
					"fechaDesactivada" => $fechaDesactivada,
					"horaDesactivada" => $horaDesactivada,
					"prioridad" => $prioridad,
					"responsable" => $responsable,
					"comentario" => $comentario,
					"activacion" => $activacion
				);
	}else if($tipoAlarma == OEE){
		$mensaje = 	array(
					"empresa" => $empresa,
					"sede" => $sede,
					"tituloNotificacion" => $tituloNotificacion,
					"tipoAlarma" => $tipoAlarma,
					"urlAlarma" => $urlAlarma,
					"nombreAlarma" => $nombreAlarma,
					"sede" => $sede,
					"proceso" => $proceso,
					"turno" => $turno,
					"oeeActivado" => $oeeActivado,
					"rendimiento" => $rendimiento,
					"disponibilidad" => $disponibilidad,
					"fechaActivada" => $fechaActivada,
					"horasActivada" => $horasActivada,
					"prioridad" => $prioridad,
					"oeeDesactivado" => $oeeDesactivado,
					"horasDesactivada" => $horasDesactivada,
					"comentario" => $comentario,
					"responsable" => $responsable
				);
	}else if($tipoAlarma == COM){
				$mensaje = 	array(
					"empresa" => $empresa,
					"sede" => $sede,
					"tituloNotificacion" => $tituloNotificacion,
					"urlAlarma" => $urlAlarma,
					"tipoAlarma" => $tipoAlarma,
					"nombreAlarma" => $nombreAlarma,
					"medidor" => $medidor,
					"prioridad" => $prioridad,
					"fallo" => $fallo,
					"comentario" => $comentario,
					"recuperacion" => $recuperacion
				);
	}else if($tipoAlarma == EXCESO){
				$mensaje = 	array(
					"empresa" => $empresa,
					"sede" => $sede,
					"tituloNotificacion" => $tituloNotificacion,
					"tipoAlarma" => $tipoAlarma,
					"nombreAlarma" => $nombreAlarma,
					"maximetro" => $maximetro,
					"urlAlarma" => $urlAlarma,
					"exceso" => $exceso,
					"coste" => $coste,
					"periodoTarifario" => $periodoTarifario,
					"costePrevisto" => $costePrevisto,
					"fechaIncidencia" => $fechaIncidencia,
					"prioridad" => $prioridad,
					"comentario" => $comentario,
					"responsable" => $responsable
				);
	}else if($tipoAlarma == GAS){
				$mensaje = 	array(
					"empresa" => $empresa,
					"sede" => $sede,
					"tituloNotificacion" => $tituloNotificacion,
					"tipoAlarma" => $tipoAlarma,
					"nombreAlarma" => $nombreAlarma,
					"contratada" => $contratada,
					"prediccion" => $prediccion,
					"urlAlarma" => $urlAlarma,
					"costePenalizacion" => $costePenalizacion,
					"maximoMesPrevisto" => $maximoMesPrevisto,
					"costeMesPrevisto" => $costeMesPrevisto,
					"fechaIncidencia" => $fechaIncidencia,
					"prioridad" => $prioridad,
					"comentario" => $comentario,
					"responsable" => $responsable
				);
	}else if($tipoAlarma == CONF){
		$mensaje = array(
			"tipoAlarma" => $tipoAlarma,
			"conf" => $conf
			);
	}


	//var_dump($token_array);
	echo "<pre>"; print_r($token_array); echo "</pre>";

	$res = sendNotification($token_array, $mensaje);

	echo $res;

?>