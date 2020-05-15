package com.github.piedpiper.node.stepfunctions;

import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.BaseNode;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterMetadata;
import com.google.common.collect.Lists;


public class StepFunctionsServiceNode extends BaseNode {
	
	public static final ParameterMetadata METHOD = new ParameterMetadata("method", ParameterMetadata.MANDATORY,
			Lists.newArrayList("EXECUTE"));
	public static final ParameterMetadata REGION = new ParameterMetadata("region", ParameterMetadata.MANDATORY);
	
	public static final ParameterMetadata ACCESSKEY = new ParameterMetadata("access key",ParameterMetadata.MANDATORY);
	public static final ParameterMetadata SECRETKEY = new ParameterMetadata("secret key",ParameterMetadata.MANDATORY);
	
	@Override
	public NodeOutput apply(NodeInput nodeInput) {
		return getStepFunctionsMethodHandler(nodeInput).apply(nodeInput);
	}
	
	private Function<NodeInput,NodeOutput> getStepFunctionsMethodHandler(NodeInput nodeInput){
		try{
			String method = getStepFunctionsMethod(nodeInput);
			switch(method){
			case "EXECUTE":
				return injector.getInstance(StepFunctionsExecute.class);
			
			default:
				throw new IllegalArgumentException(String.format("Unsupported StepFunctions Method = %s", method));
			}
		}
		catch(Exception e){
			logger.log(ExceptionUtils.getStackTrace(e));
			throw e;
		}
		
	}
	private String getStepFunctionsMethod(NodeInput nodeInput){
		return Optional.ofNullable(nodeInput).map(node -> node.getInput())
		.map(inputJsonNode -> inputJsonNode.get(METHOD.getParameterName()))
		.map(methodNode -> methodNode.get(PiedPiperConstants.VALUE))
		.map(valueNode -> valueNode.asText())
		.orElse(null);
	}
}
