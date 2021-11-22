package okta.example.reactive.webfluxtutorial

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import reactor.core.publisher.Flux
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Configuration
class WebSocketConfiguration {

    val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun executor(): Executor? {
        return Executors.newSingleThreadExecutor()
    }

    @Bean
    fun handlerMapping(wsh: WebSocketHandler): HandlerMapping {
        return object : SimpleUrlHandlerMapping() {
            init {
                urlMap = Collections.singletonMap("/ws/profiles", wsh)
                order = 10
            }
        }
    }

    @Bean
    fun webSocketHandlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }

    @Bean
    fun webSocketHandler(
        objectMapper: ObjectMapper,
        eventPublisher: ProfileCreatedEventPublisher
    ): WebSocketHandler? {
        val publish: Flux<ProfileCreatedEvent> = Flux
            .create(eventPublisher)
            .share()
        return WebSocketHandler { session: WebSocketSession ->
            val messageFlux = publish
                .map { evt: ProfileCreatedEvent ->
                    try {
                        return@map objectMapper.writeValueAsString(evt.source)
                    } catch (e: JsonProcessingException) {
                        throw RuntimeException(e)
                    }
                }
                .map { str: String ->
                    log.info("sending $str")
                    session.textMessage(str)
                }
            session.send(messageFlux)
        }
    }

}