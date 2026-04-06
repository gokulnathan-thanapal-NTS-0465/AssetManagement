package com.example.demo.DTO

case class ComplaintCreationDTO(
                                 userId: Option[Long] = None,
                                 assetId: Option[Long] = None,
                                 description: Option[String] = None
                               )

