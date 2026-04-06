package com.example.demo.Controller

import com.example.demo.Model.AssetAssignment
import com.example.demo.Service.AssetAssignmentService
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.{PathVariable, PostMapping, RequestBody, RequestMapping, RestController}


@RestController
@RequestMapping(value = Array("/api/asset-assignment"))
class AssetAssignmentController(assetAssignmentService: AssetAssignmentService) {


  @PostMapping(value = Array("/return/{assignmentId}"))
  @PreAuthorize("hasRole('EMPLOYEE') and @authService.canAccessReturn(#assignmentId)")
  def returnAsset(@PathVariable assignmentId: Long,@RequestBody userId:Long): ResponseEntity[String] = {
    val assetAssignment: AssetAssignment = assetAssignmentService.returnAsset(assignmentId,userId)
    new ResponseEntity[String](s"Asset with id ${assetAssignment.asset.id} returned successfully", HttpStatus.OK)
  }




}