package com.example.demo.DTO

import jakarta.validation.constraints.{NotBlank, Pattern}

import scala.annotation.meta.field
import scala.beans.BeanProperty

case class LoginRequestDTO(

                            @BeanProperty
                            @(NotBlank @field)(message = "Username is required")
                            @(Pattern @field)(regexp = "^[a-zA-Z0-9]{6,12}$", message = "Username must be 6-12 alphanumeric characters")
                            username: String,

                            @BeanProperty
                            @(NotBlank @field)(message = "Password is required")
                            @(Pattern @field)(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$", message = "Password must be at least 6 characters and contain both letters and numbers")
                            password:String
                          )
