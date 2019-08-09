package com.github.piedpiper.node.firebase;

import java.io.IOException;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.google.inject.Injector;



@RunWith(PowerMockRunner.class)
@PrepareForTest({FirebaseMethodHandlerFactory.class})
public class FirebaseAdminNodeTest {

	private static ObjectMapper mapper = new ObjectMapper();
	
	protected static final String EMPTY_JSON_STRING = "{}";

	@Test
	public void testSuccess() throws IOException {
		PowerMockito.mockStatic(FirebaseMethodHandlerFactory.class);
		PowerMockito.when(FirebaseMethodHandlerFactory.getHandler(Mockito.any(Injector.class), Mockito.anyString())).thenReturn(new Function<NodeInput, NodeOutput>() {
			@Override
			public NodeOutput apply(NodeInput t) {
				NodeOutput output = new NodeOutput();
				output.setOutput(mapper.createObjectNode());
				return output;
			}
		});
		JsonNode jsonInput = mapper.readTree("{\"method\": { \"value\": \"JWT_VERIFY\"}}");
		NodeInput nodeInput = new NodeInput();
		nodeInput.setInput(jsonInput);
		FirebaseAdminNode adminNode = new FirebaseAdminNode();
		NodeOutput nodeOutput = adminNode.apply(nodeInput);
		Assert.assertTrue(mapper.writeValueAsString(nodeOutput.getOutput()).equals(EMPTY_JSON_STRING));
	}
	
	@Test(expected=Exception.class)
	public void testException() {
		NodeInput nodeInput = new NodeInput();
		FirebaseAdminNode adminNode = new FirebaseAdminNode();
		adminNode.apply(nodeInput);
	}
	
}
