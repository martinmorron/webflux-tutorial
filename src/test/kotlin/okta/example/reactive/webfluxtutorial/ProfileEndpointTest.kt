package okta.example.reactive.webfluxtutorial

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*


@WebFluxTest
@Import(
    value = [ProfileEndpointConfiguration::class,
        ProfileHandler::class, ProfileService::class]
)
class ProfileEndpointTest {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @MockBean
    private lateinit var repository: ProfileRepository
    @Autowired
    private lateinit var client: WebTestClient

    @Test
    fun getAll() {
        log.info("running  " + this.javaClass.name)
        Mockito
            .`when`(repository.findAll())
            .thenReturn(Flux.just(Profile("1", "A"), Profile("2", "B")))

        client
            .get()
            .uri("/profiles")
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
            .expectBody()
            .jsonPath("$.[0].id").isEqualTo("1")
            .jsonPath("$.[0].email").isEqualTo("A")
            .jsonPath("$.[1].id").isEqualTo("2")
            .jsonPath("$.[1].email").isEqualTo("B")
    }

    @Test
    fun save() {
        val data = Profile("123", UUID.randomUUID().toString() + "@email.com")
        Mockito
            .`when`(
                repository.save(
                    Mockito.any(
                        Profile::class.java
                    )
                )
            )
            .thenReturn(Mono.just(data))
        val jsonUtf8 = MediaType.APPLICATION_JSON_UTF8
        client
            .post()
            .uri("/profiles")
            .contentType(jsonUtf8)
            .body(
                Mono.just(data),
                Profile::class.java
            )
            .exchange()
            .expectStatus().isCreated
            .expectHeader().contentType(jsonUtf8)
    }

    @Test
    fun delete() {
        val data = Profile("123", UUID.randomUUID().toString() + "@email.com")
        Mockito
            .`when`(repository.findById(data.id!!))
            .thenReturn(Mono.just(data))
        Mockito
            .`when`(repository.deleteById(data.id!!))
            .thenReturn(Mono.empty())
        client
            .delete()
            .uri("/profiles/" + data.id)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @Disabled
    fun update() {
        val data = Profile("345", "test@email.com")
        Mockito
            .`when`(repository.findById(data.id!!))
            .thenReturn(Mono.just(data))
        Mockito
            .`when`(repository.save(data))
            .thenReturn(Mono.just(data))
        client
            .put()
            .uri("/profiles/" + data.id)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(
                Mono.just(data),
                Profile::class.java
            )
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun getById() {
        val data = Profile("1", "A")
        Mockito
            .`when`(repository.findById(data.id!!))
            .thenReturn(Mono.just(data))
        client
            .get()
            .uri("/profiles/" + data.id)
            .accept(MediaType.APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
            .expectBody()
            .jsonPath("$.id").isEqualTo(data.id!!)
            .jsonPath("$.email").isEqualTo(data.email)
    }

}