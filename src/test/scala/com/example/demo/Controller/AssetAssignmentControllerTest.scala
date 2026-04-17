package com.example.demo.Controller

import com.example.demo.DTO.AssetAssignmentResponseDTO
import com.example.demo.Service.AssetAssignmentService
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

class AssetAssignmentControllerTest extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var assetAssignmentService: AssetAssignmentService = _
  var mockMvc: MockMvc = _

  override def beforeEach(): Unit = {
    assetAssignmentService = mock[AssetAssignmentService]
    mockMvc = MockMvcBuilders
      .standaloneSetup(new AssetAssignmentController(assetAssignmentService))
      .setControllerAdvice(new GlobalExceptionHandler())
      .build()
  }

  private def sampleAssignmentResponse(
                                        id: Long = 1L,
                                        assetId: Long = 10L,
                                        userId: Long = 5L,
                                        assignedAt: String = "2026-04-10T10:00:00",
                                        returnedAt: String = null
                                      ): AssetAssignmentResponseDTO = {
    AssetAssignmentResponseDTO(
      id = Some(id),
      assetId = Some(assetId),
      userId = Some(userId),
      assignedAt = Some(assignedAt),
      returnedAt = Option(returnedAt)
    )
  }

  "POST /api/asset-assignment/return/{assignmentId}" should {

    "return 200 when asset returned successfully" in {
      val response = sampleAssignmentResponse(returnedAt = "2026-04-17T10:00:00")
      when(assetAssignmentService.returnAsset(1L)).thenReturn(response)

      mockMvc.perform(post("/api/asset-assignment/return/1"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.assetId").value(10))
        .andExpect(jsonPath("$.returnedAt").value("2026-04-17T10:00:00"))
    }

    "return 404 when assignment not found" in {
      when(assetAssignmentService.returnAsset(999L))
        .thenThrow(new EntityNotFoundException("Asset assignment not found"))

      mockMvc.perform(post("/api/asset-assignment/return/999"))
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("Asset assignment not found"))
    }

    "return 400 when asset already returned" in {
      when(assetAssignmentService.returnAsset(1L))
        .thenThrow(new IllegalStateException("Asset with id 10 has already been returned"))

      mockMvc.perform(post("/api/asset-assignment/return/1"))
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Asset with id 10 has already been returned"))
    }

    "return 400 for invalid assignmentId path variable" in {
      mockMvc.perform(post("/api/asset-assignment/return/abc"))
        .andExpect(status().isBadRequest)
    }
  }


  "GET /api/asset-assignment/{assignmentId}" should {

    "return 200 with valid assignment" in {
      val response = sampleAssignmentResponse()
      when(assetAssignmentService.getAssetAssignmentById(1L)).thenReturn(response)

      mockMvc.perform(get("/api/asset-assignment/1"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.assetId").value(10L))
        .andExpect(jsonPath("$.userId").value(5L))
        .andExpect(jsonPath("$.assignedAt").value("2026-04-10T10:00:00"))
        .andExpect(jsonPath("$.returnedAt").isEmpty)
    }

    "return 404 when assignment not found" in {
      when(assetAssignmentService.getAssetAssignmentById(999L))
        .thenThrow(new EntityNotFoundException("Asset assignment not found"))

      mockMvc.perform(get("/api/asset-assignment/999"))
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("Asset assignment not found"))
    }

    "return 400 for invalid assignmentId path variable" in {
      mockMvc.perform(get("/api/asset-assignment/abc"))
        .andExpect(status().isBadRequest)
    }
  }


  "GET /api/asset-assignment/user/{userId}" should {

    "return 200 with assignments for valid userId" in {
      val assignments = List(sampleAssignmentResponse(), sampleAssignmentResponse(id = 2L, assetId = 20L))
      when(assetAssignmentService.getAssignmentsByUserId(5L)).thenReturn(assignments)

      mockMvc.perform(get("/api/asset-assignment/user/5"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].assetId").value(20))
    }

    "return 404 when user not found" in {
      when(assetAssignmentService.getAssignmentsByUserId(999L))
        .thenThrow(new EntityNotFoundException("User not found"))

      mockMvc.perform(get("/api/asset-assignment/user/999"))
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("User not found"))
    }

    "return 200 with empty list when user has no assignments" in {
      when(assetAssignmentService.getAssignmentsByUserId(5L)).thenReturn(List.empty)

      mockMvc.perform(get("/api/asset-assignment/user/5"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isEmpty)
    }

    "return 400 for invalid userId path variable" in {
      mockMvc.perform(get("/api/asset-assignment/user/abc"))
        .andExpect(status().isBadRequest)
    }
  }


  "GET /api/asset-assignment/all" should {

    "return 200 with all assignments" in {
      val assignments = List(
        sampleAssignmentResponse(),
        sampleAssignmentResponse(id = 2L, assetId = 20L, userId = 6L),
        sampleAssignmentResponse(id = 3L, assetId = 30L, returnedAt = "2026-04-15T10:00:00")
      )
      when(assetAssignmentService.getAllAssignments).thenReturn(assignments)

      mockMvc.perform(get("/api/asset-assignment/all"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[2].id").value(3))
        .andExpect(jsonPath("$[2].returnedAt").value("2026-04-15T10:00:00"))
    }

    "return 200 with empty list when no assignments exist" in {
      when(assetAssignmentService.getAllAssignments).thenReturn(List.empty)

      mockMvc.perform(get("/api/asset-assignment/all"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isEmpty)
    }
  }


  "GET /api/asset-assignment/status" should {

    "return 200 with returned assignments when returned=true" in {
      val assignments = List(
        sampleAssignmentResponse(returnedAt = "2026-04-15T10:00:00"),
        sampleAssignmentResponse(id = 2L, assetId = 20L, returnedAt = "2026-04-16T10:00:00")
      )
      when(assetAssignmentService.getAllAssignmentsByStatus(true)).thenReturn(assignments)

      mockMvc.perform(
          get("/api/asset-assignment/status").param("returned", "true")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].returnedAt").value("2026-04-15T10:00:00"))
        .andExpect(jsonPath("$[1].returnedAt").value("2026-04-16T10:00:00"))
    }

    "return 200 with active assignments when returned=false" in {
      val assignments = List(sampleAssignmentResponse())
      when(assetAssignmentService.getAllAssignmentsByStatus(false)).thenReturn(assignments)

      mockMvc.perform(
          get("/api/asset-assignment/status").param("returned", "false")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].returnedAt").isEmpty)
    }

    "return 200 with empty list when no matching assignments" in {
      when(assetAssignmentService.getAllAssignmentsByStatus(true)).thenReturn(List.empty)

      mockMvc.perform(
          get("/api/asset-assignment/status").param("returned", "true")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isEmpty)
    }

    "return 400 when returned param is invalid" in {
      mockMvc.perform(
          get("/api/asset-assignment/status").param("returned", "notboolean")
        )
        .andExpect(status().isBadRequest)
    }
  }
}
