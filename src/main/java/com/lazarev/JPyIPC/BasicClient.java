package com.lazarev.JPyIPC;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BasicClient {

    public static final String DEFAULT_IP = "127.0.0.1";
    public static final int DEFAULT_PORT = 25565;
    private final byte[] SERVER_HELLO = "Py2J".getBytes(StandardCharsets.US_ASCII);
    private final byte[] CLIENT_HELLO = "J2Py".getBytes(StandardCharsets.US_ASCII);

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

        var output = socket.getOutputStream();
        output.write(CLIENT_HELLO);
        output.flush();

        var input = socket.getInputStream();
        var res = input.readNBytes(4);

        if (!Arrays.equals(SERVER_HELLO, res)) {
            socket.close();
            throw new IOException("Server sent wrong hello");
        }
    }

    /**
     *
     * @param text - text to send to Python process
     * @throws IOException on socket errors
     * @throws RuntimeException if response cannot match the request
     */
    public void sendTextMessage(String text) throws IOException {
        var requestPacket = new RequestPacket('T', lastId++, text);
        requestPacket.writeToStream(socket.getOutputStream());
        var responsePacket = ResponsePacket.readFromStream(socket.getInputStream());

        if (requestPacket.id != responsePacket.id) {
            throw new RuntimeException(
                    "Unexpected error: Got response with wrong ID: (req)%d != (res)%d"
                            .formatted(requestPacket.id, responsePacket.id));
        }

        if (!responsePacket.ok()) {
            throw new RuntimeException(
                    "Unexpected error: server responded to the text message, but with error status: %d"
                            .formatted(responsePacket.status));
        }
    }

    /**
     * @param expression - expression to send to Python to compute
     * @return object that contains results of the computation
     * @throws IOException on socket error,
     */
    public ExpressionResponse sendExpressionMessage(String expression) throws IOException {
        var requestPacket = new RequestPacket('E', lastId++, expression);
        requestPacket.writeToStream(socket.getOutputStream());
        var responsePacket = ResponsePacket.readFromStream(socket.getInputStream());
        if (responsePacket.id != requestPacket.id) {
            throw new RuntimeException(
                    "Unexpected error: Got response with wrong ID: (req)%d != (res)%d"
                            .formatted(requestPacket.id, responsePacket.id));
        }

        return responsePacket;
    }

    private static class RequestPacket {
        byte type;
        int id;
        String text;

        RequestPacket(char type, int id, String text) {
            this.type = (byte)type;
            this.id = id;
            this.text = text;
        }

        void writeToStream(OutputStream stream) throws IOException {
            var dataStream = new DataOutputStream(stream);
            dataStream.writeByte(type);
            dataStream.writeInt(id);

            var bytes = text.getBytes(StandardCharsets.UTF_8);
            dataStream.writeInt(bytes.length);
            dataStream.write(bytes);
            dataStream.flush();
        }
    }

    private static class ResponsePacket implements ExpressionResponse {
        int id;
        int status;
        String text;

        @Override
        public String getText() {
            return text;
        }

        @Override
        public int getStatus() {
            return status;
        }

        boolean ok() {
            return status == 0;
        }

        static ResponsePacket readFromStream(InputStream stream) throws IOException {
            var dataStream = new DataInputStream(stream);
            var packet = new ResponsePacket();
            packet.id = dataStream.readInt();
            packet.status = dataStream.readInt();

            int length = dataStream.readInt();
            byte[] encoded = dataStream.readNBytes(length);
            packet.text = new String(encoded, StandardCharsets.UTF_8);
            return packet;
        }
    }

}
