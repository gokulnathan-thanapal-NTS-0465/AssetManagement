package com.example.demo.DTO

import com.example.demo.Model.Enums.Category

import scala.beans.BeanProperty




case class AssetCreationDTO(
                             modelName: Option[String] = None,
                             category: Option[Category] = None,
                           )