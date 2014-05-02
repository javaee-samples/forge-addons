package org.jboss.forge.addon.javaee7.batch.commands;

import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIInputMany;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;

import java.lang.Override;
import java.lang.Exception;

import javax.inject.Inject;

public class BatchNewJobXmlCommand extends AbstractUICommand
{
	
	@Inject @WithAttributes(label = "ItemReader") UIInput<String> reader; 
	@Inject UIInput<String> processor; 
	@Inject UIInput<String> writer; 
	@Inject UIInputMany<String> inputMany;
	@Inject UISelectOne<String> selectOne;
	@Inject UISelectMany<String> selectMany;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.forCommand(BatchNewJobXmlCommand.class).name(
            "batch-new-jobxml");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception
   {
	   builder.add(reader).add(processor).add(writer);
   }

   @Override
   public Result execute(UIExecutionContext context)
   {
      return Results.fail("Not implemented!");
   }
}