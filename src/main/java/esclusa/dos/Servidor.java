package esclusa.dos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class Servidor {

    private static final int PUERTO = 1208;
    private static final int REQUEST = 10;
    private final Queue<Socket> queueOeste = new ConcurrentLinkedQueue<>();
    private final Queue<Socket> queueEste = new ConcurrentLinkedQueue<>();
    private static final Semaphore sem = new Semaphore(1);

    public static void main(String[] args) {
        Servidor server = new Servidor();
        server.start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Esclusas listas para la llegada de barcos");

            new Thread(() -> esclusa("Oeste")).start();
            new Thread(() -> esclusa("Este")).start();

            while (true) {
                Socket connection = serverSocket.accept();
                coordinarBarcos(connection);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void coordinarBarcos(Socket connection) {
        String req = receiveString(connection);
        System.out.println("Nuevo barco llegó desde el " + req);

        if ("Oeste".equals(req)) {
            queueOeste.offer(connection);
        } else if ("Este".equals(req)) {
            queueEste.offer(connection);
        }
    }

    private void esclusa(String tipo) {
        while (true) {
            try {
                sem.acquire();
                if (tipo.equals("Oeste") && queueOeste.size() >= 2) {
                    System.out.println("Entran barcos del Oeste. Baja esclusa " + tipo);
                    Socket client1 = queueOeste.poll();
                    Socket client2 = queueOeste.poll();
                    cruzar(client1, client2, tipo);
                } else if (tipo.equals("Este") && queueEste.size() >= 2) {
                    System.out.println("Entran barcos del Este. Baja esclusa " + tipo);
                    Socket client1 = queueEste.poll();
                    Socket client2 = queueEste.poll();
                    cruzar(client1, client2, tipo);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                sem.release();
            }
        }

    }
/*
    private void esclusaEste() {
        while (true) {
            try {
                semaforo.acquire();
                if (queueEste.size() >= 2) {
                    System.out.println("Entran barcos. Baja esclusa Este");
                    Socket client1 = queueEste.poll();
                    Socket client2 = queueEste.poll();
                    cruzar(client1, client2, "Este");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaforo.release();
            }
        }
    }*/

    private void cruzar(Socket barco1, Socket barco2, String direccion) {
        String signal1, signal2;
        String entrada = direccion;
        String salida = (direccion.equals("Este") ? "Oeste" : "Este");

        sendString(barco1, "ENTRAR");
        sendString(barco2, "ENTRAR");

        signal1 = receiveString(barco1);
        signal2 = receiveString(barco2);

        if (signal1.equals("ADENTRO") && signal2.equals("ADENTRO"))
            System.out.println("Barcos en el canal. Sube esclusa " + entrada + ". Baja esclusa " + salida);

        sendString(barco1, "SALIR");
        sendString(barco2, "SALIR");

        signal1 = receiveString(barco1);
        signal2 = receiveString(barco2);

        if (signal1.equals("AFUERA") && signal2.equals("AFUERA"))
            System.out.println("Barcos afuera. Sube esclusa " + salida);
    }

    private String receiveString(Socket connection) {
        try {
            InputStream inputStream = connection.getInputStream();
            byte[] buffer = new byte[REQUEST];
            int bytesRead = inputStream.read(buffer);
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
