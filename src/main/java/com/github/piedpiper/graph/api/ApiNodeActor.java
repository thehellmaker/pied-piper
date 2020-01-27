package com.github.piedpiper.graph.api;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.commons.log.ILogger;
import com.github.piedpiper.graph.api.types.AuditInfo;
import com.github.piedpiper.graph.api.types.NodeExecutor;
import com.github.piedpiper.node.BaseNode;
import com.github.piedpiper.node.NodeOutput;
import com.google.inject.Injector;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class ApiNodeActor extends AbstractActor {

	private ObjectMapper mapper;

	private Injector injector;

	private ILogger logger;

	static Props props(Injector injector, ILogger logger) {
		return Props.create(ApiNodeActor.class, () -> new ApiNodeActor(injector, logger));
	}

	public ApiNodeActor(Injector injector, ILogger logger) {
		this.injector = injector;
		this.logger = logger;
		this.mapper = injector.getInstance(ObjectMapper.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(NodeExecutor.class, nodeExecutor -> {
			try {
				Class<? extends BaseNode> clazz = (Class<? extends BaseNode>) Class
						.forName(nodeExecutor.getNodeDefinition().getNodeClass());
				BaseNode baseNode = injector.getInstance(clazz);
				baseNode.setILogger(logger);
				baseNode.setInjector(injector);
				long startTimestamp = System.nanoTime();
				NodeOutput nodeOutput = baseNode.apply(nodeExecutor.getNodeInput());
				nodeOutput.setNodeName(nodeExecutor.getNodeDefinition().getNodeName());
				long endTimestamp = System.nanoTime();
				nodeOutput.setAuditInfo(new AuditInfo(startTimestamp, endTimestamp));
				getSender().tell(nodeOutput, getSelf());
			} catch (Exception e) {
				NodeOutput nodeOutput = new NodeOutput();
				nodeOutput.setStackTrace(ExceptionUtils.getStackTrace(e));
				nodeOutput.setNodeName(nodeExecutor.getNodeDefinition().getNodeName());
//				logger.log(String.format("Exception while executing node actor execution for nodeDefinition = %s",
//						mapper.writeValueAsString(nodeOutput)));
				getSender().tell(nodeOutput, getSelf());
			}
		}).build();
	}
}
