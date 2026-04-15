package com.example.demo.DTO

import com.example.demo.Model.Enums.Category
import jakarta.validation.constraints.{NotBlank, NotNull, Size}

import scala.beans.BeanProperty

case class AssetRequestDTO(
                            @BeanProperty
                            @NotBlank(message = "User Id is required")
                            userId: Option[String] = None,

                            @BeanProperty
                            @NotNull(message = "Category is required")
                            category: Option[Category] = None,

                            @BeanProperty
                            @NotBlank(message = "Reason is required")
                            @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
                            reason: Option[String] = None
                          )