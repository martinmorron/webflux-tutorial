package okta.example.reactive.webfluxtutorial

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Profile (@Id var id: String?, var email:String) {

    override fun toString(): String {
        return "Profile(id='$id', email='$email')"
    }
}