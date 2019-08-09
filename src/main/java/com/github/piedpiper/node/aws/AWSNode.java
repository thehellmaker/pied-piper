package com.github.piedpiper.node.aws;

import com.github.piedpiper.node.BaseNode;
import com.github.piedpiper.node.ParameterMetadata;

public abstract class AWSNode extends BaseNode {
	
	public static final ParameterMetadata ACCESS_KEY = new ParameterMetadata("accessKey", ParameterMetadata.MANDATORY);

	public static final ParameterMetadata SECRET_KEY = new ParameterMetadata("secretKey", ParameterMetadata.MANDATORY);

	public static final ParameterMetadata REGION = new ParameterMetadata("region", ParameterMetadata.MANDATORY);

}
