package com.example.demo.DTO

import com.example.demo.Model.Enums.AssetStatus
import jakarta.validation.constraints.NotNull

import scala.beans.BeanProperty

case class AssetStatusDTO(
                           @BeanProperty
                           @NotNull(message = "Status is required")
                           status: Option[AssetStatus] = None
                         ) 