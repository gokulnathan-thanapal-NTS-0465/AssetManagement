package com.example.demo.DTO

import com.example.demo.Model.Enums.AssetStatus
import com.example.demo.Model.Enums.Category

import scala.compiletime.uninitialized

class AssetResponseDTO {

  var id: Long = uninitialized
  var serialNumber: String = uninitialized
  var modelName: String = uninitialized
  var status: AssetStatus = uninitialized
  var category: Category = uninitialized
  var credit: Int = uninitialized

  override def toString = s"AssetResponseDTO($id, $serialNumber, $modelName, $status, $category)"
}