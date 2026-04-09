package com.example.demo.DTO

case class AssetAssignmentResponseDTO(
                                  id:Option[Long]=None,
                                  assetId:Option[Long]=None,
                                  userId:Option[Long]=None,
                                  assignedAt:Option[String]=None,
                                  returnedAt:Option[String]=None,
                                  
                                  )
