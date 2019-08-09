package com.github.piedpiper.node.business;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.BaseNode;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.common.collect.Lists;
import com.google.inject.Key;
import com.google.inject.name.Names;

import io.apigee.trireme.core.NodeEnvironment;
import io.apigee.trireme.core.NodeException;
import io.apigee.trireme.core.NodeScript;
import io.apigee.trireme.core.Sandbox;

public class NodeJsNode extends BaseNode {

	private static final Object END_MARKER = "== End ResultAtom8NodeJsExecution ==";

	private static final Object BEGIN_MARKER = "== Begin ResultAtom8NodeJsExecution ==";

	public static final ParameterMetadata NODE_JS_BUSINESS_LOGIC = new ParameterMetadata("nodeJsBusinessLogic",
			ParameterMetadata.MANDATORY);

	public NodeOutput apply(NodeInput nodeInput) {
		try {
			String nodeScript = getNodeScript(nodeInput);
			JsonNode output = executeNodeScript(nodeScript, nodeInput);
			if (!output.isContainerNode())
				throw new IllegalArgumentException(
						String.format("Output from the nodejs function is not a json container node. Output: %s",
								JsonUtils.mapper.writeValueAsString(output)));
			NodeOutput nodeOutput = new NodeOutput();
			nodeOutput.setOutput(output);
			return nodeOutput;
		} catch (Exception e) {
			logger.log(ExceptionUtils.getStackTrace(e));
			throw new RuntimeException(e);
		}
	}

	private JsonNode extractJsonOutputFromNodsJsScriptOutput(List<String> lines) throws IOException {
		StringBuilder builder = new StringBuilder();
		boolean startedOutputMarker = false;
		for (String line : lines) {
			if (line.equals(BEGIN_MARKER)) {
				startedOutputMarker = true;
			} else if (line.equals(END_MARKER)) {
				return JsonUtils.mapper.readTree(builder.toString());
			} else if (startedOutputMarker) {
				builder.append(line);
			}
		}
		throw new IllegalStateException(String.format(
				"Output from Node JS Application did not have an end marker. Plesae check the file pied-piper-script.js to ensure it has the end marker %s",
				END_MARKER));
	}

	protected String getNodeScript(NodeInput nodeInput) throws Exception {
		String scriptTemplate = injector
				.getInstance(Key.get(String.class, Names.named(PiedPiperConstants.NODEJS_SCRIPT_TEMPLATE)));
		return String.format(scriptTemplate, getScript(nodeInput));
	}

	protected String getScript(NodeInput nodeInput) throws Exception {
		return ParameterUtils.getParameterData(nodeInput.getInput(), NODE_JS_BUSINESS_LOGIC).getValueString();
	}

	protected JsonNode executeNodeScript(String nodeScript, NodeInput nodeInput)
			throws NodeException, InterruptedException, ExecutionException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		NodeEnvironment nodeEnv = getNodeEnvironment(baos);

		NodeScript script = nodeEnv.createScript("pied-piper-script.js", nodeScript,
				new String[] { JsonUtils.mapper.writeValueAsString(nodeInput.getInput()) });
		try {
			script.execute().get();
			String output = new String(baos.toByteArray());
			List<String> lines = Lists.newArrayList(output.split("\n"));
			return extractJsonOutputFromNodsJsScriptOutput(lines);
		} finally {
			script.close();
			nodeEnv.close();
		}
	}

	protected NodeEnvironment getNodeEnvironment(ByteArrayOutputStream baos) {
		NodeEnvironment env = new NodeEnvironment();
		env.setSandbox(getSandbox(baos));
		return env;
	}

	protected Sandbox getSandbox(ByteArrayOutputStream baos) {
		Sandbox sb = new Sandbox();
		sb.setStdout(baos);
		return sb;
	}

}
