package org.joyrest.maven.gendoc.transform;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.joyrest.maven.common.Utils.createFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.SimpleType;
import org.joyrest.context.ApplicationContext;
import org.joyrest.model.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.factories.SchemaFactoryWrapper;

import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.markup.builder.MarkupDocBuilders;
import io.github.robwin.markup.builder.MarkupLanguage;
import org.joyrest.routing.entity.CollectionType;
import org.joyrest.routing.entity.Type;

public class MarkdownTransformer implements Consumer<ApplicationContext> {

	private static Path DOCGEN_PATH = Paths.get("gendoc");

	private final ObjectMapper mapper = new ObjectMapper();
	private final SchemaFactoryWrapper visitor = new SchemaFactoryWrapper();

	@Override
	public void accept(ApplicationContext applicationContext) {
		createFolder(DOCGEN_PATH);

		try {
			createMarkdownDocument(applicationContext);
		} catch (IOException e) {
			throw new RuntimeException("Error occurred during creating a Markdown document", e);
		}
	}

	private void createMarkdownDocument(ApplicationContext context) throws IOException {
		MarkupDocBuilder builder = MarkupDocBuilders.documentBuilder(MarkupLanguage.MARKDOWN);
		builder.documentTitle("Joyrest - Generated Documentation");
		context.getRoutes().forEach(route -> {
			builder.sectionTitleLevel2(route.getHttpMethod() + "  " + route.getPath());

			List<String> consumes = mediaTypesToStrings(route.getConsumes());
			if (!consumes.isEmpty())
				builder.boldTextLine("Consumes:")
					.unorderedList(consumes);

			List<String> produces = mediaTypesToStrings(route.getProduces());
			if (!produces.isEmpty())
				builder.boldTextLine("Produces:")
					.unorderedList(produces);

			if (nonNull(route.getRequestType()))
				builder.boldTextLine("Request Class:")
					.source(entitySchema(route.getRequestType()), "json");

			if (nonNull(route.getResponseType()))
				builder.boldTextLine("Response Class:")
					.source(entitySchema(route.getResponseType()), "json");

		});
		builder.writeToFile(DOCGEN_PATH.toString(), "markdown", StandardCharsets.UTF_8);
	}

	private String entitySchema(Type type) {
		try {
			JavaType fasterType;
			if(CollectionType.class.isAssignableFrom(type.getClass())) {
				CollectionType collectionType = (CollectionType) type;
				fasterType = com.fasterxml.jackson.databind.type.CollectionType
					.construct(Collection.class, SimpleType.construct(collectionType.getParam()));
			} else
				fasterType = SimpleType.construct(type.getType());

			mapper.acceptJsonFormatVisitor(fasterType, visitor);
			JsonSchema schema = visitor.finalSchema();
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
		} catch (Exception e) {
			throw new RuntimeException("Error occurred during creation of JSON Schema", e);
		}
	}

	private static List<String> mediaTypesToStrings(List<MediaType> mediaTypes) {
		return mediaTypes.stream()
			.filter(mediaType -> MediaType.WILDCARD != mediaType)
			.map(MediaType::toString)
			.collect(toList());
	}
}
