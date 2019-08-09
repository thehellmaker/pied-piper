package com.github.piedpiper.graph.api;

import java.io.FileInputStream;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParameterResult;
import com.amazonaws.services.simplesystemsmanagement.model.Parameter;
import com.amazonaws.services.simplesystemsmanagement.model.ParameterType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.commons.log.ILogger;
import com.github.commons.log.Slf4jLoggerImpl;
import com.github.piedpiper.graph.api.types.ContractInput;
import com.github.piedpiper.graph.api.types.GraphDefinition;
import com.github.piedpiper.graph.api.types.NodeDefinition;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.dynamo.TestConstants;
import com.github.piedpiper.transformer.CleanupOutputTransformer;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class ApiGraphActorTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final ActorSystem system = ActorSystem.create("graphExecutor");

	private static final ILogger logger = new Slf4jLoggerImpl();

	protected static final String AWS_SSM_MOCK_VALUE = "AWS_SSM_MOCK_VALUE";

	private Injector injector;

	@Before
	public void setup() {
		this.injector = Guice.createInjector(new Module() {

			@Override
			public void configure(Binder arg0) {
			}

			@Provides
			@Singleton
			public AWSSimpleSystemsManagement getAWSSimpleSystemsManagementClient() {
				AWSSimpleSystemsManagement ssmClient = Mockito.mock(AWSSimpleSystemsManagement.class);
				Mockito.when(ssmClient.getParameter(Mockito.any())).thenReturn(new GetParameterResult().withParameter(
						new Parameter().withName("AccessKey").withType(ParameterType.SecureString).withValue(AWS_SSM_MOCK_VALUE)));
				return ssmClient;
			}
			@Provides
			@Singleton
			public LoadingCache<String, String> getInMemoryCache(AWSSimpleSystemsManagement ssmClient) {
				return CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, String>() {
					public String load(String parameterName) {
						return ParameterUtils.resolveAWSSSMParameter(ssmClient, parameterName);
					}
				});
			}
		});
	}

	@Test
	public void testSuccess() throws Exception {
		JsonNode graph = new ObjectMapper().readTree(new FileInputStream(getFileName("successGraph.json")));
		ContractInput input = new ContractInput(graph, mapper.readTree("{ \"runtimeValueParameterValueBasedInput\": \"ashokValueBased\"}"));
		Props graphProps = ApiGraphActor.props(injector, logger);
		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, graphProps, "graphActor1");
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = akka.pattern.Patterns.ask(ref, input, timeout);
		GraphDefinition executedGraphDefinition = (GraphDefinition) Await.result(future, timeout.duration());
		Assert.assertNotNull(executedGraphDefinition);
		NodeDefinition node1 = executedGraphDefinition.getNodeMap().get("Node1");
		NodeDefinition node2 = executedGraphDefinition.getNodeMap().get("Node2");
		NodeDefinition node3 = executedGraphDefinition.getNodeMap().get("Node3");
		NodeDefinition node4 = executedGraphDefinition.getNodeMap().get("Node4");
		NodeDefinition node5 = executedGraphDefinition.getNodeMap().get("Node5");
		NodeDefinition node6 = executedGraphDefinition.getNodeMap().get("Node6");
		NodeDefinition node7 = executedGraphDefinition.getNodeMap().get("Node7");

		Assert.assertTrue(node3.getNodeOutputList().get(0).getAuditInfo().getStartTimestamp() > node2.getNodeOutputList().get(0).getAuditInfo().getEndTimestamp());
		Assert.assertTrue(node3.getNodeOutputList().get(0).getAuditInfo().getStartTimestamp() > node1.getNodeOutputList().get(0).getAuditInfo().getEndTimestamp());

		Assert.assertTrue(node3.getNodeOutputList().get(0).getAuditInfo().getEndTimestamp() < node4.getNodeOutputList().get(0).getAuditInfo().getStartTimestamp());
		Assert.assertTrue(node3.getNodeOutputList().get(0).getAuditInfo().getEndTimestamp() < node5.getNodeOutputList().get(0).getAuditInfo().getStartTimestamp());
		Assert.assertTrue(node3.getNodeOutputList().get(0).getAuditInfo().getEndTimestamp() < node6.getNodeOutputList().get(0).getAuditInfo().getStartTimestamp());

		Assert.assertTrue(node7.getNodeOutputList().get(0).getAuditInfo().getStartTimestamp() > node4.getNodeOutputList().get(0).getAuditInfo().getEndTimestamp());
		Assert.assertTrue(node7.getNodeOutputList().get(0).getAuditInfo().getStartTimestamp() > node5.getNodeOutputList().get(0).getAuditInfo().getEndTimestamp());
		Assert.assertTrue(node7.getNodeOutputList().get(0).getAuditInfo().getStartTimestamp() > node6.getNodeOutputList().get(0).getAuditInfo().getEndTimestamp());


		Assert.assertEquals("akash/ashokValueBased/constant", node3.getNodeInputList().get(0).getInput().get("param1").get("value").asText());
		Assert.assertEquals("ashokValueBased", node3.getNodeInputList().get(0).getInput().get("runtimeValueParameterValueBased").get("value").asText());
		Assert.assertEquals("sexyValue", node3.getNodeInputList().get(0).getInput().get("param3").get("value").asText());
		Assert.assertEquals("sexyValue2", node3.getNodeInputList().get(0).getInput().get("param4").get("value").asText());
		Assert.assertEquals("akash", node3.getNodeInputList().get(0).getInput().get("param6").get("value").get("name").asText());
		Assert.assertEquals("akash", node3.getNodeInputList().get(0).getInput().get("param7").get("value").asText());

		Assert.assertEquals("sexyValue3", node4.getNodeInputList().get(0).getInput().get("param1").get("value").asText());
		Assert.assertEquals("sexyValue3", node5.getNodeInputList().get(0).getInput().get("param1").get("value").asText());
		Assert.assertEquals("sexyValue3", node6.getNodeInputList().get(0).getInput().get("param1").get("value").asText());

		Assert.assertEquals("sexyValue4", node7.getNodeInputList().get(0).getInput().get("param1").get("value").asText());
		Assert.assertEquals("sexyValue5", node7.getNodeInputList().get(0).getInput().get("param2").get("value").asText());
		Assert.assertEquals("sexyValue6", node7.getNodeInputList().get(0).getInput().get("param3").get("value").asText());
	}
	
	@Test
	public void testSubstitutionInputMissingError() throws Exception {
		JsonNode graph = new ObjectMapper().readTree(new FileInputStream(getFileName("substitutionInputMissingError.json")));
		ContractInput input = new ContractInput(graph, mapper.readTree("{}"));
		Props graphProps = ApiGraphActor.props(injector, logger);
		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, graphProps, "graphActorSubstitutionInputMissing");
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = akka.pattern.Patterns.ask(ref, input, timeout);
		GraphDefinition executedGraphDefinition = (GraphDefinition) Await.result(future, timeout.duration());
		Assert.assertNotNull(executedGraphDefinition);
		NodeDefinition node1 = executedGraphDefinition.getNodeMap().get("Node1");



		Assert.assertTrue(node1.getStackTrace().contains("Unable to substiture param = runtimeValueParameterValueBased"));

	}


	@Test
	public void testNodeException() throws Exception {
		JsonNode graph = new ObjectMapper().readTree(new FileInputStream(getFileName("nodeExceptionGraph.json")));
		ContractInput input = new ContractInput(graph, mapper.readTree("{\"runtimeValue\": \"ashok\"}"));
		Props graphProps = ApiGraphActor.props(injector, logger);
		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, graphProps, "graphActor2");
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = akka.pattern.Patterns.ask(ref, input, timeout);
		GraphDefinition response = (GraphDefinition) Await.result(future, timeout.duration());
		Assert.assertNotNull(response);
		Assert.assertTrue(StringUtils
				.isNotBlank(response.getNodeMap().get("Node1").getNodeOutputList().get(0).getStackTrace()));
	}
	
	@Test
	public void testDataTypeAttributeException() throws Exception {
		JsonNode graph = new ObjectMapper().readTree(new FileInputStream(getFileName("dataTypeAttributeExceptionGraph.json")));
		ContractInput input = new ContractInput(graph, mapper.readTree("{\"runtimeValue\": \"ashok\"}"));
		Props graphProps = ApiGraphActor.props(injector, logger);
		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, graphProps, "graphActorDataType");
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = akka.pattern.Patterns.ask(ref, input, timeout);
		GraphDefinition executedGraphDefinition = (GraphDefinition) Await.result(future, timeout.duration());
		Assert.assertNotNull(executedGraphDefinition);
		Assert.assertTrue(StringUtils
				.isNotBlank(executedGraphDefinition.getNodeMap().get("Node3").getStackTrace()));
	}

	@Test
	public void testGraphException() throws Exception {
		JsonNode graph = new ObjectMapper().readTree(new FileInputStream(getFileName("graphExceptionGraph.json")));
		ContractInput input = new ContractInput(graph, mapper.readTree("{\"runtimeValue\": \"ashok\"}"));
		Props graphProps = ApiGraphActor.props(injector, logger);
		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, graphProps, "graphActor3");
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = akka.pattern.Patterns.ask(ref, input, timeout);
		GraphDefinition executedGraphDefinition = (GraphDefinition) Await.result(future, timeout.duration());
		Assert.assertNotNull(executedGraphDefinition);
		Assert.assertTrue(StringUtils.isNotBlank(executedGraphDefinition.getExceptionTrace()));
	}

	@Test
	public void testGraphUnknownParameterTypeException() throws Exception {
		JsonNode graph = new ObjectMapper().readTree(new FileInputStream(getFileName("invalidParameterType.json")));
		ContractInput input = new ContractInput(graph, mapper.readTree("{\"runtimeValue\": \"ashok\"}"));
		Props graphProps = ApiGraphActor.props(injector, logger);
		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, graphProps, "graphActor4");
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = akka.pattern.Patterns.ask(ref, input, timeout);
		GraphDefinition executedGraphDefinition = (GraphDefinition) Await.result(future, timeout.duration());
		Assert.assertNotNull(executedGraphDefinition);
		Assert.assertTrue(
				StringUtils.contains(executedGraphDefinition.getNodeMap().get("Node1").getStackTrace(), "IncorrectParameterTypeException"));
	}

	@Test
	public void testGraphDependentNodeException() throws Exception {
		JsonNode graph = new ObjectMapper().readTree(new FileInputStream(getFileName("dependentNodeException.json")));
		ContractInput input = new ContractInput(graph, mapper.readTree("{\"runtimeValue\": \"ashok\"}"));
		Props graphProps = ApiGraphActor.props(injector, logger);
		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, graphProps, "graphActor5");
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = akka.pattern.Patterns.ask(ref, input, timeout);
		GraphDefinition executedGraphDefinition = (GraphDefinition) Await.result(future, timeout.duration());
		Assert.assertNotNull(executedGraphDefinition);
		Assert.assertTrue(StringUtils.contains(executedGraphDefinition.getNodeMap().get("Node2").getNodeOutputList().get(0).getStackTrace(), "java.lang.ClassNotFoundException"));
		Assert.assertTrue(StringUtils.contains(executedGraphDefinition.getNodeMap().get("Node3").getStackTrace(), "DependentNodeException"));
	}

	@Test
	public void testGraphMapperJsonProcessingException() throws Exception {
		Injector mockedInjector = Guice.createInjector(new Module() {

			@Override
			public void configure(Binder arg0) {
			}

			@Provides
			public ObjectMapper provideObjectMapper() throws JsonProcessingException {
				ObjectMapper mapper = Mockito.spy(ObjectMapper.class);
				JsonProcessingException jsonProcessingException = Mockito.mock(JsonProcessingException.class);
				Mockito.doThrow(jsonProcessingException).when(mapper).treeToValue(Mockito.any(), Mockito.any());
				return mapper;
			}

		});
		JsonNode graph = new ObjectMapper().readTree(new FileInputStream(getFileName("dependentNodeException.json")));
		ContractInput input = new ContractInput(graph, mapper.readTree("{\"runtimeValue\": \"ashok\"}"));
		Props graphProps = ApiGraphActor.props(mockedInjector, logger);
		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, graphProps, "graphActor6");
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = akka.pattern.Patterns.ask(ref, input, timeout);
		GraphDefinition executedGraphDefinition = (GraphDefinition) Await.result(future, timeout.duration());
		Assert.assertNotNull(executedGraphDefinition);
		Assert.assertTrue(StringUtils.contains(executedGraphDefinition.getExceptionTrace(), "JsonProcessingException"));
	}

	@Test
	public void testGraphMapperJsonProcessingExceptionHandlingThrowingException() throws Exception {
		Injector mockedInjector = Guice.createInjector(new Module() {

			@Override
			public void configure(Binder arg0) {
			}

			@Provides
			public ObjectMapper provideObjectMapper() throws JsonProcessingException {
				ObjectMapper mapper = Mockito.spy(ObjectMapper.class);
				JsonProcessingException jsonProcessingException = Mockito.mock(JsonProcessingException.class);
				Mockito.doThrow(jsonProcessingException).when(mapper).treeToValue(Mockito.any(), Mockito.any());
				Mockito.doThrow(jsonProcessingException).when(mapper)
						.writeValueAsString(Mockito.any(GraphDefinition.class));
				return mapper;
			}

		});
		JsonNode graph = new ObjectMapper().readTree(new FileInputStream(getFileName("dependentNodeException.json")));
		ContractInput input = new ContractInput(graph, mapper.readTree("{\"runtimeValue\": \"ashok\"}"));
		Props graphProps = ApiGraphActor.props(mockedInjector, logger);
		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, graphProps, "graphActor7");
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = akka.pattern.Patterns.ask(ref, input, timeout);
		GraphDefinition response = (GraphDefinition) Await.result(future, timeout.duration());
		Assert.assertNotNull(response);
	}


	@Test
	public void testParallelizeBasic() throws Exception {
		JsonNode graph = new ObjectMapper().readTree(new FileInputStream(getFileName("parallelizeNodeExecutionGraph.json")));
		ContractInput input = new ContractInput(graph, null);
		Props graphProps = ApiGraphActor.props(injector, logger);
		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, graphProps, "graphActorParallelize");
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = akka.pattern.Patterns.ask(ref, input, timeout);
		GraphDefinition executedGraphDefinition = (GraphDefinition) Await.result(future, timeout.duration());
		Assert.assertNotNull(executedGraphDefinition);
		System.out.println(executedGraphDefinition.getExceptionTrace());
		Assert.assertTrue(StringUtils.isBlank(executedGraphDefinition.getExceptionTrace()));
		List<NodeOutput> nodeOutputList = executedGraphDefinition.getNodeMap().get("Node1").getNodeOutputList();
		Assert.assertEquals(3, nodeOutputList.size());
		Assert.assertTrue(nodeOutputList.get(0).getAuditInfo().getStartTimestamp() < nodeOutputList.get(1).getAuditInfo().getEndTimestamp());
		Assert.assertTrue(nodeOutputList.get(0).getAuditInfo().getStartTimestamp() < nodeOutputList.get(2).getAuditInfo().getEndTimestamp());
		Assert.assertTrue(nodeOutputList.get(1).getAuditInfo().getStartTimestamp() < nodeOutputList.get(0).getAuditInfo().getEndTimestamp());
		Assert.assertTrue(nodeOutputList.get(1).getAuditInfo().getStartTimestamp() < nodeOutputList.get(2).getAuditInfo().getEndTimestamp());
		Assert.assertTrue(nodeOutputList.get(2).getAuditInfo().getStartTimestamp() < nodeOutputList.get(0).getAuditInfo().getEndTimestamp());
		Assert.assertTrue(nodeOutputList.get(2).getAuditInfo().getStartTimestamp() < nodeOutputList.get(1).getAuditInfo().getEndTimestamp());
		
		List<NodeInput> node2InputList = executedGraphDefinition.getNodeMap().get("Node2").getNodeInputList();
		Assert.assertTrue(node2InputList.get(0).getInput().get("param1").get("value") instanceof ArrayNode);
		
		List<NodeInput> node3InputList = executedGraphDefinition.getNodeMap().get("Node3").getNodeInputList();
		Assert.assertEquals(3, node3InputList.size());
	}
	
	@Test
	public void testMultipleParametersParallelize() throws Exception {
		JsonNode graph = new ObjectMapper().readTree(new FileInputStream(getFileName("multipleParametersWithParallelizeAttribute.json")));
		ContractInput input = new ContractInput(graph, null);
		Props graphProps = ApiGraphActor.props(injector, logger);
		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, graphProps, "graphActorMultipleParametersParallelize");
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = akka.pattern.Patterns.ask(ref, input, timeout);
		GraphDefinition executedGraphDefinition = (GraphDefinition) Await.result(future, timeout.duration());
		Assert.assertNotNull(executedGraphDefinition);
		System.out.println(executedGraphDefinition.getExceptionTrace());
		Assert.assertTrue(StringUtils.isBlank(executedGraphDefinition.getExceptionTrace()));
		NodeDefinition nodeDef = executedGraphDefinition.getNodeMap().get("Node1");
		Assert.assertTrue(nodeDef.getStackTrace().contains("has two or more parameters having parallelize attribute on them"));
		Assert.assertTrue(CollectionUtils.isEmpty(nodeDef.getNodeOutputList()));
	}
	
	private String getFileName(String fileName) {
		return TestConstants.TEST_PATH + "/graph/api/resources/" + fileName;
	}

}
