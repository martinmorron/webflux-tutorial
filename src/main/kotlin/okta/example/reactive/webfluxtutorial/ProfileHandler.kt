package okta.example.reactive.webfluxtutorial

import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI


@Component
class ProfileHandler(var service: ProfileService) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    fun all(request: ServerRequest): Mono<ServerResponse> {
        return defaultReadResponse(service.all());
    }

    fun getById(request: ServerRequest): Mono<ServerResponse> {
        return defaultReadResponse(service.get(id(request)))
    }

    fun deleteById(request: ServerRequest): Mono<ServerResponse> {
        return defaultReadResponse(service.delete(id(request)))
    }

    fun create(request: ServerRequest): Mono<ServerResponse> {
        val flux = request.bodyToFlux(Profile::class.java)
            .flatMap { profile ->
                log.info("Creating user [${profile.id}, ${profile.email}]")
                service.create(profile.email)
            }
        return defaultWriteResponse(flux);
    }

    fun updateById(request: ServerRequest): Mono<ServerResponse> {
        val flux: Flux<Profile> = request.bodyToFlux(Profile::class.java)
            .flatMap { p -> service.update(id(request), p.email) }
        return defaultReadResponse(flux)
    }

    private fun id(r: ServerRequest): String {
        return r.pathVariable("id")
    }

    private fun defaultWriteResponse(profiles: Publisher<Profile>): Mono<ServerResponse> {
        return Mono
            .from(profiles)
            .flatMap { p ->
                ServerResponse
                    .created(URI.create("/profiles/" + p.id))
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .build()
            }
    }

    private fun defaultReadResponse(profiles: Publisher<Profile>): Mono<ServerResponse> {
        return ServerResponse
            .ok()
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(profiles, Profile::class.java)
    }

}
