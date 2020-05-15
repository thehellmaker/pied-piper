package com.github.piedpiper.node.stepfunctions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.Injector;
import com.github.commons.log.Slf4jLoggerImpl;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.aws.dynamo.TestConstants;

public class StepFunctionsServiceNodeTest {
		@Test
		public void testExecuteMethod() throws FileNotFoundException, IOException{
			StepFunctionsServiceNode sfNode = new StepFunctionsServiceNode();
			sfNode.setILogger(new Slf4jLoggerImpl());
			Injector injector = Guice.createInjector(Lists.newArrayList(new AbstractModule(){
				@Override
				protected void configure(){
					
				}
				
				@Provides
				@Singleton
				public StepFunctionsExecute providesHandler() throws IOException{
					StepFunctionsExecute handler = Mockito.mock(StepFunctionsExecute.class);
					NodeOutput output = new NodeOutput();
					output.setOutput(JsonUtils.mapper.readTree("{\"method\":\"EXECUTE\"}"));
					Mockito.when(handler.apply(Mockito.any(NodeInput.class))).thenReturn(output);
					return handler;
				}
			}));
			sfNode.setInjector(injector);
			NodeInput input = new NodeInput();
			input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("EXECUTESuccessGraph.json"))));
			NodeOutput output = sfNode.apply(input);
			Assert.assertEquals(output.getOutput().get("method").asText(),"EXECUTE");
			
		}
		
		@Test
		public void testIllegalArgumentException() throws FileNotFoundException, IOException{
			StepFunctionsServiceNode sfNode = new StepFunctionsServiceNode();
			sfNode.setILogger(new Slf4jLoggerImpl());
			Injector injector = Guice.createInjector(Lists.newArrayList(new AbstractModule(){
				@Override
				protected void configure(){
					
				}
			}));
			sfNode.setInjector(injector);
			NodeInput input = new NodeInput();
			input.setInput(JsonUtils.mapper.readTree(new FileInputStream(getFileName("UnsupportedSFMethod.json"))));
			try{
				sfNode.apply(input);
				Assert.fail("sfNode didn't throw the IllegalArgumentException in testIllegalArgumentException");
			}catch(IllegalArgumentException ex){
				Assert.assertEquals("Unsupported StepFunctions Method = UNKNOWN", ex.getMessage());
			}
			
			
		}
		
		private String getFileName(String fileName) {
			return  "src/test/java/com/github/piedpiper/node/stepfunctions/resources/" + fileName;
		}
}
