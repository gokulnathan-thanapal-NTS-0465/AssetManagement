package com.example.demo.Mapper

import com.example.demo.DTO.{AssetRequestDTO, AssetRequestResponseDTO}
import com.example.demo.Model.Enums.RequestStatus
import com.example.demo.Model.{Asset, AssetRequest, User}

object AssetRequestMapper {


  def toEntity(dto: AssetRequestDTO, user: User): AssetRequest = {
    val assetRequest = new AssetRequest
    assetRequest.user = user
    assetRequest.category = dto.category
    assetRequest.reason = dto.reason
    assetRequest.status = RequestStatus.PENDING
    assetRequest
  }

  def toAssetRequestResponseDTO(assetRequest: AssetRequest): AssetRequestResponseDTO = {
    val responseDTO = new AssetRequestResponseDTO
    responseDTO.id = assetRequest.id
    responseDTO.userId = assetRequest.user.id
    responseDTO.category = assetRequest.category
    responseDTO.reason = assetRequest.reason
    responseDTO.status = assetRequest.status
    responseDTO
  }
}
