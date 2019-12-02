package com.github.piedpiper.graph.api;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.text.StrSubstitutor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.commons.log.ILogger;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.api.types.AttributeType;
import com.github.piedpiper.graph.api.types.ContractInput;
import com.github.piedpiper.graph.api.types.GraphDefinition;
import com.github.piedpiper.graph.api.types.NodeDefinition;
import com.github.piedpiper.graph.api.types.NodeExecutor;
import com.github.piedpiper.graph.api.types.NodeStatus;
import com.github.piedpiper.graph.api.types.ParameterDefinition;
import com.github.piedpiper.graph.api.types.ParameterType;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.transformer.CleanupOutputTransformer;
import com.github.piedpiper.utils.GraphDefinitionUtils;
import com.github.piedpiper.utils.ParameterUtils;
import com.github.piedpiper.utils.RegExUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import com.jayway.jsonpath.JsonPath;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ApiGraphActor extends AbstractActor {

	private ObjectMapper mapper;

	private GraphDefinition graphDefinition;

	private JsonNode inputJson;

	private ActorRef sourceSenderRef;

	private Injector injector;

	private ILogger logger;

	public static Props props(Injector injector, ILogger logger) {
		return Props.create(ApiGraphActor.class, () -> new ApiGraphActor(injector, logger));
	}

	public ApiGraphActor(Injector injector, ILogger logger) {
		this.injector = injector;
		this.logger = logger;
		this.mapper = injector.getInstance(ObjectMapper.class);
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(ContractInput.class, input -> handleExeptionAndExecute(new Function<Void, Void>() {
					@Override
					public Void apply(Void aVoid) {
						try {
							sourceSenderRef = getSender();
							graphDefinition = mapper.treeToValue(input.getGraphJson(), GraphDefinition.class);
							inputJson = input.getInputJson();
							executeResolvedNodesWithException();
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
						return null;
					}
				})).match(NodeOutput.class, nodeOutput -> handleExeptionAndExecute(new Function<Void, Void>() {
					@Override
					public Void apply(Void aVoid) {
						try {
							updateNodeResponse(nodeOutput);
							executeResolvedNodesWithException();
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
						return null;
					}

				})).build();
	}

	private void executeResolvedNodesWithException() throws Exception {
		boolean foundNodesWithException;
		do {
			foundNodesWithException = executeResolvedNodes();
		} while (foundNodesWithException == true);
	}

	private void handleExeptionAndExecute(Function<Void, Void> messageHandler) {
		try {
			messageHandler.apply(null);
		} catch (Exception e) {
			if (this.graphDefinition == null)
				this.graphDefinition = new GraphDefinition();
			this.graphDefinition.setExceptionTrace(ExceptionUtils.getStackTrace(e));
			GraphDefinition maskedOutput = new CleanupOutputTransformer().apply(this.graphDefinition);
			this.sourceSenderRef.tell(maskedOutput, getSelf());
		}
	}

	private void updateNodeResponse(NodeOutput nodeOutput) {
		Map<String, NodeDefinition> nodeMap = this.graphDefinition.getNodeMap();
		NodeDefinition nodeDef = nodeMap.get(nodeOutput.getNodeName());
		nodeDef.appendNodeOutput(nodeOutput);

		if (nodeDef.getNodeInputList().size() == nodeDef.getNodeOutputList().size()) {
			nodeDef.setNodeStatus(NodeStatus.COMPLETE);
		}
		getContext().stop(getSender());
	}

	private boolean executeResolvedNodes() throws Exception {
		if (GraphDefinitionUtils.isGraphExecutionComplete(this.graphDefinition)) {
//			logger.log(String.format("Graph execution complete for graphDefiniton = %s",
//					mapper.writeValueAsString(this.graphDefinition)));
			notifyCallerExecutionComplete();
			return false;
		}

		boolean foundNodesWithException = false;
		for (Entry<String, NodeDefinition> eachNodeEntry : this.graphDefinition.getNodeMap().entrySet()) {
			NodeDefinition nodeDef = eachNodeEntry.getValue();
			try {

				if (nodeDef.getNodeStatus() == NodeStatus.NOT_STARTED
						&& GraphDefinitionUtils.isResolved(this.graphDefinition, nodeDef)) {
//					logger.log(String.format("Resolving input for nodeDefinition = %s",
//							mapper.writeValueAsString(nodeDef.getNodeName())));
					resolveInput(nodeDef);
//					logger.log(
//							String.format("Resolved node for nodeDefintion = %s", mapper.writeValueAsString(nodeDef)));
					executeNode(nodeDef);
				}
			} catch (Exception e) {
				logger.log(ExceptionUtils.getStackTrace(e));
				nodeDef.setStackTrace(ExceptionUtils.getStackTrace(e));
				nodeDef.setNodeStatus(NodeStatus.COMPLETE);
				foundNodesWithException = true;
			}
		}

		return foundNodesWithException;
	}

	private void executeNode(NodeDefinition nodeDefinition) throws JsonProcessingException {
		nodeDefinition.setNodeStatus(NodeStatus.IN_PROGRESS);
		for (NodeInput input : nodeDefinition.getNodeInputList()) {
			ActorRef nodeExecutorActor = getContext().actorOf(ApiNodeActor.props(injector, logger));
			NodeExecutor nodeExecutor = new NodeExecutor();
			nodeExecutor.setNodeDefinition(nodeDefinition);
			nodeExecutor.setNodeInput(input);
//			logger.log(String.format("Starting Node actor for executor: %s", mapper.writeValueAsString(nodeExecutor)));
			nodeExecutorActor.tell(nodeExecutor, getSelf());
		}

	}

	private void resolveInput(NodeDefinition nodeDefinition) throws Exception {
		nodeDefinition.setNodeInputList(getNodeInputList(nodeDefinition));
	}

	@SuppressWarnings("unused")
	private List<NodeInput> getNodeInputList(NodeDefinition nodeDefinition) throws Exception {
		try {
			List<NodeInput> inputNodeList = Lists.newArrayList();
			ObjectNode inputNode = mapper.createObjectNode();
			if (MapUtils.isNotEmpty(nodeDefinition.getParameterMap())) {
				ParameterDefinition parallelizeParamDef = getParallelizeParameterDefinition(nodeDefinition);
				// Run n-1, where n is the number of parameters, passes over constant values for
				// substitution as in the
				// 1. In first pass the runtime values will get substituted but constant
				// placeholders would get resolved
				// 2. In the second pass constant placeholders would have been resolved and will
				// get substituted
				for (Entry<String, ParameterDefinition> eachParameter : nodeDefinition.getParameterMap().entrySet()) {
					Map<String, String> substitutionMap = getSubstitutionMap(inputNode);
					// Resolve constant parameter for placeholder substitution
//					try {
						for (Entry<String, ParameterDefinition> eachParameterForSubstition : nodeDefinition
								.getParameterMap().entrySet()) {
							ParameterDefinition paramDef = eachParameterForSubstition.getValue();
							Object paramValue = resolveParameter(paramDef, substitutionMap);
							inputNode.set(paramDef.getParameterName(), ParameterUtils.createParamValueNode(paramValue));
						}
//					} catch(Exception e) {
//						logger.log(String.format("Node = %s, Param = %s, Exception = %s", nodeDefinition.getNodeName(), 
//								eachParameter.getKey(),
//								ExceptionUtils.getStackTrace(e)));
//					}

				}
				Iterator<Entry<String, JsonNode>> jsonNodeIterator = inputNode.fields();
				while (jsonNodeIterator.hasNext()) {
					Entry<String, JsonNode> eachEntry = jsonNodeIterator.next();
					JsonNode node = eachEntry.getValue().get("value");
					if (node instanceof TextNode && RegExUtils.isSubstitutorString(node.asText())) {
						throw new IllegalStateException(String.format("Unable to substiture param = %s with value = %s",
								eachEntry.getKey(), node.asText()));
					} 
				}

				if (parallelizeParamDef != null) {
					ArrayNode parallelParamArrayNode = (ArrayNode) inputNode.get(parallelizeParamDef.getParameterName())
							.get("value");
					for (JsonNode node : parallelParamArrayNode) {
						ObjectNode paramValueNode = mapper.createObjectNode();
						paramValueNode.set("value", node);
						ObjectNode clonedNode = inputNode.deepCopy();
						clonedNode.set(parallelizeParamDef.getParameterName(), paramValueNode);
						NodeInput nodeInput = new NodeInput();
						nodeInput.setInput(clonedNode);
						nodeInput.setNodeSpecification(nodeDefinition.getNodeSpecification());
						inputNodeList.add(nodeInput);
					}
				}

			}
			if (CollectionUtils.isEmpty(inputNodeList)) {
				NodeInput nodeInput = new NodeInput();
				nodeInput.setInput(inputNode);
				nodeInput.setNodeSpecification(nodeDefinition.getNodeSpecification());
				inputNodeList.add(nodeInput);
			}

			return inputNodeList;
		} catch (Exception e) {
			logger.log(String.format("Node = %s, Exception = %s", nodeDefinition.getNodeName(),
					ExceptionUtils.getStackTrace(e)));
			nodeDefinition.setStackTrace(ExceptionUtils.getStackTrace(e));
			throw e;
		}
	}

	private ParameterDefinition getParallelizeParameterDefinition(NodeDefinition nodeDefinition)
			throws JsonProcessingException {
		ParameterDefinition parallelizeParamDef = null;
		for (Entry<String, ParameterDefinition> eachParameter : nodeDefinition.getParameterMap().entrySet()) {
			ParameterDefinition paramDef = eachParameter.getValue();
			if (Optional.ofNullable(paramDef.getAttributeMap())
					.map(attrMap -> attrMap.getOrDefault("PARALLELIZE", "false")).orElse("false").equals("true")) {
				if (parallelizeParamDef != null)
					throw new RuntimeException(
							String.format("Node: %s has two or more parameters having parallelize attribute on them ",
									nodeDefinition.getNodeName()));
				parallelizeParamDef = paramDef;
			}
		}
		return parallelizeParamDef;
	}

	private Map<String, String> getSubstitutionMap(JsonNode inputNode) throws JsonProcessingException {
		Map<String, String> substitutionMap = Maps.newHashMap();
		Iterator<Entry<String, JsonNode>> jsonNodeIterator = inputNode.fields();
		while (jsonNodeIterator.hasNext()) {
			Entry<String, JsonNode> eachEntry = jsonNodeIterator.next();
			JsonNode node = eachEntry.getValue().get("value");
			if (node instanceof TextNode) {
				if (RegExUtils.isNotSubstitutorString(node.asText())) {
					substitutionMap.put(eachEntry.getKey(), node.asText());
				}
			}
		}
		if (this.inputJson != null) {
			jsonNodeIterator = this.inputJson.fields();
			while (jsonNodeIterator.hasNext()) {
				Entry<String, JsonNode> eachEntry = jsonNodeIterator.next();
				String inputValue = eachEntry.getValue().asText();
				substitutionMap.put("input." + eachEntry.getKey(), inputValue);
			}
		}
		return substitutionMap;
	}

	private Object resolveParameter(ParameterDefinition paramDef, Map<String, String> substitutionMap)
			throws Exception {
		if (ParameterType.CONSTANT.equals(paramDef.getParameterType())) {
			return convertDataTypes(paramDef, resolveConstantParameter(paramDef), substitutionMap);
		} else if (ParameterType.REFERENCE_FROM_ANOTHER_NODE.equals(paramDef.getParameterType())) {
			return convertDataTypes(paramDef, resolveReferenceParameter(paramDef), substitutionMap);
		} 
		throw new IllegalArgumentException(String.format("Unknown parameterType: %s", paramDef.getParameterType()));
	}

	private Object convertDataTypes(ParameterDefinition paramDef, Object resolveConstantParameter,
			Map<String, String> substitutionMap) throws IOException {
		JsonNode node;
		if (resolveConstantParameter instanceof String) {
			String substitutedString = new StrSubstitutor(substitutionMap).replace((String) resolveConstantParameter);
			node = getValue(substitutedString);
		} else {
			node = mapper.valueToTree(resolveConstantParameter);
		}

		return Optional.ofNullable(paramDef.getAttributeMap())
				.map(attributeMap -> attributeMap.get(AttributeType.DATA_TYPE.name())).map(nonNullDataType -> {
					if (nonNullDataType.equals(PiedPiperConstants.AS_JSON)) {
						return node;
					} else {
						try {
							return MethodUtils.invokeExactMethod(node, nonNullDataType);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}).orElseGet(() -> {
					return Optional.ofNullable(node).filter(nonNullNode -> nonNullNode instanceof TextNode)
							.map(strNode -> strNode.asText())
							.orElse(Optional.ofNullable(node).filter(nonNullNode -> nonNullNode instanceof JsonNode)
									.map(jsonNodeInstance -> jsonNodeInstance.toString()).orElse(""));
				});
	}

	private Object resolveConstantParameter(ParameterDefinition paramDef) throws IOException {
		return paramDef.getParameterValue();
	}

	private JsonNode getValue(String value) throws IOException {
		try {
			return JsonUtils.mapper.readTree(value);
		} catch (Exception e) {
			return JsonUtils.mapper.valueToTree(value);
		}
	}

	private Object resolveReferenceParameter(ParameterDefinition paramDef) throws JsonProcessingException {
		String nodeName = paramDef.getReferenceNodeName();
		NodeDefinition referenceNodeDefition = this.graphDefinition.getNodeMap().get(nodeName);
		String referenceOutput = mapper.writeValueAsString(referenceNodeDefition);
		Object value = JsonPath.read(referenceOutput, (String) paramDef.getParameterValue());
		return value;
	}

	private void notifyCallerExecutionComplete() throws JsonProcessingException {
		GraphDefinition maskedOutput = new CleanupOutputTransformer().apply(this.graphDefinition);
		this.sourceSenderRef.tell(maskedOutput, getSelf());
	}

}

