package org.jboss.forge.addon.javaee7.batch.templates;

import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;

/**
 * @author Arun Gupta
 */
@Named
public class MyItemProcessor implements ItemProcessor {

    @Override
    public String processItem(Object t) {
        System.out.println("processItem: " + t);
        
        return (((Integer)t).intValue() % 2 == 0) ? null : new String(((Integer)t).intValue() * 2 + "");
    }
}
