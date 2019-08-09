package com.github.piedpiper.node;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import javassist.Modifier;
import net.sourceforge.stripes.util.ResolverUtil;

public class NodeListQueryHandler implements Function<Void, List<NodeMetadata>> {

	@Override
	public List<NodeMetadata> apply(Void input) {
		ResolverUtil<INode> resolver = new ResolverUtil<INode>();
		resolver.findImplementations(INode.class, "com.github");
		List<Class<? extends INode>> classes = resolver.getClasses().stream()
				.filter(clazz -> !Modifier.isAbstract(clazz.getModifiers())).collect(Collectors.toList());
		List<NodeMetadata> output = Lists.newArrayList();
		for(@SuppressWarnings("rawtypes") Class clazz: classes) {
			if(clazz.getName().contains("Mock")) continue;
			output.add(new NodeMetadataQueryHandler().apply(clazz));
		}
		return output;
	}

}
