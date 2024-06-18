package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;
import es.um.redes.nanoFiles.util.FileInfoExtended;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;

	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/**
	 * Estructura para guardar los nicks de usuarios registrados, y clave de sesión
	 * 
	 */
	private HashMap<String, Integer> nicks; // Nombre lioso de narices
	/**
	 * Estructura para guardar las claves de sesión y sus nicks de usuario asociados
	 * 
	 */
	private HashMap<Integer, String> sessionKeys; // Nombre confuso de narices
	
	/*
	 * TODO: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */
	private HashMap<Integer, InetSocketAddress> servers;
	
	private HashMap<Integer, LinkedList<FileInfo>> files;
	private HashMap<String, LinkedList<String>> serversByHash;

	/**
	 * Generador de claves de sesión aleatorias (sessionKeys)
	 */
	Random random = new Random();
	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * : (Boletín UDP) Inicializar el atributo socket: Crear un socket UDP ligado al
		 * puerto especificado por el argumento directoryPort en la máquina local,
		 */
		socket = new DatagramSocket(DIRECTORY_PORT);
		/*
		 * : (Boletín UDP) Inicializar el resto de atributos de esta clase (estructuras
		 * de datos que mantiene el servidor: nicks, sessionKeys, etc.)
		 */
		nicks = new HashMap<String, Integer>();
		sessionKeys = new HashMap<Integer, String>();
		
		servers = new HashMap<Integer, InetSocketAddress>();
		
		files = new HashMap<Integer, LinkedList<FileInfo>>();
		serversByHash = new HashMap<String, LinkedList<String>>();

		if (NanoFiles.testMode) { // Esto para el primer / segundo boletín
			if (socket == null || nicks == null || sessionKeys == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	public void run() throws IOException {
		byte[] receptionBuffer = null;
		InetSocketAddress clientAddr = null;
		int dataLength = -1;
		/*
		 * : (Boletín UDP) Crear un búfer para recibir datagramas y un datagrama
		 * asociado al búfer
		 */
		receptionBuffer = new byte[DirMessage.PACKET_MAX_SIZE];
		DatagramPacket packetFromClient = new DatagramPacket(receptionBuffer, receptionBuffer.length);

		System.out.println("Directory starting...");

		while (true) { // Bucle principal del servidor de directorio

			// : (Boletín UDP) Recibimos a través del socket un datagrama
			socket.receive(packetFromClient);
			// : (Boletín UDP) Establecemos dataLength con longitud del datagrama
			// recibido
			dataLength = packetFromClient.getLength();
			// : (Boletín UDP) Establecemos 'clientAddr' con la dirección del cliente,
			// obtenida del
			// datagrama recibido
			clientAddr = new InetSocketAddress(packetFromClient.getAddress(), packetFromClient.getPort());

			if (NanoFiles.testMode) {
				if (receptionBuffer == null || clientAddr == null || dataLength < 0) {
					System.err.println("NFDirectoryServer.run: code not yet fully functional.\n"
							+ "Check that all TODOs have been correctly addressed!");
					System.exit(-1);
				}
			}
			System.out.println("Directory received datagram from " + clientAddr + " of size " + dataLength + " bytes");

			// Analizamos la solicitud y la procesamos
			if (dataLength > 0) {
				String strFromClient = null;
				/*
				 * : (Boletín UDP) Construir una cadena a partir de los datos recibidos en el
				 * buffer de recepción
				 */
				strFromClient = new String(receptionBuffer, 0, packetFromClient.getLength());

				if (NanoFiles.testMode) { // En modo de prueba (mensajes en "crudo", boletín UDP)
					System.out.println("[testMode] Contents interpreted as " + dataLength + "-byte String: \""
							+ strFromClient + "\"");
					/*
					 * TODO: (Boletín UDP) Comprobar que se ha recibido un datagrama con la cadena
					 * "login" y en ese caso enviar como respuesta un mensaje al cliente con la
					 * cadena "loginok". Si el mensaje recibido no es "login", se informa del error
					 * y no se envía ninguna respuesta.
					 */
					if (strFromClient.equals("login")) {
						String responseToClient = "loginok";
						byte[] responseBuffer = responseToClient.getBytes();
						DatagramPacket packetToClient = new DatagramPacket(responseBuffer, responseBuffer.length, clientAddr);
						socket.send(packetToClient);
					} else {
						System.err.println("Loginn't");
					}

				} else { // Servidor funcionando en modo producción (mensajes bien formados)

					// Vemos si el mensaje debe ser ignorado por la probabilidad de descarte
					double rand = Math.random(); // + Simular el descarte de segmentos UDP
					if (rand < messageDiscardProbability) {
						System.err.println("Directory DISCARDED datagram from " + clientAddr);
						continue;
					}
					//TODO posibles println
					DirMessage messageFromClient = DirMessage.fromString(strFromClient);
					DirMessage messageToClient = buildResponseFromRequest(messageFromClient, clientAddr);
					String strToClient = messageToClient.toString();
					DatagramPacket packetToSend = new DatagramPacket(strToClient.getBytes(), strToClient.length(), clientAddr);
					this.socket.send(packetToSend);
					
					/*CÓDIGO COPIADO DE MARIO
					System.out.println("quiero mandarte la respuesta pero el mensaje es "+messageFromClient);
                    if(messageFromClient.contains("login")) {
                        String nickname = messageFromClient.split("&")[1];
                        String strResponse = null;
                        if(this.nicks.containsKey(nickname)) {
                            strResponse = "login_failed:-1";
                        }
                        else {
                            int num;
                            do {
                                num = random.nextInt(1000);
                            } while (this.nicks.containsValue(num));

                            this.nicks.put(nickname, num);
                            strResponse = "loginok&"+num;
                            System.out.println("Te respondo maestro");
                        }


                        DatagramPacket packetToSend = new DatagramPacket(strResponse.getBytes(), strResponse.length(), clientAddr);
                        this.socket.send(packetToSend);
                        System.out.println();
                    } */
					
					/* EL CÓDIGO DEFECTUOSO QUE HICE YO EMPIEZA AQUÍ
					if(messageFromClient.startsWith("login&")) {
						int sessionKey = random.nextInt(1001); // Número aleatorio entre 0 y 1000
						String responseText = "loginok&" + sessionKey;
						byte[] responseData = responseText.getBytes();
						DatagramPacket packetToClient = new DatagramPacket(responseData, responseData.length, clientAddr);
						try {
							System.out.println("Mandando mensaje: " + responseText);
							socket.send(packetToClient);
						} catch (IOException e) {
							System.err.println(e.getLocalizedMessage());
						}
					}
					Y TERMINA AQUÍ */

					/*
					 * TODO: Construir String partir de los datos recibidos en el datagrama. A
					 * continuación, imprimir por pantalla dicha cadena a modo de depuración.
					 * Después, usar la cadena para construir un objeto DirMessage que contenga en
					 * sus atributos los valores del mensaje (fromString).
					 */
					/*
					 * TODO: Llamar a buildResponseFromRequest para construir, a partir del objeto
					 * DirMessage con los valores del mensaje de petición recibido, un nuevo objeto
					 * DirMessage con el mensaje de respuesta a enviar. Los atributos del objeto
					 * DirMessage de respuesta deben haber sido establecidos con los valores
					 * adecuados para los diferentes campos del mensaje (operation, etc.)
					 */
					/*
					 * TODO: Convertir en string el objeto DirMessage con el mensaje de respuesta a
					 * enviar, extraer los bytes en que se codifica el string (getBytes), y
					 * finalmente enviarlos en un datagrama
					 */

				}
			} else {
				System.err.println("Directory ignores EMPTY datagram from " + clientAddr);
			}

		}
	}

	private DirMessage buildResponseFromRequest(DirMessage msg, InetSocketAddress clientAddr) {
		/*
		 * TODO: Construir un DirMessage con la respuesta en función del tipo de mensaje
		 * recibido, leyendo/modificando según sea necesario los atributos de esta clase
		 * (el "estado" guardado en el directorio: nicks, sessionKeys, servers,
		 * files...)
		 */
		String operation = msg.getOperation();

		DirMessage response = null;

		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN: {
			String username = msg.getNickname();

			/*
			 * TODO: Comprobamos si tenemos dicho usuario registrado (atributo "nicks"). Si
			 * no está, generamos su sessionKey (número aleatorio entre 0 y 1000) y añadimos
			 * el nick y su sessionKey asociada. NOTA: Puedes usar random.nextInt(1000)
			 * para generar la session key
			 */
			
			if (sessionKeys.containsValue(username)) {
				response= new DirMessage(DirMessageOps.OPERATION_INVALIDNICKNAME);
				System.out.println("El usuario " + username + " ya está registrado, melón");
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_LOGINOK);
				int key;
				do {
					key = random.nextInt(1001);
				} while (sessionKeys.containsKey(key));
				//nicks.put(username, key);
				sessionKeys.put(key, username);
				nicks.put(username, key);
				response.setKey(key);
			}
			System.out.println("Mensaje de respuesta:");
			System.out.println(response.toString());
			
			/*
			 * TODO: Construimos un mensaje de respuesta que indique el éxito/fracaso del
			 * login y contenga la sessionKey en caso de éxito, y lo devolvemos como
			 * resultado del método.
			 */
			/*
			 * TODO: Imprimimos por pantalla el resultado de procesar la petición recibida
			 * (éxito o fracaso) con los datos relevantes, a modo de depuración en el
			 * servidor
			 */

			break;
		}
		case DirMessageOps.OPERATION_LOGOUT: {
			int key = msg.getKey();
			
			if (sessionKeys.containsKey(key)) {
				response = new DirMessage(DirMessageOps.OPERATION_LOGOUTOK);
				System.out.println("Cerrando la sesión del usuario " + sessionKeys.get(key));
				sessionKeys.remove(key);
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_INVALIDKEY);
				System.out.println("La clave " + key + " no está registrada, melón");
			}
			
			break;
		}
		case DirMessageOps.OPERATION_GETUSERLIST: {
			int key = msg.getKey();
			if (sessionKeys.containsKey(key)) {
				response = new DirMessage(DirMessageOps.OPERATION_SENDUSERLIST);
				HashMap<String, Boolean> userlist = new HashMap<>();
				for(String user : sessionKeys.values()) {
					int serverKey = nicks.get(user);
					boolean isServer = servers.containsKey(serverKey);
					userlist.put(user, isServer);
				}
				response.setUserlist(userlist);
				System.out.println("Mandando la lista de usuarios" + userlist);
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_INVALIDKEY);
				System.out.println("La clave " + key + " no está registrada, melón");
			}
			
			break;
		}
		case DirMessageOps.OPERATION_REGISTER_SERVER_PORT: {
			int key = msg.getKey();
			int serverPort = msg.getPort();
			if (sessionKeys.containsKey(key) && serverPort > 0) {
				InetSocketAddress serverAddr = new InetSocketAddress(clientAddr.getAddress(), serverPort);
				servers.put(key, serverAddr);
				response = new DirMessage(DirMessageOps.OPERATION_PORTOK);
			} else if (serverPort > 0) {
				response = new DirMessage(DirMessageOps.OPERATION_INVALIDKEY);
				System.out.println("La clave " + key + " no está registrada, melón");
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_INVALIDPORT);
				System.out.println("NANI... IMPOSIBLE... no he visto un puerto negativo desde la Era Heian");
			}
			break;
		}
		case DirMessageOps.OPERATION_REMOVE_SERVER_PORT: {
			int key = msg.getKey();
			String serverNick = sessionKeys.get(key);
			if (sessionKeys.containsKey(key)) {
				servers.remove(key);
				
				files.remove(key);
				
				Iterator<Entry<String, LinkedList<String>>> it = serversByHash.entrySet().iterator();
				while(it.hasNext()) { // Para cada archivo
					Entry<String, LinkedList<String>> entry = it.next();
					entry.getValue().remove(serverNick); // Actualizamos que no lo está compartiendo
					if (entry.getValue().isEmpty())
						it.remove();
				}
				/*
				for (String hash : serversByHash.keySet()) { // Para cada archivo
					serversByHash.get(hash).remove(serverNick); // Actualizamos que no lo está compartiendo
					if (serversByHash.get(hash).isEmpty())
						serversByHash.remove(hash);
				}
				*/
				
				response = new DirMessage(DirMessageOps.OPERATION_REMOVE_PORT_OK);
				System.out.println("Enviando confirmación de stopserver");
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_INVALIDKEY);
				System.out.println("La clave " + key + " no está registrada, melón");
			}
			
			break;
		}
		case DirMessageOps.OPERATION_GET_SERVER_ADDRESS: {
			int key = msg.getKey();
			String nick = msg.getNickname();
			if (nicks.containsKey(nick)) {
				int serverKey = nicks.get(nick);
				if (sessionKeys.containsKey(key) && servers.containsKey(serverKey)) {
					InetSocketAddress serverAddr = servers.get(serverKey);
					String serverIpAddr = serverAddr.getAddress().toString();
					int serverPort = serverAddr.getPort();
					
					response = new DirMessage(DirMessageOps.OPERATION_SEND_SERVER_ADDRESS);
					response.setIpAddress(serverIpAddr);
					response.setPort(serverPort);
					System.out.println("Enviando dirección del servidor " + nick + "...");
					
				} else if (servers.containsKey(nick)) { // Es decir, si lo que falla es la clave
					response = new DirMessage(DirMessageOps.OPERATION_INVALIDKEY);
					System.out.println("La clave " + key + " no está registrada, melón");
				} else { // Si lo que falla es que el peer no es servidor
					response = new DirMessage(DirMessageOps.OPERATION_INVALIDNICKNAME);
					System.out.println("El peer " + nick + " no es un servidor");
				}
			} else {
				response = new DirMessage(DirMessageOps.OPERATION_INVALIDNICKNAME);
				System.out.println("El peer " + nick + " no está logueado");
			}
			
			break;
		}
		case DirMessageOps.OPERATION_PUBLISH: {
			int key = msg.getKey();
			List<FileInfo> filelist = msg.getFilelist();
			if (sessionKeys.containsKey(key)) {
				LinkedList<FileInfo> filesToAdd = new LinkedList<>();
				for(FileInfo file : filelist) {
					filesToAdd.add(file); // Registrar los archivos en la lista del servidor
					
					String fileHash = file.fileHash; // Registrar que este peer sirve este archivo
					if(!serversByHash.containsKey(fileHash)) {
						serversByHash.put(fileHash, new LinkedList<String>());
					}
					String nick = sessionKeys.get(key);
					if (!serversByHash.get(fileHash).contains(nick)) {
						serversByHash.get(fileHash).add(nick);
					} // Para que haciendo publish repetidas veces no aparezcan duplicados
				}
				files.put(key, filesToAdd);
				System.out.println("Se han registrado los archivos publicados");
				
				response = new DirMessage(DirMessageOps.OPERATION_PUBLISHOK);
				System.out.println("Enviando confirmación...");
			} else {
				System.out.println("key " + key + " not registered in directory");
				response = new DirMessage(DirMessageOps.OPERATION_INVALIDKEY);
			}
			break;
		}
		case DirMessageOps.OPERATION_SEARCH: {
			int key = msg.getKey();
			String fileHashSubstr = msg.getHash();
			LinkedList<String> serverList = new LinkedList<>();
			
			if (sessionKeys.containsKey(key)) {
				String fileHash = "invalidHash";
				int nHashMatches = 0;
				for (String hash : serversByHash.keySet()) {
					if (hash.contains(fileHashSubstr)) {
						fileHash = hash;
						nHashMatches++;
					}
				}
				if (nHashMatches == 0) {
					System.out.println("there are no servers providing the file identified by hash " + fileHashSubstr);
					System.out.println("returning empty list");
					response = new DirMessage(DirMessageOps.OPERATION_SEARCH_RESULTS);
					response.setServerList(new LinkedList<String>());
				} else if (nHashMatches == 1) { // Entonces fileHash será esa única coincidencia
					response = new DirMessage(DirMessageOps.OPERATION_SEARCH_RESULTS); 
					for (String srver : serversByHash.get(fileHash)) { // Añade solo los que son servidores
						if(servers.containsKey(nicks.get(srver))) {
							serverList.add(srver);
						}
					}
					response.setServerList(serverList);
					System.out.println("Servidores encontrados : " + serverList);
				} else { // Substring de hash ambigua
					System.out.println("more than one file cointains the hash substr: " + fileHashSubstr);
					System.out.println("returning empty list");
					response = new DirMessage(DirMessageOps.OPERATION_SEARCH_RESULTS);
					response.setServerList(new LinkedList<String>());
				}
			}
			else {
				System.out.println("key " + key + " not registered in directory");
				response = new DirMessage(DirMessageOps.OPERATION_INVALIDKEY);
			}
			
			
			break;
		}
		case DirMessageOps.OPERATION_GET_FILELIST: {
			int key = msg.getKey();
			LinkedList<FileInfoExtended> filelist = new LinkedList<FileInfoExtended>();
			if (sessionKeys.containsKey(key)) {
				response = new DirMessage(DirMessageOps.OPERATION_SEND_FILELIST);
				for(int serverKey : files.keySet()) {
					List<FileInfo> filesPublishedByServer = files.get(serverKey);
					for (FileInfo file : filesPublishedByServer) {
						String fileHash = file.fileHash;
						FileInfoExtended fileExtended = new FileInfoExtended(file);
						List<String> fileServerList = serversByHash.get(fileHash);
						for (String serverNick : fileServerList) {
							if(servers.containsKey(nicks.get(serverNick))) {
								fileExtended.addNickToList(serverNick);
							}
						}
						filelist.add(fileExtended);
					}
				}
				response.setExtendedFilelist(filelist);
			} else {
				System.out.println("key " + key + " not registered in directory");
				response = new DirMessage(DirMessageOps.OPERATION_INVALIDKEY);
			}
			break;
		}
		default:
			System.out.println("Unexpected message operation: \"" + operation + "\"");
		}
		return response;

	}
}
