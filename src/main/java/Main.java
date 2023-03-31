import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        //  Uncomment this block to pass the first stage
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            // Wait for connection from client.
            clientSocket = serverSocket.accept();
            final BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            final OutputStream outputStream = clientSocket.getOutputStream();

            String line;
            while((line = in.readLine()) != null) {
                System.out.println("just debug : " + line);
                if (line.equals("ping")) {
                    outputStream.write("+PONG\r\n".getBytes());
                } else if (line.equals("DOCS")) {
                    outputStream.write("+\r\n".getBytes());
                }
            }
            clientSocket.close();

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println("IOException: " + e.getMessage());
            }
        }
    }
}
