package com.example.demo.DTO

import com.example.demo.Model.Enums.UserType
import java.time.LocalDate

class UserResponseDTO {

  var id: Long = _
  var username: String = _
  var userType: UserType = _
  var employeeId: String = _
  var creditBalance: Int = _
  var department: String = _
  var joinedDate: LocalDate = _

  override def toString = s"UserResponseDTO($id, $username, $userType, $employeeId, $creditBalance, $department, $joinedDate)"
}
 
                     