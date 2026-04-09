package com.example.demo.Mapper

import com.example.demo.DTO.{AssetAssignmentDTO, AssetAssignmentResponseDTO}
import com.example.demo.Model.AssetAssignment

object AssetAssignmentMapper {
  def toAssetAssignmentDTO(assetAssignment: AssetAssignment): AssetAssignmentDTO = {
    val dto = new AssetAssignmentDTO
    dto.id = assetAssignment.id
    dto.asset = assetAssignment.asset
    dto.userId = assetAssignment.user.id
    dto.assignedAt = assetAssignment.assignedAt
    dto
  }
  
  def toAssetAssignmentResponseDTO(assetAssignment: AssetAssignment): AssetAssignmentResponseDTO = {
    AssetAssignmentResponseDTO(
      id = Some(assetAssignment.id),
      assetId = Some(assetAssignment.asset.id),
      userId = Some(assetAssignment.user.id),
      assignedAt = Some(assetAssignment.assignedAt.toString),
      returnedAt = assetAssignment.returnedAt match {
        case null => None
        case dateTime => Some(dateTime.toString)
      }
    )
  }
}
