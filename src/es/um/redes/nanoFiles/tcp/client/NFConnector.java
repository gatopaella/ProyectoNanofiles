package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDigest;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;
	
	private DataInputStream dis;
	private DataOutputStream dos;


	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		serverAddr = fserverAddr;
		/*
		 *  Se crea el socket a partir de la dirección del servidor (IP, puerto). La
		 * creación exitosa del socket significa que la conexión TCP ha sido
		 * establecida.
		 */
		socket = new Socket(serverAddr.getAddress(), serverAddr.getPort());
		/*
		 *  Se crean los DataInputStream/DataOutputStream a partir de los streams de
		 * entrada/salida del socket creado. Se usarán para enviar (dos) y recibir (dis)
		 * datos del servidor.
		 */
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
	}

	/**
	 * Método para descargar un fichero a través del socket mediante el que estamos
	 * conectados con un peer servidor.
	 * 
	 * @param targetFileHashSubstr Subcadena del hash del fichero a descargar
	 * @param file                 El objeto File que referencia el nuevo fichero
	 *                             creado en el cual se escribirán los datos
	 *                             descargados del servidor
	 * @return Verdadero si la descarga se completa con éxito, falso en caso
	 *         contrario.
	 * @throws IOException Si se produce algún error al leer/escribir del socket.
	 */
	public boolean downloadFile(String targetFileHashSubstr, File file) throws IOException {
		boolean downloaded = false;
		/*
		int entero = 20;
		System.out.println("Enviamos al servidor el entero " + entero);
		dos.writeInt(entero);
		int respuesta = dis.readInt();
		System.out.println("Respuesta del servidor: " + respuesta);
		*/
		
		/*
		 * TODO: Construir objetos PeerMessage que modelen mensajes con los valores
		 * adecuados en sus campos (atributos), según el protocolo diseñado, y enviarlos
		 * al servidor a través del "dos" del socket mediante el método
		 * writeMessageToOutputStream.
		 */
		byte opcode = PeerMessageOps.OPCODE_FILE_REQUEST;
		int hashLength = targetFileHashSubstr.length();
		byte[] hash = targetFileHashSubstr.getBytes();
		PeerMessage request = new PeerMessage(opcode, hashLength, hash);
		request.writeMessageToOutputStream(dos);
		/*
		 * TODO: Recibir mensajes del servidor a través del "dis" del socket usando
		 * PeerMessage.readMessageFromInputStream, y actuar en función del tipo de
		 * mensaje recibido, extrayendo los valores necesarios de los atributos del
		 * objeto (valores de los campos del mensaje).
		 */
		PeerMessage response = PeerMessage.readMessageFromInputStream(dis);
		opcode = response.getOpcode(); // Reutilizamos la variable opcode para almacenar el de la respuesta
		switch(opcode) {
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			System.out.println("El hash " + targetFileHashSubstr + " no se corresponde con ningún archivo");
			break;
		case PeerMessageOps.OPCODE_SEND_FILE:
			System.out.println("Datos recibidos");
			System.out.println("Escribiendo contenido en el fichero " + file.getName() + "...");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(response.getValor());
			fos.close();
			System.out.println("Hecho");
			
			System.out.println("Esperando confirmación del servidor...");
			PeerMessage confirmation = PeerMessage.readMessageFromInputStream(dis); // Si, reutilizamos response
			if(confirmation.getOpcode() == PeerMessageOps.OPCODE_FILE_SENT_CONFIRMATION) {
				System.out.println("El trozo de hash del fichero solicitado: " + targetFileHashSubstr);
				System.out.println("El hash completo según la confirmación: " 
						+ new String(confirmation.getValor()));
				String receivedHash = FileDigest.computeFileChecksumString(file.getName());
				System.out.println("El hash correspondiente al fichero recibido: "
						+ receivedHash);
				downloaded = true;
			} else {
				System.err.println("ERROR: FILE_SENT_CONFIRMATION expected");
			}
			break;
		default:
			System.err.println("Unexpected response from server\nopcode: " + opcode);
			break;
		}
		
		/*
		 * TODO: Para escribir datos de un fichero recibidos en un mensaje, se puede
		 * crear un FileOutputStream a partir del parámetro "file" para escribir cada
		 * fragmento recibido (array de bytes) en el fichero mediante el método "write".
		 * Cerrar el FileOutputStream una vez se han escrito todos los fragmentos.
		 */
		/*
		 * NOTA: Hay que tener en cuenta que puede que la subcadena del hash pasada como
		 * parámetro no identifique unívocamente ningún fichero disponible en el
		 * servidor (porque no concuerde o porque haya más de un fichero coincidente con
		 * dicha subcadena)
		 */

		/*
		 * TODO: Finalmente, comprobar la integridad del fichero creado para comprobar
		 * que es idéntico al original, calculando el hash a partir de su contenido con
		 * FileDigest.computeFileChecksumString y comparándolo con el hash completo del
		 * fichero solicitado. Para ello, es necesario obtener del servidor el hash
		 * completo del fichero descargado, ya que quizás únicamente obtuvimos una
		 * subcadena del mismo como parámetro.
		 */




		return downloaded;
	}





	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
