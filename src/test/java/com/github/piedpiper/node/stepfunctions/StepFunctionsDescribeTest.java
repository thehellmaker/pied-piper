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
import com.amazonaws.services.stepfunctions.model.DescribeStateMachineRequest;
import com.amazonaws.services.stepfunctions.model.DescribeStateMachineResult;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

@RunWith(PowerMockRunner.class)
public class StepFunctionsDescribeTest {

	@Mock
	private AWSStepFunctions sfClient;

	@Mock
	private DescribeStateMachineRequest sfDescribeRequest;

	private Injector injector;

	@Test
	@Before
	public void builderSetup() throws Exception {

		DescribeStateMachineResult sfDescribeResult = new DescribeStateMachineResult();
		sfDescribeResult.setStateMachineArn("sample_arn");
		Mockito.when(sfClient.describeStateMachine(Mockito.any())).thenReturn(sfDescribeResult);

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

		Mockito.doNothing().when(sfDescribeRequest).setStateMachineArn(Mockito.anyString());

	}

	@Test
	public void testSuccess() throws Exception {

		StepFunctionsDescribe sfDescribe = getStepFunctionsDescribeNode();
		Mockito.doReturn(sfDescribeRequest).when(sfDescribe).getDescribeStateMachineRequest();

		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("sfDescribeSuccessGraph.json"))));
		NodeOutput output = sfDescribe.apply(input);

		Assert.assertNotNull(output.getOutput());
		Mockito.verify(sfDescribeRequest).setStateMachineArn("sample_arn");
		Assert.assertEquals(output.getOutput().get("stateMachineArn").asText(), "sample_arn");

	}

	@Test(expected = RuntimeException.class)
	public void testRuntimeException() throws Exception {
		StepFunctionsDescribe sfDescribe = getStepFunctionsDescribeNode();
		Mockito.doReturn(new RuntimeException()).when(sfDescribe).getDescribeStateMachineRequest();
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("sfDescribeSuccessGraph.json"))));
		sfDescribe.apply(input);
	}

	@Test
	public void testParameterValidation() throws FileNotFoundException, IOException {

		StepFunctionsDescribe sfDescribe = getStepFunctionsDescribeNode();
		Mockito.doReturn(sfDescribeRequest).when(sfDescribe).getDescribeStateMachineRequest();
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("emptyInput.json"))));
		try {
			sfDescribe.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(e.getMessage(),
					"java.lang.IllegalArgumentException: parameter: arn required but not present");
		}

	}

	private StepFunctionsDescribe getStepFunctionsDescribeNode() {
		StepFunctionsDescribe sfDescribe = this.injector.getInstance(StepFunctionsDescribe.class);
		StepFunctionsDescribe spysfDescribe = Mockito.spy(sfDescribe);
		return spysfDescribe;
	}

	private String getFileName(String fileName) {
		return "src/test/java/com/github/piedpiper/node/stepfunctions/resources/" + fileName;
	}

}
