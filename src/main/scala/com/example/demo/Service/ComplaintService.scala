package com.example.demo.Service

import com.example.demo.DTO.{ComplaintCreationDTO, ComplaintResponseDTO}
import com.example.demo.Mapper.ComplaintMapper
import com.example.demo.Model.Enums.{AssetStatus, ComplaintStatus}
import com.example.demo.Model.{Asset, Complaint, User}
import com.example.demo.Repo.{AssetRepository, ComplaintRepository, UserRepository}
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import scala.jdk.CollectionConverters.*



@Service
class ComplaintService(userRepo: UserRepository, complaintRepo: ComplaintRepository, assetRepo: AssetRepository) {


  @Transactional
  def createComplaint(complaintCreationDTO: ComplaintCreationDTO): ComplaintResponseDTO = {

    val user: User = userRepo.findById(complaintCreationDTO.userId.getOrElse(
      throw new IllegalArgumentException("User ID is required"))).orElseThrow(()=> EntityNotFoundException("User not found"))

    val asset: Asset = assetRepo.findById(complaintCreationDTO.assetId.getOrElse(
      throw new IllegalArgumentException("Asset ID is required"))).orElseThrow(()=> EntityNotFoundException("Asset not found"))

    if (!isAssetBelongsToUser(user, asset)) {
      throw new SecurityException("Asset does not belong to the user")
    }
      
    else if (asset.status != AssetStatus.ASSIGNED) {
      throw new IllegalStateException("Asset is not currently assigned , cannot file complaint ")
    }

    var newComplaint: Complaint = ComplaintMapper.toEntity(complaintCreationDTO, user, asset)
    newComplaint = complaintRepo.save(newComplaint)
    
    val complaintResponseDTO: ComplaintResponseDTO = ComplaintMapper.toComplaintResponseDTO(newComplaint)
    complaintResponseDTO
  }
  def createComplaintAdmin(complaintCreationDTO: ComplaintCreationDTO):ComplaintResponseDTO={
    
    val asset: Asset = assetRepo.findById(complaintCreationDTO.assetId.getOrElse(
      throw new IllegalArgumentException("Asset ID is required"))).orElseThrow(()=> EntityNotFoundException("Asset not found"))
    
    val user: User = userRepo.findById(complaintCreationDTO.userId.getOrElse(
      throw new IllegalArgumentException("User not found"))).orElseThrow(()=> EntityNotFoundException("User not found"))

    if(asset.status!=AssetStatus.AVAILABLE){
      throw new IllegalStateException("Asset is not available for complaint")
    }
    var newComplaint: Complaint = ComplaintMapper.toEntity(complaintCreationDTO,user, asset)
    newComplaint=complaintRepo.save(newComplaint)
    
    val complaintResponseDTO:ComplaintResponseDTO=ComplaintMapper.toComplaintResponseDTO(newComplaint)
    complaintResponseDTO
  }

  @Transactional
  def processComplaint(complaintId: Long): ComplaintResponseDTO = {

    val complaint: Complaint = complaintRepo.findById(complaintId).orElseThrow()

    if (complaint.status != ComplaintStatus.OPEN) {
      throw new IllegalStateException("Only open complaints can be processed")
    }
    complaint.status = ComplaintStatus.IN_PROGRESS
    val updatedComplaint = complaintRepo.save(complaint)

    var asset: Asset = updatedComplaint.asset
    asset.status = AssetStatus.MAINTENANCE
    asset = assetRepo.save(asset)

    val complaintResponseDTO: ComplaintResponseDTO = ComplaintMapper.toComplaintResponseDTO(updatedComplaint)
    complaintResponseDTO
  }

  @Transactional
  def resolveComplaint(complaintId: Long): ComplaintResponseDTO = {

    val complaint: Complaint = complaintRepo.findById(complaintId).orElseThrow(()=> EntityNotFoundException("Complaint not found"))
    if (complaint.status != ComplaintStatus.IN_PROGRESS) {
      throw new IllegalStateException("Only complaints in progress can be resolved ")
    }
    complaint.status = ComplaintStatus.RESOLVED
    val updatedComplaint = complaintRepo.save(complaint)

    var asset: Asset = updatedComplaint.asset
    asset.status = AssetStatus.ASSIGNED
    asset = assetRepo.save(asset)

    val complaintResponseDTO: ComplaintResponseDTO = ComplaintMapper.toComplaintResponseDTO(updatedComplaint)
    complaintResponseDTO
  }


  def getAllComplaints(status:ComplaintStatus): List[ComplaintResponseDTO] = {

    val complaints: List[Complaint] = {
      if (status != null) {
        complaintRepo.findByStatus(status).asScala.toList
      }
      else {
        complaintRepo.findAll().asScala.toList
      }
    }
    val complaintResponseDTOs: List[ComplaintResponseDTO] = complaints.map(complaint => ComplaintMapper.toComplaintResponseDTO(complaint))
    complaintResponseDTOs

  }

  
  def getAllComplaintsByUserId(userId: Long): List[ComplaintResponseDTO] = {

    val complaints: List[Complaint] = complaintRepo.findByUserId(userId).asScala.toList
    val complaintResponseDTOs: List[ComplaintResponseDTO] = complaints.map(complaint => ComplaintMapper.toComplaintResponseDTO(complaint))
    complaintResponseDTOs

  }

  private def isAssetBelongsToUser(user: User, asset: Asset): Boolean = {

    assetRepo.findAssetByUserId(user.id)
      .stream().anyMatch(a => a.id == asset.id)

  }
}