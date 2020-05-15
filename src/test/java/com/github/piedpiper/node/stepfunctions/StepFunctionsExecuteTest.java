package com.github.piedpiper.node.stepfunctions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import org.mockito.Mock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.amazonaws.services.stepfunctions.model.StartExecutionRequest;
import com.amazonaws.services.stepfunctions.model.StartExecutionResult;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;


import org.junit.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AWSStepFunctionsClientBuilder.class,StartExecutionRequest.class})
public class StepFunctionsExecuteTest {
		
	@Mock
	private AWSStepFunctionsClientBuilder sfClientBuilder;
	
	@Mock
	private AWSStepFunctions sfClient;
	
	@Mock
	private StartExecutionRequest sfExecuteRequest;
	

	
	
	
	@Test
	@Before
	public void builderSetup() throws Exception{
		PowerMockito.mockStatic(AWSStepFunctionsClientBuilder.class);
		Mockito.when(AWSStepFunctionsClientBuilder.standard()).thenReturn(sfClientBuilder);
		Mockito.when(sfClientBuilder.withRegion(Mockito.anyString())).thenReturn(sfClientBuilder);
		Mockito.when(sfClientBuilder.withCredentials(Mockito.any())).thenReturn(sfClientBuilder);

		//sfExecuteResult is a  StartExecutionResult object having fields with null value 
		StartExecutionResult sfExecuteResult = new StartExecutionResult(); 
		Mockito.when(sfClient.startExecution(Mockito.any())).thenReturn(sfExecuteResult);
		Mockito.when(sfClientBuilder.build()).thenReturn(sfClient);
		
		
		//mock StartExecutionRequest
		PowerMockito.mock(StartExecutionRequest.class);
		Mockito.doNothing().when(sfExecuteRequest).setName(Mockito.anyString());
		Mockito.doNothing().when(sfExecuteRequest).setStateMachineArn(Mockito.anyString());
		Mockito.doNothing().when(sfExecuteRequest).setInput(Mockito.anyString());
		
		
	
	}
	
	@Test
	public void testSuccess() throws Exception {
		
		//sfExecuteResult is a StartExecutionResult object for testing 
		StartExecutionResult sfExecuteResult = new StartExecutionResult();
		sfExecuteResult.setExecutionArn("Sample_ARN");
		sfExecuteResult.setStartDate(new Date(2020,5,15));
		Mockito.when(sfClient.startExecution(Mockito.any())).thenReturn(sfExecuteResult);
		
		StepFunctionsExecute sfExecute = getStartFunctionExecuteNode();
		Mockito.doReturn(sfExecuteRequest).when(sfExecute).getStartExecutionRequest();
		
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("SFExecuteSuccessGraph.json"))));
		NodeOutput output = sfExecute.apply(input);
		
		Assert.assertNotNull(output.getOutput());
		Assert.assertEquals("Sample_ARN",output.getOutput().get("executionArn").asText());
		Assert.assertEquals("61550303400000",output.getOutput().get("startDate").asText());
		Mockito.verify(sfClientBuilder).withRegion("sample_region");
		Mockito.verify(sfExecuteRequest,Mockito.times(1)).setName("test1");
		Mockito.verify(sfExecuteRequest).setStateMachineArn("sample_arn");
		Mockito.verify(sfExecuteRequest).setInput("{}");

	}
	
	@Test(expected = RuntimeException.class)
	public void testRuntimeExceptio() throws Exception{
		StepFunctionsExecute sfExecute = getStartFunctionExecuteNode();		
		Mockito.doReturn(new RuntimeException()).when(sfExecute).getStartExecutionRequest();
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("SFExecuteSuccessGraph.json"))));
		sfExecute.apply(input);
	}
	
	@Test
	public void testParameterValidation() throws FileNotFoundException, IOException{
		
		StepFunctionsExecute sfExecute = getStartFunctionExecuteNode();
		Mockito.doReturn(sfExecuteRequest).when(sfExecute).getStartExecutionRequest();
		NodeInput input = new NodeInput();
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("emptyInput.json"))));
		try{
			sfExecute.apply(input);
			Assert.fail();
		}catch(Exception e){
			Assert.assertEquals(e.getMessage(),String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, "region"));
		}
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("missingAccessKey.json"))));
		try{
			sfExecute.apply(input);
			Assert.fail();
		}catch(Exception e){
			Assert.assertEquals(e.getMessage(),String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, "access key"));
		}
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("missingSecretKey.json"))));
		try{
			sfExecute.apply(input);
			Assert.fail();
		}catch(Exception e){
			Assert.assertEquals(e.getMessage(),String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, "secret key"));
		}
		//here if input is not present in json the code will add input as "{}" so it should not fail
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("missingInput.json"))));
		try{
			sfExecute.apply(input);
		}catch(Exception e){
			Assert.fail();
		}
		input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("missingName.json"))));
		try{
			sfExecute.apply(input);
			Assert.fail();
		}catch(Exception e){
			Assert.assertEquals(e.getMessage(),String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, "name"));
		}
	}
	
	
	private StepFunctionsExecute getStartFunctionExecuteNode(){
		StepFunctionsExecute sfExecute = new StepFunctionsExecute();
		StepFunctionsExecute spySfExecute = Mockito.spy(sfExecute);
		return spySfExecute;
	}
	private String getFileName(String fileName) {
		return  "src/test/java/com/github/piedpiper/node/stepfunctions/resources/" + fileName;
	}

}
