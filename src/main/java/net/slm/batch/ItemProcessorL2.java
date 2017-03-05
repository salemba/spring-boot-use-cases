package net.slm.batch;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;

public class ItemProcessorL2 implements ItemProcessor<Person, Person>  {

	private static final Logger log = LoggerFactory.getLogger(ItemProcessorL2.class);
	
	StepExecution stepExecution;
	
	@BeforeStep
	public void saveStepExecution( StepExecution stepExecution )
	{
	    this.stepExecution = stepExecution;
	}
   

	@Override
    public Person process(final Person person) throws Exception {
        final String firstName = person.getFirstName().toUpperCase();
        Map<String,Person> resultsFromPreviousStep = (Map)stepExecution.getJobExecution()
        		.getExecutionContext()
        		.get("entityRef");
        Person p =resultsFromPreviousStep.get(firstName);//contains firstName, LastName only
        final Person pFinal = new Person(firstName,p.getLastName(),person.getAge());
       // p.setAge(person.getAge());
        
        log.info("Converting (" + person + ") into (" + pFinal + ")");

        return pFinal;
    }
}
