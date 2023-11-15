package exclusas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class Cliente {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1209;
    private static final int REQUEST = 10;

    private String tipo;
    private int id;

    public Cliente(int id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    public static void main(String[] args) {
        String[] tipos = {"Oeste", "Este"};
        int numClientes = 4;

        for (int i = 0; i < numClientes; i++) {
            String tipo = tipos[i % tipos.length];
            int id = i;
            new Thread(() -> new Cliente(id, tipo).start()).start();
        }
    }

    public void start() {
        try {
            // while (true) {
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);

            System.out.println("Barco-" + id + " llegó a la exclusa desde el " + tipo);
            sendString(socket, tipo);

            System.out.println("Barco-" + id + " esperando confirmación para cruzar desde el " + tipo);

            String signal = receiveString(socket);
            if (signal.equals("GRANTED")) {
                System.out.println("Barco-" + id + " cruzando desde el " + tipo);

                Thread.sleep(new Random().nextInt(4) + 2);

                System.out.println("Barco-" + id + " terminó de cruzar.");
                sendString(socket, "RELEASE");
            }
            // }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendString(Socket socket, String message) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String receiveString(Socket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
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
}