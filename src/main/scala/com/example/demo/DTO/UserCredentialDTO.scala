package com.example.demo.DTO

import jakarta.validation.constraints.{Pattern, Size}

import scala.beans.BeanProperty

case class UserCredentialDTO(
                              @BeanProperty
                              @Pattern(regexp = "^[a-zA-Z0-9]{6,12}$", message = "Username must be 6-12 alphanumeric characters")
                              username:Option[String]=None,

                              @BeanProperty
                              @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$", message = "Password must be at least 6 characters and contain both letters and numbers")
                              passwordHash:Option[String]=None
                             )
