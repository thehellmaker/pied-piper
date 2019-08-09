package com.github.piedpiper.node.rest;

import com.github.piedpiper.node.NodeInput;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;

public class RESTPutHandler extends BaseBodyRestHandler {
	@Override
	protected HttpRequestWithBody getRequestWithBody(NodeInput input) throws Exception {
		return Unirest.put(getUrl(input));
	}
}
