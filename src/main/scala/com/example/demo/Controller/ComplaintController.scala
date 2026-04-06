package com.example.demo.Controller

import com.example.demo.DTO.{ComplaintCreationDTO, ComplaintResponseDTO}
import com.example.demo.Model.Enums.ComplaintStatus
import com.example.demo.Service.ComplaintService
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.{GetMapping, PathVariable, PostMapping, RequestBody, RequestMapping, RequestParam, RestController}


@RestController
@RequestMapping(value = Array("/api/complaint"))
class ComplaintController(complaintService: ComplaintService) {

  @PostMapping(value = Array("/create"))
  @PreAuthorize("hasRole('EMPLOYEE')")
  def createComplaint(@RequestBody complaintDTO: ComplaintCreationDTO): ResponseEntity[ComplaintResponseDTO] = {
    val complaintResponseDTO: ComplaintResponseDTO = complaintService.createComplaint(complaintDTO)
    new ResponseEntity[ComplaintResponseDTO](complaintResponseDTO, HttpStatus.CREATED)
  }

  @PostMapping(value=Array("/admin/create"))
  @PreAuthorize("hasRole('ADMIN')")
  def createComplaintAdmin(@RequestBody complaintDTO:ComplaintCreationDTO):ResponseEntity[ComplaintResponseDTO]={
    val complaintResponseDTO: ComplaintResponseDTO = complaintService.createComplaintAdmin(complaintDTO)
    new ResponseEntity[ComplaintResponseDTO](complaintResponseDTO, HttpStatus.CREATED)
  }
  @PostMapping(value = Array("/{complaintId}/process"))
  @PreAuthorize("hasRole('TECH')")
  def processComplaint(@PathVariable complaintId: Long): ResponseEntity[String] = {
    complaintService.processComplaint(complaintId)
    new ResponseEntity[String]("Complaint process started for complaint ID: " + complaintId, HttpStatus.OK)
  }

  @PostMapping(value = Array("/{complaintId}/resolve"))
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
}