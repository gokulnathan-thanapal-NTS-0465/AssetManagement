package com.example.demo.Controller

import com.example.demo.DTO.{AssetCountDTO, AssetCreationDTO, AssetResponseDTO, AssetStatusDTO, AssetUpdateDTO}
import com.example.demo.Model.Asset
import com.example.demo.Model.Enums.{AssetStatus, Category}
import com.example.demo.Service.AssetService
import jakarta.persistence.PostLoad
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.{CrossOrigin, DeleteMapping, GetMapping, PatchMapping, PathVariable, PostMapping, RequestBody, RequestMapping, RequestParam, RequestPart, ResponseBody, RestController}


@RestController
@RequestMapping(value = Array("/api/asset"))
@CrossOrigin(origins = Array("*"))
class AssetController(assetService: AssetService) {

  @PostMapping(value = Array("/create"))
  @PreAuthorize("hasRole('ADMIN')")
  def createAsset(@RequestBody asset: AssetCreationDTO): ResponseEntity[AssetResponseDTO] = {
    val newAsset: AssetResponseDTO = assetService.createAsset(asset)
    new ResponseEntity[AssetResponseDTO](newAsset, HttpStatus.CREATED)
  }

  @PatchMapping(value = Array("/status/{assetId}"))
  @PreAuthorize("hasRole('ADMIN')")
  def updateAssetStatusById(@RequestBody assetDTO: AssetStatusDTO, @PathVariable assetId: Long): ResponseEntity[AssetResponseDTO] = {
    val updatedAsset: AssetResponseDTO = assetService.updateAssetStatusById(assetId, assetDTO)
    new ResponseEntity[AssetResponseDTO](updatedAsset, HttpStatus.OK)
  }
  
  @PatchMapping(value = Array("/{assetId}"))
  @PreAuthorize("hasRole('ADMIN')")
  def updateAssetById(@RequestBody assetDTO: AssetUpdateDTO, @PathVariable assetId: Long): ResponseEntity[AssetResponseDTO] = {
    val updatedAsset: AssetResponseDTO = assetService.updateAssetById(assetId, assetDTO)
    new ResponseEntity[AssetResponseDTO](updatedAsset, HttpStatus.OK)
  }

  @DeleteMapping(value = Array("/{assetId}"))
  @PreAuthorize("hasRole('ADMIN')")
  def deleteAssetById(@PathVariable assetId: Long): ResponseEntity[String] = {
    assetService.deleteAssetById(assetId)
    new ResponseEntity[String]("Asset deleted Successfully", HttpStatus.OK)
  }

  @GetMapping(value = Array("/user/{userId}"))
  @PreAuthorize("hasRole('EMPLOYEE')")
  def getAssetsByUserId(@PathVariable userId: Long, @RequestParam(required = false) status: AssetStatus, @RequestParam(required = false) category: Category): ResponseEntity[List[AssetResponseDTO]] = {
    val assets: List[AssetResponseDTO] = assetService.getAssetsByUserId(userId, status, category)
    new ResponseEntity[List[AssetResponseDTO]](assets, HttpStatus.OK)
  }

  @GetMapping(value = Array("/all"))
  @PreAuthorize("hasRole('ADMIN')")
  def getAllAssets(@RequestParam(required = false) status: AssetStatus, @RequestParam(required = false) category: Category): ResponseEntity[List[AssetResponseDTO]] = {
    val assets: List[AssetResponseDTO] = assetService.getAllAssets(status, category)
    new ResponseEntity[List[AssetResponseDTO]](assets, HttpStatus.OK)
  }
  
  @GetMapping(value=Array("/count"))
  def getAllAssetCount:ResponseEntity[AssetCountDTO]  = {
    val  assetCountDTO:AssetCountDTO=assetService.getAllAssetCount
    new ResponseEntity[AssetCountDTO](assetCountDTO,HttpStatus.OK)
  }

  @GetMapping(value = Array("/{assetId}"))
  def getAssetById(@PathVariable assetId: Long): ResponseEntity[AssetResponseDTO] = {
    val asset: AssetResponseDTO = assetService.getAssetById(assetId)
    new ResponseEntity[AssetResponseDTO](asset, HttpStatus.OK)
  }
}