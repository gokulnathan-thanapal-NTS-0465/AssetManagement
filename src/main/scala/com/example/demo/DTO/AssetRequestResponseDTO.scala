package com.example.demo.DTO

import com.example.demo.Model.Enums.{Category, RequestStatus}

import scala.compiletime.uninitialized

class AssetRequestResponseDTO {

  var id: Long = uninitialized
  var userId: Long = uninitialized
  var category: Category = uninitialized
  var reason: String = uninitialized
  var status: RequestStatus = uninitialized

  override def toString = s"AssetRequestResponse($id, $userId, $category, $reason, $status)"
}
