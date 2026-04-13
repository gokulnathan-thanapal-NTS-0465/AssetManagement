package com.example.demo.Service

import com.example.demo.DTO.AssetAssignmentResponseDTO
import com.example.demo.Mapper.AssetAssignmentMapper
import com.example.demo.Model.Enums.AssetStatus
import com.example.demo.Model.{Asset, AssetAssignment, User}
import com.example.demo.Repo.{AssetAssignmentRepository, AssetRepository, UserRepository}
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import scala.jdk.CollectionConverters.*
import java.time.LocalDateTime


@Service
class AssetAssignmentService(assetAssignmentRepo: AssetAssignmentRepository, assetRepo: AssetRepository , userRepo: UserRepository) {

  @Transactional
  def returnAsset(assignmentId: Long): AssetAssignmentResponseDTO = {

    var assetAssignment: AssetAssignment = assetAssignmentRepo.findById(assignmentId).orElseThrow(()=> new EntityNotFoundException("Asset assignment not found"))
    if (assetAssignment.returnedAt != null) {
      throw new IllegalStateException(s"Asset with id ${assetAssignment.asset.id} has already been returned")
    }
    assetAssignment.returnedAt = LocalDateTime.now()
    var asset: Asset = assetAssignment.asset
    if(asset.status!=AssetStatus.MAINTENANCE){
      asset.status = AssetStatus.AVAILABLE
    }

    asset = assetRepo.save(asset)

    assetAssignment = assetAssignmentRepo.save(assetAssignment)
    val assetAssignmentResponseDTO=AssetAssignmentMapper.toAssetAssignmentResponseDTO(assetAssignment)
    assetAssignmentResponseDTO
  }

  def canAccessReturn(assignmentId:Long,userId:Long):Boolean={
    val assetAssignmentOptional=assetAssignmentRepo.findById(assignmentId)
    if(assetAssignmentOptional.isPresent){
      val assetAssignment=assetAssignmentOptional.get()
      assetAssignment.user.id==userId

    }
    else{
      false
    }
  }
  def getAllAssignments: List[AssetAssignmentResponseDTO] = {
    val assetAssignments: List[AssetAssignment] = assetAssignmentRepo.findAll().asScala.toList
    assetAssignments.map(assignment => AssetAssignmentMapper.toAssetAssignmentResponseDTO(assignment))
  }

  def getAllAssignmentsByStatus(returned:Boolean): List[AssetAssignmentResponseDTO] = {
    val assetAssignments: List[AssetAssignment] =
      if (returned) {
      assetAssignmentRepo.findAllByReturnedAtIsNotNull().asScala.toList
    } else {
      assetAssignmentRepo.findAllByReturnedAtIsNull().asScala.toList
    }
    assetAssignments.map(assignment => AssetAssignmentMapper.toAssetAssignmentResponseDTO(assignment))
  }

  def getAssignmentsByUserId(userId:Long): List[AssetAssignmentResponseDTO] = {
    val user:User=userRepo.findById(userId).orElseThrow(() => new EntityNotFoundException("User not found"))

    val assetAssignments:List[AssetAssignment]=assetAssignmentRepo.findByUser(user).asScala.toList
    assetAssignments.map(assignment => AssetAssignmentMapper.toAssetAssignmentResponseDTO(assignment))
  }

  def getAssetAssignmentById(assignmentId:Long):AssetAssignmentResponseDTO={
    val assetAssignment:AssetAssignment=assetAssignmentRepo.findById(assignmentId).orElseThrow(()=> new EntityNotFoundException("Asset assignment not found"))
    AssetAssignmentMapper.toAssetAssignmentResponseDTO(assetAssignment)
  }
  
  def canAccessAssetAssignment(assignmentId:Long,userId:Long):Boolean={
    val assetAssignmentOptional=assetAssignmentRepo.findById(assignmentId)
    if(assetAssignmentOptional.isPresent){
      val assetAssignment=assetAssignmentOptional.get()
      assetAssignment.user.id==userId
    }
    else{
      false
    }
  }
}