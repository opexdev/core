package co.nilin.opex.profile.ports.postgres.dao

import co.nilin.opex.profile.ports.postgres.sample.VALID
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono


class ProfileRepositoryTest {

    private val repository = mockk<ProfileRepository>()

    @Test
    fun givenProfileExists_whenFindByUserId_resultIsValidAndNotEmpty(): Unit = runBlocking {
        every { repository.save(any()) } returns Mono.just(VALID.profileModel)
        every { repository.findByUserId("user-123") } returns Mono.just(VALID.profileModel)

        val savedProfile = repository.save(VALID.profileModel).awaitSingle()
        assertThat(savedProfile.id).isNotNull()
        assertThat(savedProfile.userId).isEqualTo("user-123")

        val foundProfile = repository.findByUserId("user-123")?.awaitSingle()
        assertThat(foundProfile).isNotNull
        assertThat(foundProfile!!.mobile).isEqualTo("0912000000")
        assertThat(foundProfile.email).isEqualTo("user@example.com")
    }
}