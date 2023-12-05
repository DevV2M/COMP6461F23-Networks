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
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

public class UDPServer {

    private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);
    private static final int MAX_PACKET_SIZE = 1013;

    private static int sequenceNumberCount = 0;

    private static HashMap<String, Queue<Packet>> clientPackets;
    private static HashMap<String, HashSet<Long>> clientAcks;

    private void listenAndServe(int port) throws IOException {

        clientPackets = new HashMap<>();
        clientAcks = new HashMap<>();

        SocketAddress router = new InetSocketAddress("localhost", 3000);

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(port));
            logger.info("EchoServer is listening at {}", channel.getLocalAddress());

            for (; ; ) {

                // Send the response to the router not the client.
                // The peer address of the packet is the address of the client already.
                // We can use toBuilder to copy properties of the current packet.
                // This demonstrate how to create a new packet from an existing packet.

                Queue<Packet> packets = receivePackets(channel, router);
                if (packets == null) continue;
                String request = requestBuilder(packets);
                // BUILD PACKET

                System.out.println("REQUEST RECEIVED: \n" + request);
                ByteBuffer buffer = ByteBuffer.wrap(HttpServerLibrary.handleRequest(request.toString()).getBytes(StandardCharsets.UTF_8));

                // SERVER SEND
                while (buffer.hasRemaining()) {
                    int remaining = buffer.remaining();
                    int packetSize = Math.min(remaining, MAX_PACKET_SIZE);

                    byte[] payload = new byte[packetSize];
                    buffer.get(payload);

                    int sequenceNumber = sequenceNumberCount % 10;
                    sequenceNumberCount++;

                    Packet resp = packets.peek().toBuilder()
                            .setPayload(payload).setSequenceNumber(sequenceNumber)
                            .create();
                    channel.send(resp.toBuffer(), router);

                    logger.info("Sending chunk of size {} to router at {}", packetSize, router);
                }

                logger.info("All chunks sent to the client");

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String requestBuilder(Queue<Packet> listOfPackets) {
        StringBuilder request = new StringBuilder();

        for (Packet packet : listOfPackets) {
            request.append(new String(packet.getPayload(), UTF_8));
        }
        return request.toString();
    }

    private static Queue<Packet> receivePackets(DatagramChannel channel, SocketAddress router) throws Exception {

        for (; ; ) {

            Packet packet = receivePacket(channel);

            if (packet != null && packet.getType() == 1) performHandshake(channel, packet, router);
            else if (packet != null && packet.getType() == 4) {
                String clientIdentifier = packet.getPeerAddress() + ":" + packet.getPeerPort();
                return clientPackets.get(clientIdentifier);

            } else if (packet != null) { // SERVER RECEIVES HTTP REQUEST

                String clientIdentifier = packet.getPeerAddress() + ":" + packet.getPeerPort();
//                System.out.println("CLIENT: " + clientIdentifier);
//                System.out.println("ACKS: " + clientAcks.get(clientIdentifier));
                Set<Long> acks = clientAcks.get(clientIdentifier);
                Queue<Packet> packets = clientPackets.get(clientIdentifier);

                ByteBuffer buffer = ByteBuffer.wrap("ACK".getBytes(StandardCharsets.UTF_8));
                // SERVER SEND
                Packet ack = packet.toBuilder().setType(3)
                        .setPayload(buffer.array())
                        .create();

                sendPacket(channel, router, ack);
                if (acks == null) {
                    clientAcks.put(clientIdentifier, new HashSet<>());
                    clientPackets.put(clientIdentifier, new LinkedList<>());
                    acks = clientAcks.get(clientIdentifier);
                    packets = clientPackets.get(clientIdentifier);
                }
                if (!acks.contains(packet.getSequenceNumber())) {
                    packets.add(packet);
                    acks.add(packet.getSequenceNumber());
                }
            }
        }
    }


    private static void sendPacket(DatagramChannel channel, SocketAddress router, Packet packet) throws IOException {

        channel.send(packet.toBuffer(), router);

        logger.info("Sending chunk of size {} to router at {}", router);
    }

    private static Packet receivePacket(DatagramChannel channel) throws IOException {

        ByteBuffer buf = ByteBuffer
                .allocate(Packet.MAX_LEN)
                .order(ByteOrder.BIG_ENDIAN);

        for (; ; ) {

            // SERVER RECEIVE
            buf.clear();
            SocketAddress router = channel.receive(buf);

            // Parse a packet from the received raw data.
            buf.flip();
            Packet packet = Packet.fromBuffer(buf);
            buf.flip();

            if (packet != null) return packet;
        }
    }

    private static boolean performHandshake(DatagramChannel channel, Packet synPacket, SocketAddress router) throws Exception {

        String clientIdentifier = synPacket.getPeerAddress() + ":" + synPacket.getPeerPort();
        if (clientPackets.containsKey(clientIdentifier)) {
            System.out.println(String.format("Handshake failed, client already connected: %s", clientIdentifier));
            return false;
        }

        String packetString = new String(synPacket.getPayload(), StandardCharsets.UTF_8);
        if (packetString.compareTo("SYN") == 0) {
            // Save the client socket for future communication
            clientPackets.putIfAbsent(clientIdentifier, new LinkedList<Packet>());
            clientAcks.putIfAbsent(clientIdentifier, new HashSet<>());

            // Server sends SYN-ACK to client
            ByteBuffer buffer = ByteBuffer.wrap("SYN-ACK".getBytes(StandardCharsets.UTF_8));
            // SERVER SEND
            Packet synAck = synPacket.toBuilder().setType(1)
                    .setPayload(buffer.array())
                    .create();

            sendPacket(channel, router, synAck);

            return true;
        }

        // Step 3: Server receives ACK from client
        Packet ack = receivePacket(channel);
        String ackString = new String(ack.getPayload(), StandardCharsets.UTF_8);
        if (ack != null && ack.getType() == 1 && ackString.compareTo("ACK") == 0) {
            // Save the client socket for future communication
            clientPackets.putIfAbsent(clientIdentifier, new LinkedList<Packet>());
            clientAcks.putIfAbsent(clientIdentifier, new HashSet<>());
            return true;
        }
        return false;
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