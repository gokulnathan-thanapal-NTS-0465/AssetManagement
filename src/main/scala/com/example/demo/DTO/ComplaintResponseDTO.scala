package com.example.demo.DTO

import com.example.demo.Model.Enums.ComplaintStatus

class ComplaintResponseDTO {
  var id: Long = _
  var userId: Long = _
  var assetId: Long = _
  var description: String = _
  var status: ComplaintStatus = _

  override def toString = s"ComplaintResponseDTO($id, $userId, $assetId, $description, $status)"
}