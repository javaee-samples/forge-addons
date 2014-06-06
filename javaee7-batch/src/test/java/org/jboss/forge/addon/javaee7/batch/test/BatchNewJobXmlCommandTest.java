package org.jboss.forge.addon.javaee7.batch.test;

import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.forge.addon.javaee7.batch.commands.BatchNewJobXmlCommand;
import org.jboss.forge.addon.javaee7.batch.templates.MyItemProcessor;
import org.jboss.forge.addon.javaee7.batch.templates.MyItemReader;
import org.jboss.forge.addon.javaee7.batch.templates.MyItemWriter;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.output.UIMessage;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
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
			@AddonDependency(name = "org.jboss.forge.addon.javaee7.batch:javaee7-batch")})
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
						AddonDependencyEntry.create("org.jboss.forge.addon.javaee7.batch:javaee7-batch"));
		return archive;
	}
	
	@Inject
	UITestHarness harness;
	
	@Inject
	ProjectFactory factory;

	@Test
	public void testNewJobXml() throws Exception {
		Project createTempProject = factory.createTempProject();
		CommandController commandController = harness.createCommandController(BatchNewJobXmlCommand.class, createTempProject.getRoot());
		commandController.initialize();
		
		// set values
		commandController.setValueFor("reader", MyItemReader.class.getName());
		commandController.setValueFor("processor", MyItemProcessor.class.getName());
		commandController.setValueFor("writer", MyItemWriter.class.getName());
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