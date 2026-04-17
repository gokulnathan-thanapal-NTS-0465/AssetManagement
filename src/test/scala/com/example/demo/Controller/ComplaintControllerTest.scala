package com.example.demo.Controller

import com.example.demo.DTO.{ComplaintCreationDTO, ComplaintResponseDTO, ComplaintStatsDTO}
import com.example.demo.Model.Enums.ComplaintStatus
import com.example.demo.Service.ComplaintService
import com.example.demo.Util.GlobalExceptionHandler
import jakarta.persistence.EntityNotFoundException
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

import scala.compiletime.uninitialized

class ComplaintControllerTest extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var complaintService: ComplaintService = uninitialized
  var mockMvc: MockMvc = uninitialized

  override def beforeEach(): Unit = {
    complaintService = mock[ComplaintService]
    mockMvc = MockMvcBuilders
      .standaloneSetup(new ComplaintController(complaintService))
      .setControllerAdvice(new GlobalExceptionHandler())
      .build()
  }

  private def sampleComplaintResponse(
                                       id: Long = 1L,
                                       userId: Long = 5L,
                                       assetId: Long = 10L,
                                       description: String = "Screen flickering issue",
                                       status: ComplaintStatus = ComplaintStatus.OPEN
                                     ): ComplaintResponseDTO = {
    val dto = new ComplaintResponseDTO
    dto.id = id
    dto.userId = userId
    dto.assetId = assetId
    dto.description = description
    dto.status = status
    dto
  }

  private def sampleComplaintStats(): ComplaintStatsDTO = {
    val dto = new ComplaintStatsDTO
    dto.totalComplaints = 15L
    dto.open = 5L
    dto.inProgress = 4L
    dto.resolved = 6L
    dto
  }


  "POST /api/complaint/create" should {

    "return 201 with valid payload" in {
      val response = sampleComplaintResponse()
      when(complaintService.createComplaint(any[ComplaintCreationDTO])).thenReturn(response)

      mockMvc.perform(
          post("/api/complaint/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"userId":"5","assetId":"10","description":"Screen flickering issue"}""")
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.userId").value(5))
        .andExpect(jsonPath("$.assetId").value(10))
        .andExpect(jsonPath("$.status").value("OPEN"))
    }

    "return 404 when user not found" in {
      when(complaintService.createComplaint(any[ComplaintCreationDTO]))
        .thenThrow(new EntityNotFoundException("User not found"))

      mockMvc.perform(
          post("/api/complaint/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"userId":"999","assetId":"10","description":"Screen flickering issue"}""")
        )
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("User not found"))
    }

    "return 404 when asset not found" in {
      when(complaintService.createComplaint(any[ComplaintCreationDTO]))
        .thenThrow(new EntityNotFoundException("Asset not found"))

      mockMvc.perform(
          post("/api/complaint/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"userId":"5","assetId":"999","description":"Screen flickering issue"}""")
        )
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("Asset not found"))
    }

    "return 403 when asset does not belong to user" in {
      when(complaintService.createComplaint(any[ComplaintCreationDTO]))
        .thenThrow(new SecurityException("Asset does not belong to the user"))

      mockMvc.perform(
          post("/api/complaint/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"userId":"5","assetId":"10","description":"Screen flickering issue"}""")
        )
        .andExpect(status().isForbidden)
        .andExpect(jsonPath("$.message").value("Asset does not belong to the user"))
    }

    "return 400 when asset is not currently assigned" in {
      when(complaintService.createComplaint(any[ComplaintCreationDTO]))
        .thenThrow(new IllegalStateException("Asset is not currently assigned , cannot file complaint "))

      mockMvc.perform(
          post("/api/complaint/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"userId":"5","assetId":"10","description":"Screen flickering issue"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Asset is not currently assigned , cannot file complaint "))
    }

    "return 400 when userId is missing" in {
      when(complaintService.createComplaint(any[ComplaintCreationDTO]))
        .thenThrow(new IllegalArgumentException("User ID is required"))

      mockMvc.perform(
          post("/api/complaint/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"assetId":"10","description":"Screen flickering issue"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("User ID is required"))
    }

    "return 400 when assetId is missing" in {
      when(complaintService.createComplaint(any[ComplaintCreationDTO]))
        .thenThrow(new IllegalArgumentException("Asset ID is required"))

      mockMvc.perform(
          post("/api/complaint/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"userId":"5","description":"Screen flickering issue"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Asset ID is required"))
    }
  }


  "POST /api/complaint/admin/create" should {

    "return 201 with valid payload" in {
      val response = sampleComplaintResponse()
      when(complaintService.createComplaintAdmin(any[ComplaintCreationDTO])).thenReturn(response)

      mockMvc.perform(
          post("/api/complaint/admin/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"userId":"5","assetId":"10","description":"Hardware defect found"}""")
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.status").value("OPEN"))
    }

    "return 404 when asset not found" in {
      when(complaintService.createComplaintAdmin(any[ComplaintCreationDTO]))
        .thenThrow(new EntityNotFoundException("Asset not found"))

      mockMvc.perform(
          post("/api/complaint/admin/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"userId":"5","assetId":"999","description":"Hardware defect found"}""")
        )
        .andExpect(status().isNotFound)
    }

    "return 404 when user not found" in {
      when(complaintService.createComplaintAdmin(any[ComplaintCreationDTO]))
        .thenThrow(new EntityNotFoundException("User not found"))

      mockMvc.perform(
          post("/api/complaint/admin/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"userId":"999","assetId":"10","description":"Hardware defect found"}""")
        )
        .andExpect(status().isNotFound)
    }

    "return 400 when asset is not available" in {
      when(complaintService.createComplaintAdmin(any[ComplaintCreationDTO]))
        .thenThrow(new IllegalStateException("Admin cannot file complaint for an asset that is not available"))

      mockMvc.perform(
          post("/api/complaint/admin/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"userId":"5","assetId":"10","description":"Hardware defect found"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Admin cannot file complaint for an asset that is not available"))
    }

    "return 400 when assetId is missing" in {
      when(complaintService.createComplaintAdmin(any[ComplaintCreationDTO]))
        .thenThrow(new IllegalArgumentException("Asset ID is required"))

      mockMvc.perform(
          post("/api/complaint/admin/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"userId":"5","description":"Hardware defect found"}""")
        )
        .andExpect(status().isBadRequest)
    }

    "return 400 when userId is missing" in {
      when(complaintService.createComplaintAdmin(any[ComplaintCreationDTO]))
        .thenThrow(new IllegalArgumentException("User ID is required"))

      mockMvc.perform(
          post("/api/complaint/admin/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"assetId":"10","description":"Hardware defect found"}""")
        )
        .andExpect(status().isBadRequest)
    }
  }

  "PATCH /api/complaint/{complaintId}/process" should {

    "return 200 when complaint processed successfully" in {
      mockMvc.perform(patch("/api/complaint/1/process"))
        .andExpect(status().isOk)
        .andExpect(content().string("Complaint process started for complaint ID: 1"))

      verify(complaintService).processComplaint(1L)
    }

    "return 404 when complaint not found" in {
      when(complaintService.processComplaint(999L))
        .thenThrow(new EntityNotFoundException("Complaint not found"))

      mockMvc.perform(patch("/api/complaint/999/process"))
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("Complaint not found"))
    }

    "return 400 when complaint is not open" in {
      when(complaintService.processComplaint(1L))
        .thenThrow(new IllegalStateException("Only open complaints can be processed"))

      mockMvc.perform(patch("/api/complaint/1/process"))
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Only open complaints can be processed"))
    }

    "return 400 for invalid complaintId path variable" in {
      mockMvc.perform(patch("/api/complaint/abc/process"))
        .andExpect(status().isBadRequest)
    }
  }

  "PATCH /api/complaint/{complaintId}/resolve" should {

    "return 200 when complaint resolved successfully" in {
      mockMvc.perform(patch("/api/complaint/1/resolve"))
        .andExpect(status().isOk)
        .andExpect(content().string("Complaint resolved for complaint ID: 1"))

      verify(complaintService).resolveComplaint(1L)
    }

    "return 404 when complaint not found" in {
      when(complaintService.resolveComplaint(999L))
        .thenThrow(new EntityNotFoundException("Complaint not found"))

      mockMvc.perform(patch("/api/complaint/999/resolve"))
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("Complaint not found"))
    }

    "return 400 when complaint is not in progress" in {
      when(complaintService.resolveComplaint(1L))
        .thenThrow(new IllegalStateException("Only complaints in progress can be resolved "))

      mockMvc.perform(patch("/api/complaint/1/resolve"))
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Only complaints in progress can be resolved "))
    }

    "return 400 for invalid complaintId path variable" in {
      mockMvc.perform(patch("/api/complaint/abc/resolve"))
        .andExpect(status().isBadRequest)
    }
  }

  "GET /api/complaint/all" should {

    "return 200 with all complaints when no filter" in {
      val complaints = List(
        sampleComplaintResponse(),
        sampleComplaintResponse(id = 2L, status = ComplaintStatus.IN_PROGRESS)
      )
      when(complaintService.getAllComplaints(null)).thenReturn(complaints)

      mockMvc.perform(get("/api/complaint/all"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].status").value("OPEN"))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].status").value("IN_PROGRESS"))
    }

    "return 200 with filtered complaints by status OPEN" in {
      val complaints = List(sampleComplaintResponse())
      when(complaintService.getAllComplaints(ComplaintStatus.OPEN)).thenReturn(complaints)

      mockMvc.perform(get("/api/complaint/all").param("status", "OPEN"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].status").value("OPEN"))
    }

    "return 200 with filtered complaints by status RESOLVED" in {
      val complaints = List(sampleComplaintResponse(status = ComplaintStatus.RESOLVED))
      when(complaintService.getAllComplaints(ComplaintStatus.RESOLVED)).thenReturn(complaints)

      mockMvc.perform(get("/api/complaint/all").param("status", "RESOLVED"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].status").value("RESOLVED"))
    }

    "return 200 with filtered complaints by status IN_PROGRESS" in {
      val complaints = List(sampleComplaintResponse(status = ComplaintStatus.IN_PROGRESS))
      when(complaintService.getAllComplaints(ComplaintStatus.IN_PROGRESS)).thenReturn(complaints)

      mockMvc.perform(get("/api/complaint/all").param("status", "IN_PROGRESS"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
    }

    "return 200 with empty list" in {
      when(complaintService.getAllComplaints(null)).thenReturn(List.empty)

      mockMvc.perform(get("/api/complaint/all"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isEmpty)
    }

    "return 400 for invalid status param" in {
      mockMvc.perform(get("/api/complaint/all").param("status", "INVALID"))
        .andExpect(status().isBadRequest)
    }
  }


  "GET /api/complaint/user" should {

    "return 200 with complaints for valid userId" in {
      val complaints = List(sampleComplaintResponse(), sampleComplaintResponse(id = 2L))
      when(complaintService.getAllComplaintsByUserId(5L)).thenReturn(complaints)

      mockMvc.perform(get("/api/complaint/user").param("userId", "5"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[1].id").value(2))
    }

    "return 404 when user not found" in {
      when(complaintService.getAllComplaintsByUserId(999L))
        .thenThrow(new EntityNotFoundException("User not found"))

      mockMvc.perform(get("/api/complaint/user").param("userId", "999"))
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("User not found"))
    }

    "return 200 with empty list when user has no complaints" in {
      when(complaintService.getAllComplaintsByUserId(5L)).thenReturn(List.empty)

      mockMvc.perform(get("/api/complaint/user").param("userId", "5"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isEmpty)
    }

    "return 400 when userId param is missing" in {
      mockMvc.perform(get("/api/complaint/user"))
        .andExpect(status().isBadRequest)
    }

    "return 400 when userId param is not a number" in {
      mockMvc.perform(get("/api/complaint/user").param("userId", "abc"))
        .andExpect(status().isBadRequest)
    }
  }


  "GET /api/complaint/{complaintId}" should {

    "return 200 with valid complaintId" in {
      val response = sampleComplaintResponse()
      when(complaintService.getComplaintById(1L)).thenReturn(response)

      mockMvc.perform(get("/api/complaint/1"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.userId").value(5))
        .andExpect(jsonPath("$.assetId").value(10))
        .andExpect(jsonPath("$.description").value("Screen flickering issue"))
        .andExpect(jsonPath("$.status").value("OPEN"))
    }

    "return 404 when complaint not found" in {
      when(complaintService.getComplaintById(999L))
        .thenThrow(new EntityNotFoundException("Complaint not found"))

      mockMvc.perform(get("/api/complaint/999"))
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("Complaint not found"))
    }

    "return 400 for invalid complaintId path variable" in {
      mockMvc.perform(get("/api/complaint/abc"))
        .andExpect(status().isBadRequest)
    }
  }


  "GET /api/complaint/stats" should {

    "return 200 with complaint statistics" in {
      val stats = sampleComplaintStats()
      when(complaintService.getComplaintStats).thenReturn(stats)

      mockMvc.perform(get("/api/complaint/stats"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.totalComplaints").value(15))
        .andExpect(jsonPath("$.open").value(5))
        .andExpect(jsonPath("$.inProgress").value(4))
        .andExpect(jsonPath("$.resolved").value(6))
    }
  }
}
