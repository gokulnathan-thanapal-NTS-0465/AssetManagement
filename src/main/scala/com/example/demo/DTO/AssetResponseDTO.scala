package com.example.demo.DTO

import com.example.demo.Model.Enums.AssetStatus
import com.example.demo.Model.Enums.Category

class AssetResponseDTO {

  var id: Long = _
  var serialNumber: String = _
  var modelName: String = _
  var status: AssetStatus = _
  var category: Category = _
  var credit: Int = _

  override def toString = s"AssetResponseDTO($id, $serialNumber, $modelName, $status, $category)"
}