package es.um.redes.nanoFiles.tcp.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFServerComm {

	public static void serveFilesToClient(Socket socket) {
		/*
		 * TODO: Crear dis/dos a partir del socket
		 */
		try {
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			
			/* LA SIMPLIFICACIÓN DE RECIBIR Y MANDAR UN NÚMERO
			int entero = dis.readInt();
			System.out.println("Recibimos del cliente el entero " + entero);
			System.out.println("Enviandolo de vuelta al cliente...");
			dos.writeInt(entero);
			*/
			
			//TODO la condición del while tengo que modificarla y maybe añadir un mensaje de desconexión
			while(!socket.isClosed()) { // Mientras el cliente esté conectado
				PeerMessage msgFromClient = PeerMessage.readMessageFromInputStream(dis);
				byte opcode = msgFromClient.getOpcode();
				switch(opcode) {
				case PeerMessageOps.OPCODE_FILE_REQUEST:
					String hashSubstr = new String(msgFromClient.getValor());
					FileInfo files[] = NanoFiles.db.getFiles();
					FileInfo matchingFiles[] = FileInfo.lookupHashSubstring(files, hashSubstr);
					if(matchingFiles.length == 0) {
						System.out.println("No file matches the hash substring " + hashSubstr);
						PeerMessage responseToClient = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
						responseToClient.writeMessageToOutputStream(dos);
					} else if (matchingFiles.length >= 2) {
						//TODO gestionar esto bien, quizás con una respuesta de control sobre ambiguedad
						
					} else {
						String completeHash = matchingFiles[0].fileHash;
						int fileSize = (int) matchingFiles[0].fileSize;
						String path = matchingFiles[0].filePath;
						System.out.println("File found: " + path);
						File fileToSend = new File(path);
						FileInputStream fis = new FileInputStream(fileToSend);
						//TODO aquí estamos confiando en que el fichero sea pequeño
						byte fileContent[] = new byte[fileSize];
						fis.read(fileContent); // Esto podría devolver el número de bytes leídos
						fis.close();
						PeerMessage responseToClient = new PeerMessage(PeerMessageOps.OPCODE_SEND_FILE, 
																		fileSize, fileContent);
						responseToClient.writeMessageToOutputStream(dos);
						
						PeerMessage confirmation = new PeerMessage(PeerMessageOps.OPCODE_FILE_SENT_CONFIRMATION,
																	(int) completeHash.length(), 
																	completeHash.getBytes());
						confirmation.writeMessageToOutputStream(dos);
					}
					break;
				default:
					System.err.println("ERROR: No treatment for the received message");
				}
			}
			
		} catch (IOException e) {
			System.out.println("An exception ocurred while serving files to client");
			e.printStackTrace();
		}
		/*
		 * TODO: Mientras el cliente esté conectado, leer mensajes de socket,
		 * convertirlo a un objeto PeerMessage y luego actuar en función del tipo de
		 * mensaje recibido, enviando los correspondientes mensajes de respuesta.
		 */
		/*
		 * TODO: Para servir un fichero, hay que localizarlo a partir de su hash (o
		 * subcadena) en nuestra base de datos de ficheros compartidos. Los ficheros
		 * compartidos se pueden obtener con NanoFiles.db.getFiles(). El método
		 * FileInfo.lookupHashSubstring es útil para buscar coincidencias de una
		 * subcadena del hash. El método NanoFiles.db.lookupFilePath(targethash)
		 * devuelve la ruta al fichero a partir de su hash completo.
		 */



	}




}
