package com.github.piedpiper.node.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.commons.log.Slf4jLoggerImpl;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.guice.PiedPiperModule;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.dynamo.TestConstants;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@RunWith(PowerMockRunner.class)
public class NodeJsNodeTest {
	
	private static final String NODEJS_TEMPLATE_SUCCESS = "./src/main/java/com/github/piedpiper/node/business/pied-piper-script.js";
	
	private static final String NODEJS_TEMPLATE_NO_END_MARKER_FILE = "./src/test/java/com/github/piedpiper/node/business/pied-piper-script-no-end-marker.js";
	
	private static final String NODEJS_TEMPLATE_BLANK_FILE = "./src/test/java/com/github/piedpiper/node/business/pied-piper-script-empty-script.js";
	

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
