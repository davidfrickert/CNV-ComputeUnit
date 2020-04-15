package pt.ulisboa.tecnico.cnv.instrument;

import BIT.highBIT.InstructionTable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.services.dynamodbv2.model.TableDescription;

import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ServerMetrics {
    private Map<Long, SolverMetrics> threadMetrics = new ConcurrentHashMap<>();
    private AmazonDynamoDB dynamoDB;

    private void init() throws Exception {
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
        System.out.println(className + " invoking increment");
        SolverMetrics sm = threadMetrics.get(threadId);
        if (sm != null) {
            sm.incrementMethodCount();
            System.out.println(sm.getDynamicMethodCount() + " method calls.");
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

    public void sendMetricsToDynamoDB(Long threadId) {
        String tableName = "Server-metrics";

        // Create a table with a primary hash key named 'name', which holds a string
        CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName)
            .withKeySchema(new KeySchemaElement().withAttributeName("Thread-id").withKeyType(KeyType.HASH))
            .withAttributeDefinitions(new AttributeDefinition().withAttributeName("Thread-id").withAttributeType(ScalarAttributeType.S))
            .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(1L).withWriteCapacityUnits(1L));

        // Create table if it does not exist yet
        TableUtils.createTableIfNotExists(dynamoDB, createTableRequest);
        // wait for the table to move into ACTIVE state
        TableUtils.waitUntilActive(dynamoDB, tableName);

        SolverMetrics tmp = threadMetrics.get(threadId);
        Map<Long, AttributeValue> item = newItem(threadId, tmp.getDynamicMethodCount(), tmp.getNewArrayCount(), tmp.getNewReferenceArrayCount(), tmp.getNewMultiReferenceCount(), tmp.getNewObjectCount());

        //Map<String, AttributeValue> item = newItem(threadId, 1,1,1,1,1); //worked
	    
        PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
        PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
        
        //System.out.println("printing result of this computation below");
        //System.out.println(threadMetrics.get(threadId));
    }
    
    private static Map<String, AttributeValue> newItem(Long threadId, int dynamicMethodCouter, int newArrayCount, int newReferenceArrayCount, int newMultiReferenceCount, int newObjectCount) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("Thread-id", new AttributeValue(String.valueOf(threadId)));
        item.put("Method-counter", new AttributeValue().withN(Integer.toString(dynamicMethodCouter)));
        item.put("New-Array-counter", new AttributeValue().withN(Integer.toString(newArrayCount)));
        item.put("New-Reference-Array-counter", new AttributeValue().withN(Integer.toString(newReferenceArrayCount)));
        item.put("New-Multi-Reference-counter", new AttributeValue().withN(Integer.toString(newMultiReferenceCount)));
        item.put("New-Object-counter", new AttributeValue().withN(Integer.toString(newObjectCount)));

        return item;
    }


    public void add(SolverArgumentParser ap) {
        threadMetrics.put(Thread.currentThread().getId(), SolverMetrics.fromParser(ap));
    }
}
