package com.github.piedpiper.node.aws.dynamo;

import java.io.FileInputStream;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.commons.log.Slf4jLoggerImpl;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AmazonDynamoDBClientBuilder.class)
public class DynamoDBReaderNodeTest {

	@Mock
	private AmazonDynamoDBClientBuilder builderMock;

	@Mock
	private DynamoDB dynamoDBMock;

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
		Mockito.when(builderMock.build()).thenReturn(null);
	}

	@Test
	public void testParameterValidation() throws Exception {
		NodeInput input = new NodeInput();
		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("emptyjson.json"))));
		try {
			getDynamoDBReaderNode().apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, "accessKey")));
		}

		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("nosecretkey.json"))));
		try {
			getDynamoDBReaderNode().apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, "secretKey")));
		}

		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("noregion.json"))));
		try {
			getDynamoDBReaderNode().apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, "region")));
		}

		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("emptytable.json"))));
		try {
			DynamoDBReaderNode node = Mockito.spy(DynamoDBReaderNode.class);
			node.setILogger(new Slf4jLoggerImpl());
			Mockito.doReturn(dynamoDBMock).when(node).getDynamoDB(Mockito.any());
			PowerMockito.whenNew(DynamoDB.class).withAnyArguments().thenReturn(dynamoDBMock);
			node.apply(input);
			Assert.fail();
		} catch (Exception e) {
			Assert.assertTrue(e.getMessage().contains(String.format(PiedPiperConstants.REQUIRED_PARAM_ERROR_FORMAT, "tableName")));
		}
	}

	private DynamoDBReaderNode getDynamoDBReaderNode() {
		DynamoDBReaderNode reader = new DynamoDBReaderNode();
		reader.setILogger(new Slf4jLoggerImpl());
		return reader;
	}

	@Test
	public void testQueryWithFilterExpression() throws Exception {
		NodeInput input = new NodeInput();
		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("tablequerywithfilter.json"))));
		DynamoDBReaderNode node = Mockito.spy(DynamoDBReaderNode.class);
		Mockito.doReturn(dynamoDBMock).when(node).getDynamoDB(Mockito.any());
		Mockito.when(dynamoDBMock.getTable(Mockito.anyString())).thenReturn(tableMock);

		Mockito.when(tableMock.query(Mockito.any(QuerySpec.class))).thenReturn(queryItemCollectionMock);
		@SuppressWarnings("unchecked")
		Class<Iterator<Item>> itemSupportClasss = (Class<Iterator<Item>>) Class
				.forName("com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport");
		Iterator<Item> mockIterator = Mockito.mock(itemSupportClasss);
		Mockito.when(((Iterable<Item>) queryItemCollectionMock).iterator()).thenReturn(mockIterator);

		Item mockItem = new Item().with("attributeName", "Hello World");
		Mockito.doReturn(true).doReturn(false).when(mockIterator).hasNext();
		Mockito.when(mockIterator.next()).thenReturn(mockItem);
		NodeOutput output = node.apply(input);
		Assert.assertEquals("Hello World", output.getOutput().get(0).get("attributeName").asText());
	}
	
	@Test
	public void testQueryWithoutFilterExpression() throws Exception {
		NodeInput input = new NodeInput();
		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("tablequerywithoutfilter.json"))));
		DynamoDBReaderNode node = Mockito.spy(DynamoDBReaderNode.class);
		Mockito.doReturn(dynamoDBMock).when(node).getDynamoDB(Mockito.any());
		Mockito.when(dynamoDBMock.getTable(Mockito.anyString())).thenReturn(tableMock);

		Mockito.when(tableMock.query(Mockito.any(QuerySpec.class))).thenReturn(queryItemCollectionMock);
		@SuppressWarnings("unchecked")
		Class<Iterator<Item>> itemSupportClasss = (Class<Iterator<Item>>) Class
				.forName("com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport");
		Iterator<Item> mockIterator = Mockito.mock(itemSupportClasss);
		Mockito.when(((Iterable<Item>) queryItemCollectionMock).iterator()).thenReturn(mockIterator);

		Item mockItem = new Item().with("attributeName", "Hello World");
		Mockito.doReturn(true).doReturn(false).when(mockIterator).hasNext();
		Mockito.when(mockIterator.next()).thenReturn(mockItem);
		NodeOutput output = node.apply(input);
		Assert.assertEquals("Hello World", output.getOutput().get(0).get("attributeName").asText());
	}

	@Test
	public void testScanWithFilterExpression() throws Exception {
		NodeInput input = new NodeInput();
		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("tablescanwithfilter.json"))));
		DynamoDBReaderNode node = Mockito.spy(DynamoDBReaderNode.class);
		Mockito.doReturn(dynamoDBMock).when(node).getDynamoDB(Mockito.any());
		Mockito.when(dynamoDBMock.getTable(Mockito.anyString())).thenReturn(tableMock);
		Mockito.doReturn(queryItemCollectionMock).when(tableMock).scan(Mockito.any(ScanSpec.class));
		@SuppressWarnings("unchecked")
		Class<Iterator<Item>> itemSupportClasss = (Class<Iterator<Item>>) Class
				.forName("com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport");
		Iterator<Item> mockIterator = Mockito.mock(itemSupportClasss);
		Mockito.when(((Iterable<Item>) queryItemCollectionMock).iterator()).thenReturn(mockIterator);

		Item mockItem = new Item().with("attributeName", "Hello World");
		Mockito.doReturn(true).doReturn(false).when(mockIterator).hasNext();
		Mockito.when(mockIterator.next()).thenReturn(mockItem);
		NodeOutput output = node.apply(input);
		Assert.assertEquals("Hello World", output.getOutput().get(0).get("attributeName").asText());
	}
	
	@Test
	public void testScanWithoutFilterExpression() throws Exception {
		NodeInput input = new NodeInput();
		input.setInput(new ObjectMapper().readTree(new FileInputStream(getFileName("tablescanwithoutfilter.json"))));
		DynamoDBReaderNode node = Mockito.spy(DynamoDBReaderNode.class);
		Mockito.doReturn(dynamoDBMock).when(node).getDynamoDB(Mockito.any());
		Mockito.when(dynamoDBMock.getTable(Mockito.anyString())).thenReturn(tableMock);

		Mockito.doReturn(queryItemCollectionMock).when(tableMock).scan(Mockito.any(ScanSpec.class));
		@SuppressWarnings("unchecked")
		Class<Iterator<Item>> itemSupportClasss = (Class<Iterator<Item>>) Class
				.forName("com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport");
		Iterator<Item> mockIterator = Mockito.mock(itemSupportClasss);
		Mockito.when(((Iterable<Item>) queryItemCollectionMock).iterator()).thenReturn(mockIterator);

		Item mockItem = new Item().with("attributeName", "Hello World");
		Mockito.doReturn(true).doReturn(false).when(mockIterator).hasNext();
		Mockito.when(mockIterator.next()).thenReturn(mockItem);
		NodeOutput output = node.apply(input);
		Assert.assertEquals("Hello World", output.getOutput().get(0).get("attributeName").asText());
	}
	
	private String getFileName(String fileName) {
		return TestConstants.TEST_PATH + "/node/aws/dynamo/resources/" + fileName;
	}
}
