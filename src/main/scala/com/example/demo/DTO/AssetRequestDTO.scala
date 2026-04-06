package com.example.demo.DTO

import com.example.demo.Model.Enums.Category

case class AssetRequestDTO(
                            userId: Option[String] = None,
                            category: Option[Category] = None,
                            reason: Option[String] = None
                          )