package okta.example.reactive.webfluxtutorial

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import org.springframework.web.reactive.socket.client.WebSocketClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import java.net.URI
import java.util.*
import java.util.concurrent.atomic.AtomicLong

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WebSocketConfigurationTest {

    private val socketClient: WebSocketClient = ReactorNettyWebSocketClient()
    private val webClient = WebClient.builder().build()

    @Test
    @Throws(Exception::class)
    fun testNotificationsOnUpdates() {
        val count = 10L
        val counter = AtomicLong()
        val uri: URI = URI.create("ws://localhost:8080/ws/profiles")
        socketClient.execute(uri) { session: WebSocketSession ->
            val out = Mono.just(session.textMessage("test"))
            val input = session
                .receive()
                .map { obj: WebSocketMessage -> obj.payloadAsText }

            session
                .send(out)
                .thenMany(input)
                .doOnNext { counter.incrementAndGet() }
                .then()

        }.subscribe()

        Flux.generate { sink: SynchronousSink<Profile> -> sink.next(generateRandomProfile()) }
            .take(count.toLong())
            .flatMap{ p -> write(p) }
            .blockLast()

        Thread.sleep(1000)
        Assertions.assertThat(counter.get()).isEqualTo(count)
    }

    private fun write(p: Profile): Publisher<Profile> {
        return webClient
            .post()
            .uri("http://localhost:8080/profiles")
            .body(BodyInserters.fromObject(p))
            .retrieve()
            .bodyToMono(String::class.java)
            .thenReturn(p)
    }

    private fun generateRandomProfile(): Profile {
        return Profile(UUID.randomUUID().toString(), UUID.randomUUID().toString() + "@email.com")
    }

}