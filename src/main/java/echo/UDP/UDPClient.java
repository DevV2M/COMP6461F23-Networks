/**
 * COMP 6461 - Computer Networks and Protocols
 * Lab Assignment # 3
 * Group Members:
 * Vithu Maheswaran - 27052715 (70%)
 * Shafiq Imtiaz - 40159305 (30%)
 */


package echo.UDP;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class UDPClient {

    private static int sequenceNumberCount = 0;
    private static Set<Integer> acknowledgedPackets = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);
    private static final int MAX_PACKET_SIZE = 1000;

    public static void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr, String message) throws IOException {
        try (DatagramChannel channel = DatagramChannel.open()) {

            performHandshake(channel, message, serverAddr, routerAddr);

            // CLIENT SEND
            sendMessageWithSelectiveRepeat(channel, message, serverAddr, routerAddr, 1);

            receivePackets(channel);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public UDPClient(String msg) throws IOException {
        OptionParser parser = new OptionParser();
        parser.accepts("router-host", "Router hostname")
                .withOptionalArg()
                .defaultsTo("localhost");

        parser.accepts("router-port", "Router port number")
                .withOptionalArg()
                .defaultsTo("3000");

        parser.accepts("server-host", "EchoServer hostname")
                .withOptionalArg()
                .defaultsTo("localhost");

        parser.accepts("server-port", "EchoServer listening port")
                .withOptionalArg()
                .defaultsTo("8007");

        String args = "";
        OptionSet opts = parser.parse(args);

        // Router address
        String routerHost = (String) opts.valueOf("router-host");
        int routerPort = Integer.parseInt((String) opts.valueOf("router-port"));

        // Server address
        String serverHost = (String) opts.valueOf("server-host");
        int serverPort = Integer.parseInt((String) opts.valueOf("server-port"));

        SocketAddress routerAddress = new InetSocketAddress(serverHost, routerPort);
        InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);

        runClient(routerAddress, serverAddress, msg);
    }

    public static boolean sendPacket(Packet packet, SocketAddress routerAddr, DatagramChannel channel) {

        try {

            channel.send(packet.toBuffer(), routerAddr);

            logger.info("Sending chunk of size {} to router at {}", packet.getPayload().length, routerAddr);

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void sendMessageWithSelectiveRepeat(DatagramChannel channel, String message, InetSocketAddress serverAddr, SocketAddress routerAddr, int sequenceNumber) throws Exception {
//        int sequenceNumber = 0;
        int retries = 0;

        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
        while (buffer.hasRemaining()) {

            int remaining = buffer.remaining();
            int packetSize = Math.min(remaining, MAX_PACKET_SIZE);

            byte[] payload = new byte[packetSize];
            buffer.get(payload);

            Packet p = new Packet.Builder()
                    .setType(0)
                    .setSequenceNumber(sequenceNumber)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(payload)
                    .create();

            Packet ackPacket = null;

            while (ackPacket == null) {
                // Send a message to the server with sequence number
                sendPacket(p, routerAddr, channel);

                // Receive acknowledgment with timeout
                ackPacket = receivePackets(channel);

                if (ackPacket != null) {
                    break;
                }
            }

            // Process acknowledgment
            if (ackPacket != null && ackPacket.getSequenceNumber() == sequenceNumber) {
                // Acknowledgment received successfully
                System.out.println("Acknowledgment received for packet " + sequenceNumber);
                acknowledgedPackets.add(sequenceNumber);
                sequenceNumber = (sequenceNumber + 1);
            } else {
                // Retransmit the packet
                System.out.println("Acknowledgment mismatch. Retransmitting...");
            }
        }
        Packet p = new Packet.Builder()
                .setType(4)
                .setSequenceNumber(sequenceNumber)
                .setPortNumber(serverAddr.getPort())
                .setPeerAddress(serverAddr.getAddress())
                .setPayload("END".getBytes())
                .create();
//        sendPacket(p, routerAddr, channel);
        Packet ackPacket = null;

        long timeoutMillis = 1000;
        long startTime = System.currentTimeMillis();
        sendPacket(p, routerAddr, channel);
        while (ackPacket == null) {
            // Send a message to the server with sequence number
            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                sendPacket(p, routerAddr, channel);
                startTime = System.currentTimeMillis();
                break;
            }
            // Receive acknowledgment with timeout
            ackPacket = receivePackets(channel);
            if (ackPacket != null && ackPacket.getType() == 4) {
                break;
            }
        }

        channel.close();
        System.exit(0);
    }

    public static Packet receivePackets(DatagramChannel channel) {
        try {
            // Set a timeout for receiving packets (in milliseconds)

            long timeoutMillis = 1000;
            long startTime = System.currentTimeMillis();

            // Allocate a ByteBuffer to store incoming data
            ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_LEN);

            while (System.currentTimeMillis() - startTime < timeoutMillis) {
                channel.configureBlocking(false);

                // Receive data into the buffer
                InetSocketAddress router = (InetSocketAddress) channel.receive(buffer);

                if (router != null) {
                    buffer.flip(); // Prepare the buffer for reading

                    // Process the received data
                    Packet resp = Packet.fromBuffer(buffer);
                    logger.info("Packet: {}", resp);
                    logger.info("Router: {}", router);
                    String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
                    logger.info("Payload: {}", payload);
                    buffer.clear();
                    return resp;
                }
            }
//            channel.close(); // Close the channel when done

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private static void performHandshake(DatagramChannel channel, String message, InetSocketAddress serverAddr, SocketAddress routerAddr) throws Exception {
        int retries = 0;

        while (true) {
            // Step 1: Client sends SYN to server

            ByteBuffer buffer = ByteBuffer.wrap("SYN".getBytes(StandardCharsets.UTF_8));
            int remaining = buffer.remaining();
            int packetSize = Math.min(remaining, MAX_PACKET_SIZE);
            byte[] payload = new byte[packetSize];
            buffer.get(payload);

            Packet packet = new Packet.Builder()
                    .setType(1)
                    .setSequenceNumber(0)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(payload)
                    .create();

            sendPacket(packet, routerAddr, channel);

            System.out.println("CLIENT SENT CONNECTION REQUEST: SYN PACKET");
            // Step 2: Client receives SYN-ACK from server with timeout
            Packet synAckPacket = null;
            long timeoutMillis = 500;
            long startTime = System.currentTimeMillis();
            while (synAckPacket == null) {
                if (System.currentTimeMillis() - startTime < timeoutMillis) {
                    sendPacket(packet, routerAddr, channel);
                }
                synAckPacket = receivePackets(channel);
            }
            System.out.println("SYNACK DONE");
            System.out.println(synAckPacket);
            if (synAckPacket == null) continue;
            String synAck = new String(synAckPacket.getPayload(), StandardCharsets.UTF_8);

            // Process SYN-ACK packet
            if (synAck.charAt(0) == 'S' && synAck.charAt(1) == 'Y' && synAck.charAt(2) == 'N' && synAck.charAt(3) == '-' && synAckPacket.getType() == 1) {
                // This is a SYN-ACK packet
                System.out.println("Received SYN-ACK from server");

                // Step 3: Client sends ACK to server

                buffer = ByteBuffer.wrap("ACK".getBytes(StandardCharsets.UTF_8));
                remaining = buffer.remaining();
                packetSize = Math.min(remaining, MAX_PACKET_SIZE);
                payload = new byte[packetSize];
                buffer.get(payload);

                packet = new Packet.Builder()
                        .setType(1)
                        .setSequenceNumber(1)
                        .setPortNumber(serverAddr.getPort())
                        .setPeerAddress(serverAddr.getAddress())
                        .setPayload(payload)
                        .create();

                sendPacket(packet, routerAddr, channel);
                System.out.println("CLIENT SENT CONNECTION REQUEST: CONN ACK PACKET");
                // Handshake complete
                System.out.println("Handshake complete");
                break;
            } else {
                // Retransmit the SYN packet
                System.out.println("Received unexpected packet. Retransmitting SYN...");
                retries++;
                if (retries >= 5) {
                    System.out.println("Max retries reached. Exiting.");
                    System.exit(1);
                }
            }
        }
    }
}

