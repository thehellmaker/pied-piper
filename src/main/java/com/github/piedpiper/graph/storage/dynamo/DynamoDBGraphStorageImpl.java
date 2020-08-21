package com.github.piedpiper.graph.storage.dynamo;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.storage.QueryGraphInput;
import com.github.piedpiper.graph.storage.QueryGraphInput.SortType;
import com.github.piedpiper.graph.storage.IGraphStorage;
import com.github.piedpiper.graph.storage.PutGraphVersionInput;
import com.github.piedpiper.graph.storage.VersionType;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

public class DynamoDBGraphStorageImpl implements IGraphStorage {

	public static final String GRAPH_EXACT_QUERY = "projectName_graphName = :hashKey and versionType_versionDetails = :rangeKey";

	public static final String GRAPH_VERSION_TABLE_HASH_KEY_NAME = "projectName_graphName";

	public static final String GRAPH_VERSION_TABLE_RANGE_KEY_NAME = "versionType_versionDetails";

	public static final String GRAPH_VERSION = "version";

	public static final String GRAPH_ALIAS_VERSION_TYPE_RANGE_KEY_PATTERN = "Alias_%s_%s";

	public static final String GRAPH_VERSION_TYPE_RANGE_KEY_PATTERN = "%s_%s";

	public static final String GRAPH_PREFIX_RANGE_QUERY = "projectName_graphName = :hashKey "
			+ "and versionType_versionDetails between :rangeKeyBegin and :rangeKeyEnd";

	public static final String GRAPH_KEY_NAME = "graph";

	private DynamoDBPutGraphVersionFunction putGraphVersionFunction;

	private DynamoDBGetGraphFunction getGraphFunction;

	private DynamoDBQueryGraphFunction queryGraphFunction;
	
	@Inject
	public DynamoDBGraphStorageImpl(DynamoDBPutGraphVersionFunction putGraphVersionFunction,
			DynamoDBQueryGraphFunction queryGraphFunction,
			DynamoDBGetGraphFunction getGraphFunction) {
		this.putGraphVersionFunction = putGraphVersionFunction;
		this.queryGraphFunction = queryGraphFunction;
		this.getGraphFunction = getGraphFunction;
	}

	@Override
	public ArrayNode search(String projectName, String graphName, VersionType versionType, String alias, Long version, SortType sortType) {
		return queryGraphFunction.apply(new QueryGraphInput(projectName, graphName, versionType, alias, version, sortType));
		
	}

	@Override
	public JsonNode postStagingVersion(String projectName, String graphName, String graphJson,
			String versionDescription) {
		JsonNode latestVersionRecord = getLatestStagingVersion(projectName, graphName);
		
		if(latestVersionRecord != null) {
			String graphLatestVersion = latestVersionRecord.get(PiedPiperConstants.GRAPH).asText();
			
			if(graphJson.hashCode() == graphLatestVersion.hashCode()) {
				throw new RuntimeException("No change in graph. New Staging Version not created");
			}
		}
		

		long latestVersion = getLatestStagingVersionNumber(projectName, graphName);
		
		Map<String, Object> attrMap = ImmutableMap.<String, Object>builder()
				.put(PiedPiperConstants.VERSION_DESCRIPTION, versionDescription).build();

		return putGraphVersionFunction.apply(new PutGraphVersionInput(projectName, graphName, graphJson,
				VersionType.StagingVersion, latestVersion + 1, attrMap));
	}

	@Override
	public JsonNode postNewVersion(String projectName, String graphName, Long stagingVersion,
			String versionDescription) {
		JsonNode stagingGraph = this.getStagingVersion(projectName, graphName, stagingVersion);
		
		if (stagingGraph == null) {
			throw new RuntimeException(
					String.format("There are no graphs for projectName:%s graphName:%s stagingVersion: %s",
							projectName, graphName, stagingVersion));
		}
		
		
		Long stagingVersionNumber = this.getVersionNumber(stagingGraph);
		
		String graphJson = stagingGraph.get(PiedPiperConstants.GRAPH).asText();
		
		JsonNode latestPublishedGraph = this.getLatestPublishedVersion(projectName, graphName);
		
		if(latestPublishedGraph != null) {
			String latestGraphJson = latestPublishedGraph.get(PiedPiperConstants.GRAPH).asText();
			if(latestGraphJson.hashCode() == graphJson.hashCode()) {
				throw new RuntimeException("Staging version and published version are the same. Not publishign agian");
			}
		}
		
		
		Long latestPublishedVersionNumber = this.getLatestPublishedVersionNumber(projectName, graphName);
		
		Map<String, Object> attrMap = ImmutableMap.<String, Object>builder()
				.put(PiedPiperConstants.VERSION_DESCRIPTION, versionDescription)
				.put(PiedPiperConstants.STAGING_VERSION, stagingVersionNumber).build();
		
		return putGraphVersionFunction.apply(new PutGraphVersionInput(projectName, graphName, graphJson,
				VersionType.PublishedVersion, latestPublishedVersionNumber + 1, attrMap));
	}

	@Override
	public JsonNode getLatestPublishedVersion(String projectName, String graphName) {
		return this.getPublishedVersion(projectName, graphName, null);
	}

	@Override
	public JsonNode getPublishedVersion(String projectName, String graphName, Long version) {
		return getGraphFunction
				.apply(new QueryGraphInput(projectName, graphName, VersionType.PublishedVersion, version, SortType.Descending));
	}

	@Override
	public JsonNode putAlias(String projectName, String graphName, String alias, Long publishedVersion,
			String versionDescription) {
		if (publishedVersion == null) {
			throw new RuntimeException("Published version cannot be null");
		}
		
		if (StringUtils.isBlank(alias)) {
			throw new RuntimeException("alias cannot be null");
		}

		JsonNode publishedVersionRecord = this.getPublishedVersion(projectName, graphName, publishedVersion);

		if (publishedVersionRecord == null) {
			throw new RuntimeException(
					String.format("There are no graphs for projectName:%s graphName:%s publishedVersion: %s",
							projectName, graphName, publishedVersion));
		}

		String graphJson = publishedVersionRecord.get(PiedPiperConstants.GRAPH).asText();

		Long latestAliasVersionNumber = this.getLatestAliasVersionNumber(projectName, graphName, alias);

		Map<String, Object> attrMap = ImmutableMap.<String, Object>builder()
				.put(PiedPiperConstants.VERSION_DESCRIPTION, versionDescription)
				.put(PiedPiperConstants.PUBLISHED_VERSION, publishedVersion).build();

		return putGraphVersionFunction.apply(new PutGraphVersionInput(projectName, graphName, graphJson, alias,
				latestAliasVersionNumber + 1, attrMap));
	}

	@Override
	public JsonNode getAliasLatestVersion(String projectName, String graphName, String alias) {
		return this.getAliasVersion(projectName, graphName, alias, null);
	}

	@Override
	public JsonNode getAliasVersion(String projectName, String graphName, String alias, Long version) {
		if(StringUtils.isBlank(alias)) {
			throw new RuntimeException("alias cannot be empty");
		}
		return getGraphFunction.apply(new QueryGraphInput(projectName, graphName, alias, version, SortType.Descending));
	}

	@Override
	public JsonNode getStagingVersion(String projectName, String graphName, Long version) {
		return getGraphFunction
				.apply(new QueryGraphInput(projectName, graphName, VersionType.StagingVersion, version, SortType.Descending));
	}

	@Override
	public JsonNode getLatestStagingVersion(String projectName, String graphName) {
		return this.getStagingVersion(projectName, graphName, null);
	}

	private Long getLatestStagingVersionNumber(String projectName, String graphName) {
		JsonNode latestVersion = this.getLatestStagingVersion(projectName, graphName);
		if (latestVersion == null)
			return 0l;
		else {
			return getVersionNumber(latestVersion);
		}
	}

	private Long getLatestPublishedVersionNumber(String projectName, String graphName) {
		JsonNode latestVersion = this.getLatestPublishedVersion(projectName, graphName);
		if (latestVersion == null)
			return 0l;
		else {
			return getVersionNumber(latestVersion);
		}
	}

	private Long getLatestAliasVersionNumber(String projectName, String graphName, String alias) {
		JsonNode latestVersion = this.getAliasLatestVersion(projectName, graphName, alias);
		if (latestVersion == null)
			return 0l;
		else {
			return getAliasVersionNumber(latestVersion);
		}
	}

	private Long getVersionNumber(JsonNode graphRecord) {
		String rangeKeyName = graphRecord.get(DynamoDBGraphStorageImpl.GRAPH_VERSION_TABLE_RANGE_KEY_NAME).asText();
		return Long.valueOf(rangeKeyName.split("_")[1]);
	}
	
	private Long getAliasVersionNumber(JsonNode graphRecord) {
		String rangeKeyName = graphRecord.get(DynamoDBGraphStorageImpl.GRAPH_VERSION_TABLE_RANGE_KEY_NAME).asText();
		return Long.valueOf(rangeKeyName.split("_")[2]);
	}

}
