package okta.example.reactive.webfluxtutorial

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RequestPredicates.*
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration
class ProfileEndpointConfiguration {

    @Bean
    fun router(handler :ProfileHandler) : RouterFunction<ServerResponse> {
        return RouterFunctions
            .route(GET("/profiles"), handler::all)
            .andRoute(GET("/profiles/{id}"), handler::getById)
            .andRoute(DELETE("/profiles/{id}"), handler::deleteById)
            .andRoute(POST("/profiles"), handler::create)
            .andRoute(PUT("/profiles/{id}"), handler::updateById)
    }
}