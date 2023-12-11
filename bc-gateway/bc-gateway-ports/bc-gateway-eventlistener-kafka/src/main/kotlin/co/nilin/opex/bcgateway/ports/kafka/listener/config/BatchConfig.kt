package co.nilin.opex.bcgateway.ports.kafka.listener.config

import org.springframework.batch.core.Step
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.xml.StaxEventItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.MalformedURLException


@Configuration
class BatchConfig {
    @Bean
    fun itemReader(): ItemReader<Void?>? {
        return FlatFileItemReader();
    }

    @Bean
    fun itemProcessor(): ItemProcessor<Void?, Void?>? {
        return CustomItemProcessor()
    }

    @Bean
    @Throws(MalformedURLException::class)
    fun itemWriter(marshaller: Marshaller?): ItemWriter<Void?>? {
        return StaxEventItemWriter<Void>()
    }

    fun step1(jobRepository: JobRepository, reader: ItemReader<Void?>): Step {

    }

//    @Bean
//    protected Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager, ItemReader<Transaction> reader,
//    ItemProcessor<Transaction, Transaction> processor, ItemWriter<Transaction> writer, )
//    {
//        return new StepBuilder ("step1", jobRepository).<Transaction, Transaction> chunk(10, transactionManager)
//        .reader(reader).processor(processor).writer(writer).build();
//    }
}