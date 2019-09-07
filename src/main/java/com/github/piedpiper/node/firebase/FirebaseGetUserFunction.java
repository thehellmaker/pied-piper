package com.github.piedpiper.node.firebase;

import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterData;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.inject.Inject;

public class FirebaseGetUserFunction implements Function<NodeInput, NodeOutput> {

	private static final ParameterMetadata QUERY_TYPE_PARAMETER = new ParameterMetadata("queryType",
			ParameterMetadata.OPTIONAL);

	private static final String EMAIL_QUERY_TYPE = "email";

	private static final String UID_QUERY_TYPE = "uid";

	private static final ParameterMetadata ID_PARAMETER = new ParameterMetadata("id", ParameterMetadata.OPTIONAL);

	private ObjectMapper mapper;

	@Inject
	public FirebaseGetUserFunction(ObjectMapper mapper) throws IOException {
		this.mapper = mapper;

	}

	@Override
	public NodeOutput apply(NodeInput input) {
		try {
			ParameterData queryType = ParameterUtils.getParameterData(input.getInput(), QUERY_TYPE_PARAMETER);
			if (queryType == null)
				throw new IllegalArgumentException("required parameter queryType is missing");

			FirebaseAuth auth = FirebaseAuth.getInstance();

			UserRecord record;
			if (queryType.getValueString().equals(EMAIL_QUERY_TYPE)) {
				ParameterData emailParameter = ParameterUtils.getParameterData(input.getInput(), ID_PARAMETER);
				if (emailParameter == null)
					throw new IllegalArgumentException(String
							.format("required parameter id is missing for the queryType: %s", EMAIL_QUERY_TYPE));
				record = auth.getUserByEmail(emailParameter.getValueString());
			} else if (queryType.getValueString().equals(UID_QUERY_TYPE)) {
				ParameterData uidParameter = ParameterUtils.getParameterData(input.getInput(), ID_PARAMETER);
				if (uidParameter == null)
					throw new IllegalArgumentException(
							String.format("required parameter id is missing for the queryType: %s", UID_QUERY_TYPE));
				record = auth.getUser(uidParameter.getValueString());
			} else {
				throw new IllegalArgumentException(String.format("Invalid Query Type: %s", queryType.getValueString()));
			}
			NodeOutput output = new NodeOutput();
			ObjectNode jsonPayloadNode = mapper.createObjectNode();
			jsonPayloadNode.set("user", mapper.valueToTree(record));
			ObjectNode outputJson = mapper.createObjectNode();
			outputJson.set("jsonPayload", jsonPayloadNode);
			output.setOutput(outputJson);
			return output;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
