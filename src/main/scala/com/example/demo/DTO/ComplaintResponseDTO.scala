package com.example.demo.DTO

import com.example.demo.Model.Enums.ComplaintStatus

import scala.compiletime.uninitialized

class ComplaintResponseDTO {
  var id: Long = uninitialized
  var userId: Long = uninitialized
  var assetId: Long = uninitialized
  var description: String = uninitialized
  var status: ComplaintStatus = uninitialized

  override def toString = s"ComplaintResponseDTO($id, $userId, $assetId, $description, $status)"
}