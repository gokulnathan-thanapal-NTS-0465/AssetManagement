
package com.example.demo.DTO

import com.example.demo.Model.Enums.UserType
import com.fasterxml.jackson.annotation.{JsonInclude, JsonProperty}

import scala.beans.BeanProperty


case class UserCreateDTO(
                          username: Option[String] = None,
                          userType: Option[UserType] = None,
                          creditBalance: Option[Int] = None,
                          department: Option[String] = None,
                          passwordHash: Option[String] = None
                        )