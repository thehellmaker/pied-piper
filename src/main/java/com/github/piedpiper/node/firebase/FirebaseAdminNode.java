package com.github.piedpiper.node.firebase;

import java.io.InputStream;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.util.StringInputStream;
import com.github.piedpiper.node.BaseNode;
import com.github.piedpiper.node.NodeInput;
import com.github.piedpiper.node.NodeOutput;
import com.github.piedpiper.node.ParameterMetadata;
import com.github.piedpiper.utils.ParameterUtils;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FirebaseAdminNode extends BaseNode {

	public static final ParameterMetadata METHOD = new ParameterMetadata("method", ParameterMetadata.MANDATORY);

	public static final ParameterMetadata CONFIG = new ParameterMetadata("config", ParameterMetadata.MANDATORY);

	@Override
	public NodeOutput apply(NodeInput input) {
		try {
			String firebaseMethod = ParameterUtils.getParameterData(input.getInput(), METHOD).getValueString();
			String firebaseConfig = ParameterUtils.getParameterData(input.getInput(), CONFIG).getValueString();

			initializeFirebase(firebaseConfig);

			Function<NodeInput, NodeOutput> firebaseHandler = FirebaseMethodHandlerFactory.getHandler(injector,
					firebaseMethod);

			return firebaseHandler.apply(input);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void initializeFirebase(String firebaseConfig) {
		if (FirebaseApp.getApps().size() > 0)
			return;
		try {
			if(StringUtils.isBlank(firebaseConfig)) {
				throw new IllegalArgumentException("Both firebaseConfig and firebaseConfigLine are emtpy");
			} 
			
			InputStream refreshToken = new StringInputStream(firebaseConfig);

			GoogleCredentials cred = GoogleCredentials.fromStream(refreshToken);

			FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(cred)
					.build();

			FirebaseApp.initializeApp(options);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
