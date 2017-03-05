package net.slm.batch;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.CompositeStepExecutionListener;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Bean
    public ExecutionContextPromotionListener promotionListener()
    {
    	ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys( new String[] { "entityRef" } );
        return listener;
    }
    
    // tag::readerwriterprocessor[]
    @Bean
    public FlatFileItemReader<Person> reader() {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
        reader.setResource(new ClassPathResource("sample-data.csv"));
        reader.setLineMapper(new DefaultLineMapper<Person>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] { "firstName", "lastName" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }});
        }});
        return reader;
    }
    @Bean
    public FlatFileItemReader<Person> readerForAge() {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
        reader.setResource(new ClassPathResource("sample-data2.csv"));
        reader.setLineMapper(new DefaultLineMapper<Person>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] { "firstName", "age" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }});
        }});
        return reader;
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }
    
    @Bean
    public ItemProcessorL2 processorL2() {
        return new ItemProcessorL2();
    }

    @Bean
    public LastStepWriter<Person> writer() {
        LastStepWriter<Person> writer = new LastStepWriter<Person>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
        writer.setSql("INSERT INTO people (first_name, last_name, age) VALUES (:firstName, :lastName, :age)");
        writer.setDataSource(dataSource);
        return writer;
    }
    @Bean
    public InMemoryWriter<Person> writerInMemory() {
        InMemoryWriter<Person> inMemoryWriter = new InMemoryWriter<>();
        return inMemoryWriter;
    }
    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener) {
    	
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                //.listener((JobExecutionListener) promotionListener())
                .flow(step1())
                .next(step2())
                .end()
                .build();
    }

    @Bean
    public Step step1() {
       Step step=stepBuilderFactory.get("step1")
                .<Person, Person> chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writerInMemory())
                .faultTolerant()
                .build();
        return step;
    }
    
    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .<Person, Person> chunk(10)
                .reader(readerForAge())
                .processor(processorL2())
                .writer(writer())
                .build();
    }
    // end::jobstep[]
}
