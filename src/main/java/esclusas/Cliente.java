package esclusas;

import java.io.*;
import java.net.Socket;

public class Cliente {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1210;
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


                    String rta = receiveString(socket);

                    if (rta.equals("SALIR")) {
                        Thread.sleep(2000); //tiempo saliendo del canal

                        System.out.println("Barco-" + id + " salió del canal");

                        sendString(socket, "AFUERA");
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
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
