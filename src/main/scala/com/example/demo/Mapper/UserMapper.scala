package com.example.demo.Mapper

import com.example.demo.DTO.{UserCreateDTO, UserResponseDTO, UserUpdateDTO}
import com.example.demo.Model.Enums.UserType
import com.example.demo.Model.User


import java.time.LocalDate

object UserMapper {

  def toEntity(dto: UserCreateDTO): User = {
    val user = new User
    user.username = dto.username.getOrElse(
      throw new IllegalArgumentException("Username required")
    )
    user.userType = dto.userType.getOrElse(UserType.EMPLOYEE)
    user.department = dto.department.getOrElse(
      throw new IllegalArgumentException("Department required")
    )
    user.passwordHash = dto.passwordHash.getOrElse(
      throw new IllegalArgumentException("Password required")
    )
    user.creditBalance = dto.creditBalance.getOrElse(0)
    user.joinedDate = LocalDate.now()
    user
  }
  
  def toResponseDTO(userData: User): UserResponseDTO = {
    val userResponseDTO: UserResponseDTO = new UserResponseDTO
    userResponseDTO.id = userData.id
    userResponseDTO.username = userData.username
    userResponseDTO.userType = userData.userType
    userResponseDTO.employeeId = userData.employeeId
    userResponseDTO.creditBalance = userData.creditBalance
    userResponseDTO.department = userData.department
    userResponseDTO.joinedDate = userData.joinedDate
    userResponseDTO
  }

}