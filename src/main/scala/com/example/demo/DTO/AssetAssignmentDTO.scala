package com.example.demo.DTO

import com.example.demo.Model.Asset

import java.time.LocalDateTime

class AssetAssignmentDTO {
  var id: Long = _
  var asset: Asset = _
  var userId: Long = _
  var assignedAt: LocalDateTime = _

  override def toString = s"AssetAssignmentDTO($id, $asset, $userId, $assignedAt)"
}