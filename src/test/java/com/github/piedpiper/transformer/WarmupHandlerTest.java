package com.github.piedpiper.transformer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.commons.log.ILogger;
import com.github.commons.log.Slf4jLoggerImpl;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.dynamo.DynamoDBReaderNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import akka.actor.ActorSystem;

public class WarmupHandlerTest {
	
	private static final ILogger logger = new Slf4jLoggerImpl();
	@Test
	public void testPiedPiperExecuteGraphGetFromDynamoSuccess() throws Exception {
		
		Injector injector = Guice.createInjector(Lists.newArrayList(new AbstractModule() {
			@Override
			protected void configure() {
				ActorSystem system = Mockito.mock(ActorSystem.class);
				bind(ActorSystem.class).annotatedWith(Names.named(PiedPiperConstants.GRAPH_ACTOR)).toInstance(system);
				DynamoDBReaderNode searchRestServiceNode = Mockito.mock(DynamoDBReaderNode.class);
				JsonNode outputJsonNode = null;
				try {
					outputJsonNode = JsonUtils.mapper.readTree(" [{\"graph\": \"{\\\"projectName\\\": \\\"a\\\", \\\"graphName\\\": \\\"b\\\"}\"}]");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				NodeOutput nodeOutput = new NodeOutput();
				nodeOutput.setOutput(outputJsonNode);
				Mockito.when(searchRestServiceNode.apply(Mockito.any())).thenReturn(nodeOutput);
				bind(DynamoDBReaderNode.class).toInstance(searchRestServiceNode);
			}
			
			@Provides
			@Singleton
			@Named(PiedPiperConstants.AWS_SSM_CACHE)
			public LoadingCache<String, String> getInMemoryCache() {
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
			
			@Provides
			@Singleton
			@Named(PiedPiperConstants.GRAPH_CACHE)
			public Map<String, JsonNode> getGraphCache() {
				return Maps.newHashMap();
			}
			
		}));
		
		WarmupHandler handler = new WarmupHandler(injector, logger, true);

		handler.apply(null);
		
	}
}
