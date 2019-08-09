package com.github.piedpiper.node.firebase;

import org.junit.Assert;
import org.junit.Test;

import com.github.piedpiper.common.PiedPiperConstants;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;


public class FirebaseMethodHandlerFactoryTest {

	@Test
	public void testFactorySuccess() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				
			}
		});
		Assert.assertTrue(FirebaseMethodHandlerFactory.getHandler(injector, PiedPiperConstants.JWT_VERIFY) instanceof JWTVerifyFunction);
		Assert.assertTrue(FirebaseMethodHandlerFactory.getHandler(injector, PiedPiperConstants.GET_USER) instanceof GetUserFunction);
	}
	
	@Test
	public void testFactoryException() {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				
			}
		});
		try {
			FirebaseMethodHandlerFactory.getHandler(injector, "crappyMethod");
		} catch(Exception e) {
			Assert.assertTrue(e.getMessage().contains("Unknown method: crappyMethod"));
		}
	}
	
}

