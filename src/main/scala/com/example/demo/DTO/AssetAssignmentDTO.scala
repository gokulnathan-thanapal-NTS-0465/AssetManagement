package com.example.demo.DTO

import com.example.demo.Model.Asset

import java.time.LocalDateTime
import scala.compiletime.uninitialized

class AssetAssignmentDTO {
  
  var id: Long = uninitialized
  var asset: Asset = uninitialized
  var userId: Long = uninitialized
  var assignedAt: LocalDateTime = uninitialized

  override def toString = s"AssetAssignmentDTO($id, $asset, $userId, $assignedAt)"
}