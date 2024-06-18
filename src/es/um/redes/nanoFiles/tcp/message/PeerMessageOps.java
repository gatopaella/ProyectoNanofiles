package es.um.redes.nanoFiles.tcp.message;

import java.util.Map;
import java.util.TreeMap;

public class PeerMessageOps {

	public static final byte OPCODE_INVALID_CODE = 0;
	public static final long INVALID_PARAM = -1;
	public static final int INVALID_VALUE_LENGTH = 0;
	
	public static final byte OPCODE_FILE_REQUEST = 1;
	public static final byte OPCODE_SEND_FILE = 2;
	public static final byte OPCODE_FILE_SENT_CONFIRMATION = 3;
	public static final byte OPCODE_PARTIAL_FILE_REQUEST = 4;
	public static final byte OPCODE_PARTIAL_FILE_SPECIFICATION = 5;
	public static final byte OPCODE_PARTIAL_FILE_SENT = 6;
	
	public static final byte OPCODE_FILE_NOT_FOUND = 44;
	public static final byte OPCODE_INVALID_POSITION = 45;
	public static final byte OPCODE_FILE_NOT_SPECIFIED = 46;
	public static final byte OPCODE_AMBIGUOUS_HASH = 47;


	/**
	 * Definir constantes con nuevos opcodes de mensajes
	 * definidos, añadirlos al array "valid_opcodes" y añadir su
	 * representación textual a "valid_operations_str" en el mismo orden
	 */
	private static final Byte[] _valid_opcodes = {
			OPCODE_INVALID_CODE,
			OPCODE_FILE_REQUEST,
			OPCODE_SEND_FILE,
			OPCODE_FILE_SENT_CONFIRMATION,
			OPCODE_PARTIAL_FILE_REQUEST,
			OPCODE_PARTIAL_FILE_SPECIFICATION,
			OPCODE_FILE_NOT_FOUND,
			OPCODE_INVALID_POSITION,
			OPCODE_FILE_NOT_SPECIFIED,
			OPCODE_AMBIGUOUS_HASH
			};
	private static final String[] _valid_operations_str = {
			"INVALID_OPCODE",
			"FILE_REQUEST",
			"SEND_FILE",
			"FILE_SENT_CONFIRMATION",
			"PARTIAL_FILE_REQUEST",
			"PARTIAL_FILE_SPECIFICATION",
			"FILE_NOT_FOUND",
			"INVALID_POSITION",
			"FILE_NOT_SPECIFIED",
			"OPCODE_AMBIGUOUS_HASH"
			};

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;

	static {
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0; i < _valid_operations_str.length; ++i) {
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}
	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static byte operationToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OPCODE_INVALID_CODE);
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	public static String opcodeToOperation(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}
}
