package com.github.piedpiper.node.stepfunctions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.model.SendTaskSuccessRequest;
import com.amazonaws.services.stepfunctions.model.SendTaskSuccessResult;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

@RunWith(PowerMockRunner.class)
public class StepFunctionsSendTaskSuccessHandlerTest {

	@Mock
	private AWSStepFunctions sfClient;

	@Mock
	private SendTaskSuccessRequest sfSendTaskSuccessRequest;

	private Injector injector;

	@Test
	@Before
	public void builderSetup() throws Exception {

		SendTaskSuccessResult sfSendTaskSuccessResult = new SendTaskSuccessResult();
		Mockito.when(sfClient.sendTaskSuccess(Mockito.any())).thenReturn(sfSendTaskSuccessResult);

		this.injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {

			}

			@Provides
			@Singleton
			public AWSStepFunctions getStepFunctionsClient() {
				return sfClient;
			}
		});

		Mockito.doNothing().when(sfSendTaskSuccessRequest).setTaskToken(Mockito.anyString());
		Mockito.doNothing().when(sfSendTaskSuccessRequest).setOutput(Mockito.anyString());

	}

	@Test
	public void testSuccess() throws Exception {

		StepFunctionsSendTaskSuccessHandler sfSendTaskSuccess = getSendTaskSuccessNode();
		Mockito.doReturn(sfSendTaskSuccessRequest).when(sfSendTaskSuccess).getSendTaskSuccessRequest();

		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("sfSendTaskSuccessGraph.json"))));
		NodeOutput output = sfSendTaskSuccess.apply(input);

		Assert.assertNotNull(output.getOutput());
		Mockito.verify(sfSendTaskSuccessRequest, Mockito.times(1)).setOutput("{\"sampleOutput\":\"op\"}");
		Mockito.verify(sfSendTaskSuccessRequest).setTaskToken("sample_taskToken");

	}

	@Test(expected = RuntimeException.class)
	public void testRuntimeException() throws Exception {
		StepFunctionsSendTaskSuccessHandler sfSendTaskSuccess = getSendTaskSuccessNode();
		Mockito.doReturn(new RuntimeException()).when(sfSendTaskSuccess).getSendTaskSuccessRequest();
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("sfSendTaskSuccessGraph.json"))));
		sfSendTaskSuccess.apply(input);
	}

	@Test
	public void testParameterValidation() throws FileNotFoundException, IOException {

		StepFunctionsSendTaskSuccessHandler sfSendTaskSuccess = getSendTaskSuccessNode();
		Mockito.doReturn(sfSendTaskSuccessRequest).when(sfSendTaskSuccess).getSendTaskSuccessRequest();
		NodeInput input = new NodeInput();
		// this will also check the the taskToken parameter as its first
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("emptyInput.json"))));
		try {
			sfSendTaskSuccess.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(e.getMessage(),
					"java.lang.IllegalArgumentException: parameter: taskToken required but not present");
		}

		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("missingOutput.json"))));
		try {
			sfSendTaskSuccess.apply(input);
			Mockito.verify(sfSendTaskSuccessRequest).setOutput("{}");
		} catch (Exception e) {
			Assert.fail();
		}

	}

	private StepFunctionsSendTaskSuccessHandler getSendTaskSuccessNode() {
		StepFunctionsSendTaskSuccessHandler sfSendTaskSuccess = this.injector.getInstance(StepFunctionsSendTaskSuccessHandler.class);
		StepFunctionsSendTaskSuccessHandler spySfSendTaskSuccess = Mockito.spy(sfSendTaskSuccess);
		return spySfSendTaskSuccess;
	}

	private String getFileName(String fileName) {
		return "src/test/java/com/github/piedpiper/node/stepfunctions/resources/" + fileName;
	}

}
