package com.example.demo.Controller

import com.example.demo.DTO.{ComplaintCreationDTO, ComplaintResponseDTO, ComplaintStatsDTO}
import com.example.demo.Model.Enums.ComplaintStatus
import com.example.demo.Service.ComplaintService
import jakarta.validation.Valid
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.{CrossOrigin, GetMapping, PatchMapping, PathVariable, PostMapping, RequestBody, RequestMapping, RequestParam, RestController}


@RestController
@RequestMapping(value = Array("/api/complaint"))
@CrossOrigin(origins = Array("*"))
@Validated
class ComplaintController(complaintService: ComplaintService) {

  @PostMapping(value = Array("/create"))
  @PreAuthorize("hasRole('EMPLOYEE')")
  def createComplaint(@Valid @RequestBody complaintDTO: ComplaintCreationDTO): ResponseEntity[ComplaintResponseDTO] = {
    val complaintResponseDTO: ComplaintResponseDTO = complaintService.createComplaint(complaintDTO)
    new ResponseEntity[ComplaintResponseDTO](complaintResponseDTO, HttpStatus.CREATED)
  }

  @PostMapping(value=Array("/admin/create"))
  @PreAuthorize("hasRole('ADMIN')")
  def createComplaintAdmin(@Valid @RequestBody complaintDTO:ComplaintCreationDTO):ResponseEntity[ComplaintResponseDTO]={
    val complaintResponseDTO: ComplaintResponseDTO = complaintService.createComplaintAdmin(complaintDTO)
    new ResponseEntity[ComplaintResponseDTO](complaintResponseDTO, HttpStatus.CREATED)
  }

  @PatchMapping(value = Array("/{complaintId}/process"))
  @PreAuthorize("hasRole('TECH')")
  def processComplaint(@PathVariable complaintId: Long): ResponseEntity[String] = {
    complaintService.processComplaint(complaintId)
    new ResponseEntity[String]("Complaint process started for complaint ID: " + complaintId, HttpStatus.OK)
  }

  @PatchMapping(value = Array("/{complaintId}/resolve"))
  @PreAuthorize("hasRole('TECH')")
  def resolveComplaint(@PathVariable complaintId: Long): ResponseEntity[String] = {
    complaintService.resolveComplaint(complaintId)
    new ResponseEntity[String]("Complaint resolved for complaint ID: " + complaintId, HttpStatus.OK)
  }

  @GetMapping(value = Array("/all"))
  @PreAuthorize("hasRole('TECH') or hasRole('ADMIN')")
  def getAllComplaints(@RequestParam(required = false) status:ComplaintStatus): ResponseEntity[List[ComplaintResponseDTO]] = {
    val complaints: List[ComplaintResponseDTO] = complaintService.getAllComplaints(status)
    new ResponseEntity[List[ComplaintResponseDTO]](complaints, HttpStatus.OK)
  }

  @GetMapping(value=Array("/user"))
  @PreAuthorize("hasRole('TECH') or ((hasRole('EMPLOYEE') or hasRole('ADMIN')) and @authService.isCurrentUser(#userId))")
  def getAllComplaintsByUserId(@RequestParam userId: Long): ResponseEntity[List[ComplaintResponseDTO]] = {
    val complaints: List[ComplaintResponseDTO] = complaintService.getAllComplaintsByUserId(userId)
    new ResponseEntity[List[ComplaintResponseDTO]](complaints, HttpStatus.OK)
  }

  @GetMapping(value = Array("/{complaintId}"))
  @PreAuthorize("hasRole('TECH') or ((hasRole('EMPLOYEE') or  hasRole('ADMIN')) and @authService.canAccessComplaint(#complaintId))")
  def getComplaintById(@PathVariable complaintId: Long): ResponseEntity[ComplaintResponseDTO] = {
    val complaint: ComplaintResponseDTO = complaintService.getComplaintById(complaintId)
    new ResponseEntity[ComplaintResponseDTO](complaint, HttpStatus.OK)
  }

  @GetMapping(value=Array("/stats"))
  @PreAuthorize("hasRole('TECH') or hasRole('ADMIN')")
  def getComplaintStats: ResponseEntity[ComplaintStatsDTO] = {
    val complaintStatsDTO: ComplaintStatsDTO = complaintService.getComplaintStats
    new ResponseEntity[ComplaintStatsDTO](complaintStatsDTO, HttpStatus.OK)
  }
}