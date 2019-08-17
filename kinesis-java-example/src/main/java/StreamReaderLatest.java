import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.GetRecordsRequest;
import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.GetShardIteratorRequest;
import com.amazonaws.services.kinesis.model.GetShardIteratorResult;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPInputStream;

/**
 * Kinesis stream consumer App
 */
public class StreamReaderLatest {
    private final int INTERVAL = 2000; //10s
    private String amazonStreamName = "httpd-logs-app-a";
    private String amazonRegionName = "us-east-1";

    public static void main(String[] args) {
        StreamReaderLatest streamReader = new StreamReaderLatest();
        streamReader.readFromStream();
    }

    private void readFromStream() {
        AmazonKinesisClientBuilder clientBuilder = AmazonKinesisClientBuilder.standard();
        clientBuilder.setRegion(amazonRegionName);
        clientBuilder.setCredentials(new ProfileCredentialsProvider("default"));
        //clientBuilder.setClientConfiguration(new ClientConfiguration());
        AmazonKinesis client = clientBuilder.build();

        long recordNum = 0;

        String shardIterator;
        GetShardIteratorRequest getShardIteratorRequest = new GetShardIteratorRequest();
        getShardIteratorRequest.setStreamName("httpd-logs-app-a");
        getShardIteratorRequest.setShardId("shardId-000000000000");
        getShardIteratorRequest.setShardIteratorType("LATEST");

        GetShardIteratorResult getShardIteratorResult = client.getShardIterator(getShardIteratorRequest);
        shardIterator = getShardIteratorResult.getShardIterator();


        // Continuously read data records from a shard
        while (true) {
            GetRecordsRequest getRecordsRequest = new GetRecordsRequest();
            getRecordsRequest.setShardIterator(shardIterator);
            getRecordsRequest.setLimit(25);

            GetRecordsResult getRecordsResult = client.getRecords(getRecordsRequest);

            getRecordsResult.getRecords().forEach(record -> {
                String valor = decompress(record.getData().array());
                System.out.println(valor);
            });
            recordNum += getRecordsResult.getRecords().size();
            printMessageWithTime("\nReceived " + recordNum + " records. sleep for " + INTERVAL / 1000 + "s ... ");

            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException exception) {
                System.out.println("Receving InterruptedException. Exiting ...");
                return;
            }
            shardIterator = getRecordsResult.getNextShardIterator();
        }
    }

    private String decompress(byte[] compressed) {
        String retorno = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
            GZIPInputStream gis = new GZIPInputStream(bis);
            BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            gis.close();
            bis.close();

            retorno = sb.toString();
        } catch (IOException e) {
            System.err.println(e);
        }
        return retorno;
    }

    private void printMessageWithTime(String message) {

        //Get current date time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatDateTime = now.format(formatter);
        System.out.println(message + formatDateTime);

    }

}