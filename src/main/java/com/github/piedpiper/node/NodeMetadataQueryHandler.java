package com.github.piedpiper.node;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.google.common.collect.Lists;

/**
 * Queries {@link NodeMetadata} from {@link Class<INode>} for it to be displayed
 * on a user interface.
 * 
 * @author aashok
 *
 */
public class NodeMetadataQueryHandler implements Function<Class<?>, NodeMetadata> {

	@Override
	public NodeMetadata apply(Class<?> input) {
		try {
			List<Field> declaredFields = FieldUtils.getAllFieldsList(input);
			List<ParameterMetadata> parameterMetadataList = Lists.newArrayList();
			for (Field field : declaredFields) {
				if (!Modifier.isStatic(field.getModifiers()))
					continue;
				Object declaredField;

				declaredField = FieldUtils.readDeclaredStaticField(field.getDeclaringClass(), field.getName(), true);

				if (declaredField instanceof ParameterMetadata) {
					parameterMetadataList.add((ParameterMetadata) declaredField);
				}
			}
			NodeMetadata metadata = new NodeMetadata();
			metadata.setNodeClass(input);
			metadata.setParameterMetadataList(parameterMetadataList);
			return metadata;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
