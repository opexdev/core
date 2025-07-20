package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.model.entity.ProfileModel
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Pageable
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface ProfileRepository : ReactiveCrudRepository<ProfileModel, Long> {

    fun findByUserId(userId: String): Mono<ProfileModel>?

    fun findBy(pageable: Pageable): Flow<ProfileModel>

    @Query("select * from profile p where (:userId is null or p.user_id=:userId ) " +
            "and (:mobile is null or p.mobile=:mobile )" +
            " and (:email is null or p.email=:email )" +
            " and (:firstName is null or p.first_name=:firstName )" +
            " and (:lastName is null or p.last_name=:lastName )" +
            " and (:nationalCode is null or p.identifier=:nationalCode )" +
            " and (:createDateFrom is null or p.create_date > :createDateFrom )" +
            " and (:createDateTo is null or p.create_date < :createDateTo ) ")

    fun findUsersBy(userId: String?, mobile: String?, email: String?, firstName: String?, lastName: String?, nationalCode: String?, createDateFrom: LocalDateTime?, createDateTo: LocalDateTime?, pageable: Pageable): Flow<ProfileModel>?

    @Query("select * from profile p where (:userId is null or  position(lower(:userId) in lower(p.user_id))>0 ) " +
            "and (:mobile is null or position(lower(:mobile) in lower(p.mobile))>0   )" +
            " and (:email is null or  position(lower(:email) in lower(p.email))>0  )" +
            " and (:firstName is null or  position(lower(:firstName) in lower(p.first_name))>0  )" +
            " and (:lastName is null or  position(lower(:lastName) in lower(p.last_name))>0  )" +
            " and (:nationalCode is null or  position(lower(:nationalCode) in lower(p.identifier))>0 )" +
            " and (:createDateFrom is null or p.create_date > :createDateFrom )" +
            " and (:createDateTo is null or p.create_date < :createDateTo ) ")

    fun searchUsersBy(userId: String?, mobile: String?, email: String?, firstName: String?, lastName: String?, nationalCode: String?, createDateFrom: LocalDateTime?, createDateTo: LocalDateTime?, pageable: Pageable): Flow<ProfileModel>?


    @Query("""
        SELECT * FROM profile
        WHERE user_id = :userId
        OR ( :mobile IS NOT NULL AND mobile = :mobile )
        OR ( :email IS NOT NULL AND lower(email) = lower(:email) )
    """)
    fun findByUserIdOrEmailOrMobile(userId: String, email: String? , mobile : String?): Mono<ProfileModel>?

    fun findByMobile(mobile : String?): Mono<ProfileModel>?

    fun findByEmail(email: String?): Mono<ProfileModel>?

}