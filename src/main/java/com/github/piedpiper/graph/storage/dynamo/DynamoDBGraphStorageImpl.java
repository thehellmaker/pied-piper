package com.github.piedpiper.graph.storage.dynamo;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.commons.utils.JsonUtils;
import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.graph.storage.IGraphStorage;
import com.github.piedpiper.graph.storage.PostNewVersionInput;
import com.github.piedpiper.graph.storage.PutGraphVersionInput;
import com.github.piedpiper.graph.storage.QueryGraphInput;
import com.github.piedpiper.graph.storage.QueryGraphInput.SortType;
import com.github.piedpiper.utils.DynamoDBUtils;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

public class DynamoDBGraphStorageImpl implements IGraphStorage {

	

	public static final String GRAPH_VERSION_TABLE_HASH_KEY_NAME = "projectName_graphName";

	public static final String GRAPH_VERSION_TABLE_RANGE_KEY_NAME = "branchName_version";

	public static final String GRAPH_VERSION = "version";

	public static final String GRAPH_BRANCH_VERSION_RANGE_KEY_PATTERN = "%s_%s";

	public static final String GRAPH_PREFIX_RANGE_QUERY = String.format(
			"%s = :hashKey " + "and %s between :rangeKeyBegin and :rangeKeyEnd", GRAPH_VERSION_TABLE_HASH_KEY_NAME,
			GRAPH_VERSION_TABLE_RANGE_KEY_NAME);
	
	public static final String GRAPH_EXACT_QUERY = String.format("%s = :hashKey and %s = :rangeKey", GRAPH_VERSION_TABLE_HASH_KEY_NAME,
			GRAPH_VERSION_TABLE_RANGE_KEY_NAME);

	public static final String GRAPH_KEY_NAME = "graph";

	private DynamoDBPutGraphVersionFunction putGraphVersionFunction;

	private DynamoDBGetGraphFunction getGraphFunction;

	private DynamoDBQueryGraphFunction queryGraphFunction;

	@Inject
	public DynamoDBGraphStorageImpl(DynamoDBPutGraphVersionFunction putGraphVersionFunction,
			DynamoDBQueryGraphFunction queryGraphFunction, DynamoDBGetGraphFunction getGraphFunction) {
		this.putGraphVersionFunction = putGraphVersionFunction;
		this.queryGraphFunction = queryGraphFunction;
		this.getGraphFunction = getGraphFunction;
	}

	@Override
	public ArrayNode search(QueryGraphInput queryGraphInput) throws Exception {
		ArrayNode dynamoRecords = queryGraphFunction.apply(queryGraphInput);
		for(JsonNode record: dynamoRecords) {
			ObjectNode editableRecord = (ObjectNode) record;
			String branchVersion = editableRecord.get(GRAPH_VERSION_TABLE_RANGE_KEY_NAME).asText();
			String branchName = branchVersion.split("_")[0];
			editableRecord.put(PiedPiperConstants.BRANCH_NAME, branchName);
			String versionStr = branchVersion.split("_")[1];
			editableRecord.put(PiedPiperConstants.VERSION_STR, versionStr);
			try {
				Long version = Long.valueOf(versionStr);
				editableRecord.put(PiedPiperConstants.VERSION, version);
			} catch(Exception e) {
				
			}
			String graphStr = editableRecord.get(PiedPiperConstants.GRAPH).asText();
			editableRecord.set(PiedPiperConstants.GRAPH_JSON, JsonUtils.mapper.readTree(graphStr));
			editableRecord.remove(GRAPH_VERSION_TABLE_HASH_KEY_NAME);
			editableRecord.remove(GRAPH_VERSION_TABLE_RANGE_KEY_NAME);
			String prevVersionDetailsStr = editableRecord.get(PiedPiperConstants.PREVIOUS_VERSION_DETAILS).asText();
			editableRecord.set(PiedPiperConstants.PREVIOUS_VERSION_DETAILS_JSON, JsonUtils.mapper.readTree(prevVersionDetailsStr));
		}
		return dynamoRecords;
	}

	@Override
	public JsonNode postNewVersion(PostNewVersionInput input) throws Exception {
		System.out.println(JsonUtils.mapper.writeValueAsString(input));
		if (!isValid(input)) {
			throw new RuntimeException(String.format(
					"Invalid input. One of projectName, graphName, branchName or graphJson is blank. Input: %s",
					JsonUtils.mapper.writeValueAsString(input)));
		}

		JsonNode latestVersionForBranch = this.getLatestVersionForBranch(input.getProjectName(), input.getGraphName(),
				input.getBranchName());
		Long latestVersionNumberForBranch = this.getVersionNumber(latestVersionForBranch);

		String graphJson = Optional.ofNullable(latestVersionForBranch)
				.map(latestVersionForBranchNullable -> latestVersionForBranchNullable.get(PiedPiperConstants.GRAPH))
				.map(graphNode -> graphNode.asText()).orElse(StringUtils.EMPTY);

		if (input.getGraphJson().hashCode() == graphJson.hashCode()) {
			throw new RuntimeException(String.format(
					"No change between latest version of the graph in branch: %s and the new graph being posted",
					input.getBranchName()));
		}

		if (latestVersionNumberForBranch != 0
				&& (!isPreviousBranchDetailsPresent(input) || !isPreviousBranchDetailsValid(input))) {
			throw new RuntimeException(
					"Previous Project, Graph, Branch and version details are mandatory and should exist");
		}

		if ((input.getProjectName().equals(input.getPreviousProjectName())
				&& input.getGraphName().equals(input.getGraphName())
				&& input.getBranchName().equals(input.getPreviousBranchName()))
				&& input.getPreviousVersion() != latestVersionNumberForBranch) {
			throw new RuntimeException("Cannot publish an older verison of a branch on the same branch");
		}

		Map<String, Object> attrMap = ImmutableMap.<String, Object>builder()
				.put(PiedPiperConstants.VERSION_DESCRIPTION, input.getVersionDescription())
				.put(PiedPiperConstants.PREVIOUS_VERSION_DETAILS, JsonUtils.mapper.writeValueAsString(input)).build();

		String version = DynamoDBUtils.formatWithLongMaxLength(latestVersionNumberForBranch + 1);
		return putGraphVersionFunction.apply(new PutGraphVersionInput(input.getProjectName(), input.getGraphName(),
				input.getGraphJson(), input.getBranchName(), version, attrMap));
	}

	@Override
	public JsonNode putAlias(PutNewAliasInput input) throws Exception {

		if (!isValid(input)) {
			throw new RuntimeException(String.format(
					"Invalid input. One of projectName, graphName, branchName, version and aliasType is blank. Input: %s",
					JsonUtils.mapper.writeValueAsString(input)));
		}

		JsonNode versionForBranch = this.getVersionForBranch(input.getProjectName(), input.getGraphName(),
				input.getBranchName(), input.getVersion());

		if (versionForBranch == null) {
			throw new RuntimeException(
					String.format("No graph exists with projectName, graphName, branchName and version. Input: %s",
							JsonUtils.mapper.writeValueAsString(input)));
		}

		String graphJson = Optional.ofNullable(versionForBranch.get(PiedPiperConstants.GRAPH))
				.map(graphNode -> graphNode.asText()).orElse(StringUtils.EMPTY);

		Map<String, Object> attrMap = ImmutableMap.<String, Object>builder()
				.put(PiedPiperConstants.VERSION_DESCRIPTION, input.getVersionDescription())
				.put(PiedPiperConstants.PREVIOUS_VERSION_DETAILS, JsonUtils.mapper.writeValueAsString(input)).build();

		String version = input.getAliasType().name();
		return putGraphVersionFunction.apply(new PutGraphVersionInput(input.getProjectName(), input.getGraphName(),
				graphJson, input.getBranchName(), version, attrMap));
	}

	private boolean isValid(PutNewAliasInput input) {
		return StringUtils.isNotBlank(input.getProjectName()) && StringUtils.isNotBlank(input.getGraphName())
				&& StringUtils.isNotBlank(input.getBranchName()) && input.getAliasType() != null
				&& input.getVersion() != null;
	}

	private boolean isValid(PostNewVersionInput input) {
		return StringUtils.isNotBlank(input.getProjectName()) && StringUtils.isNotBlank(input.getGraphName())
				&& StringUtils.isNotBlank(input.getBranchName()) && StringUtils.isNotBlank(input.getGraphJson());
	}

	private boolean isPreviousBranchDetailsValid(PostNewVersionInput input) {
		JsonNode previousBranchVersionRecord = this.getVersionForBranch(input.getPreviousProjectName(),
				input.getPreviousGraphName(), input.getPreviousBranchName(), input.getPreviousVersion());
		if (previousBranchVersionRecord == null) {
			return false;
		}
		return true;
	}

	private boolean isPreviousBranchDetailsPresent(PostNewVersionInput input) {
		return StringUtils.isNotBlank(input.getPreviousProjectName())
				&& StringUtils.isNotBlank(input.getPreviousGraphName())
				&& StringUtils.isNotBlank(input.getPreviousBranchName()) && (input.getPreviousVersion() != null);
	}

	public JsonNode getVersionForBranch(String projectName, String graphName, String branchName, Long version) {
		return getGraphFunction
				.apply(new QueryGraphInput(projectName, graphName, branchName, version, SortType.Descending));
	}

	public JsonNode getLatestVersionForBranch(String projectName, String graphName, String branchName) {
		return this.getVersionForBranch(projectName, graphName, branchName, null);
	}

	private Long getVersionNumber(JsonNode graphRecord) {
		if (graphRecord == null) {
			return 0l;
		} else {
			String rangeKeyName = graphRecord.get(DynamoDBGraphStorageImpl.GRAPH_VERSION_TABLE_RANGE_KEY_NAME).asText();
			return Long.valueOf(rangeKeyName.split("_")[1]);
		}
	}

}
