package proyecto4_2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileClient {

    private HttpClient httpClient;

    private String url = "http://127.0.0.1:8080/";
    private Gson gson;

    private List<User> users;

    public FileClient(){

        httpClient = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(10))
        .build();
        
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private String sendRequest(String rq) throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder()
               .GET()
               .uri(URI.create(url + rq))
               .setHeader("Content-Type", "text/plain; charset=UTF-8")
               .setHeader("User-Agent", "7CM2 Client Bot")
               .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String sendPostRequest(String rq, String requestBody) throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + rq))
                .header("Content-Type", "text/plain; charset=UTF-8")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private User findUser(String currUser){
        for (User user : users) {
            if ( user.name.equals(currUser)) return user;
        }
        return null;
    }

    public User validateUser(String user, String password){
        try{
            String usersJson = sendRequest("users");

            User[] usersTemp = gson.fromJson(usersJson, User[].class);
            users = Arrays.asList(usersTemp);

            User currUser = findUser(user);

            if (currUser.password.equals(password)) return currUser;

            return null;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

        // Log[] logsTemp = gson.fromJson(logsJson, Log[].class);
        // logs = Arrays.asList(logsTemp);
    }

    public String getMovies(){
        try{
            return sendRequest("movies");
        }catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public String getMovie(String movie){
        String movieFile;
        try {
            movieFile = sendPostRequest("movies/movie", movie);
            return movieFile;
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    public String updateLog(String log){
        try {
            return sendPostRequest("logs/update", log);
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    public ArrayList<Log> getLogs(){
        try {
            String logsjson = sendRequest("logs");
            Log[] logsraw = gson.fromJson(logsjson, Log[].class);
            return new ArrayList<Log>(Arrays.asList(logsraw));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
