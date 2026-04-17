package com.example.demo.Service

import com.example.demo.DTO.{AssetCountDTO, AssetCreationDTO, AssetResponseDTO, AssetStatusDTO, AssetUpdateDTO}
import com.example.demo.Model.{Asset, AssetAssignment, Credit, User}
import com.example.demo.Model.Enums.{AssetStatus, Category, UserType}
import com.example.demo.Repo.{AssetAssignmentRepository, AssetRepository}
import jakarta.persistence.EntityNotFoundException
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

import java.time.{LocalDate, LocalDateTime}
import java.util.Optional
import scala.compiletime.uninitialized

class AssetServiceTest extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var assetRepo: AssetRepository = uninitialized
  var assetAssignmentRepo: AssetAssignmentRepository = uninitialized

  var assetService: AssetService = uninitialized

  override def beforeEach(): Unit = {
    assetRepo = mock[AssetRepository]
    assetAssignmentRepo = mock[AssetAssignmentRepository]
    assetService = new AssetService(assetRepo, assetAssignmentRepo)
  }


  def createSampleAsset(
                         id: Long = 1L,
                         serialNumber: String = "LAP-0001",
                         modelName: String = "Dell XPS 15",
                         status: AssetStatus = AssetStatus.AVAILABLE,
                         category: Category = Category.LAPTOP,
                         credit: Int = 75
                       ): Asset = {
    val asset = new Asset()
    asset.id = id
    asset.serialNumber = serialNumber
    asset.modelName = modelName
    asset.status = status
    asset.category = category
    asset.credit = credit
    asset
  }

  def createSampleUser(
                        id: Long = 1L,
                        username: String = "john.doe"
                      ): User = {
    val user = new User()
    user.id = id
    user.username = username
    user.userType = UserType.EMPLOYEE
    user.department = "IT"
    user.joinedDate = LocalDate.now()
    user.deactivated = false
    user
  }

  def createSampleAssetAssignment(
                                   id: Long = 1L,
                                   asset: Asset,
                                   user: User,
                                   returnedAt: LocalDateTime = null
                                 ): AssetAssignment = {
    val assignment = new AssetAssignment()
    assignment.id = id
    assignment.asset = asset
    assignment.user = user
    assignment.assignedAt = LocalDateTime.now().minusDays(5)
    assignment.returnedAt = returnedAt
    assignment
  }


  "AssetService" when {

    "createAsset" should {

      "create asset successfully with valid data" in {
        val dto = AssetCreationDTO(
          modelName = "MacBook Pro M3",
          category = Category.LAPTOP
        )

        val savedAsset = createSampleAsset(
          modelName = "MacBook Pro M3",
          category = Category.LAPTOP,
          status=AssetStatus.AVAILABLE
        )

        when(assetRepo.countByCategory(Category.LAPTOP)).thenReturn(0L)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(savedAsset)

        val result = assetService.createAsset(dto)

        result.modelName shouldBe "MacBook Pro M3"
        result.category shouldBe Category.LAPTOP
        result.status shouldBe AssetStatus.AVAILABLE
        verify(assetRepo).save(ArgumentMatchers.any[Asset])
      }

      "generate correct serial number for LAPTOP" in {
        val dto = AssetCreationDTO(
          modelName = "Dell XPS",
          category = Category.LAPTOP
        )

        val savedAsset = createSampleAsset( category = Category.LAPTOP)

        when(assetRepo.countByCategory(Category.LAPTOP)).thenReturn(0L)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(savedAsset)

        val result = assetService.createAsset(dto)

        result.serialNumber should startWith("LAP-")
      }

      "generate correct serial number for MOBILE" in {
        val dto = AssetCreationDTO(
          modelName = "iPhone 15 Pro",
          category = Category.MOBILE
        )

        val savedAsset = createSampleAsset(
          serialNumber = "MOB-0001",
          modelName = "iPhone 15 Pro",
          category = Category.MOBILE,
          credit = 50
        )

        when(assetRepo.countByCategory(Category.MOBILE)).thenReturn(0L)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(savedAsset)

        val result = assetService.createAsset(dto)

        result.serialNumber should startWith("MOB-")
      }

      "generate correct serial number for DESKTOP" in {
        val dto = AssetCreationDTO(
          modelName = "iMac 24",
          category = Category.DESKTOP
        )

        val savedAsset = createSampleAsset(
          serialNumber = "DES-0001",
          category = Category.DESKTOP,
        )

        when(assetRepo.countByCategory(Category.DESKTOP)).thenReturn(0L)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(savedAsset)

        val result = assetService.createAsset(dto)

        result.serialNumber should startWith("DES-")
      }

      "generate correct serial number for KEYBOARD" in {
        val dto = AssetCreationDTO(
          modelName = "Logitech MX Keys",
          category = Category.KEYBOARD
        )

        val savedAsset = createSampleAsset(
          serialNumber = "KEY-0001",
          category = Category.KEYBOARD,
          credit = 50
        )

        when(assetRepo.countByCategory(Category.KEYBOARD)).thenReturn(0L)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(savedAsset)

        val result = assetService.createAsset(dto)

        result.serialNumber should startWith("KEY-")
      }

      "generate correct serial number for MOUSE" in {
        val dto = AssetCreationDTO(
          modelName = "Logitech MX Master 3",
          category = Category.MOUSE
        )

        val savedAsset = createSampleAsset(
          serialNumber = "MOU-0001",
          category = Category.MOUSE,
          credit = 15
        )

        when(assetRepo.countByCategory(Category.MOUSE)).thenReturn(0L)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(savedAsset)

        val result = assetService.createAsset(dto)

        result.serialNumber should startWith("MOU-")
      }

      "generate incremented serial number when assets already exist" in {
        val dto = AssetCreationDTO(
          modelName = "Dell XPS",
          category = Category.LAPTOP
        )

        val savedAsset = createSampleAsset(serialNumber = "LAP-0006")

        when(assetRepo.countByCategory(Category.LAPTOP)).thenReturn(5L)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(savedAsset)

        assetService.createAsset(dto)

        verify(assetRepo).countByCategory(Category.LAPTOP)
      }

      "set correct credit for LAPTOP category" in {
        val dto = AssetCreationDTO(
          modelName = "Dell XPS",
          category = Category.LAPTOP
        )

        val savedAsset = createSampleAsset(category = Category.LAPTOP, credit = 75)

        when(assetRepo.countByCategory(Category.LAPTOP)).thenReturn(0L)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(savedAsset)

        val result = assetService.createAsset(dto)

        result.credit shouldBe 75
      }

      "set correct credit for MOUSE category" in {
        val dto = AssetCreationDTO(
          modelName = "Logitech Mouse",
          category = Category.MOUSE
        )

        val savedAsset = createSampleAsset(category = Category.MOUSE, credit = 15)

        when(assetRepo.countByCategory(Category.MOUSE)).thenReturn(0L)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(savedAsset)

        val result = assetService.createAsset(dto)

        result.credit shouldBe 15
      }

      "set status as AVAILABLE for new asset" in {
        val dto = AssetCreationDTO(
          modelName = "Dell XPS",
          category = Category.LAPTOP
        )

        val savedAsset = createSampleAsset(status = AssetStatus.AVAILABLE)

        when(assetRepo.countByCategory(Category.LAPTOP)).thenReturn(0L)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(savedAsset)

        val result = assetService.createAsset(dto)

        result.status shouldBe AssetStatus.AVAILABLE
      }
    }

    "updateAssetStatusById" should {

      "update status successfully from AVAILABLE to MAINTENANCE" in {
        val asset = createSampleAsset(status = AssetStatus.AVAILABLE)
        val dto = AssetStatusDTO(status = Some(AssetStatus.MAINTENANCE))

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        val result = assetService.updateAssetStatusById(1L, dto)

        verify(assetRepo).save(ArgumentMatchers.any[Asset])
      }

      "update status successfully from AVAILABLE to RETIRED" in {
        val asset = createSampleAsset(status = AssetStatus.AVAILABLE)
        val dto = AssetStatusDTO(status = Some(AssetStatus.RETIRED))

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        assetService.updateAssetStatusById(1L, dto)

        verify(assetRepo).save(ArgumentMatchers.any[Asset])
      }

      "update status from MAINTENANCE to AVAILABLE when not assigned" in {
        val asset = createSampleAsset(status = AssetStatus.MAINTENANCE)
        val dto = AssetStatusDTO(status = Some(AssetStatus.AVAILABLE))

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetAssignmentRepo.findByAssetIdAndReturnedAtIsNull(1L)).thenReturn(null)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        assetService.updateAssetStatusById(1L, dto)

        verify(assetRepo).save(ArgumentMatchers.any[Asset])
      }

      "throw EntityNotFoundException when asset not found" in {
        val dto = AssetStatusDTO(status = Some(AssetStatus.MAINTENANCE))

        when(assetRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          assetService.updateAssetStatusById(999L, dto)
        } should have message "Asset not found"
      }

      "throw IllegalArgumentException when status is null" in {
        val asset = createSampleAsset()
        val dto = AssetStatusDTO(status = null)

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))

        the[IllegalArgumentException] thrownBy {
          assetService.updateAssetStatusById(1L, dto)
        } should have message "Status is required"
      }

      "throw IllegalStateException when trying to set status to ASSIGNED directly" in {
        val asset = createSampleAsset(status = AssetStatus.AVAILABLE)
        val dto = AssetStatusDTO(status = Some(AssetStatus.ASSIGNED))

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))

        the[IllegalStateException] thrownBy {
          assetService.updateAssetStatusById(1L, dto)
        } should have message "Asset cannot be directly assigned to a user !"
      }

      "throw IllegalStateException when updating RETIRED asset" in {
        val asset = createSampleAsset(status = AssetStatus.RETIRED)
        val dto = AssetStatusDTO(status = Some(AssetStatus.AVAILABLE))

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))

        the[IllegalStateException] thrownBy {
          assetService.updateAssetStatusById(1L, dto)
        } should have message "Retired asset cannot be updated as AVAILABLE"
      }

      "throw IllegalStateException when updating RETIRED asset to MAINTENANCE" in {
        val asset = createSampleAsset(status = AssetStatus.RETIRED)
        val dto = AssetStatusDTO(status = Some(AssetStatus.MAINTENANCE))

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))

        the[IllegalStateException] thrownBy {
          assetService.updateAssetStatusById(1L, dto)
        } should have message "Retired asset cannot be updated as MAINTENANCE"
      }

      "throw IllegalStateException when asset is currently assigned to a user" in {
        val asset = createSampleAsset(status = AssetStatus.ASSIGNED)
        val user = createSampleUser()
        val assignment = createSampleAssetAssignment(asset = asset, user = user)
        val dto = AssetStatusDTO(status = Some(AssetStatus.MAINTENANCE))

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetAssignmentRepo.findByAssetIdAndReturnedAtIsNull(1L)).thenReturn(assignment)

        the[IllegalStateException] thrownBy {
          assetService.updateAssetStatusById(1L, dto)
        } should have message "Asset is currently assigned to a user and cannot be updated !"
      }

      "throw IllegalStateException when MAINTENANCE asset has active assignment" in {
        val asset = createSampleAsset(status = AssetStatus.MAINTENANCE)
        val user = createSampleUser()
        val assignment = createSampleAssetAssignment(asset = asset, user = user)
        val dto = AssetStatusDTO(status = Some(AssetStatus.AVAILABLE))

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetAssignmentRepo.findByAssetIdAndReturnedAtIsNull(1L)).thenReturn(assignment)

        the[IllegalStateException] thrownBy {
          assetService.updateAssetStatusById(1L, dto)
        } should have message "Asset is currently assigned to a user and cannot be updated !"
      }
    }

    "updateAssetById" should {

      "update model name successfully" in {
        val existingAsset = createSampleAsset(modelName = "Old Model")
        val updatedAsset = createSampleAsset(modelName = "New Model")
        val dto = AssetUpdateDTO(modelName = "New Model")

        when(assetRepo.findById(1L)).thenReturn(Optional.of(existingAsset))
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(updatedAsset)

        val result = assetService.updateAssetById(1L, dto)

        result.modelName shouldBe "New Model"
        verify(assetRepo).save(ArgumentMatchers.any[Asset])
      }

      "throw EntityNotFoundException when asset not found" in {
        val dto = AssetUpdateDTO(modelName = "New Model")

        when(assetRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          assetService.updateAssetById(999L, dto)
        } should have message "Asset not found "
      }

      "throw IllegalArgumentException when modelName is not provided" in {
        val existingAsset = createSampleAsset()
        val dto = AssetUpdateDTO(modelName =null)

        when(assetRepo.findById(1L)).thenReturn(Optional.of(existingAsset))

        the[IllegalArgumentException] thrownBy {
          assetService.updateAssetById(1L, dto)
        } should have message "Model name required"
      }

      "preserve other asset properties when updating model name" in {
        val existingAsset = createSampleAsset(
          modelName = "Old Model",
          status = AssetStatus.AVAILABLE,
          category = Category.LAPTOP,
        )
        val dto = AssetUpdateDTO(modelName = "New Model")

        when(assetRepo.findById(1L)).thenReturn(Optional.of(existingAsset))
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(existingAsset)

        val result = assetService.updateAssetById(1L, dto)

        result.serialNumber shouldBe "LAP-0001"
        result.category shouldBe Category.LAPTOP
        result.status shouldBe AssetStatus.AVAILABLE
      }
    }

    "deleteAssetById" should {

      "mark AVAILABLE asset as RETIRED successfully" in {
        val asset = createSampleAsset(status = AssetStatus.AVAILABLE)

        when(assetAssignmentRepo.findByAssetIdAndReturnedAtIsNull(1L)).thenReturn(null)
        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        assetService.deleteAssetById(1L)

        verify(assetRepo).save(ArgumentMatchers.any[Asset])
      }

      "mark MAINTENANCE asset as RETIRED when not assigned" in {
        val asset = createSampleAsset(status = AssetStatus.MAINTENANCE)

        when(assetAssignmentRepo.findByAssetIdAndReturnedAtIsNull(1L)).thenReturn(null)
        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        assetService.deleteAssetById(1L)

        verify(assetRepo).save(ArgumentMatchers.any[Asset])
      }

      "mark already RETIRED asset as RETIRED (no change)" in {
        val asset = createSampleAsset(status = AssetStatus.RETIRED)

        when(assetAssignmentRepo.findByAssetIdAndReturnedAtIsNull(1L)).thenReturn(null)
        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        assetService.deleteAssetById(1L)

        verify(assetRepo).save(ArgumentMatchers.any[Asset])
      }

      "allow deletion when asset was returned (returnedAt is not null)" in {
        val asset = createSampleAsset(status = AssetStatus.AVAILABLE)
        val user = createSampleUser()
        val assignment = createSampleAssetAssignment(
          asset = asset,
          user = user,
          returnedAt = LocalDateTime.now().minusDays(1)
        )

        when(assetAssignmentRepo.findByAssetIdAndReturnedAtIsNull(1L)).thenReturn(null)
        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        assetService.deleteAssetById(1L)

        verify(assetRepo).save(ArgumentMatchers.any[Asset])
      }

      "throw IllegalStateException when ASSIGNED asset is currently in use" in {
        val asset = createSampleAsset(status = AssetStatus.ASSIGNED)
        val user = createSampleUser()
        val assignment = createSampleAssetAssignment(asset = asset, user = user)

        when(assetAssignmentRepo.findByAssetIdAndReturnedAtIsNull(1L)).thenReturn(assignment)

        the[IllegalStateException] thrownBy {
          assetService.deleteAssetById(1L)
        } should have message "Asset is currently assigned to a user and cannot be deleted !"
      }

      "throw EntityNotFoundException when asset not found" in {
        when(assetAssignmentRepo.findByAssetIdAndReturnedAtIsNull(999L)).thenReturn(null)
        when(assetRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          assetService.deleteAssetById(999L)
        } should have message "Asset not found "
      }
    }

    "getAssetsByUserId" should {

      "return all assets for user when no filters" in {
        val assets = java.util.Arrays.asList(
          createSampleAsset(),
          createSampleAsset(id = 2L, serialNumber = "MOB-0001", category = Category.MOBILE)
        )

        when(assetRepo.findAssetByUserId(1L)).thenReturn(assets)

        val result = assetService.getAssetsByUserId(1L, null, null)

        result should have size 2
        verify(assetRepo).findAssetByUserId(1L)
      }

      "filter by status only" in {
        val assets = java.util.Arrays.asList(
          createSampleAsset(status = AssetStatus.ASSIGNED)
        )

        when(assetRepo.findAssetByUserIdAndStatus(AssetStatus.ASSIGNED, 1L)).thenReturn(assets)

        val result = assetService.getAssetsByUserId(1L, AssetStatus.ASSIGNED, null)

        result should have size 1
        verify(assetRepo).findAssetByUserIdAndStatus(AssetStatus.ASSIGNED, 1L)
      }

      "filter by category only" in {
        val assets = java.util.Arrays.asList(
          createSampleAsset(category = Category.LAPTOP),
          createSampleAsset(id = 2L, category = Category.LAPTOP)
        )

        when(assetRepo.findAssetByUserIdAndCategory(1L, Category.LAPTOP)).thenReturn(assets)

        val result = assetService.getAssetsByUserId(1L, null, Category.LAPTOP)

        result should have size 2
        verify(assetRepo).findAssetByUserIdAndCategory(1L, Category.LAPTOP)
      }

      "filter by both status and category" in {
        val assets = java.util.Arrays.asList(
          createSampleAsset(status = AssetStatus.ASSIGNED, category = Category.LAPTOP)
        )

        when(assetRepo.findAssetByUserIdAndStatusAndCategory(AssetStatus.ASSIGNED, 1L, Category.LAPTOP))
          .thenReturn(assets)

        val result = assetService.getAssetsByUserId(1L, AssetStatus.ASSIGNED, Category.LAPTOP)

        result should have size 1
        verify(assetRepo).findAssetByUserIdAndStatusAndCategory(AssetStatus.ASSIGNED, 1L, Category.LAPTOP)
      }

      "return empty list when user has no assets" in {
        when(assetRepo.findAssetByUserId(1L)).thenReturn(java.util.Collections.emptyList())

        val result = assetService.getAssetsByUserId(1L, null, null)

        result shouldBe empty
      }

      "return empty list when no assets match filters" in {
        when(assetRepo.findAssetByUserIdAndStatus(AssetStatus.MAINTENANCE, 1L))
          .thenReturn(java.util.Collections.emptyList())

        val result = assetService.getAssetsByUserId(1L, AssetStatus.MAINTENANCE, null)

        result shouldBe empty
      }
    }

    "getAllAssets" should {

      "return all assets when no filters" in {
        val assets = java.util.Arrays.asList(
          createSampleAsset(),
          createSampleAsset(id = 2L),
          createSampleAsset(id = 3L)
        )

        when(assetRepo.findAll()).thenReturn(assets)

        val result = assetService.getAllAssets(null, null)

        result should have size 3
        verify(assetRepo).findAll()
      }

      "filter by status only (AVAILABLE)" in {
        val assets = java.util.Arrays.asList(
          createSampleAsset(status = AssetStatus.AVAILABLE),
          createSampleAsset(id = 2L, status = AssetStatus.AVAILABLE)
        )

        when(assetRepo.findAllByStatus(AssetStatus.AVAILABLE)).thenReturn(assets)

        val result = assetService.getAllAssets(AssetStatus.AVAILABLE, null)

        result should have size 2
        verify(assetRepo).findAllByStatus(AssetStatus.AVAILABLE)
      }

      "filter by category only (LAPTOP)" in {
        val assets = java.util.Arrays.asList(
          createSampleAsset(category = Category.LAPTOP),
          createSampleAsset(id = 2L, category = Category.LAPTOP)
        )

        when(assetRepo.findAllByCategory(Category.LAPTOP)).thenReturn(assets)

        val result = assetService.getAllAssets(null, Category.LAPTOP)

        result should have size 2
        verify(assetRepo).findAllByCategory(Category.LAPTOP)
      }
      

      "filter by both status and category" in {
        val assets = java.util.Arrays.asList(
          createSampleAsset(status = AssetStatus.AVAILABLE, category = Category.LAPTOP)
        )

        when(assetRepo.findAllByStatusAndCategory(AssetStatus.AVAILABLE, Category.LAPTOP))
          .thenReturn(assets)

        val result = assetService.getAllAssets(AssetStatus.AVAILABLE, Category.LAPTOP)

        result should have size 1
        verify(assetRepo).findAllByStatusAndCategory(AssetStatus.AVAILABLE, Category.LAPTOP)
      }

      "return empty list when no assets exist" in {
        when(assetRepo.findAll()).thenReturn(java.util.Collections.emptyList())
        val result = assetService.getAllAssets(null, null)

        result shouldBe empty
      }

      "return empty list when no assets match filters" in {
        when(assetRepo.findAllByStatusAndCategory(AssetStatus.RETIRED, Category.MOBILE))
          .thenReturn(java.util.Collections.emptyList())
        val result = assetService.getAllAssets(AssetStatus.RETIRED, Category.MOBILE)

        result shouldBe empty
      }
    }

    "getAllAssetCount" should {

      "return correct count for all categories" in {
        when(assetRepo.countByStatus(AssetStatus.AVAILABLE)).thenReturn(20L)
        when(assetRepo.countByCategoryAndStatus(Category.LAPTOP, AssetStatus.AVAILABLE)).thenReturn(5L)
        when(assetRepo.countByCategoryAndStatus(Category.MOBILE, AssetStatus.AVAILABLE)).thenReturn(4L)
        when(assetRepo.countByCategoryAndStatus(Category.DESKTOP, AssetStatus.AVAILABLE)).thenReturn(3L)
        when(assetRepo.countByCategoryAndStatus(Category.KEYBOARD, AssetStatus.AVAILABLE)).thenReturn(5L)
        when(assetRepo.countByCategoryAndStatus(Category.MOUSE, AssetStatus.AVAILABLE)).thenReturn(3L)

        val result = assetService.getAllAssetCount

        result.total shouldBe 20L
        result.laptop shouldBe 5L
        result.mobile shouldBe 4L
        result.desktop shouldBe 3L
        result.keyboard shouldBe 5L
        result.mouse shouldBe 3L
      }

      "return zero counts when no available assets" in {
        when(assetRepo.countByStatus(AssetStatus.AVAILABLE)).thenReturn(0L)
        when(assetRepo.countByCategoryAndStatus(Category.LAPTOP, AssetStatus.AVAILABLE)).thenReturn(0L)
        when(assetRepo.countByCategoryAndStatus(Category.MOBILE, AssetStatus.AVAILABLE)).thenReturn(0L)
        when(assetRepo.countByCategoryAndStatus(Category.DESKTOP, AssetStatus.AVAILABLE)).thenReturn(0L)
        when(assetRepo.countByCategoryAndStatus(Category.KEYBOARD, AssetStatus.AVAILABLE)).thenReturn(0L)
        when(assetRepo.countByCategoryAndStatus(Category.MOUSE, AssetStatus.AVAILABLE)).thenReturn(0L)

        val result = assetService.getAllAssetCount

        result.total shouldBe 0L
        result.laptop shouldBe 0L
        result.mobile shouldBe 0L
        result.desktop shouldBe 0L
        result.keyboard shouldBe 0L
        result.mouse shouldBe 0L
      }

      "count only AVAILABLE assets (not assigned or retired)" in {
        when(assetRepo.countByStatus(AssetStatus.AVAILABLE)).thenReturn(10L)
        when(assetRepo.countByCategoryAndStatus(Category.LAPTOP, AssetStatus.AVAILABLE)).thenReturn(3L)
        when(assetRepo.countByCategoryAndStatus(Category.MOBILE, AssetStatus.AVAILABLE)).thenReturn(2L)
        when(assetRepo.countByCategoryAndStatus(Category.DESKTOP, AssetStatus.AVAILABLE)).thenReturn(2L)
        when(assetRepo.countByCategoryAndStatus(Category.KEYBOARD, AssetStatus.AVAILABLE)).thenReturn(2L)
        when(assetRepo.countByCategoryAndStatus(Category.MOUSE, AssetStatus.AVAILABLE)).thenReturn(1L)

        val result = assetService.getAllAssetCount

        result.total shouldBe 10L
        verify(assetRepo).countByStatus(AssetStatus.AVAILABLE)
        verify(assetRepo, times(5)).countByCategoryAndStatus(
          ArgumentMatchers.any[Category],
          ArgumentMatchers.eq(AssetStatus.AVAILABLE)
        )
      }
    }

    "getAssetById" should {

      "return asset when found" in {
        val asset = createSampleAsset(
          status = AssetStatus.AVAILABLE,
          category = Category.LAPTOP
        )

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))

        val result = assetService.getAssetById(1L)

        result.id shouldBe 1L
        result.serialNumber shouldBe "LAP-0001"
        result.modelName shouldBe "Dell XPS 15"
        result.status shouldBe AssetStatus.AVAILABLE
        result.category shouldBe Category.LAPTOP
      }

      "throw EntityNotFoundException when asset not found" in {
        when(assetRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          assetService.getAssetById(999L)
        } should have message "Asset not found "
      }

      "return correct data for ASSIGNED asset" in {
        val asset = createSampleAsset(
          status = AssetStatus.ASSIGNED
        )

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))

        val result = assetService.getAssetById(1L)

        result.status shouldBe AssetStatus.ASSIGNED
      }

      "return correct data for RETIRED asset" in {
        val asset = createSampleAsset(

          status = AssetStatus.RETIRED
        )

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))

        val result = assetService.getAssetById(1L)

        result.status shouldBe AssetStatus.RETIRED
      }

      "return correct data for MAINTENANCE asset" in {
        val asset = createSampleAsset(
          status = AssetStatus.MAINTENANCE
        )

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))

        val result = assetService.getAssetById(1L)

        result.status shouldBe AssetStatus.MAINTENANCE
      }

      "return correct credit value" in {
        val asset = createSampleAsset()

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))

        val result = assetService.getAssetById(1L)

        result.credit shouldBe 75
      }
    }
  }
}
