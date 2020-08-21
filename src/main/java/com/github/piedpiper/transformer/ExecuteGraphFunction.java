package com.github.piedpiper.transformer;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.commons.log.ILogger;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.api.ApiGraphActor;
import com.github.piedpiper.graph.api.types.AuditInfo;
import com.github.piedpiper.graph.api.types.GraphInput;
import com.github.piedpiper.graph.storage.IGraphStorage;
import com.github.piedpiper.graph.storage.VersionType;
import com.github.piedpiper.graph.storage.QueryGraphInput.SortType;
import com.github.piedpiper.graph.api.types.GraphDefinition;
import com.github.piedpiper.node.aws.dynamo.DynamoDBBaseNode;
import com.github.piedpiper.utils.SearchGraphUtils;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

public class ExecuteGraphFunction implements Function<JsonNode, GraphDefinition> {

	private ILogger logger;

	private Injector injector;

	private IGraphStorage graphStorage;

	public ExecuteGraphFunction(ILogger logger, Injector injector) {
		this.logger = logger;
		this.injector = injector;
		this.graphStorage = injector.getInstance(IGraphStorage.class);
	}

	@Override
	public GraphDefinition apply(JsonNode inputJson) {
		ActorRef graphActorRef = getActorSystem().actorOf(ApiGraphActor.props(injector, logger));
		try {
			long startTime = System.currentTimeMillis();
			GraphInput graphInput = new GraphInput(getGraphNode(inputJson), inputJson.get(PiedPiperConstants.INPUT));
			Timeout timeout = new Timeout(Duration.create(90, "seconds"));
			Future<Object> future = Patterns.ask((ActorRef) graphActorRef, graphInput, timeout);
			GraphDefinition response = (GraphDefinition) Await.result(future, timeout.duration());
			long endTime = System.currentTimeMillis();
			AuditInfo auditInfo = new AuditInfo();
			auditInfo.setStartTimestamp(startTime);
			auditInfo.setEndTimestamp(endTime);
			response.setAuditInfo(auditInfo);
			return response;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			getActorSystem().stop(graphActorRef);
		}
	}

	protected JsonNode getGraphNode(JsonNode inputJson) throws IOException, ExecutionException, InterruptedException {
				JsonNode graphJson = inputJson.get(PiedPiperConstants.GRAPH);
		if (graphJson != null) {
			return graphJson;
		} else {
			String projectName = SearchGraphUtils.getProjectName(inputJson);
			String graphName = SearchGraphUtils.getGraphName(inputJson);
			VersionType versionType = SearchGraphUtils.getVersionType(inputJson);
			String alias = SearchGraphUtils.getAlias(inputJson);
			Long version = SearchGraphUtils.getVersion(inputJson);
			SortType sortType = SearchGraphUtils.getSortType(inputJson);
			
			if(versionType == null) {
				versionType = VersionType.Alias;
				alias = "PROD";
				version = null;
			}
			ArrayNode resultArrayNode = this.graphStorage.search(projectName, graphName, versionType, alias, version, sortType);
			return resultArrayNode.isEmpty() ? null : resultArrayNode.get(0);
		}
	}

	private ActorRefFactory getActorSystem() {
		return injector.getInstance(Key.get(ActorSystem.class, Names.named(PiedPiperConstants.GRAPH_ACTOR)));
	}

}
