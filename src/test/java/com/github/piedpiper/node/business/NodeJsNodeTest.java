package com.github.piedpiper.node.business;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.commons.log.Slf4jLoggerImpl;
import com.github.piedpiper.guice.PiedPiperModule;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.dynamo.TestConstants;
import com.google.inject.Guice;
import com.google.inject.Injector;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*" })
public class NodeJsNodeTest {
	
	@Test
	public void testReturnInputParameterAsJsonSuccess() throws FileNotFoundException, IOException {
		Injector injector = Guice.createInjector(new PiedPiperModule());
		NodeJsNode node = new NodeJsNode();
		node.setInjector(injector);
		node.setILogger(new Slf4jLoggerImpl());

		NodeInput input = new NodeInput();
		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("testInputParameterAccess.json"))));
		NodeOutput output = node.apply(input);
		Assert.assertEquals("ABCDEFG", output.getOutput().get("testValue").asText());
	}

	@Test
	public void testReturnNonJsonOutputError() throws FileNotFoundException, IOException {
		Injector injector = Guice.createInjector(new PiedPiperModule());
		NodeJsNode node = new NodeJsNode();
		node.setILogger(new Slf4jLoggerImpl());
		node.setInjector(injector);
		NodeInput input = new NodeInput();
		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("testNonJsonOutputError.json"))));
		try {
			node.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains("Output from the nodejs function is not a json container node."));
		}

	}
	

	private String getFileName(String fileName) {
		return TestConstants.TEST_PATH + "/node/business/resources/" + fileName;
	}

}
