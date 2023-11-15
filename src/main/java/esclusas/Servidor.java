package esclusas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class Servidor {

    private static final int PUERTO = 1209;
    private static final int REQUEST = 10;
    private final Semaphore semaphore = new Semaphore(1);
    private final Queue<Socket> queueO = new ConcurrentLinkedQueue<>();
    private final Queue<Socket> queueE = new ConcurrentLinkedQueue<>();

    //private String ultimo;

    public static void main(String[] args) {
        Servidor server = new Servidor();
        server.start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Esclusa lista para la llegada de barcos");

            while (true) {
                try {
                    Socket connection = serverSocket.accept();
                    coordinarBarcos(connection);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void coordinarBarcos(Socket connection) {
        try {
            String req = receiveString(connection);
            System.out.println("Nuevo barco llegó desde el " + req);

            if ("Oeste".equals(req)) {
                queueO.offer(connection);
            } else if ("Este".equals(req)) {
                queueE.offer(connection);
            }
            System.out.println("Barco esperando en la fila del " + req);

            if (queueO.size() >= 2) {
                semaphore.acquire();
                System.out.println("Pasan dos barcos desde el Oeste");
                Socket client1 = queueO.poll();
                Socket client2 = queueO.poll();
                //ultimo = "Oeste";
                cruzar(client1, client2);
            } else if (queueE.size() >= 2) {
                semaphore.acquire();
                System.out.println("Pasan dos barcos desde el Este");
                Socket client1 = queueE.poll();
                Socket client2 = queueE.poll();
                //ultimo = "Este";
                cruzar(client1, client2);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void cruzar(Socket barco1, Socket barco2) {
        try {
            sendString(barco1, "GRANTED");
            sendString(barco2, "GRANTED");

            System.out.println("Esperando a que los barcos terminen de cruzar");
            Thread.sleep(2000);

            String rta1 = receiveString(barco1);
            String rta2 = receiveString(barco2);

            if (rta1.equals("RELEASE") && rta2.equals("RELEASE")) {
                System.out.println("Esclusa libre...");
                semaphore.release();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String receiveString(Socket connection) {
        try {
            InputStream inputStream = connection.getInputStream();
            byte[] buffer = new byte[REQUEST];
            int bytesRead = inputStream.read(buffer);

            if (bytesRead == -1) {
                return null;
            }
            return new String(buffer, 0, bytesRead);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendString(Socket connection, String message) {
        try {
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

