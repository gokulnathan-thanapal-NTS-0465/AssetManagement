package com.example.demo.DTO

case class UserCredentialDTO(
                               username:Option[String]=None,
                               passwordHash:Option[String]=None
                             )
