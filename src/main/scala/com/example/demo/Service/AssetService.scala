package com.example.demo.Service

import com.example.demo.DTO.{AssetCountDTO, AssetCreationDTO, AssetResponseDTO, AssetStatusDTO, AssetUpdateDTO}
import com.example.demo.Mapper.AssetMapper
import com.example.demo.Model.{Asset, AssetAssignment, Credit}
import com.example.demo.Model.Enums.{AssetStatus, Category}
import org.springframework.stereotype.Service

import scala.jdk.CollectionConverters.*
import com.example.demo.Repo.{AssetAssignmentRepository, AssetRepository}
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional

import scala.language.postfixOps


@Service
class AssetService(assetRepo: AssetRepository, assetAssignmentRepo: AssetAssignmentRepository) {


  @Transactional
  def createAsset(assetDTO: AssetCreationDTO): AssetResponseDTO = {
    val asset:Asset=Asset()
    var newAsset:Asset = AssetMapper.toEntity(assetDTO,asset)
    val generatedSerialNo = generateSerialNo(newAsset.category)
    newAsset.serialNumber = generatedSerialNo
    newAsset.credit = Credit.creditRequirements.getOrElse(newAsset.category, 0)
    newAsset = assetRepo.save(newAsset)
    val assetResponseDTO: AssetResponseDTO = AssetMapper.toResponse(newAsset)
    assetResponseDTO
  }

  @Transactional
  def updateAssetStatusById(assetId: Long, assetStatusDTO: AssetStatusDTO): AssetResponseDTO = {
    var asset = assetRepo.findById(assetId).orElseThrow(()=> new EntityNotFoundException("Asset not found"))

    if (assetStatusDTO.status == null) {
      throw new IllegalArgumentException("Status is required")
    }
    else if (assetStatusDTO.status.get== AssetStatus.ASSIGNED) {
      throw new IllegalStateException("Asset cannot be directly assigned to a user !")
    }
    else if (assetStatusDTO.status.get == AssetStatus.AVAILABLE && asset.status == AssetStatus.RETIRED) {
      throw new IllegalStateException("Retired asset cannot be made available !")
    }
    else if (asset.status != AssetStatus.AVAILABLE) {
      val assetAssignment: AssetAssignment = assetAssignmentRepo.findByAssetIdAndReturnedAtIsNull(assetId)
      if (asset.status == AssetStatus.MAINTENANCE && assetAssignment == null) {
        asset.status = assetStatusDTO.status.get
      }
      else {
        throw new IllegalStateException("Asset is currently assigned to a user and cannot be updated !")
      }
    }
    else {
      asset.status = assetStatusDTO.status.get
    }
    asset = assetRepo.save(asset)
    val updatedAsset = AssetMapper.toResponse(asset)
    updatedAsset
  }

  @Transactional
  def updateAssetById(assetId: Long, assetUpdateDTO: AssetUpdateDTO): AssetResponseDTO = {
    var asset = assetRepo.findById(assetId).orElseThrow(() => new EntityNotFoundException("Asset not found "))
    AssetMapper.updateEntity(assetUpdateDTO, asset)
    asset = assetRepo.save(asset)
    val assetResponseDTO = AssetMapper.toResponse(asset)
    assetResponseDTO
  }


  @Transactional
  def deleteAssetById(assetId: Long): Unit = {
    val assetAssignment: AssetAssignment = assetAssignmentRepo.findByAssetIdAndReturnedAtIsNull(assetId)
    if (assetAssignment == null || assetAssignment.returnedAt != null) {
      val asset:Asset=assetRepo.findById(assetId).orElseThrow(() => new EntityNotFoundException("Asset not found "))
      asset.status=AssetStatus.RETIRED
      assetRepo.save(asset)
    }
    else {
      throw new IllegalStateException("Asset is currently assigned to a user and cannot be deleted !")
    }
  }


  def generateSerialNo(category: Category): String = {
    val prefix = category match {
      case Category.LAPTOP => "LAP"
      case Category.MOBILE => "MOB"
      case Category.DESKTOP => "DES"
      case Category.KEYBOARD => "KEY"
      case Category.MOUSE => "MOU"
    }
    val count = assetRepo.countByCategory(category) + 1
    val number = f"$count%04d"
    prefix + "-" + number
  }


  def getAssetsByUserId(userId: Long, status: AssetStatus, category: Category): List[AssetResponseDTO] = {
    val assets: List[Asset] = {
      if (userId != 0 && status != null && category != null) {
        assetRepo.findAssetByUserIdAndStatusAndCategory(status, userId, category).asScala.toList
      }
      else if (userId != 0 && status != null) {
        assetRepo.findAssetByUserIdAndStatus(status, userId).asScala.toList
      }
      else if (userId != 0 && category != null) {
        assetRepo.findAssetByUserIdAndCategory(userId, category).asScala.toList
      }
      else {
        assetRepo.findAssetByUserId(userId).asScala.toList
      }
    }
    val assetResponseDTOs = assets.map(AssetMapper.toResponse)
    assetResponseDTOs
  }

  def getAllAssets(status: AssetStatus, category: Category): List[AssetResponseDTO] = {
    val assets: List[Asset] = {
      if (status != null && category != null) {
        assetRepo.findAllByStatusAndCategory(status, category).asScala.toList
      }
      else if (status != null) {
        assetRepo.findAllByStatus(status).asScala.toList
      }
      else if (category != null) {
        assetRepo.findAllByCategory(category).asScala.toList
      }
      else {
        assetRepo.findAll().asScala.toList
      }
    }
    val assetResponseDTOs = assets.map(AssetMapper.toResponse)
    assetResponseDTOs
  }
  
  
  def getAllAssetCount: AssetCountDTO = {
    val assetCountDTO: AssetCountDTO =new AssetCountDTO
    assetCountDTO.total = assetRepo.countByStatus(AssetStatus.AVAILABLE)
    assetCountDTO.mouse=assetRepo.countByCategoryAndStatus(Category.MOUSE, AssetStatus.AVAILABLE)
    assetCountDTO.mobile=assetRepo.countByCategoryAndStatus(Category.MOBILE, AssetStatus.AVAILABLE)
    assetCountDTO.desktop=assetRepo.countByCategoryAndStatus(Category.DESKTOP, AssetStatus.AVAILABLE)
    assetCountDTO.laptop=assetRepo.countByCategoryAndStatus(Category.LAPTOP, AssetStatus.AVAILABLE)
    assetCountDTO.keyboard=assetRepo.countByCategoryAndStatus(Category.KEYBOARD, AssetStatus.AVAILABLE)
    assetCountDTO
  }


}
