import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;

import java.text.SimpleDateFormat;
import java.sql.Time;
import java.util.Date;
import java.time.Duration;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class UserClient implements Runnable {

    private static Scanner in = new Scanner(System.in);
    private static String username, access, exit, timestamp = "", userMovie = "";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static HttpResponse<String> sendRequest(String rq, String body) throws IOException, InterruptedException{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/" + rq))
                .header("Content-Type", "text/plain; charset=UTF-8")
                .POST(BodyPublishers.ofString(body))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String getCredentials(){

        System.out.print("Usuario: ");
        username = in.nextLine();
        System.out.print("Contraseña: ");
        String pass = in.nextLine();

        return username + "," + pass;
    }

    private static long getSleep(String line){
        long timetosleep = 0;
        
        try{

            String beginRaw = line.substring(0, 12);
            String endRaw = line.substring(17, line.length());

            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss,SSS");
            Date begin = format.parse(beginRaw);
            Date end = format.parse(endRaw);

            timetosleep = end.getTime() - begin.getTime();
        }catch(Exception e){
            e.printStackTrace();
        }
        return timetosleep;
    }

    private static void printSubtitles(String subtitles, String currTs) {
        try{
            BufferedReader reader = new BufferedReader(new StringReader(subtitles));
            String line;
            int lineCount = 0;
            int maxLinesPrinted = 0;
            int currentTimestamp = Integer.parseInt(currTs);
            int subtitleTimestamp = -1;
            long timeToSleep = 0;
    
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    if (subtitleTimestamp >= currentTimestamp) {
                        maxLinesPrinted = Math.max(lineCount - 2, maxLinesPrinted);
    
                        for (int i = 0; i < (maxLinesPrinted - (lineCount - 2)); i++) {
                            System.out.println("\033[K");
                        }
                        TimeUnit.MILLISECONDS.sleep(timeToSleep);
                        System.out.print("\033[" + maxLinesPrinted + "A");
                    }
                    lineCount = 0;
                } else {
                    lineCount++;
                    if (lineCount == 1) {
                        timestamp = line;
                        subtitleTimestamp = Integer.parseInt(line);
                        continue;
                    } else if (lineCount == 2) {
                        timeToSleep = getSleep(line);
                        continue;
                    } else if (subtitleTimestamp < currentTimestamp) {
                        continue;
                    } else {
                        System.out.print("\033[K");
                        System.out.println(line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    private static void listMoviesOpt(List<String> movies) throws IOException, InterruptedException{
        System.out.println("Elija una película para visualizar sus subtítulos:");
        for(int i = 0 ; i < movies.size() ; ++i){
            String[] temp = movies.get(i).split(".",1);
            System.out.println(i+1 + ". " + temp[0]);
        }
        System.out.println("0. Salir");

        int opt = in.nextInt();
        if( opt == 0) return;
        else if ( opt > movies.size()){
            System.out.println("Elegiste una película invalida");
        }else{
            userMovie = movies.get(opt-1);
            System.out.println("Elegiste: " + userMovie + "\n");
            HttpResponse<String> response = sendRequest("movie", userMovie);
            String currTs = sendRequest("timestamp", userMovie).body();
            System.out.println("currts: " + currTs);
            printSubtitles(response.body(), currTs);
        }   
    }
    public static void main(String[] args) throws IOException, InterruptedException {

        String credentials = getCredentials();

        HttpResponse<String> response = sendRequest("login", credentials);

        // print response headers
        HttpHeaders headers = response.headers();
        headers.map().forEach((k, v) -> System.out.println(k + ":" + v));

        // print status code
        System.out.println(response.statusCode());

        // print response body
        String[] movies = response.body().split(",");
        if(response.statusCode() == 200) {
            Runtime.getRuntime().addShutdownHook(new Thread(new UserClient())); 
            access = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
            listMoviesOpt(Arrays.asList(movies));
        }
        else System.out.println(response.body());

        in.close();
    }

    public void run(){
        exit = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        StringBuffer body = new StringBuffer();
        if( userMovie == ""){
            body.append(username + "," + access + "," + exit);
        }else{
            body.append(username + "," + access + "," + exit + "," + userMovie + "," + timestamp);
        }

        System.out.println(body.toString());
        try{
            HttpResponse<String> response = sendRequest("logs/update",body.toString());
            System.out.println(response.body());
        }catch(Exception e){
            e.printStackTrace();
        }
        System.out.println("Caught, filleted and ate CTRL-C");
    }

}