package com.github.piedpiper.node.firebase;

import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.inject.Inject;

public class FirebaseCustomTokenFunction implements Function<NodeInput, NodeOutput> {

	private static final ParameterMetadata ID_PARAMETER = new ParameterMetadata("id", ParameterMetadata.OPTIONAL);
	private ObjectMapper mapper;

	@Inject 
	public FirebaseCustomTokenFunction(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	@Override
	public NodeOutput apply(NodeInput input) {
		try {
			FirebaseAuth auth = FirebaseAuth.getInstance();
			String id = ParameterUtils.getParameterData(input.getInput(), ID_PARAMETER).getValueString();
			String customToken = auth.createCustomToken(id);
			ObjectNode outputJson = mapper.createObjectNode();
			outputJson.put("customToken", customToken);
			NodeOutput output = new NodeOutput();
			output.setOutput(outputJson);
			return output;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}

