import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
            byte[] byteArr = new byte[100];
            InputStream inputStream = clientSocket.getInputStream();
            int readByteCount = inputStream.read(byteArr);
            String data = new String(byteArr, 0, readByteCount, "UTF-8");

            System.out.println(data);

            final OutputStream outputStream = clientSocket.getOutputStream();

            byte[] bytes = null;

            if (data.contains("DOCS")) {
                bytes = "$0\r\n\r\n*2\r\n$4\r\nPONG\r\n$4\r\nPONG\r\n".getBytes(StandardCharsets.UTF_8);
            } else if (data.contains("ping")) {
                bytes = "+PONG\r\n".getBytes(StandardCharsets.UTF_8);
            }


            outputStream.write(bytes);
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
