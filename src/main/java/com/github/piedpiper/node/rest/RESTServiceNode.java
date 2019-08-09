package com.github.piedpiper.node.rest;

import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.BaseNode;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterMetadata;
import com.google.common.collect.Lists;
import com.mashape.unirest.http.HttpMethod;

public class RESTServiceNode extends BaseNode {

	public static final ParameterMetadata URL = new ParameterMetadata("url", ParameterMetadata.MANDATORY);

	public static final ParameterMetadata METHOD = new ParameterMetadata("method", ParameterMetadata.MANDATORY,
			Lists.newArrayList(HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.PATCH.name()));

	public static final ParameterMetadata HEADERS = new ParameterMetadata("headers", ParameterMetadata.OPTIONAL);

	public static final ParameterMetadata BODY = new ParameterMetadata("body", ParameterMetadata.OPTIONAL);

	public static final ParameterMetadata OUTPUT_TYPE = new ParameterMetadata("outputType", ParameterMetadata.OPTIONAL);

	@Override
	public NodeOutput apply(NodeInput nodeInput) {
		return getMethodHandler(nodeInput).apply(nodeInput);
	}

	private Function<NodeInput, NodeOutput> getMethodHandler(NodeInput nodeInput) {
		try {
			HttpMethod method = getHttpMethod(nodeInput);
			switch (method) {
			case GET:
				return injector.getInstance(RESTGetHandler.class);
			case POST:
				return injector.getInstance(RESTPostHandler.class);
			case PUT:
				return injector.getInstance(RESTPutHandler.class);
			case PATCH:
				return injector.getInstance(RESTPatchHandler.class);
			default:
				throw new IllegalArgumentException(String.format("Unsupported Http Method = %s", method));
			}
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			throw e;
		}

	}

	private HttpMethod getHttpMethod(NodeInput nodeInput) {
		return Optional.ofNullable(nodeInput).map(node -> node.getInput())
				.map(inputJsonNode -> inputJsonNode.get(METHOD.getParameterName()))
				.map(methodNode -> methodNode.get(PiedPiperConstants.VALUE)).map(valueNode -> valueNode.asText())
				.map(methodStr -> HttpMethod.valueOf(methodStr)).orElse(null);
	}

}
