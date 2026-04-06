package com.example.demo.Service

import com.example.demo.Model.Enums.AssetStatus
import com.example.demo.Model.{Asset, AssetAssignment}
import com.example.demo.Repo.{AssetAssignmentRepository, AssetRepository}
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.time.LocalDateTime


@Service
class AssetAssignmentService(assetAssignmentRepo: AssetAssignmentRepository, assetRepo: AssetRepository) {

  @Transactional
  def returnAsset(assignmentId: Long,userId:Long): AssetAssignment = {

    var assetAssignment: AssetAssignment = assetAssignmentRepo.findById(assignmentId).orElseThrow(()=> new EntityNotFoundException("Asset assignment not found"))
    if (assetAssignment.returnedAt != null) {
      throw new IllegalStateException(s"Asset with id ${assetAssignment.asset.id} has already been returned")
    }
    else if(userId!=assetAssignment.user.id){
      throw new SecurityException(s"Asset with id ${assetAssignment.asset.id} is not assigned to user with id $userId")
    }
    assetAssignment.returnedAt = LocalDateTime.now()
    var asset: Asset = assetAssignment.asset
    asset.status = AssetStatus.AVAILABLE
    asset = assetRepo.save(asset)

    assetAssignment = assetAssignmentRepo.save(assetAssignment)
    assetAssignment
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
}