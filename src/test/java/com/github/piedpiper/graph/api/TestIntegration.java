package com.github.piedpiper.graph.api;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.util.StringInputStream;
import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.log.ILogger;
import com.github.commons.log.Slf4jLoggerImpl;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.api.types.AuditInfo;
import com.github.piedpiper.graph.api.types.GraphInput;
import com.github.piedpiper.graph.api.types.GraphDefinition;
import com.github.piedpiper.graph.api.types.NodeDefinition;
import com.github.piedpiper.guice.PiedPiperModule;
import com.github.piedpiper.lambda.ExecuteGraphLambdaFunction;
import com.github.piedpiper.lambda.TestContext;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.dynamo.DynamoDBBaseNode;
import com.github.piedpiper.node.aws.dynamo.TestConstants;
import com.github.piedpiper.node.piedpiper.SubGraphNode;
import com.github.piedpiper.node.rest.RESTPostHandler;
import com.github.piedpiper.node.rest.RESTServiceNode;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class TestIntegration {

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final ILogger logger = new Slf4jLoggerImpl();

	private static final ActorSystem system = ActorSystem.create("graphExecutor");

	@Test
	public void testIntegration() throws Exception {
//		JsonNode graph = new ObjectMapper().readTree(new FileInputStream(getFileName("successGraph.json")));
//		GraphInput input = new GraphInput(graph, mapper.readTree("{}"));
//		Props graphProps = ApiGraphActor.props(Guice.createInjector(new PiedPiperModule()), logger);
//		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, graphProps, "graphActor1");
//		Timeout timeout = new Timeout(Duration.create(25, "seconds"));
//		Future<Object> future = akka.pattern.Patterns.ask(ref, input, timeout);
//		GraphDefinition response = (GraphDefinition) Await.result(future, timeout.duration());
//		System.out.println("Response = " + JsonUtils.writeValueAsStringSilent(response));
	}
//	
//	@Test
//	public void testFirebase() throws Exception {
//		JsonNode graph = new ObjectMapper().readTree(new FileInputStream(getFileName("firebaseAdmin.json")));
//		GraphInput input = new GraphInput(graph, mapper.readTree("{}"));
//		Props graphProps = ApiGraphActor.props(Guice.createInjector(new PiedPiperModule()), logger);
//		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, graphProps, "graphActor1");
//		Timeout timeout = new Timeout(Duration.create(25, "seconds"));
//		Future<Object> future = akka.pattern.Patterns.ask(ref, input, timeout);
//		GraphDefinition response = (GraphDefinition) Await.result(future, timeout.duration());
//		System.out.println("Response = " + JsonUtils.writeValueAsStringSilent(response));
//	}
//
//
//	@Test
//	public void testRESTPost() throws FileNotFoundException, IOException {
//		RESTPostHandler handler = new RESTPostHandler();
//		NodeInput input = new NodeInput();
//		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getNodeFileName("POSTSuccessRequest.json"))));
//		System.out.println(handler.apply(input).getOutput());
//	}
//
//	@Test
//	public void testSubGraph() throws JsonProcessingException {
//		SubGraphNode node = new SubGraphNode();
//		ILogger logger = new Slf4jLoggerImpl();
//		Injector injector = Guice.createInjector(new PiedPiperModule());
//		node.setILogger(logger);
//		node.setInjector(injector);
//		NodeInput input = new NodeInput();
//		ObjectNode inputJson = JsonUtils.mapper.createObjectNode();
//		inputJson.set("projectName", ParameterUtils.createParamValueNode("Atom8"));
//		inputJson.set("graphName", ParameterUtils.createParamValueNode("ListThingsInThingGroup"));
//		inputJson.set("thingGroupName", ParameterUtils.createParamValueNode("Akash_Home"));
//		inputJson.set("authToken", ParameterUtils.createParamValueNode("7846e2af-3a8f-11e9-8806-0200cd936042"));
//		input.setInput(inputJson);
//		System.out.println(JsonUtils.mapper.writeValueAsString(node.apply(input)));
//	}
//
//	@Test
//	public void testExecuteGraphEndpoint() throws UnsupportedEncodingException, IOException {
//		for (int i = 0; i < 1; i++) {
//			Injector injector = Guice.createInjector(new PiedPiperModule());
//			ObjectNode inputJson = JsonUtils.mapper.createObjectNode();
//			inputJson.put(DynamoDBBaseNode.TABLE_NAME.getParameterName(), PiedPiperConstants.ALMIGHTY_TABLE_NAME);
//			inputJson.put(PiedPiperConstants.PROJECT_NAME, "Atom8");
//			inputJson.put(PiedPiperConstants.GRAPH_NAME, "ListThingsInThingGroup");
//
//			ObjectNode runtimeData = JsonUtils.mapper.createObjectNode();
//			runtimeData.put("thingGroupName", "NIoIgBgyuAVNeaoZVHZxaGAxRZW2");
//			runtimeData.put("idToken", "eyJhbGciOiJSUzI1NiIsImtpZCI6IjdkMmY5ZjNmYjgzZDYzMzc0OTdiNmY3Y2QyY2ZmNGRmYTVjMmU4YjgiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiQWthc2ggQXNob2siLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDQuZ29vZ2xldXNlcmNvbnRlbnQuY29tLy1vZ2VIaUl1MUJ1VS9BQUFBQUFBQUFBSS9BQUFBQUFBQUw2QS9EMFVXLXZLTzFVYy9zOTYtYy9waG90by5qcGciLCJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vYXRvbTgtMTU3NjE3IiwiYXVkIjoiYXRvbTgtMTU3NjE3IiwiYXV0aF90aW1lIjoxNTU0NjE4NTIwLCJ1c2VyX2lkIjoiTklvSWdCZ3l1QVZOZWFvWlZIWnhhR0F4UlpXMiIsInN1YiI6Ik5Jb0lnQmd5dUFWTmVhb1pWSFp4YUdBeFJaVzIiLCJpYXQiOjE1NTQ3MTQ0NzgsImV4cCI6MTU1NDcxODA3OCwiZW1haWwiOiJ0aGVoZWxsbWFrZXJAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImZpcmViYXNlIjp7ImlkZW50aXRpZXMiOnsiZ29vZ2xlLmNvbSI6WyIxMTExMzc3NzUzMDM1ODIxMzQ3NzciXSwiZW1haWwiOlsidGhlaGVsbG1ha2VyQGdtYWlsLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6Imdvb2dsZS5jb20ifX0.bp4A7mdeFesUKJg666dlvzcmIoahHYgITCm8IuoBANxYyLK6U1SMTf4pPeWEaI3Mgs5gA1EZG1wiSRWvJENS8ZMPEFPTDaD0xlyfPEVhFTxzgyVVb-EykyRDetLx2vgWSy7NYkkaBT0SuRIW5Zf4kDsbZKCwgKvpXTBV4pJkw3NJ7KoTZ2tnp-VDhvxqMPPT2Uv16cyy2_8z3ionS1Ubda2gn1TIDuKCIuuyhbZ6RbFv8RuhWlW05xaUHFJLTApwQ8WlqD7F0gSDy7IsvAdHV09tR-4GJ2ETadFB6E2UKLtgJD6qpvhO2qjdOWSKsdQovGsynTwjbpdj32K1phxSXA");
//
//			inputJson.set("input", runtimeData);
//
//			ObjectNode executeGraphRequestNode = JsonUtils.mapper.createObjectNode();
//			executeGraphRequestNode.set(RESTServiceNode.METHOD.getParameterName(),
//					ParameterUtils.createParamValueNode("POST"));
//			executeGraphRequestNode.set(RESTServiceNode.URL.getParameterName(),
//					ParameterUtils.createParamValueNode(injector.getInstance(
//							Key.get(String.class, Names.named(PiedPiperConstants.PROD_EXECUTE_GRAPH_ENDPOINT)))));
//			executeGraphRequestNode.set(RESTServiceNode.BODY.getParameterName(),
//					ParameterUtils.createParamValueNode(inputJson));
//			executeGraphRequestNode.set(RESTServiceNode.OUTPUT_TYPE.getParameterName(),
//					ParameterUtils.createParamValueNode(PiedPiperConstants.AS_JSON));
//			NodeInput nodeInput = new NodeInput();
//			nodeInput.setInput(executeGraphRequestNode);
//			RESTServiceNode restNode = new RESTServiceNode();
//			restNode.setInjector(Guice.createInjector(new PiedPiperModule()));
//			long startTime = System.currentTimeMillis();
//			NodeOutput output = restNode.apply(nodeInput);
//			long endTime = System.currentTimeMillis();
//			System.out.println("Full Time = " + (endTime - startTime));
//			GraphDefinition graphDefinition = JsonUtils.mapper.treeToValue(output.getOutput().get("body"),
//					GraphDefinition.class);
////			try {
////			printAudit(graphDefinition);
////			} catch(Exception e) {
////				e.printStackTrace();
////			}
//
//		}
////		
////		logger.log("Fetched Graoh Json from Dynamo = " + JsonUtils.mapper.writeValueAsString(graphDefinition));
//
////		ExecuteGraphFunction graphFunction = new ExecuteGraphFunction();
////		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
////		graphFunction.handleRequest(new StringInputStream(inputJson.toString()), outputStream, createContext());
////		GraphDefinition graphResponse = JsonUtils.readValueSilent(new String(outputStream.toByteArray()),
////				GraphDefinition.class);
////		printAudit(graphResponse);
////		System.out.println(Jackson.toJsonPrettyString(graphDefinition));
//	}
//
//	@Test
//	public void testSetupCloudExecuteGraphFunction() throws UnsupportedEncodingException, IOException {
//		
////		ObjectNode inputJson = JsonUtils.mapper.createObjectNode();
////		inputJson.put(DynamoDBBaseNode.TABLE_NAME.getParameterName(), PiedPiperConstants.ALMIGHTY_TABLE_NAME);
////		inputJson.put(PiedPiperConstants.PROJECT_NAME, "Atom8");
////		inputJson.put(PiedPiperConstants.GRAPH_NAME, "SetupCloud");
////
////		ObjectNode runtimeData = JsonUtils.mapper.createObjectNode();
////		ArrayNode thingNameList = JsonUtils.mapper.createArrayNode();
////		thingNameList.add("a");
////		thingNameList.add("b");
////		System.out.println("Input"+JsonUtils.mapper.writeValueAsString(thingNameList));
//		System.out.println(JsonUtils.mapper.readTree("[\"a\",\"b\"]") instanceof ArrayNode);
////		runtimeData.put("thingNameList", JsonUtils.mapper.writeValueAsString(thingNameList));
////		runtimeData.put("idToken", "eyJhbGciOiJSUzI1NiIsImtpZCI6IjdkMmY5ZjNmYjgzZDYzMzc0OTdiNmY3Y2QyY2ZmNGRmYTVjMmU4YjgiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiQWthc2ggQXNob2siLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDQuZ29vZ2xldXNlcmNvbnRlbnQuY29tLy1vZ2VIaUl1MUJ1VS9BQUFBQUFBQUFBSS9BQUFBQUFBQUw2QS9EMFVXLXZLTzFVYy9zOTYtYy9waG90by5qcGciLCJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vYXRvbTgtMTU3NjE3IiwiYXVkIjoiYXRvbTgtMTU3NjE3IiwiYXV0aF90aW1lIjoxNTU0NjE4NTIwLCJ1c2VyX2lkIjoiTklvSWdCZ3l1QVZOZWFvWlZIWnhhR0F4UlpXMiIsInN1YiI6Ik5Jb0lnQmd5dUFWTmVhb1pWSFp4YUdBeFJaVzIiLCJpYXQiOjE1NTQ3MDIwODksImV4cCI6MTU1NDcwNTY4OSwiZW1haWwiOiJ0aGVoZWxsbWFrZXJAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImZpcmViYXNlIjp7ImlkZW50aXRpZXMiOnsiZ29vZ2xlLmNvbSI6WyIxMTExMzc3NzUzMDM1ODIxMzQ3NzciXSwiZW1haWwiOlsidGhlaGVsbG1ha2VyQGdtYWlsLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6Imdvb2dsZS5jb20ifX0.O74sNInKmWB9YmcL9l1prM59u5pCxDWCmJ_IupMxPKdTouU2-Jkg64axb94GiGfmJZ9ynZhbGt_0ADb3SdebGISYS9qs0T5Aqhn9BR4R-4jTGKkNae7V44sfr1sXwmTZKftY45y7AzmfRCcmC9wJJV7eLKAgZ33s3mAP6Ds2mTJ8nBaHkEyo-imoTkUN3U1rQ8aS7F2V8MgEo9I9fpVKNy1WPVQmgND4f-SHECopbblUJhbA5LPnLZJVYIP0AGarI73--aQfTtIIEJOzobbN6w9mfO8BChrBCUrNPC6iOAy2cmbF7mJfI8xUsRX9nO_OElqjG5k6ze3Jy3Tv_gAy-A");
////		inputJson.set("input", runtimeData);
////		ExecuteGraphLambdaFunction graphFunction = new ExecuteGraphLambdaFunction(
////				Modules.override(new PiedPiperModule()).with(new AbstractModule() {
////					@Provides
////					@Singleton
////					@Named(PiedPiperConstants.NODEJS_TEMPLATE_PATH)
////					public String getNodeJsTemplatePath() {
////						return "src/main/java/com/github/piedpiper/node/business/pied-piper-script.js";
////					}
////				}));
////
////		for (int i = 0; i < 1; i++) {
////			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
////			graphFunction.handleRequest(new StringInputStream(inputJson.toString()), outputStream, createContext());
////			GraphDefinition graphResponse = JsonUtils.readValueSilent(new String(outputStream.toByteArray()),
////					GraphDefinition.class);
//////			printAudit(graphResponse);
//////			System.out.println(Jackson.toJsonPrettyString(graphResponse));
////		}
//	}
//
//	@Test
//	public void testExecuteGraphFunction() throws UnsupportedEncodingException, IOException {
//		ObjectNode inputJson = JsonUtils.mapper.createObjectNode();
//		inputJson.put(DynamoDBBaseNode.TABLE_NAME.getParameterName(), PiedPiperConstants.ALMIGHTY_TABLE_NAME);
//		inputJson.put(PiedPiperConstants.PROJECT_NAME, "Atom8");
//		inputJson.put(PiedPiperConstants.GRAPH_NAME, "ListInactiveDevices");
//
////		ObjectNode instanceNode = JsonUtils.mapper.createObjectNode();
////		instanceNode.put("instanceId", "0");
////		instanceNode.put("lastAction", "cloud_network");
////		instanceNode.put("instanceName", "Switch 0");
////		instanceNode.put("value", "1");
////
////		ObjectNode instanceListNode = JsonUtils.mapper.createObjectNode();
////		instanceListNode.set("0", instanceNode);
////
////		ObjectNode desiredJson = JsonUtils.mapper.createObjectNode();
////		desiredJson.set("instances", instanceListNode);
////
////		ObjectNode stateJson = JsonUtils.mapper.createObjectNode();
////		stateJson.set("desired", desiredJson);
////
////		ObjectNode bodyJson = JsonUtils.mapper.createObjectNode();
////		bodyJson.set("state", stateJson);
//
//		ObjectNode runtimeData = JsonUtils.mapper.createObjectNode();
////		runtimeData.put("thingGroupName", "NIoIgBgyuAVNeaoZVHZxaGAxRZW2");
////		runtimeData.put("idToken", "eyJhbGciOiJSUzI1NiIsImtpZCI6IjY1NmMzZGQyMWQwZmVmODgyZTA5ZTBkODY5MWNhNWM3ZjJiMGQ2MjEiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiQWthc2ggQXNob2siLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDQuZ29vZ2xldXNlcmNvbnRlbnQuY29tLy1vZ2VIaUl1MUJ1VS9BQUFBQUFBQUFBSS9BQUFBQUFBQUw2QS9EMFVXLXZLTzFVYy9zOTYtYy9waG90by5qcGciLCJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vYXRvbTgtMTU3NjE3IiwiYXVkIjoiYXRvbTgtMTU3NjE3IiwiYXV0aF90aW1lIjoxNTU1MDYwMzE0LCJ1c2VyX2lkIjoiTklvSWdCZ3l1QVZOZWFvWlZIWnhhR0F4UlpXMiIsInN1YiI6Ik5Jb0lnQmd5dUFWTmVhb1pWSFp4YUdBeFJaVzIiLCJpYXQiOjE1NTYxNzkzMjMsImV4cCI6MTU1NjE4MjkyMywiZW1haWwiOiJ0aGVoZWxsbWFrZXJAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImZpcmViYXNlIjp7ImlkZW50aXRpZXMiOnsiZ29vZ2xlLmNvbSI6WyIxMTExMzc3NzUzMDM1ODIxMzQ3NzciXSwiZW1haWwiOlsidGhlaGVsbG1ha2VyQGdtYWlsLmNvbSJdfSwic2lnbl9pbl9wcm92aWRlciI6Imdvb2dsZS5jb20ifX0.VpxjB7CsZFNvgs4pe6a2aonxrNAZieleZoI8Ty93b84n-VCmZNZG9Kmx6JmGk1m-L4gjwo8LfKDloHSkgK6kN0Vvsh3fEfmYApcbesHH7BGIO03b4vO7uJxQqODW0Zh7ToTIr5seCJgL66saZQtFQ5omiNfzapXR5ec6k-YigUBZrOSEphgYSWmZZkiv7jc301I3C0EUeSGX__20uVGCw7JnkWIyYNF6H7nHNqexZ03tAlsDwzWl8CZVcvQuFpTixPxGPieqxLpXCZgRw3ByZBgGOWA3-s45Ud-2rHo3iygqdiee0LigjHCoVwuHL5q9EiV3cbvS09z3d1eu4CCSTA");
//		inputJson.set("input", runtimeData);
//		ExecuteGraphLambdaFunction graphFunction = new ExecuteGraphLambdaFunction(new PiedPiperModule());
//
//		for (int i = 0; i < 1; i++) {
//			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//			graphFunction.handleRequest(new StringInputStream(inputJson.toString()), outputStream, createContext());
//			GraphDefinition graphResponse = JsonUtils.readValueSilent(new String(outputStream.toByteArray()),
//					GraphDefinition.class);
////			printAudit(graphResponse);
////			System.out.println(Jackson.toJsonPrettyString(graphResponse));
//		}
//
//	}
//
//	private void printAudit(GraphDefinition graphResponse) throws JsonProcessingException {
//		for (Entry<String, NodeDefinition> nodeMapEntry : graphResponse.getNodeMap().entrySet()) {
//			NodeDefinition nodeDef = nodeMapEntry.getValue();
//			List<NodeOutput> nodeOutputList = nodeDef.getNodeOutputList();
//			int i = 0;
//			for (NodeOutput nodeOutput : nodeOutputList) {
//				AuditInfo auditInfo = nodeOutput.getAuditInfo();
//				long timetaken = auditInfo.getEndTimestamp() - auditInfo.getStartTimestamp();
//				logger.log(String.format("GraphName: %s, NodeName: %s, NodeOutput: %s, Timetaken: %s",
//						graphResponse.getGraphName(), nodeOutput.getNodeName(), String.valueOf(++i),
//						String.valueOf(timetaken / 1000000)));
//				if (nodeDef.getNodeClass().equals("com.github.piedpiper.node.piedpiper.SubGraphNode")) {
//					GraphDefinition subGraphOutput = JsonUtils.mapper.treeToValue(nodeOutput.getOutput().get("body"),
//							GraphDefinition.class);
//					logger.log(String.format("GraphName: %s, Timetaken: %s", subGraphOutput.getGraphName(),
//							(subGraphOutput.getAuditInfo().getEndTimestamp()
//									- subGraphOutput.getAuditInfo().getStartTimestamp())));
//					printAudit(subGraphOutput);
//				}
//			}
//
//		}
//	}
//
	private String getFileName(String fileName) {
		return TestConstants.TEST_PATH + "/graph/api/resources/" + fileName;
	}

	private String getNodeFileName(String fileName) {
		return TestConstants.TEST_PATH + "/node/rest/resources/" + fileName;
	}

	private Context createContext() {
		TestContext ctx = new TestContext();
		ctx.setFunctionName("Your Function Name");
		return ctx;
	}
}
