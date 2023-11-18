package esclusas;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Servidor {

    private static final int PUERTO = 1210;
    private final Queue<Socket> queueOeste = new ConcurrentLinkedQueue<>();
    private final Queue<Socket> queueEste = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        Servidor server = new Servidor();
        server.start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Esclusas listas para la llegada de barcos");
            while (true) {
                Socket connection = serverSocket.accept();
                coordinarBarcos(connection);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void coordinarBarcos(Socket socket) {
        String req = receiveString(socket);
        System.out.println("Nuevo barco llegó desde el " + req);

        if ("Oeste".equals(req)) {
            queueOeste.offer(socket);
        } else if ("Este".equals(req)) {
            queueEste.offer(socket);
        }

        if (queueOeste.size() >= 2) {
            System.out.println("Entran barcos del Oeste. Baja esclusa Oeste");
            Socket client1 = queueOeste.poll();
            Socket client2 = queueOeste.poll();
            cruzar(client1, client2, "Oeste");
        } else if (queueEste.size() >= 2) {
            System.out.println("Entran barcos del Este. Baja esclusa Este");
            Socket client1 = queueEste.poll();
            Socket client2 = queueEste.poll();
            cruzar(client1, client2, "Este");
        }
    }

    private void cruzar(Socket barco1, Socket barco2, String direccion) {
        String signal1, signal2;
        String salida = (direccion.equals("Este") ? "Oeste" : "Este");

        sendString(barco1, "ENTRAR");
        sendString(barco2, "ENTRAR");

        signal1 = receiveString(barco1);
        signal2 = receiveString(barco2);

        if (signal1.equals("ADENTRO") && signal2.equals("ADENTRO"))
            System.out.println("Barcos en el canal. Sube esclusa " + direccion + ". Baja esclusa " + salida);

        sendString(barco1, "SALIR");
        sendString(barco2, "SALIR");

        signal1 = receiveString(barco1);
        signal2 = receiveString(barco2);

        if (signal1.equals("AFUERA") && signal2.equals("AFUERA"))
            System.out.println("Barcos afuera. Sube esclusa " + salida);
    }

    private void sendString(Socket socket, String message) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, true);
            writer.println(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String receiveString(Socket socket) {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

