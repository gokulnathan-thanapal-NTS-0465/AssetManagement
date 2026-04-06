package com.example.demo.DTO

import com.example.demo.Model.Enums.AssetStatus

import scala.beans.BeanProperty




case class AssetUpdateDTO(
                           modelName: Option[String] = None
                         )