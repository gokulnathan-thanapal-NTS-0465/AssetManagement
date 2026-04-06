package com.example.demo.Controller

import com.example.demo.DTO.{AssetAssignmentDTO, AssetRequestDTO, AssetRequestResponseDTO}
import com.example.demo.Model.AssetRequest
import com.example.demo.Model.Enums.RequestStatus
import com.example.demo.Service.AssetRequestService
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.{GetMapping, PathVariable, PostMapping, RequestBody, RequestMapping, RequestParam, ResponseBody, RestController}


@RestController
@RequestMapping(value = Array("/api/asset-request"))
class AssetRequestController(assetRequestService: AssetRequestService) {

  @PostMapping(value = Array("/create"))
  @PreAuthorize("hasRole('EMPLOYEE')")
  def createRequest(@RequestBody request: AssetRequestDTO): ResponseEntity[AssetRequestResponseDTO] = {
    val newRequest: AssetRequestResponseDTO = assetRequestService.createRequest(request)
    new ResponseEntity[AssetRequestResponseDTO](newRequest, HttpStatus.CREATED)
  }

  @PostMapping(value = Array("/accept/{requestId}"))
  @PreAuthorize("hasRole('ADMIN')")
  def acceptAssetRequest(@PathVariable requestId: Long): ResponseEntity[AssetAssignmentDTO] = {
    val assignment: AssetAssignmentDTO = assetRequestService.acceptRequest(requestId)
    new ResponseEntity[AssetAssignmentDTO](assignment, HttpStatus.OK)
  }

  @PostMapping(value = Array("/decline/{requestId}"))
  @PreAuthorize("hasRole('ADMIN')")
  def declineAssetRequest(@PathVariable requestId: Long): ResponseEntity[String] = {
    assetRequestService.declineRequest(requestId)
    new ResponseEntity[String]("Request Declined Successfully", HttpStatus.OK)
  }

  @GetMapping(value = Array("/all"))
  @PreAuthorize("hasRole('ADMIN')")
  def getAllRequest(@RequestParam(required=false) status: RequestStatus): ResponseEntity[List[AssetRequestResponseDTO]] = {
    val requests: List[AssetRequestResponseDTO] = assetRequestService.getAllRequest(status)
    new ResponseEntity[List[AssetRequestResponseDTO]](requests, HttpStatus.OK)
  }

  @GetMapping(value = Array("/{requestId}"))
  @PreAuthorize("hasRole('ADMIN') or (hasRole('EMPLOYEE') and @authService.canAccessRequest(#requestId))")
  def getRequestById(@PathVariable requestId: Long): ResponseEntity[AssetRequestResponseDTO] = {
    val request: AssetRequestResponseDTO = assetRequestService.getRequestById(requestId)
    new ResponseEntity[AssetRequestResponseDTO](request, HttpStatus.OK)
  }

  @GetMapping(value=Array("/user/{userId}"))
  @PreAuthorize("hasRole('ADMIN') or (hasRole('EMPLOYEE') and @authService.isCurrentUser(#userId))")
  def getRequestByUserId(@PathVariable userId: Long,@RequestParam(required = false) status:RequestStatus): ResponseEntity[List[AssetRequestResponseDTO]] = {
    val requests: List[AssetRequestResponseDTO] = assetRequestService.getAllRequestsByUserId(userId,status)
    new ResponseEntity[List[AssetRequestResponseDTO]](requests, HttpStatus.OK)
  }

}