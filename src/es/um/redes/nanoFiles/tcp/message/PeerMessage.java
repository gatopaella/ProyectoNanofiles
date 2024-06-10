package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {

	private byte opcode;

	/*
	 * Añadir atributos y crear otros constructores específicos para crear
	 * mensajes con otros campos (tipos de datos)
	 * 
	 */
	private long param1;
	private long param2;
	private int valueLength;
	private byte[] value;



	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
		param1 = PeerMessageOps.INVALID_PARAM;
		param2 = PeerMessageOps.INVALID_PARAM;
		valueLength = PeerMessageOps.INVALID_VALUE_LENGTH;
		value = new byte[valueLength];
	}

	public PeerMessage(byte op) { // FORMATO CONTROL
		opcode = op;
		param1 = PeerMessageOps.INVALID_PARAM;
		param2 = PeerMessageOps.INVALID_PARAM;
		valueLength = PeerMessageOps.INVALID_VALUE_LENGTH;
		value = new byte[valueLength];
	}
	
	public PeerMessage(byte op, long param1, long param2) { // FORMATO OPERACIÓN
		opcode = op;
		this.param1 = param1;
		this.param2 = param2;
		valueLength = PeerMessageOps.INVALID_VALUE_LENGTH;
		value = new byte[valueLength];
	}
	
	public PeerMessage(byte op, int longitud, byte[] value) { // FORMATO TLV
		opcode = op;
		param1 = PeerMessageOps.INVALID_PARAM;
		param2 = PeerMessageOps.INVALID_PARAM;
		this.valueLength = longitud;
		this.value = new byte[longitud];
		System.arraycopy(value, 0, this.value, 0, longitud);
	}

	/*
	 * Crear métodos getter y setter para obtener valores de nuevos atributos,
	 * comprobando previamente que dichos atributos han sido establecidos por el
	 * constructor (sanity checks)
	 */
	public byte getOpcode() {
		//if (param1 == PeerMessageOps.OPCODE_INVALID_CODE) throw new IllegalStateException("El parámetro opcode no ha sido inicializado");
		
		return opcode;
	}
	
	public long getParam1() {
		if (param1 == PeerMessageOps.INVALID_PARAM)
			throw new IllegalStateException("Este parámetro no se corresponde con el formato del mensaje");
		
		return param1;
	}
	
	public long getParam2() {
		if (param2 == PeerMessageOps.INVALID_PARAM)
			throw new IllegalStateException("Este parámetro no se corresponde con el formato del mensaje");
		
		return param2;
	}
	
	public int getLongitud() {
		if (valueLength == PeerMessageOps.INVALID_VALUE_LENGTH)
			throw new IllegalStateException("El campo longitud no se corresponde con el formato de este mensaje");
		return valueLength;
	}
	
	public byte[] getValor() {
		if (valueLength == PeerMessageOps.INVALID_VALUE_LENGTH)
			throw new IllegalStateException("El campo valor no se corresponde con el formato de este mensaje");
		byte[] newValue = new byte[valueLength];
		System.arraycopy(value, 0, newValue, 0, valueLength);
		return newValue;
	}
	
	public void setOpcode(byte opcode) {
		this.opcode = opcode;
	}
	
	public void setParam1(long param1) {
		if (param1 == PeerMessageOps.INVALID_PARAM)
			throw new IllegalArgumentException("El parámetro no puede valer " + PeerMessageOps.INVALID_PARAM);
		
		this.param1 = param1;
	}
	
	public void setParam2(long param2) {
		if (param2 == PeerMessageOps.INVALID_PARAM)
			throw new IllegalArgumentException("El parámetro no puede valer " + PeerMessageOps.INVALID_PARAM);
		
		this.param2 = param2;
	}
	
	public void setLongitud(int longitud) {
		if (longitud == PeerMessageOps.INVALID_VALUE_LENGTH)
			throw new IllegalArgumentException("La longitud del campo value debe ser estrictamente positiva");
		
		this.valueLength = longitud;
	}
	
	public void setValor(byte[] value) {
		if (value.length != valueLength) {
			throw new IllegalArgumentException("La longitud del campo value debe corresponderse con el valor"
					+ "del campo valueLength, que actualmente es " + valueLength);
		}
		
		this.value = new byte[valueLength];
		System.arraycopy(value, 0, this.value, 0, valueLength);
	}

	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: En función del tipo de mensaje, leer del socket a través del "dis" el
		 * resto de campos para ir extrayendo con los valores y establecer los atributos
		 * del un objeto DirMessage que contendrá toda la información del mensaje, y que
		 * será devuelto como resultado. NOTA: Usar dis.readFully para leer un array de
		 * bytes, dis.readInt para leer un entero, etc.
		 */
		PeerMessage message = new PeerMessage();
		byte opcode = dis.readByte();
		long p1;
		long p2;
		int len;
		byte[] val;
		switch (opcode) {
		case PeerMessageOps.OPCODE_FILE_REQUEST:
			len = dis.readInt();
			val = new byte[len];
			dis.readFully(val);
			message.setOpcode(opcode);
			message.setLongitud(len);
			message.setValor(val);
			break;
		case PeerMessageOps.OPCODE_SEND_FILE:
			len = dis.readInt();
			val = new byte[len];
			dis.readFully(val);
			message.setOpcode(opcode);
			message.setLongitud(len);
			message.setValor(val);
			break;
		case PeerMessageOps.OPCODE_FILE_SENT_CONFIRMATION:
			len = dis.readInt();
			val = new byte[len];
			dis.readFully(val);
			message.setOpcode(opcode);
			message.setLongitud(len);
			message.setValor(val);
			break;
		case PeerMessageOps.OPCODE_PARTIAL_FILE_REQUEST:
			len = dis.readInt();
			val = new byte[len];
			dis.readFully(val);
			message.setOpcode(opcode);
			message.setLongitud(len);
			message.setValor(val);
			break;
		case PeerMessageOps.OPCODE_PARTIAL_FILE_SPECIFICATION:
			p1 = dis.readLong();
			p2 = dis.readLong();
			message.setOpcode(opcode);
			message.setParam1(p1);
			message.setParam2(p2);
			break;
		case PeerMessageOps.OPCODE_PARTIAL_FILE_SENT:
			message.setOpcode(opcode);
			break;
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			message.setOpcode(opcode);
			break;
		case PeerMessageOps.OPCODE_FILE_NOT_SPECIFIED:
			message.setOpcode(opcode);
			break;
		case PeerMessageOps.OPCODE_INVALID_POSITION:
			message.setOpcode(opcode);
			break;
		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO: Escribir los bytes en los que se codifica el mensaje en el socket a
		 * través del "dos", teniendo en cuenta opcode del mensaje del que se trata y
		 * los campos relevantes en cada caso. NOTA: Usar dos.write para leer un array
		 * de bytes, dos.writeInt para escribir un entero, etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
		case PeerMessageOps.OPCODE_FILE_REQUEST:
			dos.writeInt(valueLength);
			dos.write(value);
			break;
		case PeerMessageOps.OPCODE_SEND_FILE:
			dos.writeInt(valueLength);
			dos.write(value);
			break;
		case PeerMessageOps.OPCODE_FILE_SENT_CONFIRMATION:
			dos.writeInt(valueLength);
			dos.write(value);
			break;
		case PeerMessageOps.OPCODE_PARTIAL_FILE_REQUEST:
			dos.writeInt(valueLength);
			dos.write(value);
			break;
		case PeerMessageOps.OPCODE_PARTIAL_FILE_SPECIFICATION:
			dos.writeLong(param1);
			dos.writeLong(param2);
			break;
		case PeerMessageOps.OPCODE_PARTIAL_FILE_SENT:
			break;
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			break;
		case PeerMessageOps.OPCODE_FILE_NOT_SPECIFIED:
			break;
		case PeerMessageOps.OPCODE_INVALID_POSITION:
			break;
		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}





}
