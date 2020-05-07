package net.bywire.utility.automatic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
Times are millisecond time values but in seconds (drop the last 3 digits).

curl 'https://api.automatic.com/trip/?started_at__gte=1514782800&started_at__lte=1546318800&limit=250&page=1' \
  -H 'Connection: keep-alive' \
  -H 'Pragma: no-cache' \
  -H 'Cache-Control: no-cache' \
  -H 'Authorization: bearer 96609bd2f77bc9a71dd626c3a7879f70b69511a7' \
  -H 'Accept: *//*' \
  --compressed
  
 */

public class DataDownloader {

    private static final String BASE_URL = "https://api.automatic.com/trip/";

    private static final String START_INSTANT_PARAM_NAME = "started_at__gte";
    private static final String END_INSTANT_PARAM_NAME = "started_at__lte";
    private static final String RESULT_LIMIT_PARAM_NAME = "limit";
    private static final String PAGE_NUMBER_PARAM_NAME = "page";

    public static final String BEARER_TOKEN_PROPERTY_NAME = "auth.bearer.token";
    public static final String OLDEST_DATE_PROPERTY_NAME = "oldest.date";
    public static final String NEWEST_DATE_PROPERTY_NAME = "newest.date";
    public static final String NUMBER_OF_DAYS_PROPERTY_NAME = "number.of.days";
    public static final String OUTPUT_DIRECTORY_PROPERTY_NAME = "output.directory";

    private String bearerToken = null;
    private LocalDate oldestDate = LocalDate.of(2012, Month.JANUARY, 1);
    private LocalDate newestDate = LocalDate.now().plusDays(1);
    private int numberOfDays = 7;
    private String outputDirectory = null;

    /**
     * The start date of the request that is being processed. Starts with the
     * oldest date and increments by the number of days for each request.
     */
    private LocalDate dateMarker = null;

    public void extractData() {

        try {

            File dir = new File(outputDirectory);
            dir.mkdirs();

            dateMarker = oldestDate;

            while (!dateMarker.isAfter(newestDate)) {

                LocalDate startDate = dateMarker;
                LocalDate endDate = dateMarker.plusDays(numberOfDays);
                if (endDate.isAfter(newestDate)) {
                    endDate = newestDate.plusDays(1);
                }

                String response = retrieveData(startDate, endDate);

                File f = new File(dir, "automatic_" + startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + ".json");
                System.out.println(f.getAbsolutePath());
                System.out.println(response);
                BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                writer.write(response);
                writer.flush();
                writer.close();

                dateMarker = dateMarker.plusDays(numberOfDays);

            }

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    private String retrieveData(LocalDate startDate, LocalDate endDate) {

        String result = null;

        System.out.println("Retrieving data between " + startDate + " and " + endDate);

        long startInstant = startDate.atTime(LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC).toEpochMilli();
        long endInstant = endDate.atTime(LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC).toEpochMilli();

        result = retrieveData(startInstant, endInstant);

        return result;
    }

    private String retrieveData(long startInstant, long endInstant) {

        StringBuilder resp = new StringBuilder();

        try {

            // Build the URL string.
            StringBuilder sb = new StringBuilder();
            sb.append("https://api.automatic.com/trip/?");

            sb.append(START_INSTANT_PARAM_NAME);
            sb.append("=");
            sb.append(startInstant / 1000);

            sb.append("&");
            sb.append(END_INSTANT_PARAM_NAME);
            sb.append("=");
            sb.append(endInstant / 1000);

            sb.append("&");
            sb.append(RESULT_LIMIT_PARAM_NAME);
            sb.append("=250");

            sb.append("&");
            sb.append(PAGE_NUMBER_PARAM_NAME);
            sb.append("=1");

            URL url = new URL(sb.toString());
            URLConnection conn = url.openConnection();
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Pragma", "no-cache");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Authorization", "bearer " + bearerToken);
            conn.setRequestProperty("Accept", "*/*");

            conn.connect();

            conn.getContentLengthLong();
            BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) conn.getContent()));
            reader.lines().forEachOrdered(s -> {
                resp.append(s);
                resp.append("\n");
            });
            reader.close();

        } catch (MalformedURLException ex) {
            Logger.getLogger(DataDownloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return resp.toString();
    }

    public static final void main(String[] args) {

        DataDownloader dd = new DataDownloader();

        try {
            dd.loadProperties();
            dd.extractData();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void loadProperties() throws IOException {

        Properties p = new Properties();

        File f = new File("download.properties");
        System.out.println(f.getAbsoluteFile());
        BufferedReader reader = new BufferedReader(new FileReader(f));
        p.load(reader);
        reader.close();

        setProperties(p);
        System.out.println("Loaded properties: " + p.entrySet());

    }

    public void setProperties(Properties props) {

        bearerToken = props.getProperty(BEARER_TOKEN_PROPERTY_NAME);

        if (bearerToken == null) {
            throw new RuntimeException("A bearer token must be provided.");
        }
        System.out.println(bearerToken);

        String oldDate = props.getProperty(OLDEST_DATE_PROPERTY_NAME, "2012-01-01");
        if (oldDate != null) {
            oldestDate = LocalDate.parse(oldDate);
        }
        System.out.println(oldestDate);

        String newDate = props.getProperty(NEWEST_DATE_PROPERTY_NAME, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        if (newDate != null) {
            newestDate = LocalDate.parse(newDate);
        }
        System.out.println(newestDate);

        String num = props.getProperty(NUMBER_OF_DAYS_PROPERTY_NAME, "7");
        if (num != null) {
            numberOfDays = Integer.parseInt(num);
        }
        System.out.println(numberOfDays);

        outputDirectory = props.getProperty(OUTPUT_DIRECTORY_PROPERTY_NAME, "target/output");
        System.out.println(outputDirectory);

    }

}
