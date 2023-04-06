import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static final Map<String, ValueWithOptions> MEMORY = new ConcurrentHashMap<>();

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

                Thread thread = new Thread(() -> {
                    boolean beforeExistsCommand = false;
                    boolean beforeGetCommand = false;
                    boolean beforeSetCommand = false;
                    boolean beforePxCommand = false;
                    int commandCount = 0;
                    String key = null;
                    Long px = null;

                    try {
                        String line;
                        while ((line = in.readLine()) != null) {
                            System.out.println("just debug : " + line);

                            if (line.startsWith("*")) {
                                commandCount = Integer.valueOf(line.substring(1));
                            }
                            if (line.equalsIgnoreCase("GET")) {
                                beforeGetCommand = true;
                            } else if (beforeGetCommand && line.startsWith("$")) {
                            } else if (beforeGetCommand) {
                                final ValueWithOptions res = MEMORY.get(line);
                                beforeGetCommand = false;
                                commandCount = 0;
                                if (res != null) {
                                    if (isExpired(res.expiredTime, res.setTime)) {
                                        MEMORY.remove(line);
                                        outputStream.write("$-1\r\n".getBytes());
                                        continue;
                                    }
                                    String realRes = "+" + res.value + "\r\n";
                                    outputStream.write(realRes.getBytes());
                                } else {
                                    outputStream.write("$-1\r\n".getBytes());
                                }
                            } else if (line.equalsIgnoreCase("SET")) {
                                beforeSetCommand = true;
                            } else if (line.equalsIgnoreCase("PX")) {
                                beforePxCommand = true;
                            } else if (beforeSetCommand && line.startsWith("$")) {
                            } else if (beforePxCommand && line.startsWith("$")) {
                            } else if (beforeSetCommand && key == null) {
                                key = line;
                            } else if (beforePxCommand && px == null) {
                                px = Long.valueOf(line);
                            }

                            if (beforePxCommand && px != null && commandCount == 5) {
                                ValueWithOptions valueWithOptions = new ValueWithOptions(key, px, System.currentTimeMillis());
                                MEMORY.put(key, valueWithOptions);
                                beforePxCommand = false;
                                beforeSetCommand = false;
                                key = null;
                                px = null;
                                commandCount = 0;
                                outputStream.write("+OK\r\n".getBytes());
                            } else if (beforeSetCommand && key != null && commandCount == 3) {
                                ValueWithOptions valueWithOptions = new ValueWithOptions(key, null, System.currentTimeMillis());
                                MEMORY.put(key, valueWithOptions);
                                beforeSetCommand = false;
                                key = null;
                                commandCount = 0;
                                outputStream.write("+OK\r\n".getBytes());
                            } else if ("echo".equalsIgnoreCase(line)) {
                                beforeExistsCommand = true;
                            } else if (!"echo".equalsIgnoreCase(line) && line.startsWith("$")) {
                            } else if (!"echo".equalsIgnoreCase(line) && beforeExistsCommand) {
                                String res = "+" + line + "\r\n";
                                beforeExistsCommand = false;
                                commandCount = 0;
                                outputStream.write(res.getBytes());
                            } else if ("ping".equalsIgnoreCase(line)) {
                                commandCount = 0;
                                outputStream.write("+PONG\r\n".getBytes());
                            } else if ("DOCS".equalsIgnoreCase(line)) {
                                commandCount = 0;
                                outputStream.write("+\r\n".getBytes());
                            }
                        }
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

    private static boolean isExpired(final Long expiredTime, final Long setTime) {
        long diffCurrentAndSetTime = System.currentTimeMillis() - setTime;

        return expiredTime != null && expiredTime <= diffCurrentAndSetTime;
    }

    static class ValueWithOptions {
        private String value;
        private Long expiredTime;
        private Long setTime;

        public ValueWithOptions(final String value, final Long expiredTime, final Long setTime) {
            this.value = value;
            this.expiredTime = expiredTime;
            this.setTime = setTime;
        }
    }
}
