package com.github.piedpiper.graph.api;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.commons.log.ILogger;
import com.github.commons.log.Slf4jLoggerImpl;
import com.github.piedpiper.graph.api.types.NodeDefinition;
import com.github.piedpiper.graph.api.types.NodeExecutor;
import com.github.piedpiper.guice.PiedPiperModule;
import com.github.piedpiper.node.NodeOutput;
import com.google.inject.Guice;
import com.google.inject.Injector;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class ApiNodeActorTest {
	
	private static final ILogger logger = new Slf4jLoggerImpl();

	private ActorSystem system;

	private Injector injector;

	@Before
	public void setupClass() {
		this.injector = Guice.createInjector(new PiedPiperModule()); 
		this.system = ActorSystem.create();
	}

	@Test
	public void testSuccess() {
		final TestKit probe = new TestKit(this.system);
		final ActorRef apiNodeActor = system.actorOf(ApiNodeActor.props(injector, logger), "nodeActor");
		NodeDefinition nodeDef = new NodeDefinition();
		nodeDef.setNodeClass("com.github.piedpiper.node.INodeDummyMock");
		NodeExecutor executor = new NodeExecutor();
		executor.setNodeDefinition(nodeDef);
		apiNodeActor.tell(executor, probe.getRef());
		Assert.assertNotNull(probe.expectMsgClass(NodeOutput.class));
	}
	
	@Test
	public void testException() throws Exception {
		final Props nodeProps = ApiNodeActor.props(injector, logger);
		NodeDefinition nodeDef = new NodeDefinition();
		nodeDef.setNodeClass("UnknownClass");
		NodeExecutor executor = new NodeExecutor();
		executor.setNodeDefinition(nodeDef);
		TestActorRef<ApiGraphActor> ref = TestActorRef.create(system, nodeProps, "nodeActor");
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = akka.pattern.Patterns.ask(ref, executor, timeout);
		NodeOutput response = (NodeOutput) Await.result(future, timeout.duration());
		Assert.assertNotNull(response);
		Assert.assertTrue(StringUtils.isNotBlank(response.getStackTrace()));
	}

}
