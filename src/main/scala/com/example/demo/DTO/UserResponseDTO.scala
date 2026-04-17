package com.example.demo.DTO

import com.example.demo.Model.Enums.UserType

import java.time.LocalDate
import scala.compiletime.uninitialized

class UserResponseDTO {

  var id: Long = uninitialized
  var username: String = uninitialized
  var userType: UserType = uninitialized
  var employeeId: String = uninitialized
  var creditBalance: Int = uninitialized
  var department: String = uninitialized
  var joinedDate: LocalDate = uninitialized

  override def toString = s"UserResponseDTO($id, $username, $userType, $employeeId, $creditBalance, $department, $joinedDate)"
}
 
                     