import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws Exception {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.out.println("Logs from your program will appear here!");

        //  Uncomment this block to pass the first stage
        ServerSocket serverSocket;
        int port = 6379;
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            // Wait for connection from client.
            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream outputStream = clientSocket.getOutputStream();

                final Thread thread = new Thread(() -> {
                    try {
                        String line;
                        while ((line = in.readLine()) != null) {
                            System.out.println("just debug : " + line);
                            if (line.contains("ECHO")) {
                                final String[] split = line.split("\r\n");
                                final String echoStr = split[split.length - 1];
                                final String res = "+" + echoStr + "\r\n";
                                outputStream.write(res.getBytes());
                            } else if ("ping".equalsIgnoreCase(line)) {
                                outputStream.write("+PONG\r\n".getBytes());
                            } else if ("DOCS".equalsIgnoreCase(line)) {
                                outputStream.write("+\r\n".getBytes());
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                thread.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
