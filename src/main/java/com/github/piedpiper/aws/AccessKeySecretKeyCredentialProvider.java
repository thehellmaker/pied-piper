package com.github.piedpiper.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

public class AccessKeySecretKeyCredentialProvider implements AWSCredentialsProvider {

	private String accessKey;
	
	private String secretKey;
	
	public AccessKeySecretKeyCredentialProvider(String accessKey, String secretKey) {
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}
	
	public AWSCredentials getCredentials() {
		return new AWSCredentials() {
			
			public String getAWSSecretKey() {
				return secretKey;
			}
			
			public String getAWSAccessKeyId() {
				return accessKey;				
			}
		};

	}

	public void refresh() {
	}

}
