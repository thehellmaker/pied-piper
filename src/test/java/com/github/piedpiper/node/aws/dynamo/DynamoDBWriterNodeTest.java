package com.github.piedpiper.node.aws.dynamo;

import java.io.FileInputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.commons.log.Slf4jLoggerImpl;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.google.common.collect.ImmutableMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AmazonDynamoDBClientBuilder.class)
public class DynamoDBWriterNodeTest {

	@Mock
	private AmazonDynamoDBClientBuilder builderMock;

	@Mock
	private AmazonDynamoDB dynamoDBMock;

	@Mock
	private Table tableMock;

	@Mock
	private ItemCollection<QueryOutcome> queryItemCollectionMock;

	@Mock
	private ItemCollection<ScanOutcome> scanItemCollectionMock;

	@Before
	public void setup() {
		PowerMockito.mockStatic(AmazonDynamoDBClientBuilder.class);
		Mockito.when(AmazonDynamoDBClientBuilder.standard()).thenReturn(builderMock);
		Mockito.when(builderMock.withCredentials(Mockito.any())).thenReturn(builderMock);
		Mockito.when(builderMock.withEndpointConfiguration(Mockito.any())).thenReturn(builderMock);
		Mockito.when(builderMock.withRegion(Mockito.anyString())).thenReturn(builderMock);
		Mockito.when(builderMock.build()).thenReturn(dynamoDBMock);
	}

	@Test
	public void testParameterValidation() throws Exception {
		NodeInput input = new NodeInput();
		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("emptyjson.json"))));
		try {
			getDynamoDBWriterNode().apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, "accessKey")));
		}

		try {
			getDynamoDBWriterNode().apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, "accessKey")));
		}

		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("nosecretkey.json"))));
		try {
			getDynamoDBWriterNode().apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, "secretKey")));
		}

		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("noregion.json"))));
		try {
			getDynamoDBWriterNode().apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, "region")));
		}

		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("emptytable.json"))));
		try {
			DynamoDBWriterNode node = getDynamoDBWriterNode();
			node.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, "tableName")));
		}
	}

	private DynamoDBWriterNode getDynamoDBWriterNode() {
		DynamoDBWriterNode writer = new DynamoDBWriterNode();
		writer.setILogger(new Slf4jLoggerImpl());
		return writer;
	}
	
	@Test
	public void testWriteSuccess() throws Exception {
		NodeInput input = new NodeInput();
		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("writerSuccess.json"))));
		DynamoDBWriterNode node = new DynamoDBWriterNode();
		node.setILogger(new Slf4jLoggerImpl());
		Mockito.doReturn(new PutItemResult().withAttributes(
				ImmutableMap.<String, AttributeValue>builder().put("akash", new AttributeValue("ashok")).build()))
				.when(dynamoDBMock).putItem(Mockito.any(), Mockito.any());
		NodeOutput output = node.apply(input);
		Assert.assertNotNull(output.getOutput());
		Assert.assertEquals(1, output.getOutput().get("attributes").size());
	}

	@Test(expected = RuntimeException.class)
	public void testException() throws Exception {
		NodeInput input = new NodeInput();
		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("writerSuccess.json"))));
		DynamoDBWriterNode node = new DynamoDBWriterNode();
		node.setILogger(new Slf4jLoggerImpl());
		Mockito.doThrow(new RuntimeException("CustomException"))
				.when(dynamoDBMock).putItem(Mockito.any(), Mockito.any());
		node.apply(input);
	}

	private String getFileName(String fileName) {
		return TestConstants.TEST_PATH + "/node/aws/dynamo/resources/" + fileName;
	}
}
