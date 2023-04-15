package com.lazarev.JPyIPC;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BasicClient implements Client {

    static final String DEFAULT_IP = "127.0.0.1";
    static final int DEFAULT_PORT = 25565;

    private final String ip;
    private final int port;
    private final Socket socket = new Socket();
    private int lastId = 0;

    public BasicClient() {
        this(DEFAULT_IP, DEFAULT_PORT);
    }

    public BasicClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * connect to the socket
     * @throws IOException if error occurred during connection
     */
    public void connect() throws IOException {
        socket.connect(new InetSocketAddress(this.ip, this.port));
    }

    @Override
    public void sendTextMessage(String text, Runnable callback) {
        ++lastId;
        try {
            var output = new DataOutputStream(socket.getOutputStream());
            output.writeByte('T');
            output.writeInt(lastId);

            var bytes = text.getBytes(StandardCharsets.UTF_8);
            output.writeInt(bytes.length);
            output.write(bytes);
            output.flush();

            var input = new DataInputStream(socket.getInputStream());
            int id = input.readInt();
            byte code = input.readByte();
            int length = input.readInt();
            byte[] data = input.readNBytes(length);

            callback.run();
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO: Replace with not shit
        }

    }

    @Override
    public void sendExpressionMessage(String expression, Consumer<Response> callback) {
        ++lastId;
    }

}
