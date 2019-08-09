package com.github.piedpiper.node;

import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.github.piedpiper.node.aws.dynamo.DynamoDBReaderNode;


public class NodeMetadataQueryHandlerTest {

	@Test
	public void testQueryHandler() throws Exception {
		NodeMetadata metadata = new NodeMetadataQueryHandler().apply(DynamoDBReaderNode.class);
		Assert.assertEquals(4, metadata.getParameterMetadataList().stream()
				.filter(nodeUIFieldMetadata -> nodeUIFieldMetadata.isRequired())
				.collect(Collectors.toList()).size());
		Assert.assertEquals(6, metadata.getParameterMetadataList().size());
	}

}
