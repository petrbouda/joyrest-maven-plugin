package org.joyrest.maven.gendoc.transform;

import static freemarker.template.Configuration.VERSION_2_3_22;
import static java.nio.file.Files.exists;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.*;
import java.util.function.Consumer;

import freemarker.ext.beans.*;
import freemarker.template.*;
import org.joyrest.context.ApplicationContext;

public class FreeMarkerTransformer implements Transformer {

	private static String TEMPLATE_DIR = "gendoc/templates";

	private static String TEMPLATE_FILE = "documentation.ftl";

	private static String OUTPUT_DIR = "gendoc/generated";

	private static String OUTPUT_FILE = "documentation.html";

	private final String templateFileName;

	private final Path templatePath;

	private final Path outputPath;

	public FreeMarkerTransformer(String templateDir, String templateFileName, String outputDir, String outputFileName) {
		this.templateFileName = templateFileName;

		this.templatePath = Paths.get(templateDir, templateFileName);
		this.outputPath = Paths.get(outputDir, outputFileName);
	}

	public FreeMarkerTransformer() {
		this(TEMPLATE_DIR, TEMPLATE_FILE, OUTPUT_DIR, OUTPUT_FILE);
	}

	@Override
	public void accept(ApplicationContext applicationContext) {
		try {
			if (!exists(templatePath))
				throw new RuntimeException("There is no FreeMarker templates directory.");

			if (!exists(outputPath))
				Files.createFile(outputPath);

			createFreeMarkerDocument(applicationContext);
		} catch (IOException e) {
			throw new RuntimeException("Error occurred during creating a FreeMarker document.", e);
		} catch (TemplateException e) {
			throw new RuntimeException("Error occurred during processing a FreeMarker template.", e);
		}
	}

	private void createFreeMarkerDocument(ApplicationContext context) throws IOException, TemplateException {
		Configuration config = new Configuration(VERSION_2_3_22);
		config.setDirectoryForTemplateLoading(new File(TEMPLATE_DIR));
		config.setDefaultEncoding("UTF-8");
		config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		Template temp = config.getTemplate(templateFileName);
		TemplateCollectionModel model = new CollectionModel(context.getRoutes(), new BeansWrapper(VERSION_2_3_22));

		try (Writer writer = Files.newBufferedWriter(outputPath)) {
			temp.process(model, writer);
		}
	}
}
