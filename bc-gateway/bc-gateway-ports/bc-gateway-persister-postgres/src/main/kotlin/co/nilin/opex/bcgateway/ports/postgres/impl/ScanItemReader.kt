package co.nilin.opex.bcgateway.ports.postgres.impl

import co.nilin.opex.bcgateway.ports.postgres.dao.AssignedAddressRepository
import co.nilin.opex.bcgateway.ports.postgres.model.AssignedAddressModel
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.data.domain.Sort
import javax.annotation.PostConstruct


open class ScanItemReader(private val assignedAddressRepository: AssignedAddressRepository) : RepositoryItemReader<AssignedAddressModel>() {
    @PostConstruct
    protected fun init() {

//        val sorts: Map<String, Sort.Direction> = HashMap();
//        sorts. .put("Your sort parameter"), Direction.ASC);// This could be any field name of your Entity class
//        this.setRepository(this.repository);
//        this.setSort(sorts);
//        this.setMethodName(""); // You should sepcify the method which
//        //spring batch should call in your repository to fetch
//        // data and the arguments it needs needs to be
//        //specified with the below method.
//        // And this method  must return Page<T>
//        this.setArguments();
    }
}