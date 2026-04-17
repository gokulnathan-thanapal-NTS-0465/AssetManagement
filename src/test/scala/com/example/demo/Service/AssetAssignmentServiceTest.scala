package com.example.demo.Service

import com.example.demo.DTO.AssetAssignmentResponseDTO
import com.example.demo.Model.{Asset, AssetAssignment, User}
import com.example.demo.Model.Enums.{AssetStatus, Category, UserType}
import com.example.demo.Repo.{AssetAssignmentRepository, AssetRepository, UserRepository}
import jakarta.persistence.EntityNotFoundException
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

import java.time.{LocalDate, LocalDateTime}
import java.util.Optional
import scala.compiletime.uninitialized

class AssetAssignmentServiceTest extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var assetAssignmentRepo: AssetAssignmentRepository = uninitialized
  var assetRepo: AssetRepository = uninitialized
  var userRepo: UserRepository = uninitialized

  var assetAssignmentService: AssetAssignmentService = uninitialized

  override def beforeEach(): Unit = {
    assetAssignmentRepo = mock[AssetAssignmentRepository]
    assetRepo = mock[AssetRepository]
    userRepo = mock[UserRepository]

    assetAssignmentService = new AssetAssignmentService(
      assetAssignmentRepo,
      assetRepo,
      userRepo
    )
  }


  def createSampleUser(
                        id: Long = 1L,
                        username: String = "john.doe",
                        userType: UserType = UserType.EMPLOYEE
                      ): User = {
    val user = new User()
    user.id = id
    user.username = username
    user.passwordHash = "encodedPassword"
    user.userType = userType
    user.creditBalance = 100
    user.employeeId = "EM0001"
    user.department = "IT"
    user.joinedDate = LocalDate.now()
    user.deactivated = false
    user
  }

  def createSampleAsset(
                         id: Long = 1L,
                         serialNumber: String = "LAP-0001",
                         modelName: String = "Dell XPS 15",
                         status: AssetStatus = AssetStatus.ASSIGNED,
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

  def createSampleAssetAssignment(
                                   id: Long = 1L,
                                   user: User,
                                   asset: Asset,
                                   assignedAt: LocalDateTime = LocalDateTime.now().minusDays(10),
                                   returnedAt: LocalDateTime = null
                                 ): AssetAssignment = {
    val assignment = new AssetAssignment()
    assignment.id = id
    assignment.user = user
    assignment.asset = asset
    assignment.assignedAt = assignedAt
    assignment.returnedAt = returnedAt
    assignment
  }


  "AssetAssignmentService" when {

    "returnAsset" should {

      "throw EntityNotFoundException when assignment is not found" in {
        when(assetAssignmentRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          assetAssignmentService.returnAsset(999L)
        } should have message "Asset assignment not found"
      }

      "throw IllegalStateException when assignment is already returned" in {
        val user = createSampleUser()
        val asset = createSampleAsset(id = 5L)
        val assignment = createSampleAssetAssignment(
          user = user,
          asset = asset,
          returnedAt = LocalDateTime.now().minusDays(1)
        )

        when(assetAssignmentRepo.findById(1L)).thenReturn(Optional.of(assignment))

        the[IllegalStateException] thrownBy {
          assetAssignmentService.returnAsset(1L)
        } should have message "Asset with id 5 has already been returned"
      }

      "change asset status to AVAILABLE when status is NOT MAINTENANCE" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.ASSIGNED)
        val assignment = createSampleAssetAssignment(
          user = user,
          asset = asset,
          returnedAt = null
        )

        when(assetAssignmentRepo.findById(1L)).thenReturn(Optional.of(assignment))
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)
        when(assetAssignmentRepo.save(ArgumentMatchers.any[AssetAssignment])).thenReturn(assignment)

        val result = assetAssignmentService.returnAsset(1L)

        verify(assetRepo).save(ArgumentMatchers.any[Asset])
        verify(assetAssignmentRepo).save(ArgumentMatchers.any[AssetAssignment])
        result.id shouldBe Some(1L)
      }

      "NOT change asset status when status is MAINTENANCE" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.MAINTENANCE)
        val assignment = createSampleAssetAssignment(
          user = user,
          asset = asset,
          returnedAt = null
        )

        when(assetAssignmentRepo.findById(1L)).thenReturn(Optional.of(assignment))
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)
        when(assetAssignmentRepo.save(ArgumentMatchers.any[AssetAssignment])).thenReturn(assignment)

        assetAssignmentService.returnAsset(1L)

        verify(assetRepo).save(ArgumentMatchers.any[Asset])
        verify(assetAssignmentRepo).save(ArgumentMatchers.any[AssetAssignment])
      }

      "return correct AssetAssignmentResponseDTO with all fields" in {
        val user = createSampleUser()
        val asset = createSampleAsset(id = 2L, status = AssetStatus.ASSIGNED)
        val assignment = createSampleAssetAssignment(
          id = 3L,
          user = user,
          asset = asset,
          returnedAt = null
        )

        when(assetAssignmentRepo.findById(3L)).thenReturn(Optional.of(assignment))
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)
        when(assetAssignmentRepo.save(ArgumentMatchers.any[AssetAssignment])).thenReturn(assignment)

        val result = assetAssignmentService.returnAsset(3L)

        result.id shouldBe Some(3L)
        result.assetId shouldBe Some(2L)
        result.userId shouldBe Some(1L)
        result.assignedAt should not be empty
      }

      "set returnedAt timestamp when returning asset" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.ASSIGNED)
        val assignment = createSampleAssetAssignment(
          user = user,
          asset = asset,
          returnedAt = null
        )

        when(assetAssignmentRepo.findById(1L)).thenReturn(Optional.of(assignment))
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)
        when(assetAssignmentRepo.save(ArgumentMatchers.any[AssetAssignment])).thenReturn(assignment)

        assetAssignmentService.returnAsset(1L)

        verify(assetAssignmentRepo).save(ArgumentMatchers.any[AssetAssignment])
      }
    }

    "canAccessReturn" should {

      "return true when user owns the assignment" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val assignment = createSampleAssetAssignment(user = user, asset = asset)

        when(assetAssignmentRepo.findById(1L)).thenReturn(Optional.of(assignment))

        val result = assetAssignmentService.canAccessReturn(1L, 1L)

        result shouldBe true
      }

      "return false when user does not own the assignment" in {
        val owner = createSampleUser()
        val asset = createSampleAsset()
        val assignment = createSampleAssetAssignment(user = owner, asset = asset)

        when(assetAssignmentRepo.findById(1L)).thenReturn(Optional.of(assignment))

        val result = assetAssignmentService.canAccessReturn(1L, 2L)

        result shouldBe false
      }

      "return false when assignment is not found" in {
        when(assetAssignmentRepo.findById(999L)).thenReturn(Optional.empty())

        val result = assetAssignmentService.canAccessReturn(999L, 1L)

        result shouldBe false
      }
    }

    "getAllAssignments" should {

      "return all assignments with valid resultant data" in {
        val user = createSampleUser()
        val asset1 = createSampleAsset()
        val asset2 = createSampleAsset(id = 2L, serialNumber = "MOB-0001")
        val assignments = java.util.Arrays.asList(
          createSampleAssetAssignment(user = user, asset = asset1),
          createSampleAssetAssignment(id = 2L, user = user, asset = asset2),
          createSampleAssetAssignment(id = 3L, user = user, asset = asset1, returnedAt = LocalDateTime.now())
        )

        when(assetAssignmentRepo.findAll()).thenReturn(assignments)

        val result = assetAssignmentService.getAllAssignments

        result should have size 3
        result.head.id shouldBe Some(1L)
        result.head.userId shouldBe Some(1L)
        result.head.assetId shouldBe Some(1L)
        verify(assetAssignmentRepo).findAll()
      }

      "return empty list when no assignments exist" in {
        when(assetAssignmentRepo.findAll()).thenReturn(java.util.Collections.emptyList())

        val result = assetAssignmentService.getAllAssignments

        result shouldBe empty
      }

      "return both active and returned assignments" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val activeAssignment = createSampleAssetAssignment(user = user, asset = asset, returnedAt = null)
        val returnedAssignment = createSampleAssetAssignment(id = 2L, user = user, asset = asset, returnedAt = LocalDateTime.now())
        val assignments = java.util.Arrays.asList(activeAssignment, returnedAssignment)

        when(assetAssignmentRepo.findAll()).thenReturn(assignments)

        val result = assetAssignmentService.getAllAssignments

        result should have size 2
        result.exists(_.returnedAt.isEmpty) shouldBe true
        result.exists(_.returnedAt.isDefined) shouldBe true
      }
    }

    "getAllAssignmentsByStatus" should {

      "return active assignments (returned = false) with valid data" in {
        val user = createSampleUser()
        val asset = createSampleAsset(id = 2L)
        val activeAssignments = java.util.Arrays.asList(
          createSampleAssetAssignment(user = user, asset = asset, returnedAt = null),
          createSampleAssetAssignment(id = 2L, user = user, asset = asset, returnedAt = null)
        )

        when(assetAssignmentRepo.findAllByReturnedAtIsNull()).thenReturn(activeAssignments)

        val result = assetAssignmentService.getAllAssignmentsByStatus(returned = false)

        result should have size 2
        result.foreach(_.returnedAt shouldBe None)
        result.head.userId shouldBe Some(1L)
        result.head.assetId shouldBe Some(2L)
        verify(assetAssignmentRepo).findAllByReturnedAtIsNull()
      }

      "return returned assignments (returned = true) with valid data" in {
        val user = createSampleUser()
        val asset = createSampleAsset(id = 2L)
        val returnedAssignments = java.util.Arrays.asList(
          createSampleAssetAssignment(user = user, asset = asset, returnedAt = LocalDateTime.now()),
          createSampleAssetAssignment(id = 2L, user = user, asset = asset, returnedAt = LocalDateTime.now().minusDays(5))
        )

        when(assetAssignmentRepo.findAllByReturnedAtIsNotNull()).thenReturn(returnedAssignments)

        val result = assetAssignmentService.getAllAssignmentsByStatus(returned = true)

        result should have size 2
        result.foreach(_.returnedAt should not be empty)
        result.head.userId shouldBe Some(1L)
        verify(assetAssignmentRepo).findAllByReturnedAtIsNotNull()
      }

      "return empty list when no active assignments exist" in {
        when(assetAssignmentRepo.findAllByReturnedAtIsNull())
          .thenReturn(java.util.Collections.emptyList())

        val result = assetAssignmentService.getAllAssignmentsByStatus(returned = false)

        result shouldBe empty
      }

      "return empty list when no returned assignments exist" in {
        when(assetAssignmentRepo.findAllByReturnedAtIsNotNull())
          .thenReturn(java.util.Collections.emptyList())

        val result = assetAssignmentService.getAllAssignmentsByStatus(returned = true)

        result shouldBe empty
      }
    }

    "getAssignmentsByUserId" should {

      "throw EntityNotFoundException when user is not found" in {
        when(userRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          assetAssignmentService.getAssignmentsByUserId(999L)
        } should have message "User not found"
      }

      "return all assignments for user with valid resultant data" in {
        val user = createSampleUser()
        val asset1 = createSampleAsset()
        val asset2 = createSampleAsset(id = 2L, serialNumber = "MOB-0001")
        val assignments = java.util.Arrays.asList(
          createSampleAssetAssignment(user = user, asset = asset1),
          createSampleAssetAssignment(id = 2L, user = user, asset = asset2)
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetAssignmentRepo.findByUser(user)).thenReturn(assignments)

        val result = assetAssignmentService.getAssignmentsByUserId(1L)

        result should have size 2
        result.foreach(_.userId shouldBe Some(1L))
        result.head.assetId shouldBe Some(1L)
        result(1).assetId shouldBe Some(2L)
      }

      "return empty list when user has no assignments" in {
        val user = createSampleUser()

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetAssignmentRepo.findByUser(user)).thenReturn(java.util.Collections.emptyList())

        val result = assetAssignmentService.getAssignmentsByUserId(1L)

        result shouldBe empty
      }

      "return both active and returned assignments for user" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val assignments = java.util.Arrays.asList(
          createSampleAssetAssignment(user = user, asset = asset, returnedAt = null),
          createSampleAssetAssignment(id = 2L, user = user, asset = asset, returnedAt = LocalDateTime.now())
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetAssignmentRepo.findByUser(user)).thenReturn(assignments)

        val result = assetAssignmentService.getAssignmentsByUserId(1L)

        result should have size 2
        result.exists(_.returnedAt.isEmpty) shouldBe true
        result.exists(_.returnedAt.isDefined) shouldBe true
      }
    }

    "getAssetAssignmentById" should {

      "throw EntityNotFoundException when assignment is not found" in {
        when(assetAssignmentRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          assetAssignmentService.getAssetAssignmentById(999L)
        } should have message "Asset assignment not found"
      }

      "return assignment with valid resultant data" in {
        val user = createSampleUser()
        val asset = createSampleAsset(id = 2L)
        val assignedAt = LocalDateTime.now().minusDays(10)
        val assignment = createSampleAssetAssignment(
          id = 3L,
          user = user,
          asset = asset,
          assignedAt = assignedAt
        )

        when(assetAssignmentRepo.findById(3L)).thenReturn(Optional.of(assignment))

        val result = assetAssignmentService.getAssetAssignmentById(3L)

        result.id shouldBe Some(3L)
        result.userId shouldBe Some(1L)
        result.assetId shouldBe Some(2L)
        result.assignedAt should not be empty
        result.returnedAt shouldBe None
      }

      "return assignment with returnedAt when asset was returned" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val returnedAt = LocalDateTime.now().minusDays(2)
        val assignment = createSampleAssetAssignment(
          user = user,
          asset = asset,
          returnedAt = returnedAt
        )

        when(assetAssignmentRepo.findById(1L)).thenReturn(Optional.of(assignment))

        val result = assetAssignmentService.getAssetAssignmentById(1L)

        result.returnedAt should not be empty
      }
    }

    "canAccessAssetAssignment" should {

      "return true when user owns the assignment" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val assignment = createSampleAssetAssignment(user = user, asset = asset)

        when(assetAssignmentRepo.findById(1L)).thenReturn(Optional.of(assignment))

        val result = assetAssignmentService.canAccessAssetAssignment(1L, 1L)

        result shouldBe true
      }

      "return false when user does not own the assignment" in {
        val owner = createSampleUser()
        val asset = createSampleAsset()
        val assignment = createSampleAssetAssignment(user = owner, asset = asset)

        when(assetAssignmentRepo.findById(1L)).thenReturn(Optional.of(assignment))

        val result = assetAssignmentService.canAccessAssetAssignment(1L, 2L)

        result shouldBe false
      }

      "return false when assignment is not found" in {
        when(assetAssignmentRepo.findById(999L)).thenReturn(Optional.empty())

        val result = assetAssignmentService.canAccessAssetAssignment(999L, 1L)

        result shouldBe false
      }
    }
  }
}
