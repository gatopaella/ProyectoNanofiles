package es.um.redes.nanoFiles.udp.message;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import es.um.redes.nanoFiles.util.FileInfo;
import es.um.redes.nanoFiles.util.FileInfoExtended;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	private static final String FIELDNAME_NICKNAME = "nickname";
	private static final String FIELDNAME_KEY = "key";
	private static final String FIELDNAME_USER = "user";
	private static final String FIELDNAME_ISSERVER = "isServer";
	private static final String FIELDNAME_IP = "ipAddress";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_FILENAME = "name";
	private static final String FIELDNAME_FILEHASH = "hash";
	private static final String FIELDNAME_FILESIZE = "size";
	private static final String FIELDNAME_FILEPATH = "path";
	private static final String FIELDNAME_SERVER = "server";
	
	/*
	 * TODO: Definir de manera simbólica los nombres de todos los campos que pueden
	 * aparecer en los mensajes de este protocolo (formato campo:valor)
	 */

	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/*
	 * TODO: Crear un atributo correspondiente a cada uno de los campos de los
	 * diferentes mensajes de este protocolo.
	 */
	
	//TODO ¿Inicializarlos por defecto?
	private String nickname;
	private int key;
	private String ipAddress;
	private int port;
	private HashMap<String, Boolean> userlist;
	private LinkedList<FileInfo> filelist;
	private LinkedList<FileInfoExtended> extendedFilelist;
	private LinkedList<String> serverList;
	private String hash;
	
	public static final int LOGIN_FAILED_KEY = -1;

	public DirMessage(String op) {
		operation = op;
		userlist = new HashMap<String, Boolean>();
		filelist = new LinkedList<FileInfo>();
		serverList = new LinkedList<String>();
		extendedFilelist = new LinkedList<FileInfoExtended>();
	}


	/*
	 * TODO: Crear diferentes constructores adecuados para construir mensajes de
	 * diferentes tipos con sus correspondientes argumentos (campos del mensaje)
	 */

	public String getOperation() {
		return operation;
	}

	public void setNickname(String nick) {
		//TODO comprobar que el campo es correcto para el mensaje escogido
		nickname = nick;
	}

	public void setKey(int key) {
		//TODO que compruebe que el campo es correcto en el mensaje escogido
		this.key = key;
	}
	
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public void setPort(int port) {
		//TODO comprobar que el campo existe en el mensaje escogido
		this.port = port;
	}
	
	public void setUserlist(Map<String, Boolean> userlist) {
		//TODO comprobar que este campo está en el mensaje
		this.userlist = new HashMap<String, Boolean>(userlist);
	}
	
	public void setFilelist(List<FileInfo> filelist) {
		this.filelist = new LinkedList<FileInfo>(filelist);
	}
	
	
	public void addUserToList(String nick, Boolean isServer) {
		userlist.put(nick, isServer);
	}
	
	public void setServerList(List<String> servers) {
		this.serverList = new LinkedList<String>(servers);
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public void setExtendedFilelist(List<FileInfoExtended> extendedFilelist) {
		this.extendedFilelist = new LinkedList<FileInfoExtended>(extendedFilelist);
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public int getKey() {
		return key;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	
	public int getPort() {
		return port;
	}

	public Map<String, Boolean> getUserlist() {
		return Collections.unmodifiableMap(userlist);
	}
	
	public List<FileInfo> getFilelist() {
		return Collections.unmodifiableList(filelist);
	}
	
	public List<String> getServerList() {
		return Collections.unmodifiableList(serverList);
	}
	
	public String getHash() {
		return hash;
	}
	
	public List<FileInfoExtended> getExtendedFilelist() {
		return Collections.unmodifiableList(extendedFilelist);
	}
	

	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * TODO: Usar un bucle para parsear el mensaje línea a línea, extrayendo para
		 * cada línea el nombre del campo y el valor, usando el delimitador DELIMITER, y
		 * guardarlo en variables locales.
		 */
		
		// System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		String[] lines = message.split(END_LINE + "");
		// Local variables to save data during parsing
		DirMessage m = null;
		String user = "INVALID_USER";
		String filename = "INVALID_NAME";
		String hash = "INVALID_HASH";
		FileInfoExtended fileInfoExtended = null;
		long size = -1;

		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			//String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String fieldName = line.substring(0, idx); // Las mayúsculas las respetas
			String value = line.substring(idx + 1).trim();
			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			case FIELDNAME_NICKNAME: {
				m.setNickname(value);
				break;
			}
			case FIELDNAME_KEY: {
				m.setKey(Integer.parseInt(value));
				break;
			}
			case FIELDNAME_USER: {
				user = value;
				break;
			}
			case FIELDNAME_ISSERVER: {
				m.addUserToList(user, Boolean.parseBoolean(value));
				break;
			}
			case FIELDNAME_IP: {
				m.setIpAddress(value);
				break;
			}
			case FIELDNAME_PORT: {
				m.setPort(Integer.parseInt(value));
				break;
			}
			case FIELDNAME_FILENAME: {
				if (fileInfoExtended != null) {
					m.extendedFilelist.add(fileInfoExtended);
				}
				filename = value;
				break;
			}
			case FIELDNAME_FILEHASH: {
				hash = value;
				m.setHash(hash);
				break;
			}
			case FIELDNAME_FILESIZE: {
				size = Long.parseLong(value);
				break;
			}
			case FIELDNAME_FILEPATH: {
				String path = value;
				m.filelist.add(new FileInfo(hash, filename, size, path));
				if (m.operation.equals(DirMessageOps.OPERATION_SEND_FILELIST)) {
					fileInfoExtended = new FileInfoExtended(new FileInfo(hash, filename, size, path));
				}
				break;
			}
			case FIELDNAME_SERVER: {
				m.serverList.add(value);
				
				if (m.operation.equals(DirMessageOps.OPERATION_SEND_FILELIST)) {
					fileInfoExtended.addNickToList(value);
				}
				
				break;
			}
			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}
		
		if (fileInfoExtended != null) {
			m.extendedFilelist.add(fileInfoExtended);
		}

		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * TODO: En función del tipo de mensaje, crear una cadena con el tipo y
		 * concatenar el resto de campos necesarios usando los valores de los atributos
		 * del objeto.
		 */
		switch (operation) {
		case DirMessageOps.OPERATION_LOGIN: { // Si es un mensaje de login
			sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_LOGINOK: {
			sb.append(FIELDNAME_KEY + DELIMITER + key + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_LOGOUT: {
			sb.append(FIELDNAME_KEY + DELIMITER + key + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_LOGOUTOK: {
			break;
		}
		case DirMessageOps.OPERATION_GETUSERLIST: {
			sb.append(FIELDNAME_KEY + DELIMITER + key + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_SENDUSERLIST: {
			for(String user : userlist.keySet()) {
				sb.append(FIELDNAME_USER + DELIMITER + user + END_LINE);
				sb.append(FIELDNAME_ISSERVER + DELIMITER + userlist.get(user) + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_REGISTER_SERVER_ADDRESS: {
			sb.append(FIELDNAME_KEY + DELIMITER + key + END_LINE);
			sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_GET_SERVER_ADDRESS: {
			sb.append(FIELDNAME_KEY + DELIMITER + key + END_LINE);
			sb.append(FIELDNAME_NICKNAME + DELIMITER + nickname + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_SEND_SERVER_ADDRESS: {
			sb.append(FIELDNAME_IP + DELIMITER + ipAddress + END_LINE);
			sb.append(FIELDNAME_PORT + DELIMITER + port + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_ADDRESSOK: {
			break;
		}
		case DirMessageOps.OPERATION_REMOVE_SERVER_ADDRESS: {
			sb.append(FIELDNAME_KEY + DELIMITER + key + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_REMOVE_ADDRESS_OK: {
			break;
		}
		case DirMessageOps.OPERATION_PUBLISH: {
			sb.append(FIELDNAME_KEY + DELIMITER + key + END_LINE);
			for (FileInfo file : filelist) {
				sb.append(FIELDNAME_FILENAME + DELIMITER + file.fileName + END_LINE);
				sb.append(FIELDNAME_FILEHASH + DELIMITER + file.fileHash + END_LINE);
				sb.append(FIELDNAME_FILESIZE + DELIMITER + file.fileSize + END_LINE);
				sb.append(FIELDNAME_FILEPATH + DELIMITER + file.filePath + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_PUBLISHOK: {
			break;
		}
		case DirMessageOps.OPERATION_SEARCH: {
			sb.append(FIELDNAME_KEY + DELIMITER + key + END_LINE);
			sb.append(FIELDNAME_FILEHASH + DELIMITER + hash + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_SEARCH_RESULTS: {
			for(String server : serverList) {
				sb.append(FIELDNAME_SERVER + DELIMITER + server + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_GET_FILELIST: {
			sb.append(FIELDNAME_KEY + DELIMITER + key + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_SEND_FILELIST: {
			for(FileInfoExtended file : extendedFilelist) {
				sb.append(FIELDNAME_FILENAME + DELIMITER + file.getFileInfo().fileName + END_LINE);
				sb.append(FIELDNAME_FILEHASH + DELIMITER + file.getFileInfo().fileHash + END_LINE);
				sb.append(FIELDNAME_FILESIZE + DELIMITER + file.getFileInfo().fileSize + END_LINE);
				sb.append(FIELDNAME_FILEPATH + DELIMITER + file.getFileInfo().filePath + END_LINE);
				for (String server : file.getNicklist()) {
					sb.append(FIELDNAME_SERVER + DELIMITER + server + END_LINE);
				}
			}
			
			break;
		}
		case DirMessageOps.OPERATION_INVALIDNICKNAME: {
			break;
		}
		case DirMessageOps.OPERATION_INVALIDKEY: {
			break;
		}
		case DirMessageOps.OPERATION_INVALIDPORT: {
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + operation);
		}


		sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}
}
