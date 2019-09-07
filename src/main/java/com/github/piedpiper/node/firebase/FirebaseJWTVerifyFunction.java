package com.github.piedpiper.node.firebase;

import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterData;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.inject.Inject;

public class FirebaseJWTVerifyFunction implements Function<NodeInput, NodeOutput> {

	private static final ParameterMetadata ID_TOKEN = new ParameterMetadata("idToken", ParameterMetadata.OPTIONAL);
	
	private ObjectMapper mapper;

	@Inject
	public FirebaseJWTVerifyFunction(ObjectMapper mapper) {
		this.mapper = mapper;
		
	}
	
	@Override
	public NodeOutput apply(NodeInput input) {
		try {
			ParameterData parameterData = ParameterUtils.getParameterData(input.getInput(), ID_TOKEN);
			
			if(parameterData == null) 
				throw new IllegalArgumentException("idToken parameter is missing from input");
			
			String token = parameterData.getValueString();
			FirebaseAuth auth = FirebaseAuth.getInstance();
			ObjectNode outputJson = mapper.createObjectNode();
			outputJson.set("jsonPayload", verifyAndProcessTokenOutputToJson(auth, token));
			NodeOutput output = new NodeOutput();
			output.setOutput(outputJson);
			return output;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}

	protected JsonNode verifyAndProcessTokenOutputToJson(FirebaseAuth auth, String token) throws FirebaseAuthException {
		FirebaseToken firebaseToken = auth.verifyIdToken(token);
		return mapper.valueToTree(firebaseToken);
	}

}
