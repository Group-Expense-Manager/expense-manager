package pl.edu.agh.gem.external.controller

import org.springframework.core.Ordered.LOWEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.ControllerAdvice

@ControllerAdvice
@Order(LOWEST_PRECEDENCE)
class ApiExceptionHandler
