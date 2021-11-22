package okta.example.reactive.webfluxtutorial

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.StringUtils
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.util.*
import java.util.function.Predicate


@DataMongoTest
@ExtendWith(SpringExtension::class)
@Import(ProfileService::class)
internal class ProfileServiceTest {

    @Autowired
    private lateinit var repository: ProfileRepository

    @Autowired
    private lateinit var service: ProfileService

    @Test
    fun getAll() {
        val saved: Flux<Profile> =
            repository.saveAll(Flux.just(Profile("1", "Josh"), Profile("2", "Matt"), Profile("3", "Jane")))
        val composite: Flux<Profile> = service.all().thenMany(saved)
        val match: Predicate<Profile> =
            Predicate<Profile> { profile ->
                saved.any { saveItem -> saveItem == profile }
                    .block()!!
            }
        StepVerifier
            .create(composite)
            .expectNextMatches(match)
            .expectNextMatches(match)
            .expectNextMatches(match)
            .verifyComplete()
    }

    @Test
    fun save() {
        val profileMono = service.create("email@email.com")

        StepVerifier
            .create(profileMono)
            .expectNextMatches { saved -> StringUtils.hasText(saved.id) }
            .verifyComplete()
    }

    @Test
    fun delete() {
        val test = "test"
        val deleted = service
            .create(test)
            .flatMap { saved -> service.delete(saved.id!!) }

        StepVerifier
            .create(deleted)
            .expectNextMatches { profile -> profile.email.equals(test, ignoreCase = true) }
            .verifyComplete()
    }

    @Test
    @Throws(Exception::class)
    fun update() {
        val saved = service
            .create("test")
            .flatMap { p: Profile -> service.update(p.id!!, "test1") }

        StepVerifier
            .create(saved)
            .expectNextMatches { p: Profile -> p.email.equals("test1", ignoreCase = true) }
            .verifyComplete()
    }

    @Test
    fun getById() {
        val test = UUID.randomUUID().toString()
        val deleted = service.create(test)
            .flatMap { saved -> service.get(saved.id!!) }

        StepVerifier
            .create(deleted)
            .expectNextMatches { profile ->
                StringUtils.hasText(profile.id) &&
                        test.equals(profile.email, ignoreCase = true)
            }
            .verifyComplete()
    }

}