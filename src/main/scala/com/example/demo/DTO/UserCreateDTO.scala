
package com.example.demo.DTO

import com.example.demo.Model.Enums.UserType
import com.fasterxml.jackson.annotation.{JsonInclude, JsonProperty}
import jakarta.validation.constraints.{NotBlank, NotNull, Pattern, Size}

import scala.annotation.meta.field
import scala.beans.BeanProperty


case class UserCreateDTO(

                          @BeanProperty
                          @(NotBlank @field)(message = "Username is required")
                          @(Size  @field)(min = 3 ,max = 50 ,message= "Username must be between 3 and 50 characters")
                          @(Pattern  @field)(regexp = "^[a-zA-Z0-9]{6,12}$", message = "Username must be 6-12 alphanumeric characters")
                          username: String ,

                          @BeanProperty
                          @(NotNull @field)(message = "UserType is required")
                          userType: UserType,

                          @BeanProperty
                          creditBalance: Option[Int] = None,

                          @BeanProperty
                          @(NotBlank @field)(message = "Department is required")
                          @(Pattern @field)(regexp = "^[a-zA-Z0-9]{6,12}$", message = "department must be 6-12 alphanumeric characters")
                          department: String,

                          @BeanProperty
                          @(NotBlank @field)(message = "Password is required")
                          @(Size @field)(min = 6, message = "Password must be at least 6 characters")
                          @(Pattern @field)(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$", message = "Password must be at least 6 characters and contain both letters and numbers")
                          passwordHash: String
                        )