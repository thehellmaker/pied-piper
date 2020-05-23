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
import com.amazonaws.services.stepfunctions.model.StartExecutionRequest;
import com.amazonaws.services.stepfunctions.model.StartExecutionResult;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

@RunWith(PowerMockRunner.class)
public class StepFunctionsExecuteTest {

	@Mock
	private AWSStepFunctions sfClient;

	@Mock
	private StartExecutionRequest sfExecuteRequest;

	private Injector injector;

	@Test
	@Before
	public void builderSetup() throws Exception {

		StartExecutionResult sfExecuteResult = new StartExecutionResult();
		sfExecuteResult.setExecutionArn("sample_arn");
		Mockito.when(sfClient.startExecution(Mockito.any())).thenReturn(sfExecuteResult);

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

		Mockito.doNothing().when(sfExecuteRequest).setName(Mockito.anyString());
		Mockito.doNothing().when(sfExecuteRequest).setStateMachineArn(Mockito.anyString());
		Mockito.doNothing().when(sfExecuteRequest).setInput(Mockito.anyString());

	}

	@Test
	public void testSuccess() throws Exception {

		StepFunctionsExecute sfExecute = getStartFunctionExecuteNode();
		Mockito.doReturn(sfExecuteRequest).when(sfExecute).getStartExecutionRequest();

		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("SFExecuteSuccessGraph.json"))));
		NodeOutput output = sfExecute.apply(input);

		Assert.assertNotNull(output.getOutput());
		Mockito.verify(sfExecuteRequest, Mockito.times(1)).setName("test1");
		Mockito.verify(sfExecuteRequest).setStateMachineArn("sample_arn");
		Mockito.verify(sfExecuteRequest).setInput("{}");
		Assert.assertEquals(output.getOutput().get("executionArn").asText(), "sample_arn");

	}

	@Test(expected = RuntimeException.class)
	public void testRuntimeException() throws Exception {
		StepFunctionsExecute sfExecute = getStartFunctionExecuteNode();
		Mockito.doReturn(new RuntimeException()).when(sfExecute).getStartExecutionRequest();
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("SFExecuteSuccessGraph.json"))));
		sfExecute.apply(input);
	}

	@Test
	public void testParameterValidation() throws FileNotFoundException, IOException {

		StepFunctionsExecute sfExecute = getStartFunctionExecuteNode();
		Mockito.doReturn(sfExecuteRequest).when(sfExecute).getStartExecutionRequest();
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("emptyInput.json"))));
		try {
			sfExecute.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(e.getMessage(),
					"java.lang.IllegalArgumentException: parameter: name required but not present");
		}

		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("missingInput.json"))));
		try {
			sfExecute.apply(input);
		} catch (Exception e) {
			Assert.fail();
		}
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("missingARN.json"))));
		try {
			sfExecute.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(e.getMessage(),
					"java.lang.IllegalArgumentException: parameter: arn required but not present");
		}
	}

	private StepFunctionsExecute getStartFunctionExecuteNode() {
		StepFunctionsExecute sfExecute = this.injector.getInstance(StepFunctionsExecute.class);
		StepFunctionsExecute spySfExecute = Mockito.spy(sfExecute);
		return spySfExecute;
	}

	private String getFileName(String fileName) {
		return "src/test/java/com/github/piedpiper/node/stepfunctions/resources/" + fileName;
	}

}
