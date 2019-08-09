package com.github.piedpiper.node.rest;

import com.github.piedpiper.node.NodeInput;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;

public class RESTPostHandler extends BaseBodyRestHandler {

	@Override
	protected HttpRequestWithBody getRequestWithBody(NodeInput input) throws Exception {
		return Unirest.post(getUrl(input));
	}
}
