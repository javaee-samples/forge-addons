package org.jboss.forge.addon.javaee7.batch.commands;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.parser.java.resources.JavaResourceVisitor;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.visit.VisitContext;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.input.UICompleter;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIInputMany;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

import java.io.FileNotFoundException;
import java.lang.Override;
import java.lang.Exception;
import java.util.HashSet;
import java.util.Set;

import javax.batch.api.chunk.AbstractItemReader;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.batch.api.chunk.ItemProcessor;
import javax.batch.api.chunk.ItemReader;
import javax.batch.api.chunk.ItemWriter;
import javax.inject.Inject;

public class BatchNewJobXmlCommand extends AbstractProjectCommand
{
	
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
			selectedProject.getFacet(JavaSourceFacet.class).visitJavaSources(new JavaResourceVisitor() {

				@Override
				public void visit(VisitContext context,
						JavaResource javaResource) {
					try {
						JavaSource<?> javaType = javaResource.getJavaType();
						if (javaType.isClass() && 
							(((JavaClassSource)javaType).hasInterface(baseInterface) ||
									(baseClass != null && baseClass.getName().equals(((JavaClassSource)javaType).getSuperType()))
									)) {
							result.add(javaType.getQualifiedName());
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});;
			
			return result;
		}
	}

	@Inject @WithAttributes(label = "ItemReader", type=InputType.JAVA_CLASS_PICKER) UIInput<String> reader; 
	@Inject @WithAttributes(label = "ItemProcessor", type=InputType.JAVA_CLASS_PICKER) UIInput<String> processor; 
	@Inject @WithAttributes(label = "ItemWriter", type=InputType.JAVA_CLASS_PICKER) UIInput<String> writer; 
	@Inject UIInputMany<String> inputMany;
	@Inject UISelectOne<String> selectOne;
	@Inject UISelectMany<String> selectMany;
	
	@Inject ProjectFactory projectFactory;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.forCommand(BatchNewJobXmlCommand.class).name(
            "batch-new-jobxml");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
	   reader.setCompleter(new BatchUICompleter(AbstractItemReader.class, ItemReader.class));
	   processor.setCompleter(new BatchUICompleter(null, ItemProcessor.class));
	   writer.setCompleter(new BatchUICompleter(AbstractItemWriter.class, ItemWriter.class));
	   builder.add(reader).add(processor).add(writer);
   }

   @Override
   public Result execute(UIExecutionContext context)
   {
      return Results.fail("Not implemented!");
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