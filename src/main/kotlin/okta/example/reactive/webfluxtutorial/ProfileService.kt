package okta.example.reactive.webfluxtutorial

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*


@Component
class ProfileService(val publisher: ApplicationEventPublisher, val profileRepository: ProfileRepository) {

    fun all(): Flux<Profile> {
        return profileRepository.findAll()
    }

    fun get(id: String): Mono<Profile> {
        return profileRepository.findById(id)
    }

    fun update(id: String, email: String): Mono<Profile> {
        return profileRepository
            .findById(id)
            .map { p -> Profile(p.id, email) }
            .flatMap(profileRepository::save)
    }

    fun delete(id: String): Mono<Profile> {
        return profileRepository
            .findById(id)
            .flatMap { p -> profileRepository.deleteById(p.id!!).thenReturn(p) }
    }

    fun create(email: String): Mono<Profile> {
        return profileRepository
            .save(Profile(UUID.randomUUID().toString(), email))
            .doOnSuccess { profile -> publisher.publishEvent(ProfileCreatedEvent(profile)) }
    }
}