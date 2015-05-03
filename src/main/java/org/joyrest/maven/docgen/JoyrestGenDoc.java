package org.joyrest.maven.docgen;

import static java.util.Objects.nonNull;

import java.io.File;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.joyrest.context.ApplicationContext;
import org.joyrest.context.Configurer;
import org.joyrest.maven.docgen.transform.MarkdownTransformer;

@Mojo(name = "gendoc", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class JoyrestGenDoc extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${plugin}", readonly = true)
	private PluginDescriptor descriptor;

	@Parameter
	private String translator;

	@Parameter
	private String applicationConfigClass;
	private Object applicationConfig;

	@Parameter
	private String configurerClass;
	private Configurer configurer;

	@Parameter
	private Content content;

	@Parameter
	private GenerationStrategy generationStrategy;

	@Parameter
	private Content[] contents;

	public void execute() throws MojoExecutionException, MojoFailureException {
		loadClassPath();

		if (nonNull(configurerClass))
			this.configurer = getInstanceFromClazz(configurerClass, Configurer.class);

		if (nonNull(applicationConfigClass))
			this.applicationConfig = getInstanceFromClazz(applicationConfigClass);

		@SuppressWarnings("unchecked")
		ApplicationContext context = configurer.initialize(applicationConfig);

		MarkdownTransformer transformer = new MarkdownTransformer();
		transformer.accept(context);
	}

	private void loadClassPath() {
		try {
			List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();
			ClassRealm realm = descriptor.getClassRealm();

			for (String element : runtimeClasspathElements) {
				File elementFile = new File(element);
				realm.addURL(elementFile.toURI().toURL());
			}
		} catch (Exception e) {
			throw new RuntimeException("Error occurred during loading project classes to a plugin runtime environment", e);
		}
	}

	private static Object getInstanceFromClazz(String clazzName) {
		return getInstanceFromClazz(clazzName, Object.class);
	}

	@SuppressWarnings("unchecked")
	private static <T> T getInstanceFromClazz(String clazzName, Class<T> expectedClazz) {
		try {
			Class<?> clazz = Class.forName(clazzName);
			return (T) clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Invalid expected class", e);
		}
	}

}
