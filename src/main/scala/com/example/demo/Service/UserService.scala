package com.example.demo.Service

import com.example.demo.DTO.{UserCreateDTO, UserCredentialDTO, UserDepartmentDTO, UserResponseDTO, UserUpdateDTO}
import com.example.demo.Mapper.UserMapper
import com.example.demo.Model.Enums.{AssetStatus, ComplaintStatus, RequestStatus, UserType}
import org.springframework.stereotype.Service
import com.example.demo.Model.{Asset, AssetAssignment, AssetRequest, Complaint, User}
import com.example.demo.Repo.{AssetAssignmentRepository, AssetRepository, AssetRequestRepository, ComplaintRepository, UserRepository}
import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional

import java.time.LocalDateTime
import scala.jdk.CollectionConverters.*


@Service
class UserService @Autowired(userRepo: UserRepository, passwordEncoder: PasswordEncoder, assetAssignmentRepo: AssetAssignmentRepository, complaintRepo: ComplaintRepository, assetRequestRepo: AssetRequestRepository, assetRepo: AssetRepository) {

  @Transactional
  def createUser(dto: UserCreateDTO): UserResponseDTO = {

    val userName: String = dto.username.getOrElse(throw new IllegalArgumentException("Username is required"))
    if(userRepo.findAllByUsername(userName).size()>0){
      throw new IllegalStateException("Username already exists")
    }
    var user = UserMapper.toEntity(dto)
    val generatedUserId: String = generateUserId(user.userType)
    val passwordEncoded = passwordEncoder.encode(dto.passwordHash.getOrElse(throw new IllegalArgumentException("Password required")))
    user.employeeId = generatedUserId
    user.passwordHash = passwordEncoded
    user = userRepo.save(user)

    val userResponse: UserResponseDTO = UserMapper.toResponseDTO(user)
    userResponse

  }


  def getUserById(userId: Long): UserResponseDTO = {

    val user: User = userRepo.findById(userId).orElseThrow(() => new EntityNotFoundException("User not found"))
    val userResponseDTO: UserResponseDTO = UserMapper.toResponseDTO(user)
    userResponseDTO

  }

  @Transactional
  def deactivateUserById(userId: Long): Unit = {

    val user: User = userRepo.findById(userId).orElseThrow(() => new EntityNotFoundException("User not found"))
    if (user.deactivated) {
      return
    }

    val assetAssignments: List[AssetAssignment] = assetAssignmentRepo.findAllByUserIdAndReturnedAtIsNull(userId).asScala.toList
    assetAssignments.foreach(assetAssignment => {
      assetAssignment.returnedAt = LocalDateTime.now()
      if (assetAssignment.asset.status == AssetStatus.ASSIGNED) {
        val asset: Asset = assetAssignment.asset
        asset.status = AssetStatus.AVAILABLE
        assetRepo.save(asset)
      }
      assetAssignmentRepo.save(assetAssignment)
    })

    val complaints: List[Complaint] = complaintRepo.findByUserId(userId).asScala.toList
    complaints.foreach(complaint => {
      if (complaint.status != ComplaintStatus.RESOLVED) {
        complaint.status = ComplaintStatus.WITHDRAWN
        complaintRepo.save(complaint)
      }
    })

    val assetRequests: List[AssetRequest] = assetRequestRepo.findAllByUserId(userId).asScala.toList
    assetRequests.foreach(assetRequest => {
      if (assetRequest.status == RequestStatus.PENDING) {
        assetRequest.status = RequestStatus.CANCELLED
        assetRequestRepo.save(assetRequest)
      }
    })
    user.deactivated = true
    userRepo.save(user)
  }


  @Transactional
  def updateUserCredentialById(userId: Long, dto: UserCredentialDTO): UserResponseDTO = {
    var existingUser:User = userRepo.findById(userId).orElseThrow(() => new EntityNotFoundException("User not found"))
    if(existingUser.deactivated){
      throw new IllegalStateException("Cannot update credentials for a deactivated user")
    }
    if (dto.passwordHash.isDefined) {
      existingUser.passwordHash = passwordEncoder.encode(dto.passwordHash.get)
    }
    if (dto.username.isDefined && dto.username.get != existingUser.username) {
      if (userRepo.findAllByUsername(dto.username.get).size() >0) {
        throw new IllegalStateException("Username already exists")
      }
      existingUser.username = dto.username.get
    }
    else if(dto.username.isDefined && dto.username.get==existingUser.username){
      throw new IllegalArgumentException("New username cannot be the same as the current username")
    }
    existingUser = userRepo.save(existingUser)
    UserMapper.toResponseDTO(existingUser)
  }

  @Transactional
  def updateUserDepartmentById(userId:Long,dto:UserDepartmentDTO): UserResponseDTO = {
    var existingUser:User=userRepo.findById(userId).orElseThrow(() => new EntityNotFoundException("User not found"))
    if(existingUser.deactivated){
      throw new IllegalStateException("Cannot update department for a deactivated user")
    }
    if(dto.department.isDefined && dto.department.get!=null&&dto.department.get!=existingUser.department){
      existingUser.department=dto.department.get
    }
    else if(dto.department.isDefined && dto.department.get==existingUser.department){
      throw new IllegalArgumentException("New department cannot be the same as the current department")
    }
    existingUser=userRepo.save(existingUser)
    UserMapper.toResponseDTO(existingUser)
  }

  def getAllUsers(userTypeOpt: Option[UserType], deactivatedOpt: Option[Boolean]): List[UserResponseDTO] ={
    val users = (userTypeOpt, deactivatedOpt) match {
      case (Some(userType), Some(true))  =>
        userRepo.findAllByUserTypeAndDeactivatedIsTrue(userType, true).asScala.toList

      case (Some(userType), Some(false)) =>
        userRepo.findAllByUserTypeAndDeactivatedIsFalse(userType).asScala.toList

      case (Some(userType), None) =>
        userRepo.findAllByUserType(userType).asScala.toList

      case (None, Some(true))  =>
        userRepo.findAllByDeactivatedIsTrue().asScala.toList

      case (None, Some(false)) =>
        userRepo.findAllByDeactivatedIsFalse().asScala.toList
      case (None, None) =>
        userRepo.findAll().asScala.toList
    }

    users.map(UserMapper.toResponseDTO)
  }


  @Transactional
  def updateCreditBalance(userId: Long, newBalance: Int): UserResponseDTO = {
    if (newBalance < 0) {
      throw new IllegalArgumentException("Credit balance cannot be negative")
    }
    var existingUser = userRepo.findById(userId).orElseThrow(() => new EntityNotFoundException("User not found"))
    if (existingUser.deactivated) {
      throw new IllegalStateException("Cannot update credit balance for a deactivated user")
    }
    existingUser.creditBalance = newBalance
    existingUser = userRepo.save(existingUser)
    val userResponseDTO: UserResponseDTO = UserMapper.toResponseDTO(existingUser)
    userResponseDTO
  }

  private def generateUserId(userType: UserType): String = {
    val prefix = userType match {
      case UserType.EMPLOYEE => "EM"
      case UserType.ADMIN => "AD"
      case UserType.TECH => "TE"
    }
    val count = userRepo.countByUserType(userType)
    val nextId = count + 1

    val formattedId = f"$nextId%04d"
    prefix + formattedId
  }
}