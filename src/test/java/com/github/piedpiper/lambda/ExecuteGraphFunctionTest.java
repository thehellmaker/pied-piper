package com.github.piedpiper.lambda;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.util.StringInputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.api.types.ContractInput;
import com.github.piedpiper.graph.api.types.GraphDefinition;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.dynamo.DynamoDBReaderNode;
import com.github.piedpiper.transformer.ExecuteGraphFunction;
import com.github.piedpiper.transformer.WarmupHandler;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@SuppressWarnings("unchecked")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Patterns.class, Await.class, ExecuteGraphFunction.class })
public class ExecuteGraphFunctionTest {

	@BeforeClass
	public static void createInput() throws IOException {
	}

	private Context createContext() {
		TestContext ctx = new TestContext();
		ctx.setFunctionName("Your Function Name");
		return ctx;
	}

	@Test
	public void testPiedPiperExecuteGraphSuccess() throws Exception {
		PowerMockito.mockStatic(Patterns.class);
		Future<Object> future = Mockito.mock(Future.class);
		PowerMockito.when(
				Patterns.ask(Mockito.any(ActorRef.class), Mockito.any(ContractInput.class), Mockito.any(Timeout.class)))
				.thenReturn(future);
		PowerMockito.mockStatic(Await.class);
		GraphDefinition definition = new GraphDefinition();
		definition.setExceptionTrace("dummy");
		PowerMockito.when(Await.result(Mockito.any(), Mockito.any())).thenReturn(definition);
		WarmupHandler warmup = Mockito.mock(WarmupHandler.class);
		Mockito.when(warmup.apply(Mockito.any())).thenReturn(null);
		PowerMockito.whenNew(WarmupHandler.class).withAnyArguments().thenReturn(warmup);
		ExecuteGraphLambdaFunction handler = new ExecuteGraphLambdaFunction(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
				ActorSystem system = Mockito.mock(ActorSystem.class);
				bind(ActorSystem.class).annotatedWith(Names.named(PiedPiperConstants.GRAPH_ACTOR)).toInstance(system);
			}
			@Provides
			@Singleton
			@Named(PiedPiperConstants.GRAPH_CACHE)
			public Map<String, JsonNode> getGraphCache() {
				return Maps.newHashMap();
			}
			
			@Provides
			@Singleton
			@Named(PiedPiperConstants.AWS_SSM_CACHE)
			public LoadingCache<String, String> getInMemoryAWSSSMCache() {
				return CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<String, String>() {
					public String load(String parameterName) {
						return "";
					}
				});
			}

			@Provides
			@Singleton
			public ExecutorService getExecutorService() {
				return Executors.newCachedThreadPool();
			}
		}));
		
		Context ctx = createContext();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		handler.handleRequest(new StringInputStream("{\"graph\": \"dummy\"}"), outputStream, ctx);
		GraphDefinition output = JsonUtils.readValueSilent(new String(outputStream.toByteArray()),
				GraphDefinition.class);
		ArgumentCaptor<ContractInput> contractInputCaptor = ArgumentCaptor.forClass(ContractInput.class);
		PowerMockito.verifyStatic(Patterns.class);
		Patterns.ask(Mockito.any(ActorRef.class), contractInputCaptor.capture(), Mockito.any(Timeout.class));
		Assert.assertEquals("dummy", contractInputCaptor.getValue().getGraphJson().asText());
		Assert.assertEquals("dummy", output.getExceptionTrace());
	}

	@Test
	public void testPiedPiperExecuteGraphException() throws Exception {
		PowerMockito.mockStatic(Patterns.class);
		Future<Object> future = Mockito.mock(Future.class);
		PowerMockito.when(Patterns.ask(Mockito.any(ActorRef.class), Mockito.any(Object.class), Mockito.anyLong()))
				.thenReturn(future);
		PowerMockito.mockStatic(Await.class);
		PowerMockito.when(Await.result(Mockito.any(), Mockito.any())).thenThrow(new NullPointerException());
		ExecuteGraphLambdaFunction handler = new ExecuteGraphLambdaFunction(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
				ActorSystem system = Mockito.mock(ActorSystem.class);
				bind(ActorSystem.class).annotatedWith(Names.named(PiedPiperConstants.GRAPH_ACTOR)).toInstance(system);
			}
			@Provides
			@Singleton
			@Named(PiedPiperConstants.GRAPH_CACHE)
			public Map<String, JsonNode> getGraphCache() {
				return Maps.newHashMap();
			}
			
		}));
		Context ctx = createContext();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		handler.handleRequest(new StringInputStream(""), outputStream, ctx);
		GraphDefinition output = JsonUtils.readValueSilent(new String(outputStream.toByteArray()),
				GraphDefinition.class);
		// TODO: validate output here if needed.
		Assert.assertTrue(output.getExceptionTrace().contains("NullPointerException"));
	}

	

	@Test
	public void testPiedPiperExecuteGraphGetNoGraphIdentifierException() throws Exception {
		PowerMockito.mockStatic(Patterns.class);
		Future<Object> future = Mockito.mock(Future.class);
		PowerMockito
				.when(Patterns.ask(Mockito.any(ActorRef.class), Mockito.any(ContractInput.class), Mockito.anyLong()))
				.thenReturn(future);
		PowerMockito.mockStatic(Await.class);
		PowerMockito.when(Await.result(Mockito.any(), Mockito.any())).thenReturn("{}");
		ExecuteGraphLambdaFunction handler = new ExecuteGraphLambdaFunction(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
				ActorSystem system = Mockito.mock(ActorSystem.class);
				bind(ActorSystem.class).annotatedWith(Names.named(PiedPiperConstants.GRAPH_ACTOR)).toInstance(system);
			}
			@Provides
			@Singleton
			@Named(PiedPiperConstants.GRAPH_CACHE)
			public Map<String, JsonNode> getGraphCache() {
				return Maps.newHashMap();
			}
			
		}));

		Context ctx = createContext();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		handler.handleRequest(new StringInputStream("{}"), outputStream, ctx);
		GraphDefinition output = JsonUtils.readValueSilent(new String(outputStream.toByteArray()),
				GraphDefinition.class);
		Assert.assertTrue(output.getExceptionTrace().contains("RuntimeException"));
	}

}
