package org.joyrest.maven.gendoc.transform;

import static freemarker.template.Configuration.VERSION_2_3_22;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

import freemarker.ext.beans.*;
import freemarker.template.*;
import org.joyrest.context.ApplicationContext;
import org.joyrest.model.http.MediaType;
import org.joyrest.routing.entity.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;
import io.github.robwin.markup.builder.*;

public class FreeMarkerTransformer implements Consumer<ApplicationContext> {

	private static String TEMPLATES_PATH = "gendoc/templates";

	private final ObjectMapper mapper = new ObjectMapper();
	private final SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();

	@Override
	public void accept(ApplicationContext applicationContext) {
		if(Files.isDirectory(TEMPLATES_PATH)) {}

		try {
			createFreeMarkerDocument(applicationContext);
		} catch (IOException e) {
			throw new RuntimeException("Error occurred during creating a FreeMarker document", e);
		}
	}

	private void createFreeMarkerDocument(ApplicationContext context) throws IOException {
		TemplateCollectionModel model = new CollectionModel(context.getRoutes(), new BeansWrapper(VERSION_2_3_22));




	}

}
