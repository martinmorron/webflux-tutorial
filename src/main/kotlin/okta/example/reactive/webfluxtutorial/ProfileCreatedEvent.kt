package okta.example.reactive.webfluxtutorial

import org.springframework.context.ApplicationEvent

class ProfileCreatedEvent(profile: Profile) : ApplicationEvent(profile) {

    override fun toString(): String {
        return "ProfileCreatedEvent() ${super.toString()}"
    }
}
