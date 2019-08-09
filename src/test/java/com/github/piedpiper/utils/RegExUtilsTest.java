package com.github.piedpiper.utils;

import org.junit.Assert;
import org.junit.Test;

public class RegExUtilsTest {
	
	@Test
	public void testSuccessIsSubstitutorString() {
		Assert.assertTrue(RegExUtils.isSubstitutorString("${akash}"));
	}
	
	@Test
	public void testFailureIsSubstitutorString() {
		Assert.assertFalse(RegExUtils.isSubstitutorString("asdasd ${akash}"));
		Assert.assertFalse(RegExUtils.isSubstitutorString("asdasd${akash}"));
		Assert.assertFalse(RegExUtils.isSubstitutorString("${akash}asdasdasd"));
		Assert.assertFalse(RegExUtils.isSubstitutorString("${akash} asdasdasd"));
		Assert.assertFalse(RegExUtils.isSubstitutorString("${akash"));
		Assert.assertFalse(RegExUtils.isSubstitutorString("$akash}"));
		Assert.assertFalse(RegExUtils.isSubstitutorString("{akash}"));
		Assert.assertFalse(RegExUtils.isSubstitutorString(" ${akash}"));
	}
	
	@Test
	public void testSuccessIsNotSubstitutorString() {
		Assert.assertTrue(RegExUtils.isNotSubstitutorString("asdasd ${akash}"));
		Assert.assertTrue(RegExUtils.isNotSubstitutorString("asdasd${akash}"));
		Assert.assertTrue(RegExUtils.isNotSubstitutorString("${akash}asdasdasd"));
		Assert.assertTrue(RegExUtils.isNotSubstitutorString("${akash} asdasdasd"));
		Assert.assertTrue(RegExUtils.isNotSubstitutorString("${akash"));
		Assert.assertTrue(RegExUtils.isNotSubstitutorString("$akash}"));
		Assert.assertTrue(RegExUtils.isNotSubstitutorString("{akash}"));
		Assert.assertTrue(RegExUtils.isNotSubstitutorString(" ${akash}"));
	}
	
	@Test
	public void testFailureIsNotSubstitutorString() {
		Assert.assertFalse(RegExUtils.isNotSubstitutorString("${akash}"));
	}
	
}

