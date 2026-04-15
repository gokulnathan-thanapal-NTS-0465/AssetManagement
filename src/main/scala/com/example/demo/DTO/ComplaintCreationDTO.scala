package com.example.demo.DTO

import jakarta.validation.constraints.{NotBlank, Size}

import scala.beans.BeanProperty

case class ComplaintCreationDTO(

                                 @BeanProperty
                                 @NotBlank(message="User Id is required")
                                 userId: Option[String] = None,

                                 @BeanProperty
                                 @NotBlank(message="Asset Id is required")
                                 assetId: Option[String] = None,

                                 @BeanProperty
                                 @Size(min= 10 , max=100 , message = "Description must be between 10 and 100 characters")
                                 description: Option[String] = None
                               )

