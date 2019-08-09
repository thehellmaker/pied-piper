package com.github.piedpiper.node.firebase;

import java.util.function.Function;

import com.github.piedpiper.node.BaseNode;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterData;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.utils.ParameterUtils;

public class FirebaseAdminNode extends BaseNode {

	public static final ParameterMetadata METHOD = new ParameterMetadata("method", ParameterMetadata.MANDATORY);

	@Override
	public NodeOutput apply(NodeInput input) {
		try {
			ParameterData methodParameterData = ParameterUtils.getParameterData(input.getInput(), METHOD);
			String firebaseMethod = methodParameterData.getValueString();
			Function<NodeInput, NodeOutput> firebaseHandler = FirebaseMethodHandlerFactory.getHandler(injector,
					firebaseMethod);
			return firebaseHandler.apply(input);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
