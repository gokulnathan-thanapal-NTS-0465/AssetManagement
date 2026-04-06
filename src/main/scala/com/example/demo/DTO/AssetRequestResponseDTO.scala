package com.example.demo.DTO

import com.example.demo.Model.Enums.{Category, RequestStatus}

class AssetRequestResponseDTO {

  var id: Long = _
  var userId: Long = _
  var category: Category = _
  var reason: String = _
  var status: RequestStatus = _

  override def toString = s"AssetRequestResponse($id, $userId, $category, $reason, $status)"
}
