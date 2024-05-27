package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

public class PeerMessageTest {

	public static void main(String[] args) throws IOException {
		String nombreArchivo = "peermsg.bin";
		DataOutputStream fos = new DataOutputStream(new FileOutputStream(nombreArchivo));

		/*
		 * TODO: Probar a crear diferentes tipos de mensajes (con los opcodes válidos
		 * definidos en PeerMessageOps), estableciendo los atributos adecuados a cada
		 * tipo de mensaje. Luego, escribir el mensaje a un fichero con
		 * writeMessageToOutputStream para comprobar que readMessageFromInputStream
		 * construye un mensaje idéntico al original.
		 */
		String str = "hash del fichero con el mario galaxy";
		PeerMessage msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE_REQUEST, str.length(), str.getBytes());
		msgOut.writeMessageToOutputStream(fos);

		str = "el codigo del mario galaxy";
		msgOut = new PeerMessage(PeerMessageOps.OPCODE_SEND_FILE, str.length(), str.getBytes());
		msgOut.writeMessageToOutputStream(fos);
		
		str = "hash del fichero con el mario galaxy";
		msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE_SENT_CONFIRMATION, str.length(), str.getBytes());
		msgOut.writeMessageToOutputStream(fos);
		
		msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
		msgOut.writeMessageToOutputStream(fos);
		
		str = "parte del codigo del mario galaxy";
		msgOut = new PeerMessage(PeerMessageOps.OPCODE_PARTIAL_FILE_REQUEST, str.length(), str.getBytes());
		msgOut.writeMessageToOutputStream(fos);
		
		msgOut = new PeerMessage(PeerMessageOps.OPCODE_PARTIAL_FILE_SPECIFICATION, 17, 20);
		msgOut.writeMessageToOutputStream(fos);
		
		msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_SPECIFIED);
		msgOut.writeMessageToOutputStream(fos);
		
		msgOut = new PeerMessage(PeerMessageOps.OPCODE_INVALID_POSITION);
		msgOut.writeMessageToOutputStream(fos);
		
		DataInputStream fis = new DataInputStream(new FileInputStream(nombreArchivo));
		
		for (int i = 0; i < 8; i++) {
			PeerMessage msgIn = PeerMessage.readMessageFromInputStream((DataInputStream) fis);
			
			System.out.println("Operación = " + PeerMessageOps.opcodeToOperation(msgIn.getOpcode()));
			try {
				System.out.println("Param1 = " + msgIn.getParam1());
				System.out.println("Param2 = " + msgIn.getParam2());
			} catch (IllegalStateException e) {
				
			}
			try {
				System.out.println("Longitud = " + msgIn.getLongitud());
				System.out.println("Valor = " + new String(msgIn.getValor()));
			} catch (Exception e) {
				
			}
			System.out.println();
		}
		/*
		PeerMessage msgIn = PeerMessage.readMessageFromInputStream((DataInputStream) fis);
		
		 * TODO: Comprobar que coinciden los valores de los atributos relevantes al tipo
		 * de mensaje en ambos mensajes (msgOut y msgIn), empezando por el opcode.
		 
		if (msgOut.getOpcode() != msgIn.getOpcode()) {
			System.err.println("Opcode does not match!");
		} else {
			System.out.println("Operación = " + PeerMessageOps.opcodeToOperation(msgIn.getOpcode()));
			try {
				System.out.println("Param1 = " + msgIn.getParam1());
				System.out.println("Param2 = " + msgIn.getParam2());
			} catch (IllegalStateException e) {
				
			}
			try {
				System.out.println("Longitud = " + msgIn.getLongitud());
				System.out.println("Valor = " + new String(msgIn.getValor()));
			} catch (Exception e) {
				
			}
		}
		*/
	}

}

