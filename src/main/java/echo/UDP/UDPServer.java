package echo.UDP;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

public class UDPServer {

    private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);
    private static final int MAX_PACKET_SIZE = 1024;

    private static int sequenceNumberCount = 0;

    private void listenAndServe(int port) throws IOException {

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(port));
            logger.info("EchoServer is listening at {}", channel.getLocalAddress());
            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);

            for (; ; ) {
                StringBuilder request = new StringBuilder();
                buf.clear();
                SocketAddress router = channel.receive(buf);

                // Parse a packet from the received raw data.
                buf.flip();
                Packet packet = Packet.fromBuffer(buf);
                buf.flip();

                String payloadReceived = new String(packet.getPayload(), UTF_8);
                request.append(payloadReceived);
                logger.info("Packet: {}", packet);
                logger.info("Payload: {}", payloadReceived);
                logger.info("Router: {}", router);

                // Send the response to the router not the client.
                // The peer address of the packet is the address of the client already.
                // We can use toBuilder to copy properties of the current packet.
                // This demonstrate how to create a new packet from an existing packet.

                Packet resp = packet.toBuilder()
                        .setPayload(payloadReceived.getBytes())
                        .create();
                channel.send(resp.toBuffer(), router);

                /**    **/

                ByteBuffer buffer = ByteBuffer.wrap(HttpServerLibrary.handleRequest(request.toString()).getBytes(StandardCharsets.UTF_8));

                while (buffer.hasRemaining()) {
                    int remaining = buffer.remaining();
                    int packetSize = Math.min(remaining, MAX_PACKET_SIZE);

                    byte[] payload = new byte[packetSize];
                    buffer.get(payload);

                    int sequenceNumber = sequenceNumberCount % 10;
                    sequenceNumberCount++;

                    resp = packet.toBuilder()
                            .setPayload(payload).setSequenceNumber(sequenceNumber)
                            .create();
                    channel.send(resp.toBuffer(), router);

//                    Packet p = new Packet.Builder()
//                            .setType(0)
//                            .setSequenceNumber(1L)
//                            .setPortNumber(serverAddr.getPort())
//                            .setPeerAddress(serverAddr.getAddress())
//                            .setPayload(payload)
//                            .create();
//                    channel.send(p.toBuffer(), router);

                    logger.info("Sending chunk of size {} to router at {}", packetSize, router);
                }

                logger.info("All chunks sent to the client");

                /**    **/

            }

        }
    }

    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        parser.acceptsAll(asList("port", "p"), "Listening port")
                .withOptionalArg()
                .defaultsTo("8007");

        OptionSet opts = parser.parse(args);
        int port = Integer.parseInt((String) opts.valueOf("port"));
        UDPServer server = new UDPServer();
        server.listenAndServe(port);
    }
}