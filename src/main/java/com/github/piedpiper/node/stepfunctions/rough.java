package com.github.piedpiper.node.stepfunctions;

import com.github.piedpiper.guice.PiedPiperModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class rough {
	public static void main(String[] args) {
		Injector i = Guice.createInjector(new PiedPiperModule());
		StepFunctionsExecute e1 = i.getInstance(StepFunctionsExecute.class);
		StepFunctionsExecute e2 = i.getInstance(StepFunctionsExecute.class);
	}
}
