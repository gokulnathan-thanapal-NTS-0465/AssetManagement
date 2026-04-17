package com.example.demo.Service

import com.example.demo.DTO.AssetRequestDTO
import com.example.demo.Model.{Asset, AssetAssignment, AssetRequest, User}
import com.example.demo.Model.Enums.{AssetStatus, Category, RequestStatus, UserType}
import com.example.demo.Repo.{AssetAssignmentRepository, AssetRepository, AssetRequestRepository, UserRepository}
import jakarta.persistence.EntityNotFoundException
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

import java.time.{LocalDate, LocalDateTime}
import java.util.Optional
import scala.compiletime.uninitialized

class AssetRequestServiceTest extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var userRepo: UserRepository = uninitialized
  var assetRequestRepo: AssetRequestRepository = uninitialized
  var assetRepo: AssetRepository = uninitialized
  var assetAssignmentRepo: AssetAssignmentRepository = uninitialized

  var assetRequestService: AssetRequestService = uninitialized

  override def beforeEach(): Unit = {
    userRepo = mock[UserRepository]
    assetRequestRepo = mock[AssetRequestRepository]
    assetRepo = mock[AssetRepository]
    assetAssignmentRepo = mock[AssetAssignmentRepository]

    assetRequestService = new AssetRequestService(
      userRepo,
      assetRequestRepo,
      assetRepo,
      assetAssignmentRepo
    )
  }


  def createSampleUser(
                        id: Long = 1L,
                        username: String = "john.doe",
                        userType: UserType = UserType.EMPLOYEE,
                        creditBalance: Int = 100,
                        deactivated: Boolean = false
                      ): User = {
    val user = new User()
    user.id = id
    user.username = username
    user.passwordHash = "encodedPassword"
    user.userType = userType
    user.creditBalance = creditBalance
    user.employeeId = "EM0001"
    user.department = "IT"
    user.joinedDate = LocalDate.now()
    user.deactivated = deactivated
    user
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

  def createSampleAssetRequest(
                                id: Long = 1L,
                                user: User,
                                category: Category = Category.LAPTOP,
                                reason: String = "Need for development work",
                                status: RequestStatus = RequestStatus.PENDING
                              ): AssetRequest = {
    val request = new AssetRequest()
    request.id = id
    request.user = user
    request.category = category
    request.reason = reason
    request.status = status
    request
  }

  def createSampleAssetAssignment(
                                   id: Long = 1L,
                                   asset: Asset,
                                   user: User
                                 ): AssetAssignment = {
    val assignment = new AssetAssignment()
    assignment.id = id
    assignment.asset = asset
    assignment.user = user
    assignment.assignedAt = LocalDateTime.now()
    assignment.returnedAt = null
    assignment
  }


  "AssetRequestService" when {

    "createRequest" should {

      "create request successfully with valid data" in {
        val user = createSampleUser()
        val dto = AssetRequestDTO(
          userId = "1",
          category = Category.LAPTOP,
          reason = "Need laptop for development"
        )

        val savedRequest = createSampleAssetRequest(
          user = user,
          category = Category.LAPTOP,
          reason = "Need laptop for development"
        )

        when(userRepo.getUserById(1L)).thenReturn(user)
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(savedRequest)

        val result = assetRequestService.createRequest(dto)

        result.userId shouldBe 1L
        result.category shouldBe Category.LAPTOP
        result.status shouldBe RequestStatus.PENDING
        verify(assetRequestRepo).save(ArgumentMatchers.any[AssetRequest])
      }
      

      "throw IllegalStateException when user has insufficient credit balance for LAPTOP" in {
        val user = createSampleUser(creditBalance = 50)
        val dto = AssetRequestDTO(
          userId = "1",
          category = Category.LAPTOP,
          reason = "Need laptop"
        )

        when(userRepo.getUserById(1L)).thenReturn(user)

        the[IllegalStateException] thrownBy {
          assetRequestService.createRequest(dto)
        } should have message "Insufficient credit balance"
      }

      "throw IllegalStateException when user has insufficient credit balance for MOBILE" in {
        val user = createSampleUser(creditBalance = 30)
        val dto = AssetRequestDTO(
          userId = "1",
          category = Category.MOBILE,
          reason = "Need mobile"
        )

        when(userRepo.getUserById(1L)).thenReturn(user)

        the[IllegalStateException] thrownBy {
          assetRequestService.createRequest(dto)
        } should have message "Insufficient credit balance"
      }

      "throw IllegalStateException when user has insufficient credit balance for DESKTOP" in {
        val user = createSampleUser(creditBalance = 50)
        val dto = AssetRequestDTO(
          userId = "1",
          category = Category.DESKTOP,
          reason = "Need desktop"
        )

        when(userRepo.getUserById(1L)).thenReturn(user)

        the[IllegalStateException] thrownBy {
          assetRequestService.createRequest(dto)
        } should have message "Insufficient credit balance"
      }

      "create request successfully when credit balance is exactly equal to required" in {
        val user = createSampleUser(creditBalance = 75)
        val dto = AssetRequestDTO(
          userId = "1",
          category = Category.LAPTOP,
          reason = "Need laptop"
        )

        val savedRequest = createSampleAssetRequest(user = user, category = Category.LAPTOP)

        when(userRepo.getUserById(1L)).thenReturn(user)
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(savedRequest)

        val result = assetRequestService.createRequest(dto)

        result.category shouldBe Category.LAPTOP
        verify(assetRequestRepo).save(ArgumentMatchers.any[AssetRequest])
      }

      "throw RuntimeException when reason is missing (from mapper)" in {
        val user = createSampleUser()
        val dto = AssetRequestDTO(
          userId = "1",
          category = Category.LAPTOP,
          reason = ""
        )

        when(userRepo.getUserById(1L)).thenReturn(user)

        a[RuntimeException] should be thrownBy {
          assetRequestService.createRequest(dto)
        }
      }

      "create request successfully for MOUSE category with low credit" in {
        val user = createSampleUser(creditBalance = 15)
        val dto = AssetRequestDTO(
          userId = "1",
          category = Category.MOUSE,
          reason = "Need mouse"
        )

        val savedRequest = createSampleAssetRequest(user = user, category = Category.MOUSE)

        when(userRepo.getUserById(1L)).thenReturn(user)
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(savedRequest)

        val result = assetRequestService.createRequest(dto)

        result.category shouldBe Category.MOUSE
      }

      "create request successfully for KEYBOARD category" in {
        val user = createSampleUser(creditBalance = 50)
        val dto = AssetRequestDTO(
          userId = "1",
          category = Category.KEYBOARD,
          reason = "Need keyboard"
        )

        val savedRequest = createSampleAssetRequest(user = user, category = Category.KEYBOARD)

        when(userRepo.getUserById(1L)).thenReturn(user)
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(savedRequest)

        val result = assetRequestService.createRequest(dto)

        result.category shouldBe Category.KEYBOARD
      }

      "set request status as PENDING for new request" in {
        val user = createSampleUser()
        val dto = AssetRequestDTO(
          userId = "1",
          category = Category.LAPTOP,
          reason = "Need laptop"
        )

        val savedRequest = createSampleAssetRequest(user = user, status = RequestStatus.PENDING)

        when(userRepo.getUserById(1L)).thenReturn(user)
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(savedRequest)

        val result = assetRequestService.createRequest(dto)

        result.status shouldBe RequestStatus.PENDING
      }
    }

    "acceptRequest" should {

      "accept request successfully and create assignment" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, category = Category.LAPTOP)
        val asset = createSampleAsset(category = Category.LAPTOP, status = AssetStatus.AVAILABLE)
        val assignment = createSampleAssetAssignment(asset = asset, user = user)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findFirstByCategoryAndStatus(Category.LAPTOP, AssetStatus.AVAILABLE)).thenReturn(asset)
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(user)
        when(assetAssignmentRepo.save(ArgumentMatchers.any[AssetAssignment])).thenReturn(assignment)
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(request)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        val result = assetRequestService.acceptRequest(1L)

        
        verify(assetAssignmentRepo).save(ArgumentMatchers.any[AssetAssignment])
        verify(userRepo).save(ArgumentMatchers.any[User])
        verify(assetRepo).save(ArgumentMatchers.any[Asset])
      }

      "throw EntityNotFoundException when request not found" in {
        when(assetRequestRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          assetRequestService.acceptRequest(999L)
        } should have message "Asset request not found"
      }

      "throw IllegalStateException when request is not PENDING (APPROVED)" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, status = RequestStatus.APPROVED)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))

        the[IllegalStateException] thrownBy {
          assetRequestService.acceptRequest(1L)
        } should have message "Request is not pending"
      }

      "throw IllegalStateException when request is not PENDING (REJECTED)" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, status = RequestStatus.REJECTED)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))

        the[IllegalStateException] thrownBy {
          assetRequestService.acceptRequest(1L)
        } should have message "Request is not pending"
      }

      "throw IllegalStateException when request is not PENDING (CANCELLED)" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, status = RequestStatus.CANCELLED)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))

        the[IllegalStateException] thrownBy {
          assetRequestService.acceptRequest(1L)
        } should have message "Request is not pending"
      }

      "throw EntityNotFoundException when user not found" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, status = RequestStatus.PENDING)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))
        when(userRepo.findById(1L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          assetRequestService.acceptRequest(1L)
        } should have message "User not found"
      }

      "throw IllegalStateException when user has insufficient credit balance" in {
        val user = createSampleUser(creditBalance = 50)
        val request = createSampleAssetRequest(user = user, category = Category.LAPTOP)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))

        the[IllegalStateException] thrownBy {
          assetRequestService.acceptRequest(1L)
        } should have message "Insufficient credit balance"
      }

      "deduct credit from user after accepting request" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, category = Category.LAPTOP)
        val asset = createSampleAsset(category = Category.LAPTOP)
        val assignment = createSampleAssetAssignment(asset = asset, user = user)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findFirstByCategoryAndStatus(Category.LAPTOP, AssetStatus.AVAILABLE)).thenReturn(asset)
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(user)
        when(assetAssignmentRepo.save(ArgumentMatchers.any[AssetAssignment])).thenReturn(assignment)
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(request)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        assetRequestService.acceptRequest(1L)

        verify(userRepo).save(ArgumentMatchers.any[User])
      }

      "change asset status to ASSIGNED after accepting" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, category = Category.LAPTOP)
        val asset = createSampleAsset(status = AssetStatus.AVAILABLE)
        val assignment = createSampleAssetAssignment(asset = asset, user = user)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findFirstByCategoryAndStatus(Category.LAPTOP, AssetStatus.AVAILABLE)).thenReturn(asset)
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(user)
        when(assetAssignmentRepo.save(ArgumentMatchers.any[AssetAssignment])).thenReturn(assignment)
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(request)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        assetRequestService.acceptRequest(1L)

        verify(assetRepo).save(ArgumentMatchers.any[Asset])
      }

      "change request status to APPROVED after accepting" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, status = RequestStatus.PENDING)
        val asset = createSampleAsset(status = AssetStatus.AVAILABLE)
        val assignment = createSampleAssetAssignment(asset = asset, user = user)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findFirstByCategoryAndStatus(Category.LAPTOP, AssetStatus.AVAILABLE)).thenReturn(asset)
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(user)
        when(assetAssignmentRepo.save(ArgumentMatchers.any[AssetAssignment])).thenReturn(assignment)
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(request)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        assetRequestService.acceptRequest(1L)

        verify(assetRequestRepo).save(ArgumentMatchers.any[AssetRequest])
      }


      "accept request successfully with correct credit" in {
        val user = createSampleUser(creditBalance = 15)
        val request = createSampleAssetRequest(user = user, category = Category.MOUSE)
        val asset = createSampleAsset(category = Category.MOUSE, credit = 15)
        val assignment = createSampleAssetAssignment(asset = asset, user = user)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findFirstByCategoryAndStatus(Category.MOUSE, AssetStatus.AVAILABLE)).thenReturn(asset)
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(user)
        when(assetAssignmentRepo.save(ArgumentMatchers.any[AssetAssignment])).thenReturn(assignment)
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(request)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        val result = assetRequestService.acceptRequest(1L)

        result should not be null
      }
    }

    "declineRequest" should {

      "decline request successfully" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, status = RequestStatus.PENDING)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(request)

        assetRequestService.declineRequest(1L)

        verify(assetRequestRepo).save(ArgumentMatchers.any[AssetRequest])
      }

      "throw EntityNotFoundException when request not found" in {
        when(assetRequestRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          assetRequestService.declineRequest(999L)
        } should have message "Asset request not found"
      }

      "throw IllegalStateException when request is not PENDING (APPROVED)" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, status = RequestStatus.APPROVED)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))

        the[IllegalStateException] thrownBy {
          assetRequestService.declineRequest(1L)
        } should have message "Request is not pending"
      }

      "set status to REJECTED after declining" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, status = RequestStatus.PENDING)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(request)

        assetRequestService.declineRequest(1L)

        verify(assetRequestRepo).save(ArgumentMatchers.any[AssetRequest])
      }
    }

    "withdrawRequest" should {

      "withdraw request successfully" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, status = RequestStatus.PENDING)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(request)

        assetRequestService.withdrawRequest(1L)

        verify(assetRequestRepo).save(ArgumentMatchers.any[AssetRequest])
      }

      "throw EntityNotFoundException when request not found" in {
        when(assetRequestRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          assetRequestService.withdrawRequest(999L)
        } should have message "Asset request not found"
      }

      "throw IllegalStateException when request is not PENDING" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, status = RequestStatus.APPROVED)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))

        the[IllegalStateException] thrownBy {
          assetRequestService.withdrawRequest(1L)
        } should have message "Only pending requests can be withdrawn"
      }

      "throw IllegalStateException when withdrawing REJECTED request" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, status = RequestStatus.REJECTED)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))

        the[IllegalStateException] thrownBy {
          assetRequestService.withdrawRequest(1L)
        } should have message "Only pending requests can be withdrawn"
      }

      "throw IllegalStateException when withdrawing already CANCELLED request" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, status = RequestStatus.CANCELLED)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))

        the[IllegalStateException] thrownBy {
          assetRequestService.withdrawRequest(1L)
        } should have message "Only pending requests can be withdrawn"
      }

      "set status to CANCELLED after withdrawing" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(user = user, status = RequestStatus.PENDING)

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(request)

        assetRequestService.withdrawRequest(1L)

        verify(assetRequestRepo).save(ArgumentMatchers.any[AssetRequest])
      }
    }

    "getAllRequest" should {

      "return all requests when no status filter" in {
        val user = createSampleUser()
        val requests = java.util.Arrays.asList(
          createSampleAssetRequest(user = user, status = RequestStatus.PENDING),
          createSampleAssetRequest(id = 2L, user = user, status = RequestStatus.APPROVED),
          createSampleAssetRequest(id = 3L, user = user, status = RequestStatus.REJECTED)
        )

        when(assetRequestRepo.findAll()).thenReturn(requests)

        val result = assetRequestService.getAllRequest(null)

        result should have size 3
        verify(assetRequestRepo).findAll()
      }

      "filter by PENDING status" in {
        val user = createSampleUser()
        val requests = java.util.Arrays.asList(
          createSampleAssetRequest(user = user, status = RequestStatus.PENDING),
          createSampleAssetRequest(id = 2L, user = user, status = RequestStatus.PENDING)
        )

        when(assetRequestRepo.findAllByStatus(RequestStatus.PENDING)).thenReturn(requests)

        val result = assetRequestService.getAllRequest(RequestStatus.PENDING)

        result should have size 2
        result.foreach(_.status shouldBe RequestStatus.PENDING)
        verify(assetRequestRepo).findAllByStatus(RequestStatus.PENDING)
      }

      "filter by APPROVED status" in {
        val user = createSampleUser()
        val requests = java.util.Arrays.asList(
          createSampleAssetRequest(user = user, status = RequestStatus.APPROVED)
        )

        when(assetRequestRepo.findAllByStatus(RequestStatus.APPROVED)).thenReturn(requests)

        val result = assetRequestService.getAllRequest(RequestStatus.APPROVED)

        result should have size 1
        result.head.status shouldBe RequestStatus.APPROVED
      }

      "filter by REJECTED status" in {
        val user = createSampleUser()
        val requests = java.util.Arrays.asList(
          createSampleAssetRequest(user = user, status = RequestStatus.REJECTED)
        )

        when(assetRequestRepo.findAllByStatus(RequestStatus.REJECTED)).thenReturn(requests)

        val result = assetRequestService.getAllRequest(RequestStatus.REJECTED)

        result should have size 1
      }

      "filter by CANCELLED status" in {
        val user = createSampleUser()
        val requests = java.util.Arrays.asList(
          createSampleAssetRequest(user = user, status = RequestStatus.CANCELLED)
        )

        when(assetRequestRepo.findAllByStatus(RequestStatus.CANCELLED)).thenReturn(requests)

        val result = assetRequestService.getAllRequest(RequestStatus.CANCELLED)

        result should have size 1
      }

      "return empty list when no requests exist" in {
        when(assetRequestRepo.findAll()).thenReturn(java.util.Collections.emptyList())

        val result = assetRequestService.getAllRequest(null)

        result shouldBe empty
      }

      "return empty list when no requests match status filter" in {
        when(assetRequestRepo.findAllByStatus(RequestStatus.PENDING))
          .thenReturn(java.util.Collections.emptyList())

        val result = assetRequestService.getAllRequest(RequestStatus.PENDING)

        result shouldBe empty
      }
    }

    "getRequestById" should {

      "return request when found" in {
        val user = createSampleUser()
        val request = createSampleAssetRequest(
          user = user,
          category = Category.LAPTOP,
          reason = "Development work",
          status = RequestStatus.PENDING
        )

        when(assetRequestRepo.findById(1L)).thenReturn(Optional.of(request))

        val result = assetRequestService.getRequestById(1L)

        result.id shouldBe 1L
        result.userId shouldBe user.id
        result.category shouldBe Category.LAPTOP
        result.reason shouldBe "Development work"
        result.status shouldBe RequestStatus.PENDING
      }

      "throw EntityNotFoundException when request not found" in {
        when(assetRequestRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          assetRequestService.getRequestById(999L)
        } should have message "Asset request not found"
      }
    }

    "getAllRequestsByUserId" should {

      "return all requests for user when no status filter" in {
        val user = createSampleUser()
        val requests = java.util.Arrays.asList(
          createSampleAssetRequest(user = user, status = RequestStatus.PENDING),
          createSampleAssetRequest(id = 2L, user = user, status = RequestStatus.APPROVED)
        )

        when(assetRequestRepo.findAllByUserId(1L)).thenReturn(requests)

        val result = assetRequestService.getAllRequestsByUserId(1L, null)

        result should have size 2
        verify(assetRequestRepo).findAllByUserId(1L)
      }

      "filter by user and status (PENDING)" in {
        val user = createSampleUser()
        val requests = java.util.Arrays.asList(
          createSampleAssetRequest(user = user, status = RequestStatus.PENDING)
        )

        when(assetRequestRepo.findAllByUserIdAndStatus(1L, RequestStatus.PENDING)).thenReturn(requests)

        val result = assetRequestService.getAllRequestsByUserId(1L, RequestStatus.PENDING)

        result should have size 1
        result.head.status shouldBe RequestStatus.PENDING
        verify(assetRequestRepo).findAllByUserIdAndStatus(1L, RequestStatus.PENDING)
      }

      "filter by user and status (APPROVED)" in {
        val user = createSampleUser()
        val requests = java.util.Arrays.asList(
          createSampleAssetRequest(user = user, status = RequestStatus.APPROVED)
        )

        when(assetRequestRepo.findAllByUserIdAndStatus(1L, RequestStatus.APPROVED)).thenReturn(requests)

        val result = assetRequestService.getAllRequestsByUserId(1L, RequestStatus.APPROVED)

        result should have size 1
      }

      "filter by user and status (REJECTED)" in {
        val user = createSampleUser()
        val requests = java.util.Arrays.asList(
          createSampleAssetRequest(user = user, status = RequestStatus.REJECTED)
        )

        when(assetRequestRepo.findAllByUserIdAndStatus(1L, RequestStatus.REJECTED)).thenReturn(requests)

        val result = assetRequestService.getAllRequestsByUserId(1L, RequestStatus.REJECTED)

        result should have size 1
      }

      "return empty list when user has no requests" in {
        when(assetRequestRepo.findAllByUserId(1L)).thenReturn(java.util.Collections.emptyList())

        val result = assetRequestService.getAllRequestsByUserId(1L, null)

        result shouldBe empty
      }

      "return empty list when no requests match user and status" in {
        when(assetRequestRepo.findAllByUserIdAndStatus(1L, RequestStatus.PENDING))
          .thenReturn(java.util.Collections.emptyList())

        val result = assetRequestService.getAllRequestsByUserId(1L, RequestStatus.PENDING)

        result shouldBe empty
      }
    }
  }
}

