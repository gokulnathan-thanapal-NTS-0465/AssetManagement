package com.example.demo.Controller

import com.example.demo.DTO.{AssetCountDTO, AssetCreationDTO, AssetResponseDTO, AssetStatusDTO, AssetUpdateDTO}
import com.example.demo.Model.Enums.{AssetStatus, Category}
import com.example.demo.Service.AssetService
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

class AssetControllerTest extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var assetService: AssetService = uninitialized
  var mockMvc: MockMvc = uninitialized

  override def beforeEach(): Unit = {
    assetService = mock[AssetService]
    mockMvc = MockMvcBuilders
      .standaloneSetup(new AssetController(assetService))
      .setControllerAdvice(new GlobalExceptionHandler())
      .build()
  }

  private def sampleAssetResponse(): AssetResponseDTO = {
    val dto = new AssetResponseDTO
    dto.id = 1L
    dto.serialNumber = "LAP-0001"
    dto.modelName = "ThinkPad X1"
    dto.status = AssetStatus.AVAILABLE
    dto.category = Category.LAPTOP
    dto.credit = 30
    dto
  }

  private def sampleAssetCountDTO(): AssetCountDTO = {
    val dto = new AssetCountDTO
    dto.total = 10L
    dto.laptop = 3L
    dto.desktop = 2L
    dto.mouse = 2L
    dto.keyboard = 1L
    dto.mobile = 2L
    dto
  }


  "POST /api/asset/create" should {

    "return 201 with valid payload" in {
      val response = sampleAssetResponse()
      when(assetService.createAsset(any[AssetCreationDTO])).thenReturn(response)

      mockMvc.perform(
          post("/api/asset/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"modelName":"ThinkPad X1","category":"LAPTOP"}""")
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.serialNumber").value("LAP-0001"))
        .andExpect(jsonPath("$.modelName").value("ThinkPad X1"))
        .andExpect(jsonPath("$.status").value("AVAILABLE"))
        .andExpect(jsonPath("$.category").value("LAPTOP"))
        .andExpect(jsonPath("$.credit").value(30))
    }

 
    
    
  }


  "PATCH /api/asset/status/{assetId}" should {

    "return 200 with valid status update" in {
      val response = sampleAssetResponse()
      response.status = AssetStatus.MAINTENANCE
      when(assetService.updateAssetStatusById(eqTo(1L), any[AssetStatusDTO])).thenReturn(response)

      mockMvc.perform(
          patch("/api/asset/status/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"status":"MAINTENANCE"}""")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.status").value("MAINTENANCE"))
    }

    "return 404 when asset not found" in {
      when(assetService.updateAssetStatusById(eqTo(999L), any[AssetStatusDTO]))
        .thenThrow(new EntityNotFoundException("Asset not found"))

      mockMvc.perform(
          patch("/api/asset/status/999")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"status":"MAINTENANCE"}""")
        )
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("Asset not found"))
    }

    "return 400 when status is null" in {
      when(assetService.updateAssetStatusById(eqTo(1L), any[AssetStatusDTO]))
        .thenThrow(new IllegalArgumentException("Status is required"))

      mockMvc.perform(
          patch("/api/asset/status/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{}""")
        )
        .andExpect(status().isBadRequest)
    }

    "return 400 when trying to directly assign" in {
      when(assetService.updateAssetStatusById(eqTo(1L), any[AssetStatusDTO]))
        .thenThrow(new IllegalStateException("Asset cannot be directly assigned to a user !"))

      mockMvc.perform(
          patch("/api/asset/status/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"status":"ASSIGNED"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Asset cannot be directly assigned to a user !"))
    }

    "return 400 when asset is retired" in {
      when(assetService.updateAssetStatusById(eqTo(1L), any[AssetStatusDTO]))
        .thenThrow(new IllegalStateException("Retired asset cannot be updated as MAINTENANCE"))

      mockMvc.perform(
          patch("/api/asset/status/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"status":"MAINTENANCE"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Retired asset cannot be updated as MAINTENANCE"))
    }

    "return 400 when asset is currently assigned" in {
      when(assetService.updateAssetStatusById(eqTo(1L), any[AssetStatusDTO]))
        .thenThrow(new IllegalStateException("Asset is currently assigned to a user and cannot be updated !"))

      mockMvc.perform(
          patch("/api/asset/status/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"status":"AVAILABLE"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Asset is currently assigned to a user and cannot be updated !"))
    }

  }


  "PATCH /api/asset/{assetId}" should {

    "return 200 with valid model name update" in {
      val response = sampleAssetResponse()
      response.modelName = "ThinkPad X2"
      when(assetService.updateAssetById(eqTo(1L), any[AssetUpdateDTO])).thenReturn(response)

      mockMvc.perform(
          patch("/api/asset/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"modelName":"ThinkPad X2"}""")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.modelName").value("ThinkPad X2"))
    }

    "return 404 when asset not found" in {
      when(assetService.updateAssetById(eqTo(999L), any[AssetUpdateDTO]))
        .thenThrow(new EntityNotFoundException("Asset not found "))

      mockMvc.perform(
          patch("/api/asset/999")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"modelName":"ThinkPad X2"}""")
        )
        .andExpect(status().isNotFound)
    }
    
  }


  "DELETE /api/asset/{assetId}" should {

    "return 200 when asset deleted successfully" in {
      mockMvc.perform(delete("/api/asset/1"))
        .andExpect(status().isOk)
        .andExpect(content().string("Asset deleted Successfully"))

      verify(assetService).deleteAssetById(1L)
    }

    "return 404 when asset not found" in {
      when(assetService.deleteAssetById(999L)).thenThrow(new EntityNotFoundException("Asset not found "))

      mockMvc.perform(delete("/api/asset/999"))
        .andExpect(status().isNotFound)
    }

    "return 400 when asset is currently assigned" in {
      when(assetService.deleteAssetById(1L))
        .thenThrow(new IllegalStateException("Asset is currently assigned to a user and cannot be deleted !"))

      mockMvc.perform(delete("/api/asset/1"))
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Asset is currently assigned to a user and cannot be deleted !"))
    }
  }


  "GET /api/asset/user/{userId}" should {

    "return 200 with assets for valid userId" in {
      val assets = List(sampleAssetResponse())
      when(assetService.getAssetsByUserId(eqTo(1L), any[AssetStatus], any[Category])).thenReturn(assets)

      mockMvc.perform(get("/api/asset/user/1"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].serialNumber").value("LAP-0001"))
    }

    "return 200 with status filter" in {
      val assets = List(sampleAssetResponse())
      when(assetService.getAssetsByUserId(eqTo(1L), eqTo(AssetStatus.AVAILABLE), any[Category])).thenReturn(assets)

      mockMvc.perform(
          get("/api/asset/user/1").param("status", "AVAILABLE")
        )
        .andExpect(status().isOk)
    }

    "return 200 with category filter" in {
      val assets = List(sampleAssetResponse())
      when(assetService.getAssetsByUserId(eqTo(1L), any[AssetStatus], eqTo(Category.LAPTOP))).thenReturn(assets)

      mockMvc.perform(
          get("/api/asset/user/1").param("category", "LAPTOP")
        )
        .andExpect(status().isOk)
    }

    "return 200 with both filters" in {
      val assets = List(sampleAssetResponse())
      when(assetService.getAssetsByUserId(eqTo(1L), eqTo(AssetStatus.AVAILABLE), eqTo(Category.LAPTOP))).thenReturn(assets)

      mockMvc.perform(
          get("/api/asset/user/1")
            .param("status", "AVAILABLE")
            .param("category", "LAPTOP")
        )
        .andExpect(status().isOk)
    }

    "return 200 with empty list when no assets found" in {
      when(assetService.getAssetsByUserId(eqTo(1L), any[AssetStatus], any[Category])).thenReturn(List.empty)

      mockMvc.perform(get("/api/asset/user/1"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isEmpty)
    }

    "return 400 for invalid userId path variable" in {
      mockMvc.perform(get("/api/asset/user/abc"))
        .andExpect(status().isBadRequest)
    }


  }


  "GET /api/asset/all" should {

    "return 200 with no filters" in {
      val assets = List(sampleAssetResponse())
      when(assetService.getAllAssets(any[AssetStatus], any[Category])).thenReturn(assets)

      mockMvc.perform(get("/api/asset/all"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].modelName").value("ThinkPad X1"))
    }

    "return 200 with status filter" in {
      val assets = List(sampleAssetResponse())
      when(assetService.getAllAssets(eqTo(AssetStatus.AVAILABLE), any[Category])).thenReturn(assets)

      mockMvc.perform(
          get("/api/asset/all").param("status", "AVAILABLE")
        )
        .andExpect(status().isOk)
    }

    "return 200 with category filter" in {
      val assets = List(sampleAssetResponse())
      when(assetService.getAllAssets(any[AssetStatus], eqTo(Category.LAPTOP))).thenReturn(assets)

      mockMvc.perform(
          get("/api/asset/all").param("category", "LAPTOP")
        )
        .andExpect(status().isOk)
    }

    "return 200 with both filters" in {
      val assets = List(sampleAssetResponse())
      when(assetService.getAllAssets(eqTo(AssetStatus.MAINTENANCE), eqTo(Category.DESKTOP))).thenReturn(assets)

      mockMvc.perform(
          get("/api/asset/all")
            .param("status", "MAINTENANCE")
            .param("category", "DESKTOP")
        )
        .andExpect(status().isOk)
    }

    "return 200 with empty list" in {
      when(assetService.getAllAssets(any[AssetStatus], any[Category])).thenReturn(List.empty)

      mockMvc.perform(get("/api/asset/all"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isEmpty)
    }
  }


  "GET /api/asset/count" should {

    "return 200 with asset counts" in {
      val countDTO = sampleAssetCountDTO()
      when(assetService.getAllAssetCount).thenReturn(countDTO)

      mockMvc.perform(get("/api/asset/count"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.total").value(10))
        .andExpect(jsonPath("$.laptop").value(3))
        .andExpect(jsonPath("$.desktop").value(2))
        .andExpect(jsonPath("$.mouse").value(2))
        .andExpect(jsonPath("$.keyboard").value(1))
        .andExpect(jsonPath("$.mobile").value(2))
    }
  }


  "GET /api/asset/{assetId}" should {

    "return 200 with valid assetId" in {
      val response = sampleAssetResponse()
      when(assetService.getAssetById(1L)).thenReturn(response)

      mockMvc.perform(get("/api/asset/1"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.serialNumber").value("LAP-0001"))
        .andExpect(jsonPath("$.modelName").value("ThinkPad X1"))
    }

    "return 404 when asset not found" in {
      when(assetService.getAssetById(999L)).thenThrow(new EntityNotFoundException("Asset not found "))

      mockMvc.perform(get("/api/asset/999"))
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("Asset not found "))
    }

    "return 400 for invalid assetId path variable" in {
      mockMvc.perform(get("/api/asset/abc"))
        .andExpect(status().isBadRequest)
    }
  }
}
