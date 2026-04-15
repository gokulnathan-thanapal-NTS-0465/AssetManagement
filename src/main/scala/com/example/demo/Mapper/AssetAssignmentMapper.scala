package com.example.demo.Mapper

import com.example.demo.DTO.{AssetAssignmentDTO, AssetAssignmentResponseDTO}
import com.example.demo.Model.AssetAssignment

object AssetAssignmentMapper {
  
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
