package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.profile.Gender
import co.nilin.opex.profile.core.data.profile.NationalityType
import co.nilin.opex.profile.core.data.profile.ProfileStatus
import co.nilin.opex.profile.ports.postgres.model.entity.ProfileModel
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface ProfileRepository : ReactiveCrudRepository<ProfileModel, Long> {

    fun findByUserId(userId: String): Mono<ProfileModel>

    @Query("select * from profile where identifier = :identifier order by last_update_date desc limit 1")
    fun findLatestByIdentifier(identifier: String ): Mono<ProfileModel>

    @Query(
        """
    SELECT * 
    FROM profile p 
    WHERE (:userId IS NULL OR p.user_id = :userId)
      AND (:firstName IS NULL OR p.first_name ILIKE CONCAT('%', :firstName, '%'))
      AND (:lastName IS NULL OR p.last_name ILIKE CONCAT('%', :lastName, '%'))
      AND (:mobile IS NULL OR p.mobile ILIKE CONCAT('%', :mobile, '%'))
      AND (:email IS NULL OR p.email ILIKE CONCAT('%', :email, '%'))
      AND (:identifier IS NULL OR p.identifier = :identifier)
      AND (:nationality IS NULL OR p.nationality = :nationality)
      AND (:gender IS NULL OR p.gender = :gender)
      AND (:status IS NULL OR p.status = :status)
      AND (:kycLevel IS NULL OR p.kyc_level = :kycLevel)
      AND (:createDateFrom IS NULL OR p.create_date >= :createDateFrom)
      AND (:createDateTo IS NULL OR p.create_date <= :createDateTo)
    ORDER BY p.create_date DESC
    LIMIT :limit OFFSET :offset
    """
    )
    fun findByCriteriaDesc(
        userId: String?,
        firstName: String?,
        lastName: String?,
        mobile: String?,
        email: String?,
        identifier: String?,
        nationality: NationalityType?,
        gender: Gender?,
        status: ProfileStatus?,
        kycLevel: KycLevel?,
        createDateFrom: LocalDateTime?,
        createDateTo: LocalDateTime?,
        limit: Int,
        offset: Int
    ): Flux<ProfileModel>

    @Query(
        """
    SELECT * 
    FROM profile p 
    WHERE (:userId IS NULL OR p.user_id = :userId)
      AND (:firstName IS NULL OR p.first_name ILIKE CONCAT('%', :firstName, '%'))
      AND (:lastName IS NULL OR p.last_name ILIKE CONCAT('%', :lastName, '%'))
      AND (:mobile IS NULL OR p.mobile ILIKE CONCAT('%', :mobile, '%'))
      AND (:email IS NULL OR p.email ILIKE CONCAT('%', :email, '%'))
      AND (:identifier IS NULL OR p.identifier = :identifier)
      AND (:nationality IS NULL OR p.nationality = :nationality)
      AND (:gender IS NULL OR p.gender = :gender)
      AND (:status IS NULL OR p.status = :status)
      AND (:kycLevel IS NULL OR p.kyc_level = :kycLevel)
      AND (:createDateFrom IS NULL OR p.create_date >= :createDateFrom)
      AND (:createDateTo IS NULL OR p.create_date <= :createDateTo)
    ORDER BY p.create_date
    LIMIT :limit OFFSET :offset
    """
    )
    fun findByCriteriaAsc(
        userId: String?,
        firstName: String?,
        lastName: String?,
        mobile: String?,
        email: String?,
        identifier: String?,
        nationality: NationalityType?,
        gender: Gender?,
        status: ProfileStatus?,
        kycLevel: KycLevel?,
        createDateFrom: LocalDateTime?,
        createDateTo: LocalDateTime?,
        limit: Int,
        offset: Int
    ): Flux<ProfileModel>

    @Query(
        """
        SELECT * FROM profile
        WHERE user_id = :userId
        OR ( :mobile IS NOT NULL AND mobile = :mobile )
        OR ( :email IS NOT NULL AND lower(email) = lower(:email) )
    """
    )
    fun findByUserIdOrEmailOrMobile(userId: String, email: String?, mobile: String?): Mono<ProfileModel>?

    fun findByMobile(mobile: String?): Mono<ProfileModel>?

    fun findByEmail(email: String?): Mono<ProfileModel>?

    @Query("UPDATE profile SET kyc_level = :level WHERE user_id = :userId")
    fun updateKycLevelByUserId(userId: String, level: String): Mono<Void>

}