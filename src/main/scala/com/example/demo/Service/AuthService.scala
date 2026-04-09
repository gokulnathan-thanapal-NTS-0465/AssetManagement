package com.example.demo.Service

import com.example.demo.DTO.{LoginRequestDTO, LoginResponseDTO}
import com.example.demo.Repo.UserRepository
import com.example.demo.Util.JwtUtil
import jakarta.persistence.EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService @Autowired()(
                                userRepository: UserRepository,
                                passwordEncoder: PasswordEncoder,
                                jwtUtil: JwtUtil,
                                assetRequestService: AssetRequestService,
                                assetAssignmentService: AssetAssignmentService,
                                complaintService: ComplaintService
                              ) {

  def login(loginRequest: LoginRequestDTO): LoginResponseDTO = {
    val userOptional = userRepository.findByUsername(loginRequest.username.getOrElse(throw new IllegalArgumentException("Username is required")))
    
    
    if (userOptional.isPresent) {
      val user = userOptional.get()

      if (user.deactivated) {
        throw new IllegalStateException("User account is deactivated")
      }
      else if(loginRequest.password.isEmpty){
        throw new IllegalArgumentException("Password is required")
      }

      if (passwordEncoder.matches(loginRequest.password.get, user.passwordHash)) {
        val token = jwtUtil.generateToken(user.username, user.userType, user.id)
        LoginResponseDTO(
          token = token,
          username = user.username,
          userType = user.userType,
          userId = user.id
        )
      } else {
        throw new SecurityException("Invalid credentials")
      }
    } else {
      throw new EntityNotFoundException("User not found")
    }
  }
  
  def isCurrentUser(userId:Long) :Boolean={
    val authentication=SecurityContextHolder.getContext.getAuthentication
    if(authentication!=null && authentication.getDetails!=null){
      authentication.getDetails match {
        case details:Map[String,Any]=>
          details.get("userId").contains(userId)
        case _=>false
      }
    }
    else{
      false
    }
  }
  
  def canAccessRequest(requestId:Long):Boolean={
    getCurrentUserId match{
      case Some(userId) =>assetRequestService.canUserAccessRequest(requestId , userId )
      case None => false
    }
  }
  
  def canAccessReturn(assignmentId:Long):Boolean={
    getCurrentUserId match{
      case Some(userId) =>assetAssignmentService.canAccessReturn(assignmentId , userId )
      case None => false
    }
  }
  
  def canAccessComplaint(complaintId:Long):Boolean={
    getCurrentUserId match{
      case Some(userId) =>complaintService.canUserAccessComplaint(complaintId , userId )
      case None => false
    }
  }
  def canAccessAssignment(assignment:Long):Boolean={
    getCurrentUserId match{
      case Some(userId) => assetAssignmentService.canAccessAssetAssignment(assignment,userId)
      case None => false
    }
  }
  
  private def getCurrentUserId: Option[Long] = {
    val authentication = SecurityContextHolder.getContext.getAuthentication
    if (authentication != null && authentication.getDetails != null) {
      authentication.getDetails match {
        case details: Map[String, Any] =>
          details.get("userId").map(_.asInstanceOf[Long])
        case _ => None
      }
    } else {
      None
    }
  }
}
