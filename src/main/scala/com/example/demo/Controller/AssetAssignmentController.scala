package com.example.demo.Controller

import com.example.demo.DTO.AssetAssignmentResponseDTO
import com.example.demo.Model.AssetAssignment
import com.example.demo.Service.AssetAssignmentService
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.{CrossOrigin, GetMapping, PathVariable, PostMapping, RequestBody, RequestMapping, RequestParam, RestController}


@RestController
@RequestMapping(value = Array("/api/asset-assignment"))
@CrossOrigin(origins = Array("*"))
class AssetAssignmentController(assetAssignmentService: AssetAssignmentService) {


  @PostMapping(value = Array("/return/{assignmentId}"))
  @PreAuthorize("hasRole('EMPLOYEE') and @authService.canAccessAssignment(#assignmentId)")
  def returnAsset(@PathVariable assignmentId: Long): ResponseEntity[String] = {
    val assetAssignment: AssetAssignment = assetAssignmentService.returnAsset(assignmentId)
    new ResponseEntity[String](s"Asset with id ${assetAssignment.asset.id} returned successfully", HttpStatus.OK)
  }

  @GetMapping(value = Array("/{assignmentId}"))
  @PreAuthorize("hasRole('ADMIN') or (hasRole('EMPLOYEE') and @authService.canAccessAssignment(#assignmentId))")
  def getAssetAssignmentById(assignmentId: Long): ResponseEntity[AssetAssignmentResponseDTO] = {
    val assetAssignment: AssetAssignmentResponseDTO = assetAssignmentService.getAssetAssignmentById(assignmentId)
    new ResponseEntity[AssetAssignmentResponseDTO](assetAssignment, HttpStatus.OK)
  }


  @GetMapping(value = Array("/user/{userId}"))
  @PreAuthorize("hasRole('EMPLOYEE') ")
  def getAssignmentsByUserId(@PathVariable userId: Long): ResponseEntity[List[AssetAssignmentResponseDTO]] = {
    val assignments: List[AssetAssignmentResponseDTO] = assetAssignmentService.getAssignmentsByUserId(userId)
    new ResponseEntity[List[AssetAssignmentResponseDTO]](assignments, HttpStatus.OK)
  }


  @GetMapping(value = Array("/all"))
  @PreAuthorize("hasRole('ADMIN')")
  def getAllAssignments:ResponseEntity[List[AssetAssignmentResponseDTO]]={
    val assignments: List[AssetAssignmentResponseDTO] = assetAssignmentService.getAllAssignments
    new ResponseEntity[List[AssetAssignmentResponseDTO]](assignments, HttpStatus.OK)
  }

  @GetMapping(value = Array("/status"))
  @PreAuthorize("hasRole('ADMIN')")
  def getAllAssignmentsByStatus(@RequestParam(required = false) returned: Boolean): ResponseEntity[List[AssetAssignmentResponseDTO]] = {
    val assignments: List[AssetAssignmentResponseDTO] = assetAssignmentService.getAllAssignmentsByStatus(returned)
    new ResponseEntity[List[AssetAssignmentResponseDTO]](assignments, HttpStatus.OK)
  }
}