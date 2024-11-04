package proyecto4;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class FileServer {
    private static final String STATUS_ENDPOINT = "/status";
    private static final String MOVIES_ENDPOINT = "/movies";
    private static final String MOVIES_MOVIE_ENDPOINT = "/movies/movie";
    private static final String LOGS_ENDPOINT = "/logs";
    private static final String USERS_ENDPOINT = "/users";
    private static final String LOGS_UPDATE_ENDPOINT = "/logs/update";
    private static final String USERS_UPDATE_ENDPOINT = "/users/update";

    private final int port;
    private HttpServer server;

    private static StorageData gcpData;

    public static void main(String[] args) {
        int serverPort = 8080;
        gcpData = new StorageData();

        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }

        FileServer webServer = new FileServer(serverPort);
        webServer.startServer();

        System.out.println("Servidor escuchando en el puerto " + serverPort);
    }

    public FileServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
        HttpContext moviesContext = server.createContext(MOVIES_ENDPOINT);
        HttpContext moviesMovieContext = server.createContext(MOVIES_MOVIE_ENDPOINT);
        HttpContext logsContext = server.createContext(LOGS_ENDPOINT);
        HttpContext usersContext = server.createContext(USERS_ENDPOINT);
        HttpContext logsUpdateContext = server.createContext(LOGS_UPDATE_ENDPOINT);
        HttpContext usersUpdateContext = server.createContext(USERS_UPDATE_ENDPOINT);

        statusContext.setHandler(this::handleStatusCheckRequest);
        moviesContext.setHandler(this::handleMoviesRequest);
        moviesMovieContext.setHandler(this::handleMoviesMovieRequest);
        logsContext.setHandler(this::handleLogsRequest);
        usersContext.setHandler(this::handlegetUsersRequest);
        logsUpdateContext.setHandler(this::handleLogsUpdateRequest);
        usersUpdateContext.setHandler(this::handleUsersUpdateRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void handleMoviesMovieRequest(HttpExchange exchange) throws IOException{
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }

        byte[] requestBytes = exchange.getRequestBody().readAllBytes();

        String movie = new String(requestBytes);

        String responseMessage = gcpData.getMovie(movie);
        sendResponse(responseMessage, exchange);
    }

    private void handleLogsUpdateRequest(HttpExchange exchange) throws IOException{
        if (!exchange.getRequestMethod().equalsIgnoreCase("post")) {
            exchange.close();
            return;
        }

        byte[] requestBytes = exchange.getRequestBody().readAllBytes();

        String sData = new String(requestBytes);

        String responseMessage = gcpData.addLog(sData);
        sendResponse(sData, exchange);
    }

    private void handleMoviesRequest(HttpExchange exchange) throws IOException{
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String responseMessage = gcpData.listMovies();
        sendResponse(responseMessage, exchange);
    }

    private void handlegetUsersRequest(HttpExchange exchange) throws IOException{
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String responseMessage = gcpData.getUsers();
        sendResponse(responseMessage, exchange);
    }

    private void handleLogsRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String responseMessage = gcpData.getLogs();
        sendResponse(responseMessage, exchange);
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        String responseMessage = "El servidor está vivo\n";
        sendResponse(responseMessage, exchange);
    }

    private void sendResponse(String response, HttpExchange exchange) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }


    private void handleUsersUpdateRequest(HttpExchange exchange){
        
    }
}