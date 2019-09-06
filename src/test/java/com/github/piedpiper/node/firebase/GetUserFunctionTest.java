package com.github.piedpiper.node.firebase;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FirebaseAuth.class })
public class GetUserFunctionTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testEmailQuerySuccess() throws FirebaseAuthException {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			public void configure() {

			}

			@Provides
			public ObjectMapper getObjectMapper() {
				ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mockMapper = Mockito.spy(mapper);
				ObjectNode node = mapper.createObjectNode();
				node.put("iron", "man");
				Mockito.when(mockMapper.valueToTree(Mockito.any(UserRecord.class))).thenReturn(node);
				
				return mockMapper;
			}
		});

		FirebaseAuth auth = Mockito.mock(FirebaseAuth.class);
		UserRecord record = Mockito.mock(UserRecord.class);

		Mockito.when(auth.getUserByEmail(Mockito.anyString())).thenReturn(record);
		PowerMockito.mockStatic(FirebaseAuth.class);
		PowerMockito.when(FirebaseAuth.getInstance()).thenReturn(auth);
		GetUserFunction function = (GetUserFunction) injector.getInstance(GetUserFunction.class);

		ObjectNode inputNode = mapper.createObjectNode();
		ObjectNode idTokenValueNode = mapper.createObjectNode();
		idTokenValueNode.put("value", "email");
		inputNode.set("queryType", idTokenValueNode);
		ObjectNode emailQueryValueNode = mapper.createObjectNode();
		emailQueryValueNode.put("value", "a@b.com");
		inputNode.set("id", emailQueryValueNode);
		NodeInput input = new NodeInput();
		input.setInput(inputNode);
		NodeOutput output = function.apply(input);
		Assert.assertEquals("man", output.getOutput().get("jsonPayload").get("user").get("iron").asText());
	}

	@Test
	public void testEmailQueryEmailParameterMissing() throws FirebaseAuthException {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			public void configure() {

			}

			@Provides
			public ObjectMapper getObjectMapper() {
				ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mockMapper = Mockito.spy(mapper);
				ObjectNode node = mapper.createObjectNode();
				node.put("iron", "man");
				Mockito.when(mockMapper.valueToTree(Mockito.anyString())).thenReturn(node);
				return mockMapper;
			}
		});

		FirebaseAuth auth = Mockito.mock(FirebaseAuth.class);
		UserRecord record = Mockito.mock(UserRecord.class);

		Mockito.when(auth.getUserByEmail(Mockito.anyString())).thenReturn(record);
		PowerMockito.mockStatic(FirebaseAuth.class);
		PowerMockito.when(FirebaseAuth.getInstance()).thenReturn(auth);
		GetUserFunction function = (GetUserFunction) injector.getInstance(GetUserFunction.class);

		ObjectNode inputNode = mapper.createObjectNode();
		ObjectNode idTokenValueNode = mapper.createObjectNode();
		idTokenValueNode.put("value", "email");
		inputNode.set("queryType", idTokenValueNode);
		NodeInput input = new NodeInput();
		input.setInput(inputNode);
		try {
			function.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(ExceptionUtils.getStackTrace(e).contains(IllegalArgumentException.class.getName()));
		}
	}

	@Test
	public void testUidQuerySuccess() throws FirebaseAuthException {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			public void configure() {

			}

			@Provides
			public ObjectMapper getObjectMapper() {
				ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mockMapper = Mockito.spy(mapper);
				ObjectNode node = mapper.createObjectNode();
				node.put("iron", "man");
				Mockito.when(mockMapper.valueToTree(Mockito.any(UserRecord.class))).thenReturn(node);
				return mockMapper;
			}
		});

		FirebaseAuth auth = Mockito.mock(FirebaseAuth.class);
		UserRecord record = Mockito.mock(UserRecord.class);

		Mockito.when(auth.getUser(Mockito.anyString())).thenReturn(record);
		PowerMockito.mockStatic(FirebaseAuth.class);
		PowerMockito.when(FirebaseAuth.getInstance()).thenReturn(auth);
		GetUserFunction function = (GetUserFunction) injector.getInstance(GetUserFunction.class);

		ObjectNode inputNode = mapper.createObjectNode();
		ObjectNode idTokenValueNode = mapper.createObjectNode();
		idTokenValueNode.put("value", "uid");
		inputNode.set("queryType", idTokenValueNode);
		ObjectNode uidQueryValueNode = mapper.createObjectNode();
		uidQueryValueNode.put("value", "1231dsaafcdsfc");
		inputNode.set("id", uidQueryValueNode);
		NodeInput input = new NodeInput();
		input.setInput(inputNode);
		NodeOutput output = function.apply(input);
		Assert.assertEquals("man", output.getOutput().get("jsonPayload").get("user").get("iron").asText());
	}

	@Test
	public void testUidQueryEmailParameterMissing() throws FirebaseAuthException {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			public void configure() {

			}

			@Provides
			public ObjectMapper getObjectMapper() {
				ObjectMapper mapper = new ObjectMapper();
				ObjectMapper mockMapper = Mockito.spy(mapper);
				ObjectNode node = mapper.createObjectNode();
				node.put("iron", "man");
				Mockito.when(mockMapper.valueToTree(Mockito.anyString())).thenReturn(node);
				return mockMapper;
			}
		});

		FirebaseAuth auth = Mockito.mock(FirebaseAuth.class);
		UserRecord record = Mockito.mock(UserRecord.class);

		Mockito.when(auth.getUserByEmail(Mockito.anyString())).thenReturn(record);
		PowerMockito.mockStatic(FirebaseAuth.class);
		PowerMockito.when(FirebaseAuth.getInstance()).thenReturn(auth);
		GetUserFunction function = (GetUserFunction) injector.getInstance(GetUserFunction.class);

		ObjectNode inputNode = mapper.createObjectNode();
		ObjectNode idTokenValueNode = mapper.createObjectNode();
		idTokenValueNode.put("value", "uid");
		inputNode.set("queryType", idTokenValueNode);
		NodeInput input = new NodeInput();
		input.setInput(inputNode);
		try {
			function.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(ExceptionUtils.getStackTrace(e).contains(IllegalArgumentException.class.getName()));
		}
	}

	@Test
	public void testQueryTypeParameterWrong() throws FirebaseAuthException {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			public void configure() {

			}
		});

		FirebaseAuth auth = Mockito.mock(FirebaseAuth.class);
		PowerMockito.mockStatic(FirebaseAuth.class);
		PowerMockito.when(FirebaseAuth.getInstance()).thenReturn(auth);
		GetUserFunction function = (GetUserFunction) injector.getInstance(GetUserFunction.class);

		ObjectNode inputNode = mapper.createObjectNode();
		ObjectNode idTokenValueNode = mapper.createObjectNode();
		idTokenValueNode.put("value", "unknown");
		inputNode.set("queryType", idTokenValueNode);
		NodeInput input = new NodeInput();
		input.setInput(inputNode);
		try {
			function.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(ExceptionUtils.getStackTrace(e).contains(IllegalArgumentException.class.getName()));
			Assert.assertTrue(ExceptionUtils.getStackTrace(e).contains("Invalid Query Type: unknown"));
		}
	}
	
	@Test
	public void testQueryTypeParameterMissing() throws FirebaseAuthException {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			public void configure() {

			}
		});

		FirebaseAuth auth = Mockito.mock(FirebaseAuth.class);
		PowerMockito.mockStatic(FirebaseAuth.class);
		PowerMockito.when(FirebaseAuth.getInstance()).thenReturn(auth);
		GetUserFunction function = (GetUserFunction) injector.getInstance(GetUserFunction.class);

		ObjectNode inputNode = mapper.createObjectNode();
		NodeInput input = new NodeInput();
		input.setInput(inputNode);
		try {
			function.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(ExceptionUtils.getStackTrace(e).contains(IllegalArgumentException.class.getName()));
			Assert.assertTrue(ExceptionUtils.getStackTrace(e).contains("required parameter queryType is missing"));
		}
	}

}
