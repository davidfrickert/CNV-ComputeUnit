package pt.ulisboa.tecnico.cnv.instrument;

import BIT.highBIT.InstructionTable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkBaseException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMetrics {
    private Map<Long, SolverMetrics> threadMetrics = new ConcurrentHashMap<>();
    private AmazonDynamoDB dynamoDB;

    private void init() {
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
    
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }

        dynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion("eu-west-1")
            .build();

    }

    
    private ServerMetrics() { 
        init();
    }
    
    private static ServerMetrics INSTANCE;
    
    public static synchronized ServerMetrics getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerMetrics();
        }
        return INSTANCE;
    }

    public void increment(String className, Long threadId) {
        //System.out.println(className + " invoking increment");
        SolverMetrics sm = threadMetrics.get(threadId);
        if (sm != null) {
            sm.incrementMethodCount();
            //System.out.println(sm.getDynamicMethodCount() + " method calls.");
        } else {
            System.out.println("attempt to increment thread not in HashMap. ThreadId=" + threadId);
        }
    }

    public void incrementAllocCount(Long threadId, int opcode) {
        SolverMetrics sm = threadMetrics.get(threadId);
        if (sm != null) {
            switch (opcode) {
                case InstructionTable.NEW:
                    sm.incrementNewObjectCount();
                    break;
                case InstructionTable.newarray:
                    sm.incrementNewArrayCount();
                    break;
                case InstructionTable.anewarray:
                    sm.incrementNewReferenceArrayCount();
                    break;
                case InstructionTable.multianewarray:
                    sm.incrementNewMultiDimArrayCount();
                    break;
            }
        } else {
            System.out.println("attempt to increment thread not in HashMap. ThreadId=" + threadId);
        }

    }

    public boolean sendMetricsToDynamoDB(Long threadId) {
        try {
            System.out.println("Sending to dynamoDB");
            String tableName = "Server-metrics";
            // Create a table with a primary hash key named 'name', which holds a string
            CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
                    .withKeySchema(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH))
                    .withAttributeDefinitions(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S))
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

            // Create table if it does not exist yet
            TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
            // wait for the table to move into ACTIVE state
            try {
                TableUtils.waitUntilActive(dynamoDB, tableName);
            } catch (InterruptedException exc) {
                exc.printStackTrace();
                System.out.println("Table exists and is active");
                return false;
            }
            
            //temporary solution
            ScanRequest scanRequest = new ScanRequest(tableName);
            ScanResult scanResult = dynamoDB.scan(scanRequest);
            String id = "0";
            for (Map<String, AttributeValue> e : scanResult.getItems()) {
                //System.out.println(e.get("id").getS());
                if(e.get("id").getS().compareTo(id) > 0){
                    id = e.get("id").getS();
                }
            }
            int futureId = Integer.parseInt(id) + 1;
            
            SolverMetrics metrics = threadMetrics.get(threadId);
            System.out.println("Sending" + metrics);
            Map<String, AttributeValue> item = newItem(futureId, threadId, metrics);//, tmp.getDynamicMethodCount(), tmp.getNewArrayCount(), tmp.getNewReferenceArrayCount(), tmp.getNewMultiDimensionalArrayCount(), tmp.getNewObjectCount());

            PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
            PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
            System.out.println("Response from aws: " + putItemResult);

            return true;
        } catch (SdkBaseException e) {
            System.out.println("Problem with AWS SDK, stack trace below:");
            e.printStackTrace();
            return false;
        }
    }
    
    private static Map<String, AttributeValue> newItem(int id, Long threadId, SolverMetrics metrics){//, Long threadId, String columns, String rows, String entries, int dynamicMethodCouter, int newArrayCount, int newReferenceArrayCount, int newMultiReferenceCount, int newObjectCount) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("id", new AttributeValue(String.valueOf(id)));
        item.put("Thread-id", new AttributeValue().withN(String.valueOf(threadId)));
        item.put("Columns", new AttributeValue().withN(String.valueOf(metrics.getnColumns())));
        item.put("Lines", new AttributeValue().withN(String.valueOf(metrics.getnLines())));
        item.put("Unassigned-Entries", new AttributeValue().withN(String.valueOf(metrics.getUnassignedEntries())));
        item.put("Method-counter", new AttributeValue().withN(String.valueOf(metrics.getDynamicMethodCount())));
        item.put("New-Array-counter", new AttributeValue().withN(String.valueOf(metrics.getNewArrayCount())));
        item.put("New-Reference-Array-counter", new AttributeValue().withN(String.valueOf(metrics.getNewReferenceArrayCount())));
        item.put("New-Multi-Reference-counter", new AttributeValue().withN(String.valueOf(metrics.getNewMultiDimensionalArrayCount())));
        item.put("New-Object-counter", new AttributeValue().withN(String.valueOf(metrics.getNewObjectCount())));

        return item;
    }


    public void add(SolverArgumentParser ap) {
        SolverMetrics sm = SolverMetrics.fromParser(ap);
        threadMetrics.put(Thread.currentThread().getId(), sm);
    }
}
