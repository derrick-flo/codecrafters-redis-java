import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static Map<String, String> memory = new HashMap<>();

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
                    boolean beforeExistsEcho = false;
                    try {
                        String line;
                        while ((line = in.readLine()) != null) {
                            System.out.println("just debug : " + line);

                            if (line.contains("GET")) {
                                final String[] splitStr = line.split(" ");
                                String key = splitStr[1];
                                final String res = memory.get(key);
                                if (res != null) {
                                    outputStream.write(res.getBytes());
                                }
                            } else if (line.contains("SET")) {
                                final String[] splitStr = line.split(" ");
                                String key = splitStr[1];
                                String value = splitStr[2];
                                final String res = memory.put(key, value);
                                if (res != null) {
                                    outputStream.write("OK".getBytes());
                                }
                            } else if ("echo".equalsIgnoreCase(line)) {
                                beforeExistsEcho = true;
                            } else if (!"echo".equalsIgnoreCase(line) && line.startsWith("$")) {
                            } else if (!"echo".equalsIgnoreCase(line) && beforeExistsEcho) {
                                String res = "+" + line + "\r\n";
                                beforeExistsEcho = false;
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
