package com.example.demo.Controller

import com.example.demo.DTO.{UserCreateDTO, UserCredentialDTO, UserDepartmentDTO, UserResponseDTO}
import com.example.demo.Model.Enums.UserType
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{CrossOrigin, DeleteMapping, GetMapping, PatchMapping, PathVariable, PostMapping, RequestBody, RequestMapping, RequestParam, RestController}
import com.example.demo.Model.User
import com.example.demo.Service.UserService
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated


@RestController
@RequestMapping(value = Array("/api/user"))
@CrossOrigin(origins = Array("*"))
@Validated
class UserController @Autowired(userService: UserService) {

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(value = Array("/create"))
  def createUser(@Valid @RequestBody user: UserCreateDTO): ResponseEntity[UserResponseDTO] = {
    val newUser: UserResponseDTO = userService.createUser(user)
    new ResponseEntity[UserResponseDTO](newUser, HttpStatus.CREATED)
  }

  @GetMapping(value = Array("/{userId}"))
  @PreAuthorize("hasRole('ADMIN') or hasRole('TECH') or (hasRole('EMPLOYEE') and @authService.isCurrentUser(#userId))")
  def getUserById(@PathVariable userId: Long): ResponseEntity[UserResponseDTO] = {
    val user: UserResponseDTO = userService.getUserById(userId)
    new ResponseEntity[UserResponseDTO](user, HttpStatus.OK)
  }

  @DeleteMapping(value = Array("/{userId}"))
  @PreAuthorize("hasRole('ADMIN') or (hasRole('EMPLOYEE') and @authService.isCurrentUser(#userId))")
  def deleteUserById(@PathVariable userId: Long): ResponseEntity[String] = {
    userService.deactivateUserById(userId)
    new ResponseEntity[String]("User deactivated Successfully", HttpStatus.OK)
  }

  @PatchMapping(value = Array("/{userId}/credentials"))
  @PreAuthorize("@authService.isCurrentUser(#userId)")
  def updateUserCredentialById(@Valid @RequestBody userCredentialDTO: UserCredentialDTO, @PathVariable userId: Long): ResponseEntity[UserResponseDTO] = {
    val updatedUser: UserResponseDTO = userService.updateUserCredentialById(userId, userCredentialDTO)
    new ResponseEntity[UserResponseDTO](updatedUser, HttpStatus.OK)
  }

  @PatchMapping(value=Array("/{userId}/department"))
  @PreAuthorize("hasRole('ADMIN')")
  def updateUserDepartmentById(@Valid @RequestBody userDepartmentDTO:UserDepartmentDTO, @PathVariable userId: Long): ResponseEntity[UserResponseDTO] = {
    val updatedUser: UserResponseDTO = userService.updateUserDepartmentById(userId, userDepartmentDTO)
    new ResponseEntity[UserResponseDTO](updatedUser, HttpStatus.OK)
  }

  @GetMapping(value = Array("/all"))
  @PreAuthorize("hasRole('ADMIN')")
  def getAllUsers(@RequestParam(required = false) userType:UserType,@RequestParam(required = false) deactivated:java.lang.Boolean):ResponseEntity[List[UserResponseDTO]]={
    val userTypeOpt:Option[UserType]=Option(userType)
    val deactivatedOpt:Option[Boolean]=Option(deactivated).map(_.booleanValue())
    val userResponseDTOs:List[UserResponseDTO]=userService.getAllUsers(userTypeOpt,deactivatedOpt)
    new ResponseEntity[List[UserResponseDTO]](userResponseDTOs,HttpStatus.OK)
  }

  @PatchMapping(value=Array("/credit/{userId}"))
  @PreAuthorize("hasRole('ADMIN')")
  def updateCreditBalance(@PathVariable userId:Long,@Valid @RequestBody newBalance:Int):ResponseEntity[UserResponseDTO]={
    val updateUser:UserResponseDTO=userService.updateCreditBalance(userId,newBalance)
    new ResponseEntity[UserResponseDTO](updateUser,HttpStatus.OK)
  }
}
