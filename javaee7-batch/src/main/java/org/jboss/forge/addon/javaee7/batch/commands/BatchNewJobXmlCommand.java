package org.jboss.forge.addon.javaee7.batch.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.batch.api.chunk.AbstractItemReader;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.batch.api.chunk.ItemProcessor;
import javax.batch.api.chunk.ItemReader;
import javax.batch.api.chunk.ItemWriter;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.facets.constraints.FacetConstraints;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.parser.java.resources.JavaResourceVisitor;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.resource.URLResource;
import org.jboss.forge.addon.resource.visit.VisitContext;
import org.jboss.forge.addon.templates.Template;
import org.jboss.forge.addon.templates.TemplateFactory;
import org.jboss.forge.addon.templates.freemarker.FreemarkerTemplate;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIContextProvider;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.input.UICompleter;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIInputMany;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.validate.UIValidator;
import org.jboss.forge.roaster.model.Annotation;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;
import org.jboss.forge.roaster.model.util.Strings;

//@FacetConstraint({ResourcesFacet.class, JavaSourceFacet.class})
public class BatchNewJobXmlCommand extends AbstractProjectCommand {

	private final class NamedValidator implements UIValidator {
		@Override
		public void validate(UIValidationContext context) {
			Project selectedProject = getSelectedProject(context);
			JavaSourceFacet javaFacet = selectedProject.getFacet(JavaSourceFacet.class);
			try {
				if (null == context.getCurrentInputComponent().getValue())
					return;
				JavaResource javaResource = javaFacet.getJavaResource((String)context.getCurrentInputComponent().getValue());
				if (!javaResource.getJavaType().hasAnnotation(Named.class)) {
					context.addValidationError(context.getCurrentInputComponent(), javaResource.getFullyQualifiedName() + " must be annotated with @Named");
				}
			} catch (FileNotFoundException e) {
				context.addValidationError(context.getCurrentInputComponent(), e.getMessage());
			}				
		}
	}

	private final class BatchUICompleter implements UICompleter<String> {
		Class<?> baseClass;
		Class<?> baseInterface;

		public BatchUICompleter(Class<?> baseClass, Class<?> baseInterface) {
			this.baseClass = baseClass;
			this.baseInterface = baseInterface;
		}

		@Override
		public Iterable<String> getCompletionProposals(UIContext context,
				InputComponent<?, String> input, String value) {
			final Set<String> result = new HashSet<>();
			Project selectedProject = getSelectedProject(context);
			selectedProject.getFacet(JavaSourceFacet.class).visitJavaSources(
					new JavaResourceVisitor() {

						@Override
						public void visit(VisitContext context,
								JavaResource javaResource) {
							try {
								JavaSource<?> javaType = javaResource
										.getJavaType();
								if (javaType.isClass()
										&& (((JavaClassSource) javaType)
												.hasInterface(baseInterface) || (baseClass != null && baseClass
												.getName()
												.equals(((JavaClassSource) javaType)
														.getSuperType())))) {
									result.add(javaType.getQualifiedName());
								}
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
			;

			return result;
		}
	}

	@Inject
	@WithAttributes(label = "JobXML", required = true)
	UIInput<String> jobXML;
	@Inject
	@WithAttributes(label = "ItemReader", type = InputType.JAVA_CLASS_PICKER, required=true)
	UIInput<String> reader;
	@Inject
	@WithAttributes(label = "ItemProcessor", type = InputType.JAVA_CLASS_PICKER)
	UIInput<String> processor;
	@Inject
	@WithAttributes(label = "ItemWriter", type = InputType.JAVA_CLASS_PICKER, required=true)
	UIInput<String> writer;

	@Inject
	ProjectFactory projectFactory;
	
	@Inject
	TemplateFactory templateFactory;
	
	@Inject
	ResourceFactory resourceFactory;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(BatchNewJobXmlCommand.class).name(
				"batch-new-jobxml");
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {
		reader.setCompleter(new BatchUICompleter(AbstractItemReader.class,
				ItemReader.class));
		processor.setCompleter(new BatchUICompleter(null, ItemProcessor.class));
		writer.setCompleter(new BatchUICompleter(AbstractItemWriter.class,
				ItemWriter.class));
		
		reader.addValidator(new NamedValidator());
		processor.addValidator(new NamedValidator());
		writer.addValidator(new NamedValidator());

		jobXML.addValidator(new UIValidator() {

			@Override
			public void validate(UIValidationContext context) {
				String jobXMLName = ((String)context.getCurrentInputComponent().getValue());
				
				FileResource<?> resource = getJobXMLResource(context.getUIContext(),
						jobXMLName);
				if (resource.exists()) {
					context.addValidationError(context.getCurrentInputComponent(), resource.getFullyQualifiedName() + " already exists");
				}
			}

		});
		builder.add(jobXML).add(reader).add(processor).add(writer);
	}

	private FileResource<?> getJobXMLResource(
			UIContext context, String jobXMLName) {
		ResourcesFacet facet = getSelectedProject(context).getFacet(ResourcesFacet.class);
		FileResource<?> resource = facet.getResource("META-INF" + File.separator + "batch-jobs" + File.separator + jobXMLName);
		return resource;
	}

	@Override
	public Result execute(UIExecutionContext context) {
		
		FileResource<?> jobXMLResource = getJobXMLResource(context.getUIContext(), jobXML.getValue());
//		 BatchXMLDescriptor descriptor = Descriptors.create(BatchXMLDescriptor.class);
		Resource<URL> templateJobXML = resourceFactory.create(getClass().getResource("/templates/job.ftl")).reify(URLResource.class);
		Template template = templateFactory.create(templateJobXML, FreemarkerTemplate.class);
		
		Map<String, Object> templateContext = new HashMap<>();
		try {
			templateContext.put("readerBeanName", getCDIBeanName(context, reader.getValue()));
			templateContext.put("writerBeanName", getCDIBeanName(context, writer.getValue()));
			if (processor.hasValue()) {
			    templateContext.put("processorBeanName", getCDIBeanName(context, processor.getValue()));
			}

			jobXMLResource.createNewFile();
			jobXMLResource.setContents(template.process(templateContext));
		} catch (IOException e) {
			return Results.fail(e.getMessage(), e);
		}
		return Results.success("Created JobXML");
	}

	private String getCDIBeanName(UIContextProvider context, String fqcn) throws FileNotFoundException {
		Project selectedProject = getSelectedProject(context);
		JavaSourceFacet javaFacet = selectedProject.getFacet(JavaSourceFacet.class);
		JavaResource javaResource = javaFacet.getJavaResource(fqcn);
		JavaType<?> javaType = javaResource.getJavaType();
		Annotation<?> named = javaType.getAnnotation(Named.class);
		if (named.getStringValue() != null) {
			return named.getStringValue();
		}
		return Strings.uncapitalize(javaType.getName());
	}

	@Override
	protected ProjectFactory getProjectFactory() {
		return projectFactory;
	}

	@Override
	protected boolean isProjectRequired() {
		return true;
	}
}
