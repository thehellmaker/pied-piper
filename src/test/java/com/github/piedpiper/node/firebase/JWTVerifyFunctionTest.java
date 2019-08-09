package com.github.piedpiper.node.firebase;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FirebaseAuth.class })
public class JWTVerifyFunctionTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testSuccess() throws FirebaseAuthException, JsonProcessingException {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			public void configure() {

			}
		});
		FirebaseAuth auth = Mockito.mock(FirebaseAuth.class);
		PowerMockito.mockStatic(FirebaseAuth.class);
		PowerMockito.when(FirebaseAuth.getInstance()).thenReturn(auth);
		JWTVerifyFunction function = (JWTVerifyFunction) injector.getInstance(JWTVerifyFunction.class);
		JWTVerifyFunction spyFunction = Mockito.spy(function);

		ObjectNode inputNode = mapper.createObjectNode();
		ObjectNode idTokenValueNode = mapper.createObjectNode();
		idTokenValueNode.put("value", "testToken");
		inputNode.set("idToken", idTokenValueNode);
		Mockito.doReturn(inputNode).when(spyFunction).verifyAndProcessTokenOutputToJson(Mockito.any(FirebaseAuth.class),
				Mockito.anyString());

		NodeInput input = new NodeInput();
		input.setInput(inputNode);
		NodeOutput output = spyFunction.apply(input);
		Assert.assertTrue(output.getOutput().get("jsonPayload").get("idToken").get("value").asText().equals("testToken"));
	}

	@Test
	public void testIdTokenMissing() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			public void configure() {

			}
		});
		JWTVerifyFunction function = injector.getInstance(JWTVerifyFunction.class);
		NodeInput input = new NodeInput();
		input.setInput(mapper.createObjectNode());
		try {
			function.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(ExceptionUtils.getStackTrace(e).contains("idToken parameter is missing from input"));
		}
	}

}
