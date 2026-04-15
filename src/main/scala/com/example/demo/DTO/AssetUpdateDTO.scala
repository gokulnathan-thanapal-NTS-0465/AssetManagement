package com.example.demo.DTO

import com.example.demo.Model.Enums.AssetStatus
import jakarta.validation.constraints.{NotBlank, Size}

import scala.beans.BeanProperty


case class AssetUpdateDTO(
                           @BeanProperty
                           @NotBlank(message = "Model name is required")
                           @Size(min = 2, max = 100, message = "Model name must be between 2 and 100 characters")
                           modelName: Option[String] = None
                         )