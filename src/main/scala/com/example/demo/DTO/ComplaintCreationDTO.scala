package com.example.demo.DTO

case class ComplaintCreationDTO(
                                 userId: Option[String] = None,
                                 assetId: Option[String] = None,
                                 description: Option[String] = None
                               )

