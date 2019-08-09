package com.github.piedpiper.node.firebase;

import java.util.function.Function;

import com.github.piedpiper.common.PiedPiperConstants;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.google.inject.Injector;

public class FirebaseMethodHandlerFactory {
	
	public static Function<NodeInput, NodeOutput> getHandler(Injector injector, String methodName) {
		switch(methodName) {
			case PiedPiperConstants.JWT_VERIFY:
				return injector.getInstance(JWTVerifyFunction.class);
			case PiedPiperConstants.GET_USER:
				return injector.getInstance(GetUserFunction.class);
		}
		throw new RuntimeException(String.format("Unknown method: %s", methodName));
	}
	
}
