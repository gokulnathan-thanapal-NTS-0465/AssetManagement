package com.example.demo.DTO

import jakarta.validation.constraints.{NotBlank, Pattern}

import scala.beans.BeanProperty

case class UserDepartmentDTO(
                              @BeanProperty
                              @NotBlank(message = "Department is required")
                              @Pattern(regexp = "^[a-zA-Z0-9]{6,12}$", message = "department must be 6-12 alphanumeric characters")
                              department: Option[String]
                            )
