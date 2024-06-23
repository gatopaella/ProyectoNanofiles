package es.um.redes.nanoFiles.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;
import es.um.redes.nanoFiles.util.FileInfoExtended;

/**
 * Cliente con métodos de consulta y actualización específicos del directorio
 */
public class DirectoryConnector {
	/**
	 * Puerto en el que atienden los servidores de directorio
	 */
	private static final int DIRECTORY_PORT = 6868;
	/**
	 * Tiempo máximo en milisegundos que se esperará a recibir una respuesta por el
	 * socket antes de que se deba lanzar una excepción SocketTimeoutException para
	 * recuperar el control
	 */
	private static final int TIMEOUT = 1000;
	/**
	 * Número de intentos máximos para obtener del directorio una respuesta a una
	 * solicitud enviada. Cada vez que expira el timeout sin recibir respuesta se
	 * cuenta como un intento.
	 */
	private static final int MAX_NUMBER_OF_ATTEMPTS = 5;

	/**
	 * Valor inválido de la clave de sesión, antes de ser obtenida del directorio al
	 * loguearse
	 */
	public static final int INVALID_SESSION_KEY = -1;

	/**
	 * Socket UDP usado para la comunicación con el directorio
	 */
	private DatagramSocket socket;
	/**
	 * Dirección de socket del directorio (IP:puertoUDP)
	 */
	private InetSocketAddress directoryAddress;

	private int sessionKey = INVALID_SESSION_KEY;
	private boolean successfulResponseStatus;
	private String errorDescription;

	public DirectoryConnector(String address) throws IOException {
		/*
		 * Convertir el nombre de host 'address' a InetAddress y guardar la
		 * dirección de socket (address:DIRECTORY_PORT) del directorio en el atributo
		 * directoryAddress, para poder enviar datagramas a dicho destino.
		 */
		InetAddress serverIp = InetAddress.getByName(address);
		directoryAddress = new InetSocketAddress(serverIp, DIRECTORY_PORT);
		/*
		 * Crea el socket UDP en cualquier puerto para enviar datagramas al
		 * directorio
		 */
		socket = new DatagramSocket();
	}

	/**
	 * Método para enviar y recibir datagramas al/del directorio
	 * 
	 * @param requestData los datos a enviar al directorio (mensaje de solicitud)
	 * @return los datos recibidos del directorio (mensaje de respuesta)
	 */
	private byte[] sendAndReceiveDatagrams(byte[] requestData) {
		byte responseData[] = new byte[DirMessage.PACKET_MAX_SIZE];
		byte response[] = null;
		if (directoryAddress == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP server destination address is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"directoryAddress\"");
			System.exit(-1);

		}
		if (socket == null) {
			System.err.println("DirectoryConnector.sendAndReceiveDatagrams: UDP socket is null!");
			System.err.println(
					"DirectoryConnector.sendAndReceiveDatagrams: make sure constructor initializes field \"socket\"");
			System.exit(-1);
		}
		/*
		 * TODO: Enviar datos en un datagrama al directorio y recibir una respuesta. El
		 * array devuelto debe contener únicamente los datos recibidos, *NO* el búfer de
		 * recepción al completo.
		 */
		DatagramPacket packetToServer = new DatagramPacket(requestData, requestData.length, directoryAddress);
		
		try {
			socket.send(packetToServer);
		} catch (IOException ioException) {
			System.err.println("IOException sending packetToServer");
			System.exit(-1);
		}
		
		DatagramPacket packetFromServer = new DatagramPacket(responseData, responseData.length);
		
		int attempts = 0;
		boolean packetReceived = false;
		while (attempts < MAX_NUMBER_OF_ATTEMPTS && !packetReceived) {
			try {
				socket.setSoTimeout(TIMEOUT);
				socket.receive(packetFromServer);
				packetReceived = true;
			} catch (SocketTimeoutException socketTimeout){
				System.err.println("SocketTimeoutException receiving packetFromServer");
				attempts++;
				if (attempts < MAX_NUMBER_OF_ATTEMPTS - 1) System.err.println("Trying again...");
				
				try { // Esto lo quito si Fernando me lo ordena
					socket.send(packetToServer);
				} catch (IOException ioException) {
					System.err.println("IOException sending packetToServer");
					System.exit(-1);
				}
				
			} catch (IOException ioException) {
				System.err.println("IOException receiving packetFromServer");
				System.exit(-1);
			}
		}
		if (attempts == MAX_NUMBER_OF_ATTEMPTS) {
			System.err.println("Reception failed 5 times");
			System.exit(-1);
		}
		
		response = new byte[packetFromServer.getLength()];
		System.arraycopy(responseData, 0, response, 0, packetFromServer.getLength());
		
		/*
		 * Una vez el envío y recepción asumiendo un canal confiable (sin
		 * pérdidas) esté terminado y probado, debe implementarse un mecanismo de
		 * retransmisión usando temporizador, en caso de que no se reciba respuesta en
		 * el plazo de TIMEOUT. En caso de salte el timeout, se debe reintentar como
		 * máximo en MAX_NUMBER_OF_ATTEMPTS ocasiones.
		 */
		
		/*
		 * Las excepciones que puedan lanzarse al leer/escribir en el socket deben
		 * ser capturadas y tratadas en este método. Si se produce una excepción de
		 * entrada/salida (error del que no es posible recuperarse), se debe informar y
		 * terminar el programa.
		 */
		/*
		 * NOTA: Las excepciones deben tratarse de la más concreta a la más genérica.
		 * SocketTimeoutException es más concreta que IOException.
		 */



		if (response != null && response.length == responseData.length) {
			System.err.println("Your response is as large as the datagram reception buffer!!\n"
					+ "You must extract from the buffer only the bytes that belong to the datagram!");
		}
		return response;
	}

	/**
	 * Método para probar la comunicación con el directorio mediante el envío y
	 * recepción de mensajes sin formatear ("en crudo")
	 * 
	 * @return verdadero si se ha enviado un datagrama y recibido una respuesta
	 */
	public boolean testSendAndReceive() {
		/*
		 * : Probar el correcto funcionamiento de sendAndReceiveDatagrams. Se debe
		 * enviar un datagrama con la cadena "login" y comprobar que la respuesta
		 * recibida es "loginok". En tal caso, devuelve verdadero, falso si la respuesta
		 * no contiene los datos esperados.
		 */
		String login = "login";
		byte[] loginBytes = login.getBytes();
		byte[] responseBytes = sendAndReceiveDatagrams(loginBytes);
		String response = new String(responseBytes, 0, responseBytes.length);
		
		boolean success = false;
		if (response.equals("loginok")) success = true;


		return success;
	}

	public InetSocketAddress getDirectoryAddress() {
		return directoryAddress;
	}

	public int getSessionKey() {
		return sessionKey;
	}

	/**
	 * Método para "iniciar sesión" en el directorio, comprobar que está operativo y
	 * obtener la clave de sesión asociada a este usuario.
	 * 
	 * @param nickname El nickname del usuario a registrar
	 * @return La clave de sesión asignada al usuario que acaba de loguearse, o -1
	 *         en caso de error
	 */
	public boolean logIntoDirectory(String nickname) {
		assert (sessionKey == INVALID_SESSION_KEY);
		boolean success = false;
		// TODO: 1.Crear el mensaje a enviar (objeto DirMessage) con atributos adecuados
		// (operation, etc.) NOTA: Usar como operaciones las constantes definidas en la clase
		// DirMessageOps
		// TODO: 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		
		// TODO: 3.Crear un datagrama con los bytes en que se codifica la cadena
		// TODO: 4.Enviar datagrama y recibir una respuesta (sendAndReceiveDatagrams).
		// TODO: 5.Convertir respuesta recibida en un objeto DirMessage (método
		// DirMessage.fromString)
		// TODO: 6.Extraer datos del objeto DirMessage y procesarlos (p.ej., sessionKey)
		// TODO: 7.Devolver éxito/fracaso de la operación

		DirMessage messageToSend = new DirMessage(DirMessageOps.OPERATION_LOGIN);
		messageToSend.setNickname(nickname);
		String strToSend = messageToSend.toString();
		byte[] messageInBytes = strToSend.getBytes();
		System.out.print("La solicitud login es:\n" + strToSend);
		byte[] messageReceivedInBytes = sendAndReceiveDatagrams(messageInBytes);
		String strReceived = new String(messageReceivedInBytes);
		DirMessage messageReceived = DirMessage.fromString(strReceived);
		System.out.print("El mensjaje recibido es:\n" + strReceived);
		if (messageReceived.getOperation().equals(DirMessageOps.OPERATION_LOGINOK)) {
			sessionKey = messageReceived.getKey();
			success = true;
		}
		
		/* RELIQUIAS DEL PASADO (boletín 3)
		String message = "login&" + nickname;
		byte[] messageInBytes = message.getBytes();
		System.out.println("La solicitud login es: " + message);
		byte[] messageReceivedInBytes = sendAndReceiveDatagrams(messageInBytes);
		String messageReceived = new String(messageReceivedInBytes);
		
		try {
			int num = Integer.parseInt(messageReceived.substring(8));
			if (messageReceived.startsWith("loginok&") && 0 <= num && num <= 1000) {
				success = messageReceived.startsWith("loginok&") && 0 <= num && num <= 1000;
				sessionKey = num;
				System.out.println("¡Login realizado con éxito!");
				System.out.println("sessionKey = " + sessionKey);
				success = true;
			} else {
				success = false;
				System.err.println("Ha habido un error: login't (pon bien la sessionKey melón");
				//TODO que el mensaje de error no sea un suspenso directo
			}
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
			success = false;
		}
		*/
		
		return success;
	}

	/**
	 * Método para obtener la lista de "nicknames" registrados en el directorio.
	 * Opcionalmente, la respuesta puede indicar para cada nickname si dicho peer
	 * está sirviendo ficheros en este instante.
	 * 
	 * @return La lista de nombres de usuario registrados, o null si el directorio
	 *         no pudo satisfacer nuestra solicitud
	 */
	public boolean getUserList() {
		//LinkedList<String> userlist = new LinkedList<>();
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar
		boolean success = false;
		Map<String, Boolean> userMap;
		// TODO: 1.Crear el mensaje a enviar (objeto DirMessage) con atributos adecuados
		// (operation, etc.) NOTA: Usar como operaciones las constantes definidas en la clase
		// DirMessageOps
		// TODO: 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		
		// TODO: 3.Crear un datagrama con los bytes en que se codifica la cadena
		// TODO: 4.Enviar datagrama y recibir una respuesta (sendAndReceiveDatagrams).
		// TODO: 5.Convertir respuesta recibida en un objeto DirMessage (método
		// DirMessage.fromString)
		// TODO: 6.Extraer datos del objeto DirMessage y procesarlos (p.ej., sessionKey)
		// TODO: 7.Devolver éxito/fracaso de la operación

		DirMessage messageToSend = new DirMessage(DirMessageOps.OPERATION_GETUSERLIST);
		messageToSend.setKey(sessionKey);
		String strToSend = messageToSend.toString();
		byte[] messageInBytes = strToSend.getBytes();
		System.out.println("La solicitud de lista de usuarios es:\n" + strToSend);
		byte[] messageReceivedInBytes = sendAndReceiveDatagrams(messageInBytes);
		String strReceived = new String(messageReceivedInBytes);
		DirMessage messageReceived = DirMessage.fromString(strReceived);
		
		
		if (messageReceived.getOperation().equals(DirMessageOps.OPERATION_SENDUSERLIST)) {
			userMap = messageReceived.getUserlist();
			for(String user : userMap.keySet()) {
				System.out.println("user: " + user);
				System.out.println("is server: " + userMap.get(user));
			}
		} else {
			System.out.println("No se ha podido obtener la lista de usuarios");
			System.out.println("Respuesta del directorio:");
			System.out.print(strReceived);
		}

		return success;
	}

	/**
	 * Método para "cerrar sesión" en el directorio
	 * 
	 * @return Verdadero si el directorio eliminó a este usuario exitosamente
	 */
	public boolean logoutFromDirectory() {
		assert (sessionKey != INVALID_SESSION_KEY);
		// TODO: Ver TODOs en logIntoDirectory y seguir esquema similar
		boolean success = false;
		// TODO: 1.Crear el mensaje a enviar (objeto DirMessage) con atributos adecuados
		// (operation, etc.) NOTA: Usar como operaciones las constantes definidas en la clase
		// DirMessageOps
		// TODO: 2.Convertir el objeto DirMessage a enviar a un string (método toString)
		
		// TODO: 3.Crear un datagrama con los bytes en que se codifica la cadena
		// TODO: 4.Enviar datagrama y recibir una respuesta (sendAndReceiveDatagrams).
		// TODO: 5.Convertir respuesta recibida en un objeto DirMessage (método
		// DirMessage.fromString)
		// TODO: 6.Extraer datos del objeto DirMessage y procesarlos (p.ej., sessionKey)
		// TODO: 7.Devolver éxito/fracaso de la operación

		DirMessage messageToSend = new DirMessage(DirMessageOps.OPERATION_LOGOUT);
		messageToSend.setKey(sessionKey);
		
		String strToSend = messageToSend.toString();
		byte[] messageInBytes = messageToSend.toString().getBytes();
		
		System.out.print("La solicitud logout es: " + strToSend);
		
		byte[] messageReceivedInBytes = sendAndReceiveDatagrams(messageInBytes);
		
		String strReceived = new String(messageReceivedInBytes);
		
		DirMessage messageReceived = DirMessage.fromString(strReceived);
		
		System.out.println("Campo operation = " + messageReceived.getOperation());
		
		if (messageReceived.getOperation().equals(DirMessageOps.OPERATION_LOGOUTOK)) {
			success = true;
			sessionKey = INVALID_SESSION_KEY; // Cerramos la sesión
		}

		return success;
	}

	/**
	 * Método para dar de alta como servidor de ficheros en el puerto indicado a
	 * este peer.
	 * 
	 * @param serverPort El puerto TCP en el que este peer sirve ficheros a otros
	 * @return Verdadero si el directorio acepta que este peer se convierta en
	 *         servidor.
	 */
	public boolean registerServerPort(int serverPort) {
		boolean success = false;

		DirMessage messageToSend = new DirMessage(DirMessageOps.OPERATION_REGISTER_SERVER_ADDRESS);
		messageToSend.setKey(sessionKey);
		messageToSend.setPort(serverPort);
		String strToSend = messageToSend.toString();
		byte[] bytesToSend = strToSend.getBytes();
		//System.out.print("El mensaje para registrar el puerto es:\n" + strToSend);
		
		byte[] bytesReceived = sendAndReceiveDatagrams(bytesToSend);
		String strReceived = new String(bytesReceived);
		DirMessage messageReceived = DirMessage.fromString(strReceived);
		String operation = messageReceived.getOperation();
		//System.out.println("Campo operation = " + operation);
		
		if(operation.equals(DirMessageOps.OPERATION_ADDRESSOK)) {
			success = true;
			System.out.println("Address registered in directory");
		} else if (operation.equals(DirMessageOps.OPERATION_INVALIDPORT)) {
			System.out.println("ERRPR: port " + messageToSend.getPort() + " not valid");
		} else if (operation.equals(DirMessageOps.OPERATION_INVALIDKEY)) {
			System.out.println("ERROR: key " + messageToSend.getKey() + " not valid");
		} else {
			System.err.println("ERROR: unexpected response from directory:");
			System.out.print(strReceived);
		}
		
		return success;
	}
	
	public boolean UnregisterServerPort() {
		boolean success = false;
		
		DirMessage msgToSend = new DirMessage(DirMessageOps.OPERATION_REMOVE_SERVER_ADDRESS);
		msgToSend.setKey(sessionKey);
		String strToSend = msgToSend.toString();
		byte[] bytesToSend = strToSend.getBytes();
		
		byte[] bytesReceived = sendAndReceiveDatagrams(bytesToSend);
		String strReceived = new String(bytesReceived);
		DirMessage msgReceived = DirMessage.fromString(strReceived);
		
		String operation = msgReceived.getOperation();
		if (operation.equals(DirMessageOps.OPERATION_REMOVE_ADDRESS_OK)) {
			System.out.println("Server stopped");
			success = true;
		} else if (operation.equals(DirMessageOps.OPERATION_INVALIDKEY)) {
			System.out.println("ERROR: key " + msgToSend.getKey() + " not valid");
		} else { // No tengo en cuenta el caso "no es un servidor" porque se encarga el autómata
			System.err.println("ERROR: unexpected response from directory:");
			System.out.print(strReceived);
		}
		return success;
	}

	/**
	 * Método para obtener del directorio la dirección de socket (IP:puerto)
	 * asociada a un determinado nickname.
	 * 
	 * @param nick El nickname del servidor de ficheros por el que se pregunta
	 * @return La dirección de socket del servidor en caso de que haya algún
	 *         servidor dado de alta en el directorio con ese nick, o null en caso
	 *         contrario.
	 */
	public InetSocketAddress lookupServerAddrByUsername(String nick) {
		InetSocketAddress serverAddr = null;

		DirMessage messageToSend = new DirMessage(DirMessageOps.OPERATION_GET_SERVER_ADDRESS);
		messageToSend.setKey(sessionKey);
		messageToSend.setNickname(nick);
		String strToSend = messageToSend.toString();
		byte[] bytesToSend = strToSend.getBytes();
		
		//System.out.print("La solicitud de dirección es: " + strToSend);
		
		byte[] bytesReceived = sendAndReceiveDatagrams(bytesToSend);
		String strReceived = new String(bytesReceived);
		DirMessage messageReceived = DirMessage.fromString(strReceived);
		
		String operation = messageReceived.getOperation();
		//System.out.print("Mensaje recibido: " + strReceived);
		
		if(operation.equals(DirMessageOps.OPERATION_SEND_SERVER_ADDRESS)) {
			try {
				InetAddress serverIp = InetAddress.getByName(messageReceived.getIpAddress().substring(1));
				int serverPort = messageReceived.getPort();
				System.out.println("Received address: " + serverIp + ":" + serverPort);
				serverAddr = new InetSocketAddress(serverIp, serverPort);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error obtaining the address");
				System.out.println("Returning null...");
			}
		} else if (operation.equals(DirMessageOps.OPERATION_INVALIDKEY)) {
			System.out.println("Error during server address request: invalid key");
			System.out.println("Returning value null...");
		} else if (operation.equals(DirMessageOps.OPERATION_INVALIDNICKNAME)) {
			System.out.println("Error during server address request:");
			System.out.println(nick + " is not a server");
			System.out.println("Returning value null...");
		} else {
			System.err.println("Error during server address request: unexpected response from directory");
		}

		return serverAddr;
	}

	/**
	 * Método para publicar ficheros que este peer servidor de ficheros están
	 * compartiendo.
	 * 
	 * @param files La lista de ficheros que este peer está sirviendo.
	 * @return Verdadero si el directorio tiene registrado a este peer como servidor
	 *         y acepta la lista de ficheros, falso en caso contrario.
	 */
	public boolean publishLocalFiles(FileInfo[] files) {
		boolean success = false;
		LinkedList<FileInfo> filesToPublish = new LinkedList<FileInfo>();
		for (int i = 0; i < files.length; i++) {
			filesToPublish.add(files[i]);
		}
		
		DirMessage msgToSend = new DirMessage(DirMessageOps.OPERATION_PUBLISH);
		msgToSend.setKey(sessionKey);
		msgToSend.setFilelist(filesToPublish);
		String strToSend = msgToSend.toString();
		byte[] bytesToSend = strToSend.getBytes();
		
		System.out.print("Sending message\n" + strToSend);
		
		byte[] bytesReceived = sendAndReceiveDatagrams(bytesToSend);
		String strReceived = new String(bytesReceived);
		DirMessage msgReceived = DirMessage.fromString(strReceived);
		
		if (msgReceived.getOperation().equals(DirMessageOps.OPERATION_PUBLISHOK)) {
			System.out.println("Files published");
			success = true;
		} else if (msgReceived.getOperation().equals(DirMessageOps.OPERATION_INVALIDKEY)) {
			System.out.println("key " + msgToSend.getKey() + " not registered in directory");
		} else {
			System.err.println("ERROR: unexpected response from directory:");
			System.out.print(strReceived);
		}

		return success;
	}

	/**
	 * Método para obtener la lista de ficheros que los peers servidores han
	 * publicado al directorio. Para cada fichero se debe obtener un objeto FileInfo
	 * con nombre, tamaño y hash. Opcionalmente, puede incluirse para cada fichero,
	 * su lista de peers servidores que lo están compartiendo.
	 * 
	 * @return Los ficheros publicados al directorio, o null si el directorio no
	 *         pudo satisfacer nuestra solicitud
	 */
	public List<FileInfoExtended> getFileList() {
		List<FileInfoExtended> filelist = null;
		
		DirMessage msgToSend = new DirMessage(DirMessageOps.OPERATION_GET_FILELIST);
		msgToSend.setKey(sessionKey);
		String strToSend = msgToSend.toString();
		byte[] bytesToSend = strToSend.getBytes();
		System.out.println("Sending request to directory:");
		System.out.print(strToSend);
		
		byte[] bytesReceived = sendAndReceiveDatagrams(bytesToSend);
		String strReceived = new String(bytesReceived);
		DirMessage msgReceived = DirMessage.fromString(strReceived);
		
		if (msgReceived.getOperation().equals(DirMessageOps.OPERATION_SEND_FILELIST)) {
			System.out.println("List received");
			filelist = msgReceived.getExtendedFilelist();
		} else if (msgReceived.getOperation().equals(DirMessageOps.OPERATION_INVALIDKEY)) {
			System.out.println("key " + msgToSend.getKey() + " not registered in directory");
		} else {
			System.err.println("ERROR: unexpected response from directory:");
			System.out.print(strReceived);
		}
		
		return filelist;
	}

	/**
	 * Método para obtener la lista de nicknames de los peers servidores que tienen
	 * un fichero identificado por su hash. Opcionalmente, puede aceptar también
	 * buscar por una subcadena del hash, en vez de por el hash completo.
	 * 
	 * @return La lista de nicknames de los servidores que han publicado al
	 *         directorio el fichero indicado. Si no hay ningún servidor, devuelve
	 *         una lista vacía.
	 */
	public List<String> getServerNicknamesSharingThisFile(String fileHash) {
		List<String> nicklist = null;
		
		DirMessage msgToSend = new DirMessage(DirMessageOps.OPERATION_SEARCH);
		msgToSend.setKey(sessionKey);
		msgToSend.setHash(fileHash);
		String strToSend = msgToSend.toString();
		byte[] bytesToSend = strToSend.getBytes();
		System.out.println("Sending request:");
		System.out.print(strToSend);
		
		byte[] bytesReceived = sendAndReceiveDatagrams(bytesToSend);
		String strReceived = new String(bytesReceived);
		DirMessage msgReceived = DirMessage.fromString(strReceived);
		
		if (msgReceived.getOperation().equals(DirMessageOps.OPERATION_SEARCH_RESULTS)) {
			nicklist = msgReceived.getServerList();
			for (String nick : nicklist) {
				System.out.println(nick);
			}
			
		} else if (msgReceived.getOperation().equals(DirMessageOps.OPERATION_INVALIDKEY)) {
			System.out.println("key " + msgToSend.getKey() + " not registered in directory");
		} else {
			System.err.println("ERROR: unexpected response from directory:");
			System.out.print(strReceived);
		}


		return nicklist;
	}




}
