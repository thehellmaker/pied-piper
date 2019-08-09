package com.github.piedpiper.node;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class NodeListQueryHandlerTest {

	@Test
	public void testNodeList() throws Exception {
		List<NodeMetadata> nodeMetadataList = new NodeListQueryHandler().apply(null);
		Assert.assertEquals(9, nodeMetadataList.size());
	}
	
	
}
