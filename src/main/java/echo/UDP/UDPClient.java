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

    //    private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);
//
//    private static void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr) throws IOException {
//        try(DatagramChannel channel = DatagramChannel.open()){
//            String msg = "Hello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello WorldHello World";
//            Packet p = new Packet.Builder()
//                    .setType(0)
//                    .setSequenceNumber(1L)
//                    .setPortNumber(serverAddr.getPort())
//                    .setPeerAddress(serverAddr.getAddress())
//                    .setPayload(msg.getBytes())
//                    .create();
//            channel.send(p.toBuffer(), routerAddr);
//
//            logger.info("Sending \"{}\" to router at {}", msg, routerAddr);
    private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);
    private static final int MAX_PACKET_SIZE = 1000;

    public static void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr, String message) throws IOException {
        try (DatagramChannel channel = DatagramChannel.open()) {

            // CLIENT SEND
//            send(msg, serverAddr, routerAddr, channel);

            performHandshake(channel, message, serverAddr, routerAddr);

            // CLIENT SEND
            sendMessageWithSelectiveRepeat(channel, message, serverAddr, routerAddr, 1);
//            sendMessageWithSelectiveRepeat(channel, message, serverAddr, routerAddr, 0);

            receivePackets(channel);
//            logger.info("All chunks sent to the router");
//            logger.info("Waiting for response");
            // CLIENT RECEIVE
//            receivePackets(channel);

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
//
//        SocketAddress routerAddress = new InetSocketAddress(routerHost, routerPort);

        SocketAddress routerAddress = new InetSocketAddress(serverHost, routerPort);
        InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);

        runClient(routerAddress, serverAddress, msg);
    }

//    public static boolean send(String msg, InetSocketAddress serverAddr, SocketAddress routerAddr, DatagramChannel channel) {
//
//        try {
//            ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
//            while (buffer.hasRemaining()) {
//                int remaining = buffer.remaining();
//                int packetSize = Math.min(remaining, MAX_PACKET_SIZE);
//
//                byte[] payload = new byte[packetSize];
//                buffer.get(payload);
//
//                int sequenceNumber = sequenceNumberCount;
//                sequenceNumberCount++;
//                Packet p = new Packet.Builder()
//                        .setType(0)
//                        .setSequenceNumber(sequenceNumber)
//                        .setPortNumber(serverAddr.getPort())
//                        .setPeerAddress(serverAddr.getAddress())
//                        .setPayload(payload)
//                        .create();
//
//                channel.send(p.toBuffer(), routerAddr);
//
//                logger.info("Sending chunk of size {} to router at {}", packetSize, routerAddr);
//            }
//            return true;
//        } catch (IOException e) {
//            return false;
//        }
//    }

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

            // Send a message to the server with sequence number
            sendPacket(p, routerAddr, channel);

            // Receive acknowledgment with timeout
            Packet ackPacket = receivePackets(channel);

            // Process acknowledgment
            if (ackPacket != null && ackPacket.getSequenceNumber() == sequenceNumber) {
                // Acknowledgment received successfully
                System.out.println("Acknowledgment received for packet " + sequenceNumber);
                acknowledgedPackets.add(sequenceNumber);
                sequenceNumber = (sequenceNumber + 1);
            } else {
                // Retransmit the packet
                System.out.println("Acknowledgment mismatch. Retransmitting...");
                retries++;
                if (retries >= 5) {
                    System.out.println("Max retries reached. Exiting.");
                    System.exit(1);
                }
            }
        }
        Packet p = new Packet.Builder()
                .setType(4)
                .setSequenceNumber(sequenceNumber)
                .setPortNumber(serverAddr.getPort())
                .setPeerAddress(serverAddr.getAddress())
                .setPayload("END".getBytes())
                .create();
        sendPacket(p, routerAddr, channel);
        System.out.println("MADE IT");
    }

    public static Packet receivePackets(DatagramChannel channel) {
        try {
            // Set a timeout for receiving packets (in milliseconds)
            long timeoutMillis = 10000;
            long startTime = System.currentTimeMillis();

            // Allocate a ByteBuffer to store incoming data
            ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_LEN);

            int retries = 0;
            while (System.currentTimeMillis() - startTime < timeoutMillis) {

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
            System.out.println("Timeout reached. Exiting...");
            channel.close(); // Close the channel when done

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
            Packet synAckPacket = receivePackets(channel);

            String synAck = new String(synAckPacket.getPayload(), StandardCharsets.UTF_8);

            // Process SYN-ACK packet
            if (synAck.charAt(0) == 'S' && synAck.charAt(1) == 'Y' && synAck.charAt(2) == 'N' && synAck.charAt(3) == '-' && synAckPacket.getType() == 1) {
                // This is a SYN-ACK packet
                System.out.println("Received SYN-ACK from server");

                // Step 3: Client sends ACK to server
//                public static boolean sendPacket(Packet packet, SocketAddress routerAddr, DatagramChannel channel)
//
//                sendPacket(clientSocket, InetAddress.getByName("localhost"), 3000, "ACK",1);

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


    /** DO NOT DELETE **/
//    public void oldReceiveClient(){
    //            // Try to receive a packet within timeout.
//            channel.configureBlocking(false);
//            Selector selector = Selector.open();
//            channel.register(selector, OP_READ);
//            logger.info("Waiting for the response");
//            selector.select(5000);
//
//            Set<SelectionKey> keys = selector.selectedKeys();
//            if (keys.isEmpty()) {
//                logger.error("No response after timeout");
//                return;
//            }
//
//            long startTime = System.currentTimeMillis();
//            long timeoutMillis = 10000; // Adjust the timeout as needed
//
//            // CLIENT RECEIVE WITHIN TIMEOUT
//            while (System.currentTimeMillis() - startTime < timeoutMillis) {
//                selector.select(timeoutMillis);
//
//                if (keys.isEmpty()) {
//                    logger.info("Timeout reached. No more responses will be received.");
//                    break;
//                }
//
//
//                ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
//                SocketAddress router = channel.receive(buf);
//                buf.flip();
//                Packet resp = Packet.fromBuffer(buf);
//                logger.info("Packet: {}", resp);
//                logger.info("Router: {}", router);
//                String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
//                logger.info("Payload: {}", payload);
//
//                keys.clear();
//            }
//    }
}

