package com.example.demo.Repo

import org.springframework.data.jpa.repository.JpaRepository
import com.example.demo.Model.*
import com.example.demo.Model.Enums.UserType
import org.springframework.stereotype.Repository

import java.util.Optional

@Repository
trait UserRepository extends JpaRepository[User, Long] {

  def getUserById(userId: Long): User

  def countByUserType(userType: UserType): Long
  
  def findAllByUserType(userType:UserType): java.util.List[User]
  
  def findAllByUserTypeAndDeactivatedIsTrue(userType:UserType,deactivated:Boolean): java.util.List[User]
  
  def findAllByUserTypeAndDeactivatedIsFalse(userType:UserType): java.util.List[User]
  
  def findAllByUsername(username:String): java.util.List[User]
  
  def findAllByDeactivatedIsTrue(): java.util.List[User]
  
  def findAllByDeactivatedIsFalse():java.util.List[User]
  
  def findByUsername(username:String):Optional[User]
}