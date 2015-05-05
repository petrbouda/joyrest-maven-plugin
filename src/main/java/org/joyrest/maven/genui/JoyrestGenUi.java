package org.joyrest.maven.genui;

import static java.util.Objects.nonNull;

import org.apache.maven.plugin.*;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.joyrest.context.*;
import org.joyrest.maven.common.Utils;
import org.joyrest.maven.gendoc.transform.MarkdownTransformer;

@Mojo(name = "genui", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class JoyrestGenUi extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	@Parameter(defaultValue = "${plugin}", readonly = true)
	private PluginDescriptor descriptor;

	@Parameter
	private String applicationConfigClass;
	private Object applicationConfig;

	@Parameter
	private String configurerClass;
	private Configurer configurer;

	public void execute() throws MojoExecutionException, MojoFailureException {
		Utils.loadClassPath(project, descriptor);

		if (nonNull(configurerClass))
			this.configurer = Utils.getInstanceFromClazz(configurerClass, Configurer.class);

		if (nonNull(applicationConfigClass))
			this.applicationConfig = Utils.getInstanceFromClazz(applicationConfigClass);

		@SuppressWarnings("unchecked")
		ApplicationContext context = configurer.initialize(applicationConfig);

		MarkdownTransformer transformer = new MarkdownTransformer();
		transformer.accept(context);
	}

}
