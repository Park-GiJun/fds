package com.gijun.fds.gateway.infrastructure.config

import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http
import org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

@Configuration
class RouteConfig {

    @Bean
    fun transactionRoute() = route("transaction-service")
        .route(path("/api/v1/transactions/**"), http())
        .before(uri(URI.create("http://localhost:8081")))
        .build()

    @Bean
    fun detectionRoute() = route("detection-service")
        .route(path("/api/v1/detections/**"), http())
        .before(uri(URI.create("http://localhost:8082")))
        .build()

    @Bean
    fun alertRoute() = route("alert-service")
        .route(path("/api/v1/alerts/**"), http())
        .before(uri(URI.create("http://localhost:8083")))
        .build()
}
