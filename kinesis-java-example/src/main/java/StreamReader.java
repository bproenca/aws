import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * Kinesis stream consumer App
 */
public class StreamReader {
    private final int INTERVAL = 2000; //10s
    private String amazonStreamName = "httpd-logs-app-a";
    private String amazonRegionName = "us-east-1";

    public static void main(String[] args) {
        StreamReader streamReader = new StreamReader();
        streamReader.readFromStream();
    }

    private void readFromStream() {
        AmazonKinesisClientBuilder clientBuilder = AmazonKinesisClientBuilder.standard();
        clientBuilder.setRegion(amazonRegionName);
        clientBuilder.setCredentials(new ProfileCredentialsProvider("default"));
        //clientBuilder.setClientConfiguration(new ClientConfiguration());
        AmazonKinesis client = clientBuilder.build();

        long recordNum = 0;

        // Getting initial stream description from aws
        System.out.println(client.describeStream(amazonStreamName).toString());
        List<Shard> initialShardData = client.describeStream(amazonStreamName).getStreamDescription().getShards();
        System.out.println("\nlist of shards:");
        initialShardData.forEach(d -> System.out.println(d.toString()));

        // Getting shardIterators (at beginning sequence number) for reach shard
        List<String> initialShardIterators = initialShardData.stream().map(s ->
                client.getShardIterator(new GetShardIteratorRequest()
                        .withStreamName(amazonStreamName)
                        .withShardId(s.getShardId())
                        //.withShardIteratorType(ShardIteratorType.TRIM_HORIZON)
                        .withStartingSequenceNumber(s.getSequenceNumberRange().getStartingSequenceNumber())
                        .withShardIteratorType(ShardIteratorType.AT_SEQUENCE_NUMBER)
                ).getShardIterator()
        ).collect(Collectors.toList());

        System.out.println("\nlist of ShardIterators:");
        initialShardIterators.forEach(i -> System.out.println(i));
        System.out.println("\nwaiting for messages....");

        // WARNING!!! Assume that only have one shard. So only use that shard
        String shardIterator = initialShardIterators.get(0);

        // Continuously read data records from a shard
        while (true) {
            // Create a new getRecordsRequest with an existing shardIterator
            // Set the maximum records to return to 25
            GetRecordsRequest getRecordsRequest = new GetRecordsRequest();
            getRecordsRequest.setShardIterator(shardIterator);
            getRecordsRequest.setLimit(25);

            GetRecordsResult recordResult = client.getRecords(getRecordsRequest);

            recordResult.getRecords().forEach(record -> {
                //System.out.println(record.getData().toString());
                String valor = decompress(record.getData().array());
                System.out.println(valor);
            });

            recordNum += recordResult.getRecords().size();
            printMessageWithTime("\nReceived " + recordNum + " records. sleep for " + INTERVAL / 1000 + "s ... ");
            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException exception) {
                System.out.println("Receving InterruptedException. Exiting ...");
                return;
            }
            shardIterator = recordResult.getNextShardIterator();
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