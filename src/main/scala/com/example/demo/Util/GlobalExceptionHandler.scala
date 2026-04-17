package com.example.demo.Util

import jakarta.persistence.EntityNotFoundException
import org.springframework.dao.{DataAccessException, DataIntegrityViolationException}
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.{ControllerAdvice, ExceptionHandler}
import org.springframework.web.bind.{MethodArgumentNotValidException, MissingServletRequestParameterException}
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

import scala.jdk.CollectionConverters.*
import java.time.LocalDateTime
import scala.compiletime.uninitialized

class ErrorResponse {
   var timestamp: LocalDateTime = uninitialized
   var status: Int = uninitialized
   var error: String = uninitialized
   var message: String = uninitialized
}

object ErrorResponse {
  def apply(status: Int, error: String, message: String): ErrorResponse = {
    val response = new ErrorResponse
    response.timestamp = LocalDateTime.now()
    response.status = status
    response.error = error
    response.message = message
    response
  }
}

@ControllerAdvice
class GlobalExceptionHandler {


  @ExceptionHandler(Array(classOf[IllegalArgumentException]))
  def handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity[ErrorResponse] = {
    val errorResponse = ErrorResponse(
      HttpStatus.BAD_REQUEST.value(),
      "Bad Request",
      ex.getMessage
    )
    new ResponseEntity[ErrorResponse](errorResponse, HttpStatus.BAD_REQUEST)
  }


  @ExceptionHandler(Array(classOf[MissingServletRequestParameterException]))
  def handleMissingServletRequestParameterException(ex: MissingServletRequestParameterException): ResponseEntity[ErrorResponse] = {
    val errorResponse = ErrorResponse(
      HttpStatus.BAD_REQUEST.value(),
      "Bad Request",
      s"Required parameter '${ex.getParameterName}' is missing"
    )
    new ResponseEntity[ErrorResponse](errorResponse, HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(Array(classOf[MethodArgumentTypeMismatchException]))
  def handleMethodArgumentTypeMismatchException(ex: MethodArgumentTypeMismatchException): ResponseEntity[ErrorResponse] = {
    val errorResponse = ErrorResponse(
      HttpStatus.BAD_REQUEST.value(),
      "Bad Request",
      s"Invalid value '${ex.getValue}' for parameter '${ex.getName}'"
    )
    new ResponseEntity[ErrorResponse](errorResponse, HttpStatus.BAD_REQUEST)
  }


  @ExceptionHandler(Array(classOf[EntityNotFoundException]))
  def handleEntityNotFoundException(ex: EntityNotFoundException): ResponseEntity[ErrorResponse] = {
    val errorResponse = ErrorResponse(
      HttpStatus.NOT_FOUND.value(),
      "Not Found",
      ex.getMessage
    )
    new ResponseEntity[ErrorResponse](errorResponse, HttpStatus.NOT_FOUND)
  }
  
  @ExceptionHandler(Array(classOf[NoSuchElementException]))
  def handleNoSuchElementException(ex: NoSuchElementException): ResponseEntity[ErrorResponse] = {
    val errorResponse = ErrorResponse(
      HttpStatus.NOT_FOUND.value(),
      "Not Found",
      "Requested resource not found"
    )
    new ResponseEntity[ErrorResponse](errorResponse, HttpStatus.NOT_FOUND)
  }

  @ExceptionHandler(Array(classOf[SecurityException]))
  def handleSecurityException(ex: SecurityException): ResponseEntity[ErrorResponse] = {
    val errorResponse = ErrorResponse(
      HttpStatus.FORBIDDEN.value(),
      "Forbidden",
      ex.getMessage
    )
    new ResponseEntity[ErrorResponse](errorResponse, HttpStatus.FORBIDDEN)
  }

  @ExceptionHandler(Array(classOf[AccessDeniedException]))
  def handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity[ErrorResponse] = {
    val errorResponse = ErrorResponse(
      HttpStatus.FORBIDDEN.value(),
      "Forbidden",
      "You don't have permission to access this resource"
    )
    new ResponseEntity[ErrorResponse](errorResponse, HttpStatus.FORBIDDEN)
  }

  @ExceptionHandler(Array(classOf[IllegalStateException]))
  def handleIllegalStateException(ex: IllegalStateException): ResponseEntity[ErrorResponse] = {
    val errorResponse = ErrorResponse(
      HttpStatus.BAD_REQUEST.value(),
      "Unprocessable Entity",
      ex.getMessage
    )
    new ResponseEntity[ErrorResponse](errorResponse, HttpStatus.BAD_REQUEST)
  }


  @ExceptionHandler(Array(classOf[DataIntegrityViolationException]))
  def handleDataIntegrityViolationException(ex: DataIntegrityViolationException): ResponseEntity[ErrorResponse] = {
    
    val errorResponse = ErrorResponse(
      HttpStatus.CONFLICT.value(),
      "Conflict",
      ex.getMessage
    )
    new ResponseEntity[ErrorResponse](errorResponse, HttpStatus.CONFLICT)
  }
  
  @ExceptionHandler(Array(classOf[DataAccessException]))
  def handleDataAccessException(ex: DataAccessException): ResponseEntity[ErrorResponse] = {
    ex.printStackTrace()
    val errorResponse = ErrorResponse(
      HttpStatus.INTERNAL_SERVER_ERROR.value(),
      "Database Error",
      "An error occurred while accessing the database"
    )
    new ResponseEntity[ErrorResponse](errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
  }
  
  @ExceptionHandler(Array(classOf[Exception]))
  def handleGenericException(ex: Exception): ResponseEntity[ErrorResponse] = {
    ex.printStackTrace()
    val errorResponse = ErrorResponse(
      HttpStatus.INTERNAL_SERVER_ERROR.value(),
      "Internal Server Error",
      "An unexpected error occurred"
    )
    new ResponseEntity[ErrorResponse](errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
  }


  @ExceptionHandler(Array(classOf[MethodArgumentNotValidException]))
  def handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity[Map[String, Any]] = {
    val errors: Map[String, String] = ex.getBindingResult
      .getAllErrors
      .asScala
      .map { error =>
        val fieldName = error match {
          case fieldError: FieldError => fieldError.getField
          case _ => error.getObjectName
        }
        fieldName -> error.getDefaultMessage
      }
      .toMap

    val response = Map(
      "status" -> "error",
      "message" -> "Validation failed",
      "errors" -> errors
    )
    new ResponseEntity[Map[String, Any]](response, HttpStatus.BAD_REQUEST)
  }
}
