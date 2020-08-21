package com.github.piedpiper.graph.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.piedpiper.graph.storage.QueryGraphInput.SortType;

public interface IGraphStorage {

	/**
	 * This method saves the graphJson as the latest version in the database. On
	 * every save the latest is actually appended and not really overwrite the
	 * previous latestVersion
	 * 
	 * @param graphJson
	 */

	JsonNode getStagingVersion(String projectName, String graphName, Long version);

	JsonNode getLatestStagingVersion(String projectName, String graphName);

	/**
	 * This method takes the latest staged version of the graph and pubishes a new
	 * version with the version description
	 * 
	 * @param versionDescription
	 */
	JsonNode postNewVersion(String projectName, String graphName, Long stagingVersion, String versionDescription);

	/**
	 * This function points the alias to the version requested in the version
	 * parameter and adds a description for this change. Older aliases are not
	 * deleted they are immutable and the newer alias as just new addition so that
	 * history of alias changes can always very queried upon.
	 * 
	 * @param alias
	 * @param version
	 * @param description
	 */
	JsonNode putAlias(String projectName, String graphName, String alias, Long version, String description);

	JsonNode getAliasLatestVersion(String projectName, String graphName, String alias);

	JsonNode getAliasVersion(String projectName, String graphName, String alias, Long version);

	JsonNode getPublishedVersion(String projectName, String graphName, Long version);

	JsonNode getLatestPublishedVersion(String projectName, String graphName);

	JsonNode postStagingVersion(String projectName, String graphName, String graphJson, String versionDescription);

	ArrayNode search(String projectName, String graphName, VersionType versionType, String alias, Long version,
			SortType sortType);

}
