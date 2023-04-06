import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
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
                Scanner scanner = new Scanner(clientSocket.getInputStream());
                OutputStream outputStream = clientSocket.getOutputStream();

                Thread thread = new Thread(() -> {
                    List<String> commands = new ArrayList<>();
                    int commandCount = 0;

                    try {
                        while (scanner.hasNext()) {
                            String line = scanner.nextLine();
                            System.out.println("just debug : " + line);
                            if (line.startsWith("*")) {
                                commandCount = Integer.valueOf(line.substring(1));
                                continue;
                            }
                            if (line.startsWith("$")) {
                                continue;
                            }
                            if (line.contains("\n")) {
                                System.out.println(line);
                            }
                            commands.add(line);
                            if (line.equalsIgnoreCase("DOCS")) {
                                outputStream.write(bytesForOutputStream(""));
                                commands.clear();
                            } else if (line.equalsIgnoreCase("PING")) {
                                outputStream.write(bytesForOutputStream("PONG"));
                                commands.clear();
                            }
                            if (commands.size() == commandCount) {
                                break;
                            }
                        }

                        for (final String command : commands) {
                            if (command.equalsIgnoreCase("GET")) {
                                final int getIndex = commands.indexOf(command);
                                final String key = commands.get(getIndex + 1);

                                final ValueWithOptions res = MEMORY.get(key);
//                                System.out.println(res.value + " " + res.setTime + " " + res.expiredTime);
                                if (res == null) {
                                    outputStream.write("$-1\r\n".getBytes());
                                    continue;
                                }
                                if (res != null && isExpired(res.getExpiredTime(), res.getSetTime())) {

                                    System.out.println("expired :" + res.getExpiredTime() + "setTime : " + res.getSetTime());
                                    MEMORY.remove(key);
                                    outputStream.write("$-1\r\n".getBytes());
                                    continue;
                                }
                                outputStream.write(bytesForOutputStream(res.getValue()));
                            } else if (command.equalsIgnoreCase("SET")) {
                                final int setIndex = commands.indexOf(command);

                                final String key = commands.get(setIndex + 1);
                                final String value = commands.get(setIndex + 2);
                                long pxTime = 0;

                                if (commands.size() > 4) {

                                    final int pxIndex = commands.indexOf("px");
                                    pxTime = Long.parseLong(commands.get(pxIndex + 1));
                                }
                                MEMORY.put(key, new ValueWithOptions(value, pxTime, System.currentTimeMillis()));
                                outputStream.write(bytesForOutputStream("OK"));
                            } else if (command.equalsIgnoreCase("ECHO")) {
                                final int echoIndex = commands.indexOf(command);
                                final String echoCommand = commands.get(echoIndex + 1);

                                outputStream.write(bytesForOutputStream(echoCommand));
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
        } catch (Exception e) {
        }
    }


    private static byte[] bytesForOutputStream(String str) {
        return ("+" + str + "\r\n").getBytes();
    }

    private static boolean isExpired(final Long expiredTime, final Long setTime) {
        if (expiredTime == 0) {
            return false;
        }
        long diffCurrentAndSetTime = System.currentTimeMillis() - setTime;

        return expiredTime <= diffCurrentAndSetTime;
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

        public String getValue() {
            return value;
        }

        public Long getExpiredTime() {
            return expiredTime;
        }

        public Long getSetTime() {
            return setTime;
        }
    }

}
