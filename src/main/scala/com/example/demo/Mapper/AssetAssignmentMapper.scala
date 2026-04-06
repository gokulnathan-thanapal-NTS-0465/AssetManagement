package com.example.demo.Mapper

import com.example.demo.DTO.AssetAssignmentDTO
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
}
