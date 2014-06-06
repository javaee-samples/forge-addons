package org.forge.samples.javaee7.batch;

import java.util.List;
import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Arun Gupta
 */
@Named
public class MyItemWriter extends AbstractItemWriter {
    @Override
    public void writeItems(List list) {
        System.out.println("writeItems: " + list);
    }
}
