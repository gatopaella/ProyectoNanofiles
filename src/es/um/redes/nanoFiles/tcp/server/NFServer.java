package es.um.redes.nanoFiles.tcp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor que se ejecuta en un hilo propio. Creará objetos
 * {@link NFServerThread} cada vez que se conecte un cliente.
 */
public class NFServer implements Runnable {

	private ServerSocket serverSocket = null;
	private boolean stopServer = false;
	private static final int SERVERSOCKET_ACCEPT_TIMEOUT_MILISECS = 1000;
	
	private static final int PORT = 10000;

	public NFServer() throws IOException {
		/*
		 * TODO: Crear un socket servidor y ligarlo a cualquier puerto disponible
		 */
		serverSocket = new ServerSocket();
		serverSocket.bind(null); // creo que esto es ligarlo a un puerto disponible cualquiera
	}

	public int getPort() {
		if(serverSocket == null) return -1;
		return serverSocket.getLocalPort();
	}
	
	public void closeServer() {
		stopServer = true;
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Método que crea un socket servidor y ejecuta el hilo principal del servidor,
	 * esperando conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*
		 * : Usar el socket servidor para esperar conexiones de otros peers que
		 * soliciten descargar ficheros
		 */
		//System.out.println("\nServer is listening on port " + serverSocket.getLocalPort());
		try {
			while(!stopServer) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("\nNew client connected: " +
						clientSocket.getInetAddress().toString() + ":" + clientSocket.getPort());
				
				NFServerThread serverThread = new NFServerThread(clientSocket);
				serverThread.start();
			}
			
		} catch (IOException e) {
			System.out.println("Server exception: " + e.getMessage());
			//e.printStackTrace();
		}
		/*
		 * : Al establecerse la conexión con un peer, la comunicación con dicho
		 * cliente se hace en el método NFServerComm.serveFilesToClient(socket), al cual
		 * hay que pasarle el socket devuelto por accept
		 */
		/*
		 * : (Opcional) Crear un hilo nuevo de la clase NFServerThread, que llevará
		 * a cabo la comunicación con el cliente que se acaba de conectar, mientras este
		 * hilo vuelve a quedar a la escucha de conexiones de nuevos clientes (para
		 * soportar múltiples clientes). Si este hilo es el que se encarga de atender al
		 * cliente conectado, no podremos tener más de un cliente conectado a este
		 * servidor.
		 */



	}
	/**
	 * TODO: Añadir métodos a esta clase para: 1) Arrancar el servidor en un hilo
	 * nuevo que se ejecutará en segundo plano 2) Detener el servidor (stopserver)
	 * 3) Obtener el puerto de escucha del servidor etc.
	 */




}
