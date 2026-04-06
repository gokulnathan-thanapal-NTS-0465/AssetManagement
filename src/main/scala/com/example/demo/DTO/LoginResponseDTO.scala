package com.example.demo.DTO

import com.example.demo.Model.Enums.UserType

case class LoginResponseDTO(
                             token: String,
                             username: String,
                             userType: UserType,
                             userId: Long
                           )
