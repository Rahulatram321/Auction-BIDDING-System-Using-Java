import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

// Class for managing the Auction process on the server
class AuctionServer {
    private Auction auction;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();

    public AuctionServer(Auction auction) {
        this.auction = auction;
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Auction server started...");

            // Timer to close the auction after a fixed duration (30 seconds)
            auction.startAuction(30);

            // Accept clients in a loop
            while (auction.isAuctionOpen()) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, auction);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcasts auction updates to all clients
    public static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static void main(String[] args) {
        AuctionItem item = new AuctionItem("Vintage Painting", 500.0);
        Auction auction = new Auction(item);
        AuctionServer auctionServer = new AuctionServer(auction);
        auctionServer.start(12345); // Server runs on port 12345
    }
}

// ClientHandler class to handle individual client connections
class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Auction auction;
    private PrintWriter out;
    private BufferedReader in;
    private static int userCount = 0;
    private String userId;

    public ClientHandler(Socket clientSocket, Auction auction) {
        this.clientSocket = clientSocket;
        this.auction = auction;
        userCount++;
        this.userId = "User" + userCount;  // Assign an anonymous ID
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            sendMessage("Welcome to the auction! You are " + userId);
            AuctionServer.broadcast(userId + " has joined the auction.");

            String input;
            while ((input = in.readLine()) != null) {
                try {
                    double bidAmount = Double.parseDouble(input);
                    auction.placeBid(new User(userId), bidAmount);
                    AuctionServer.broadcast(userId + " placed a bid of $" + bidAmount);
                } catch (NumberFormatException e) {
                    sendMessage("Invalid bid amount.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Send message to this client
    public void sendMessage(String message) {
        out.println(message);
    }
}

// Auction, User, AuctionItem, and Bid classes are similar to the ones in your original code (no changes needed)
