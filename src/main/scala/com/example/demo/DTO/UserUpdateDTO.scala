package com.example.demo.DTO

case class UserUpdateDTO(
                          username: Option[String] = None,
                          department: Option[String] = None,
                          passwordHash: Option[String] = None
                        )

