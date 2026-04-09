package com.example.demo.Mapper

import com.example.demo.DTO.{ComplaintCreationDTO, ComplaintResponseDTO}
import com.example.demo.Model.Enums.ComplaintStatus
import com.example.demo.Model.{Asset, Complaint, User}

object ComplaintMapper {

  def toEntity(dto: ComplaintCreationDTO, user: User, asset: Asset): Complaint = {
    val complaint = new Complaint
    complaint.user = user
    complaint.asset = asset
    complaint.status = ComplaintStatus.OPEN
    complaint.description = dto.description.getOrElse("Description not provided")
    complaint
  }

  def toComplaintResponseDTO(complaint: Complaint): ComplaintResponseDTO = {
    val responseDTO: ComplaintResponseDTO = new ComplaintResponseDTO
    responseDTO.id = complaint.id
    responseDTO.userId = complaint.user.id
    responseDTO.assetId = complaint.asset.id
    responseDTO.description = complaint.description
    responseDTO.status = complaint.status
    responseDTO
  }
  
  
}
