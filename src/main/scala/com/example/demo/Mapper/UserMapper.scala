package com.example.demo.Mapper

import com.example.demo.DTO.{UserCreateDTO, UserResponseDTO}
import com.example.demo.Model.Enums.UserType
import com.example.demo.Model.User


import java.time.LocalDate

object UserMapper {

  def toEntity(dto: UserCreateDTO): User = {
    if(dto.username.isEmpty){
      throw new IllegalArgumentException("Username is required")
    }
    val user = new User
    user.username = dto.username
    user.userType = dto.userType
    user.department = dto.department
    user.passwordHash = dto.passwordHash
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