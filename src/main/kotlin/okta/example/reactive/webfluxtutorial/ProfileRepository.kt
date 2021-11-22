package okta.example.reactive.webfluxtutorial

import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface ProfileRepository : ReactiveMongoRepository<Profile, String> {
}