package es.um.redes.nanoFiles.udp.message;

public class DirMessageOps {

	/*
	 * TODO: Añadir aquí todas las constantes que definen los diferentes tipos de
	 * mensajes del protocolo de comunicación con el directorio.
	 */
	public static final String OPERATION_INVALID = "invalid_operation";
	public static final String OPERATION_LOGIN = "login";
	public static final String OPERATION_LOGINOK = "loginOK";
	public static final String OPERATION_LOGOUT = "logout";
	public static final String OPERATION_LOGOUTOK = "logoutOK";
	public static final String OPERATION_GETUSERLIST = "getUserlist";
	public static final String OPERATION_SENDUSERLIST = "sendUserlist";
	public static final String OPERATION_REGISTER_SERVER_PORT = "registerServerPort";
	public static final String OPERATION_PORTOK = "portOK";
	public static final String OPERATION_REMOVE_SERVER_PORT = "removeServerPort";
	public static final String OPERATION_REMOVE_PORT_OK = "removePortOK";
	public static final String OPERATION_GET_SERVER_ADDRESS = "getServerAddress";
	public static final String OPERATION_SEND_SERVER_ADDRESS = "sendServerAddress";
	public static final String OPERATION_PUBLISH = "publish";
	public static final String OPERATION_PUBLISHOK = "publishOK";
	public static final String OPERATION_SEARCH = "search";
	public static final String OPERATION_SEARCH_RESULTS = "searchResults";
	public static final String OPERATION_GET_FILELIST = "getFilelist";
	public static final String OPERATION_SEND_FILELIST = "sendFilelist";
	
	public static final String OPERATION_INVALIDKEY = "invalidKey";
	public static final String OPERATION_INVALIDNICKNAME = "invalidNickname";
	public static final String OPERATION_INVALIDPORT = "invalidPort";
}
