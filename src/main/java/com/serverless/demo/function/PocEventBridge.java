package com.serverless.demo.function;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.eventbridge.AmazonEventBridgeClient;
import com.amazonaws.services.eventbridge.model.PutEventsRequest;
import com.amazonaws.services.eventbridge.model.PutEventsRequestEntry;
import com.amazonaws.services.eventbridge.model.PutEventsResult;
import com.amazonaws.services.eventbridge.model.PutEventsResultEntry;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.demo.model.ServerlessInput;
import com.serverless.demo.model.ServerlessOutput;

/**
 * Lambda function that simply prints "Hello World" if the input String is not provided,
 * otherwise, print "Hello " with the provided input String.
 */
public class PocEventBridge implements RequestHandler<ServerlessInput, ServerlessOutput> {
    @Override
    public ServerlessOutput handleRequest(ServerlessInput serverlessInput, Context context) {
    	String content = serverlessInput.getBody();
    	Map<String, AttributeValue> attributes = convert(serverlessInput.getQueryStringParameters());
        
    	//attributes.put("event", putEvent(context));
    	putEvent(context);
        ServerlessOutput output = new ServerlessOutput();
        output.setStatusCode(200);
        output.setBody("Successfully insert Event in AWS Event Bridge !!!");
        context.getLogger().log(output.getBody());
        return output;
    }
    
    public PutEventsResult putEvent(Context context){
    	context.getLogger().log("Entrou no método putEvent");
        PutEventsRequestEntry requestEntry = new PutEventsRequestEntry()
                .withTime(new java.util.Date())
                .withSource("com.mycompany.myapp")
                .withDetailType("myDetailType")
                .withResources("resource1", "resource2")
                .withDetail("{ \"key1\": \"value1\", \"key2\": \"value2\" }");

        PutEventsRequest request = new PutEventsRequest()
                .withEntries(requestEntry, requestEntry);
        AmazonEventBridgeClient amazonEventBridgeClient = (AmazonEventBridgeClient) AmazonEventBridgeClient.builder().build();
        PutEventsResult result = amazonEventBridgeClient.putEvents(request);
        for (PutEventsResultEntry resultEntry : result.getEntries()) {
            if (resultEntry.getEventId() != null) {
            	context.getLogger().log("Event Id: " + resultEntry.getEventId());
            } else {
            	context.getLogger().log("Injection failed with Error Code: " + resultEntry.getErrorCode());
            }
        }
		return result;
    }
    private Map<String, AttributeValue> convert(Map<String, String> map) {
        return Optional.ofNullable(map).orElseGet(HashMap::new).entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, e -> new AttributeValue().withS(e.getValue())));
    }
}