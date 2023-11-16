package esclusa.dos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Cliente {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1208;
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
            while (true) {
                Socket socket = new Socket(SERVER_HOST, SERVER_PORT);

                System.out.println("Barco-" + id + " llegó a la esclusa " + tipo);
                sendString(socket, tipo);

                System.out.println("Barco-" + id + " esperando confirmación para cruzar desde el " + tipo);

                String signal = receiveString(socket);
                if (signal.equals("ENTRAR")) {
                    Thread.sleep(2000); //tiempo entrando al canal

                    System.out.println("Barco-" + id + " entró al canal");
                    sendString(socket, "ADENTRO");
                }

                String rta = receiveString(socket);

                if (rta.equals("SALIR")) {
                    Thread.sleep(2000); //tiempo saliendo del canal

                    System.out.println("Barco-" + id + " salió del canal");

                    sendString(socket, "AFUERA");
                }
            }
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
            return new String(buffer, 0, bytesRead);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
