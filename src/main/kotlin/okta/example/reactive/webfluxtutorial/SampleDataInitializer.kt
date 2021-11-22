package okta.example.reactive.webfluxtutorial

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.util.*

@Component
class SampleDataInitializer (val profileRepository: ProfileRepository) : ApplicationListener<ApplicationReadyEvent> {

    val log: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        profileRepository
            .deleteAll()
            .thenMany(Flux
                .just("A", "B", "C", "D")
                .map { name: String -> Profile(UUID.randomUUID().toString(), "$name@email.com")}
                .flatMap<Any>(profileRepository::save)
            )
            .thenMany(profileRepository.findAll())
            .subscribe { profile -> log.info("Saved user [${profile.id}, ${profile.email}]")};
    }
}