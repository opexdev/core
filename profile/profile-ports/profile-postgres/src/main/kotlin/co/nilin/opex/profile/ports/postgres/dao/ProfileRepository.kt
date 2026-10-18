package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.core.data.kyc.KycLevel
import co.nilin.opex.profile.core.data.profile.Gender
import co.nilin.opex.profile.core.data.profile.NationalityType
import co.nilin.opex.profile.core.data.profile.ProfileStatus
import co.nilin.opex.profile.ports.postgres.ProfileFullNameProjection
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

    @Query("select p.user_id AS uuid, CONCAT (p.first_name, ' ', p.last_name) AS full_name  from profile p where p.user_id in (:uuids)")
    fun findFullNameByUuid(uuids: List<String>): Flux<ProfileFullNameProjection>

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

    // Single statement so it cannot race the UserCreated kafka listener, which creates the same row
    @Query(
        """
        INSERT INTO profile (user_id, email, mobile, first_name, last_name, identifier,
                             person_type, kyc_level, status, create_date, last_update_date, creator)
        VALUES (:userId, :email, :mobile, :firstName, :lastName, :identifier,
                :personType, :kycLevel, :status, now(), now(), :creator)
        ON CONFLICT (user_id) DO UPDATE
            SET email            = EXCLUDED.email,
                mobile           = EXCLUDED.mobile,
                first_name       = EXCLUDED.first_name,
                last_name        = EXCLUDED.last_name,
                identifier       = EXCLUDED.identifier,
                person_type      = EXCLUDED.person_type,
                kyc_level        = EXCLUDED.kyc_level,
                status           = EXCLUDED.status,
                last_update_date = now()
    """
    )
    fun upsertLegalRepresentative(
        userId: String,
        email: String,
        mobile: String,
        firstName: String,
        lastName: String,
        identifier: String,
        personType: String,
        kycLevel: String,
        status: String,
        creator: String
    ): Mono<Void>

}