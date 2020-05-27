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
import com.amazonaws.services.stepfunctions.model.DescribeExecutionRequest;
import com.amazonaws.services.stepfunctions.model.DescribeExecutionResult;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

@RunWith(PowerMockRunner.class)
public class StepFunctionsDescribeExecutionHandlerTest {

	@Mock
	private AWSStepFunctions sfClient;

	@Mock
	private DescribeExecutionRequest sfDescribeExecutionRequest;

	private Injector injector;

	@Test
	@Before
	public void builderSetup() throws Exception {

		DescribeExecutionResult sfDescribeExecutionResult = new DescribeExecutionResult();
		sfDescribeExecutionResult.setExecutionArn("sample_arn");
		Mockito.when(sfClient.describeExecution(Mockito.any())).thenReturn(sfDescribeExecutionResult);

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

		Mockito.doNothing().when(sfDescribeExecutionRequest).setExecutionArn(Mockito.anyString());

	}

	@Test
	public void testSuccess() throws Exception {

		StepFunctionsDescribeExecutionHandler sfDescribeExecution = getStepFunctionsDescribeExecutionNode();
		Mockito.doReturn(sfDescribeExecutionRequest).when(sfDescribeExecution).getDescribeExecutionRequest();

		NodeInput input = new NodeInput();
		input.setInput(
				JsonUtils.mapper.readTree(new FileInputStream(getFileName("sfDescribeExecutionSuccessGraph.json"))));
		NodeOutput output = sfDescribeExecution.apply(input);

		Assert.assertNotNull(output.getOutput());
		Mockito.verify(sfDescribeExecutionRequest).setExecutionArn("sample_arn");
		Assert.assertEquals(output.getOutput().get("executionArn").asText(), "sample_arn");

	}

	@Test(expected = RuntimeException.class)
	public void testRuntimeException() throws Exception {
		StepFunctionsDescribeExecutionHandler sfDescribeExecution = getStepFunctionsDescribeExecutionNode();
		Mockito.doReturn(new RuntimeException()).when(sfDescribeExecution).getDescribeExecutionRequest();
		NodeInput input = new NodeInput();
		input.setInput(
				JsonUtils.mapper.readTree(new FileInputStream(getFileName("sfDescribeExecutionSuccessGraph.json"))));
		sfDescribeExecution.apply(input);
	}

	@Test
	public void testParameterValidation() throws FileNotFoundException, IOException {

		StepFunctionsDescribeExecutionHandler sfDescribeExecution = getStepFunctionsDescribeExecutionNode();
		Mockito.doReturn(sfDescribeExecutionRequest).when(sfDescribeExecution).getDescribeExecutionRequest();
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("emptyInput.json"))));
		try {
			sfDescribeExecution.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(e.getMessage(),
					"java.lang.IllegalArgumentException: parameter: arn required but not present");
		}

	}

	private StepFunctionsDescribeExecutionHandler getStepFunctionsDescribeExecutionNode() {
		StepFunctionsDescribeExecutionHandler sfDescribeExecution = this.injector
				.getInstance(StepFunctionsDescribeExecutionHandler.class);
		StepFunctionsDescribeExecutionHandler spysfDescribeExecution = Mockito.spy(sfDescribeExecution);
		return spysfDescribeExecution;
	}

	private String getFileName(String fileName) {
		return "src/test/java/com/github/piedpiper/node/stepfunctions/resources/" + fileName;
	}

}
