package com.example.demo.DTO

import jakarta.validation.constraints.{NotBlank, Size}

import scala.beans.BeanProperty

case class ComplaintCreationDTO(

                                 @BeanProperty
                                 @NotBlank(message="User Id is required")
                                 userId: String ,

                                 @BeanProperty
                                 @NotBlank(message="Asset Id is required")
                                 assetId: String,

                                 @BeanProperty
                                 @Size(min= 10 , max=100 , message = "Description must be between 10 and 100 characters")
                                 @NotBlank(message="Description is required")
                                 description: String
                               )

