package com.example.demo.Service

import com.example.demo.DTO.{AssetAssignmentDTO, AssetRequestDTO, AssetRequestResponseDTO}
import com.example.demo.Mapper.{AssetAssignmentMapper, AssetRequestMapper}
import com.example.demo.Model.Enums.AssetStatus.AVAILABLE
import com.example.demo.Model.{Asset, AssetAssignment, AssetRequest, Credit, User}
import com.example.demo.Model.Enums.{AssetStatus, Category, RequestStatus}
import com.example.demo.Repo.{AssetAssignmentRepository, AssetRepository, AssetRequestRepository, UserRepository}
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import scala.jdk.CollectionConverters.*
import java.time.LocalDateTime


@Service
class AssetRequestService(userRepo: UserRepository, assetRequestRepo: AssetRequestRepository, assetRepo: AssetRepository, assetAssignmentRepo: AssetAssignmentRepository) {


  @Transactional
  def createRequest(request: AssetRequestDTO): AssetRequestResponseDTO = {
    val userId = request.userId.getOrElse(
      throw new IllegalArgumentException("User ID is required")
    )
    val user = userRepo.getUserById(userId.toLong)
    
    val requestedCategory: Category = request.category.getOrElse(
      throw new IllegalArgumentException("Category is required")
    )
    val requiredCredit: Int = Credit.creditRequirements.getOrElse(requestedCategory, throw new IllegalArgumentException("Invalid category"))
    
    if (user.creditBalance < requiredCredit) {
      throw new IllegalStateException("Insufficient credit balance")
    }
    var newRequest: AssetRequest = AssetRequestMapper.toEntity(request, user)
    newRequest = assetRequestRepo.save(newRequest)
    
    val newRequestResponse: AssetRequestResponseDTO = AssetRequestMapper.toAssetRequestResponseDTO(newRequest)
    newRequestResponse
  }

  @Transactional
  def acceptRequest(requestId: Long): AssetAssignmentDTO = {
    var assetRequest: AssetRequest = assetRequestRepo.findById(requestId).orElseThrow(()=>new EntityNotFoundException("Asset request not found"))
    if (assetRequest.status != RequestStatus.PENDING) {
      throw new IllegalStateException("Request is not pending")
    }
    var user: User = userRepo.findById(assetRequest.user.id).orElseThrow(()=> new EntityNotFoundException("User not found"))
    val requestedCategory: Category = assetRequest.category
    
    val requiredCredit: Int = Credit
      .creditRequirements
      .getOrElse(requestedCategory, throw new IllegalArgumentException("Invalid category"))

    if (user.creditBalance < requiredCredit) {
      throw new IllegalStateException("Insufficient credit balance")
    }
    var assetAssignment = new AssetAssignment
    var asset: Asset = assetRepo.findFirstByCategoryAndStatus(assetRequest.category, AVAILABLE)

    user.creditBalance = user.creditBalance - requiredCredit
    user = userRepo.save(user)

    assetAssignment.asset = asset
    assetAssignment.user = user
    assetAssignment.assignedAt = LocalDateTime.now()
    assetAssignment = assetAssignmentRepo.save(assetAssignment)

    assetRequest.status = RequestStatus.APPROVED
    assetRequest = assetRequestRepo.save(assetRequest)

    asset.status = AssetStatus.ASSIGNED
    asset = assetRepo.save(asset)

    val assetAssignmentDTO = AssetAssignmentMapper.toAssetAssignmentDTO(assetAssignment)
    assetAssignmentDTO
  }

  @Transactional
  def declineRequest(requestId: Long): Unit = {
    var assetRequest: AssetRequest = assetRequestRepo.findById(requestId).orElseThrow(() => new EntityNotFoundException("Asset request not found"))

    if (assetRequest.status != RequestStatus.PENDING) {
      throw new IllegalStateException("Request is not pending")
    }
    assetRequest.status = RequestStatus.REJECTED
    assetRequest = assetRequestRepo.save(assetRequest)
  }

  def getAllRequest(status: RequestStatus): List[AssetRequestResponseDTO] = {

    val assetRequests: List[AssetRequest] = {
      if (status != null) {
        assetRequestRepo.findAllByStatus(status).asScala.toList
      }
      else {
        assetRequestRepo.findAll().asScala.toList
      }
    }

    val assetRequestResponseDTOs: List[AssetRequestResponseDTO] = assetRequests.map(AssetRequestMapper.toAssetRequestResponseDTO)
    assetRequestResponseDTOs
  }

  def getRequestById(requestId: Long): AssetRequestResponseDTO = {
    val assetRequest: AssetRequest = assetRequestRepo.findById(requestId).orElseThrow(()=> new EntityNotFoundException("Asset request not found"))
    val assetRequestResponseDTO: AssetRequestResponseDTO = AssetRequestMapper.toAssetRequestResponseDTO(assetRequest)
    assetRequestResponseDTO
  }

  def getAllRequestsByUserId(userId: Long, status: RequestStatus): List[AssetRequestResponseDTO] = {

    val assetRequests: List[AssetRequest] = {

      if (status != null) {
        assetRequestRepo.findAllByUserIdAndStatus(userId, status).asScala.toList
      }
      else {
        assetRequestRepo.findAllByUserId(userId).asScala.toList}
    }
    val assetRequestResponseDTOs: List[AssetRequestResponseDTO] = assetRequests.map(AssetRequestMapper.toAssetRequestResponseDTO)
    assetRequestResponseDTOs
  }

  def canUserAccessRequest(requestId: Long, userId: Long): Boolean = {
    val requestOpt = assetRequestRepo.findById(requestId)
    if (requestOpt.isPresent) {
      requestOpt.get().user.id == userId
    } else {
      false
    }
  }

}