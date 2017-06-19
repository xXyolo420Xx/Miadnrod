<?php 
if (isset($_POST["user"]) && isset($_POST['pass']) && isset($_POST['token'])){

	$user = $_POST["user"];

	$usuario = substr($user, 0, strpos($user, "@"));
	$login = substr($user, strpos($user, "@")+1);

	$token = $_POST['token'];
	$clave = $_POST["pass"];

	if(!$usuario){
		$error = "Usuario no válido";
	}

	if(!$login){
		$error = "Alias empresa no válido";
	}

	if(!$token){
		$error = "Identificador no válido";
	}

	if(@!$error){
		//Primera consulta, sacamos el id de la empresa y el alias, para luego
		$mysqli = new mysqli('localhost', 'root', '', 'firebase');

		$stmt = $mysqli->prepare("SELECT id AS empresaid, alias FROM empresas WHERE login = ? ");

		$stmt->bind_param("s", $login);

		$stmt->execute();

		$stmt->bind_result($empresaid, $alias);

		$stmt->fetch();
		
		$mysqli->close();

		$tablaEmpresa = $alias."_usuarios";

		if(!$empresaid || !$alias || !$tablaEmpresa){
			$error = "Usuario incorrecto";
		}
	}


	if(@!$error){
		//Segunda consulta, sacamos la clave del usuario
		$mysqli = new mysqli('localhost', 'root', '', 'firebase');

		$stmt = $mysqli->prepare("SELECT clave FROM $tablaEmpresa WHERE empresa = ? AND usuario = ?");

		$stmt->bind_param("ss", $empresaid, $usuario);

		$stmt->execute();

		$stmt->bind_result($clavebbdd);

		$stmt->fetch();
				
		$mysqli->close();

		if(!$clavebbdd){
			$error = "Usuario incorrecto";
		}
	}

	if(@!$error){
		//Comprobamos que las claves coincidan
		if($clave == $clavebbdd){
			//Si coinciden
			try{
				$mysqli = new mysqli('localhost', 'root', '', 'firebase');

				$stmt = $mysqli->prepare("UPDATE $tablaEmpresa SET token = NULL WHERE token = ?");

				$stmt->bind_param("s", $token);

				$stmt->execute();
					
				$mysqli->close();

			}catch(Exception $e){

			}


			$mysqli = new mysqli('localhost', 'root', '', 'firebase');

			$stmt = $mysqli->prepare("UPDATE $tablaEmpresa SET token = ? WHERE empresa = ? AND usuario = ? ");

			$stmt->bind_param("sss", $token, $empresaid, $usuario);

			$stmt->execute();
				
			if($mysqli->affected_rows < 1){
				$error = "No se ha podido registrar la aplicación, inténtalo de nuevo";
			}

			$mysqli->close();

		}else{
			$error = "Contraseña incorrecta";
		}
	}


}else{
	$error = "Error conectando al servidor";
	echo $error;
}
if(@$error){
	echo $error;
}else{
	echo "1";
}

?>