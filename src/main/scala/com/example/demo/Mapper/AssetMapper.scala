package com.example.demo.Mapper

import com.example.demo.DTO.{AssetCreationDTO, AssetResponseDTO, AssetUpdateDTO}
import com.example.demo.Model.Asset
import com.example.demo.Model.Enums.{AssetStatus, Category}


object AssetMapper {
  def toEntity(dto: AssetCreationDTO, asset: Asset): Asset = {
    asset.modelName = dto.modelName
    asset.status = AssetStatus.AVAILABLE
    asset.category=dto.category
    asset
  }


  def updateEntity(dto: AssetUpdateDTO, asset: Asset): Asset = {
    if(dto.modelName !=null ){
      asset.modelName = dto.modelName
      asset
    }
    else {
      throw new IllegalArgumentException("Model name required")
    }

  }

  def toResponse(asset: Asset): AssetResponseDTO = {
    val responseDTO = new AssetResponseDTO()
    responseDTO.id = asset.id
    responseDTO.status = asset.status
    responseDTO.category = asset.category
    responseDTO.modelName = asset.modelName
    responseDTO.serialNumber = asset.serialNumber
    responseDTO.credit = asset.credit
    responseDTO
  }

}
