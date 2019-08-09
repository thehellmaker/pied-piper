package com.github.piedpiper.node.aws.ssm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterData;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.node.aws.AWSNode;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class AWSSSMNode extends AWSNode {

	public static final ParameterMetadata KEY_NAME_PARAMETER = new ParameterMetadata("keyName",
			ParameterMetadata.MANDATORY);
	
	public static final String SECURE_VALUE = "secureValue";
	
	private LoadingCache<String, String> keyStoreCache;
	private ObjectMapper mapper;

	@Inject
	public AWSSSMNode(@Named(PiedPiperConstants.AWS_SSM_CACHE) LoadingCache<String, String> keyStoreCache, ObjectMapper mapper) {
		this.keyStoreCache = keyStoreCache;
		this.mapper = mapper;
	}

	@Override
	public NodeOutput apply(NodeInput input) {
		try {
			ParameterData keyParameter = ParameterUtils.getParameterData(input.getInput(), KEY_NAME_PARAMETER);
			String keyStoreValue = this.keyStoreCache.get(keyParameter.getValueString());
			ObjectNode ouputJson = mapper.createObjectNode();
			ouputJson.put(SECURE_VALUE, keyStoreValue);
			NodeOutput output = new NodeOutput();
			output.setOutput(ouputJson);
			return output;
		} catch (Exception e) {
			if(e instanceof RuntimeException) throw ((RuntimeException)e);
			else throw new RuntimeException(e);
		}
	}

}
