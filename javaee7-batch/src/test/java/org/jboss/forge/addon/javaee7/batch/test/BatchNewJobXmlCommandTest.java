package org.jboss.forge.addon.javaee7.batch.test;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.forge.addon.javaee7.batch.commands.BatchNewJobXmlCommand;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.resource.ResourceFactory;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.output.UIMessage;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.shrinkwrap.api.ShrinkWrap;

@RunWith(Arquillian.class)
public class BatchNewJobXmlCommandTest {

	@Deployment
	@Dependencies({
			@AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
			@AddonDependency(name = "org.jboss.forge.addon:ui"),
			@AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
			@AddonDependency(name = "org.jboss.forge.addon:maven"),
			@AddonDependency(name = "org.jboss.forge.addon:projects"),
			@AddonDependency(name = "org.jboss.forge.addon.javaee7.batch:javaee7-batch"),
			@AddonDependency(name = "org.jboss.forge.addon:parser-java")})
	public static ForgeArchive getDeployment() {
		ForgeArchive archive = ShrinkWrap
				.create(ForgeArchive.class)
				.addBeansXML()
				.addAsAddonDependencies(
						AddonDependencyEntry
								.create("org.jboss.forge.furnace.container:cdi"),
						AddonDependencyEntry.create("org.jboss.forge.addon:ui"),
						AddonDependencyEntry.create("org.jboss.forge.addon:ui-test-harness"),
						AddonDependencyEntry.create("org.jboss.forge.addon:maven"),
						AddonDependencyEntry.create("org.jboss.forge.addon:projects"),
						AddonDependencyEntry.create("org.jboss.forge.addon.javaee7.batch:javaee7-batch"),
						AddonDependencyEntry.create("org.jboss.forge.addon:parser-java"));
		return archive;
	}
	
	@Inject
	UITestHarness harness;
	
	@Inject
	ProjectFactory factory;
		
	@Test
	public void testNewJobXml() throws Exception {
		Project project = factory.createTempProject(Arrays.asList(ResourcesFacet.class, JavaSourceFacet.class));
		JavaClassSource reader = Roaster.parse(JavaClassSource.class, getClass().getClassLoader().getResource("templates/MyItemReader.jv"));
		JavaClassSource processor = Roaster.parse(JavaClassSource.class, getClass().getClassLoader().getResource("templates/MyItemProcessor.jv"));
		JavaClassSource writer = Roaster.parse(JavaClassSource.class, getClass().getClassLoader().getResource("templates/MyItemWriter.jv"));
		JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
		JavaResource readerResource = java.saveJavaSource(reader);
		JavaResource processorResource = java.saveJavaSource(processor);
		JavaResource writerResource = java.saveJavaSource(writer);
		
		CommandController commandController = harness.createCommandController(BatchNewJobXmlCommand.class, project.getRoot());
		commandController.initialize();
		
		// set values
		commandController.setValueFor("reader", readerResource.getJavaType().getQualifiedName());
		commandController.setValueFor("processor", processorResource.getJavaType().getQualifiedName());
		commandController.setValueFor("writer", writerResource.getJavaType().getQualifiedName());
		commandController.setValueFor("jobXML", "myJob.xml");
		
		// validate
		List<UIMessage> validate = commandController.validate();
		Assert.assertEquals(0,  validate.size());
		
		// execute
		Result result = commandController.execute();
		
		// verify results
		Assert.assertFalse(result instanceof Failed);
	}
	

	@Test
	public void testNewJobXmlOptionalProcessor() throws Exception {
		Project project = factory.createTempProject(Arrays.asList(ResourcesFacet.class, JavaSourceFacet.class));
		
		JavaClassSource reader = Roaster.parse(JavaClassSource.class, getClass().getClassLoader().getResource("templates/MyItemReader.jv"));
		JavaClassSource writer = Roaster.parse(JavaClassSource.class, getClass().getClassLoader().getResource("templates/MyItemWriter.jv"));
		JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
		JavaResource readerResource = java.saveJavaSource(reader);
		JavaResource writerResource = java.saveJavaSource(writer);
		
		CommandController commandController = harness.createCommandController(BatchNewJobXmlCommand.class, project.getRoot());
		commandController.initialize();
		
		// set values
		commandController.setValueFor("reader", readerResource.getJavaType().getQualifiedName());
		commandController.setValueFor("writer", writerResource.getJavaType().getQualifiedName());
		commandController.setValueFor("jobXML", "myJob.xml");
		
		// validate
		List<UIMessage> validate = commandController.validate();
		Assert.assertEquals(0,  validate.size());
		
		// execute
		Result result = commandController.execute();
		
		// verify results
		Assert.assertFalse(result instanceof Failed);
	}

}
