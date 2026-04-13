package com.example.demo.Service

import com.example.demo.DTO.ComplaintCreationDTO
import com.example.demo.Model.{Asset, Complaint, User}
import com.example.demo.Model.Enums.{AssetStatus, Category, ComplaintStatus, UserType}
import com.example.demo.Repo.{AssetRepository, ComplaintRepository, UserRepository}
import jakarta.persistence.EntityNotFoundException
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDate
import java.util.Optional

class ComplaintServiceTest extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var userRepo: UserRepository = _
  var complaintRepo: ComplaintRepository = _
  var assetRepo: AssetRepository = _

  var complaintService: ComplaintService = _

  override def beforeEach(): Unit = {
    userRepo = mock[UserRepository]
    complaintRepo = mock[ComplaintRepository]
    assetRepo = mock[AssetRepository]

    complaintService = new ComplaintService(userRepo, complaintRepo, assetRepo)
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

  def createSampleComplaint(
                             id: Long = 1L,
                             user: User,
                             asset: Asset,
                             description: String = "Screen flickering issue",
                             status: ComplaintStatus = ComplaintStatus.OPEN
                           ): Complaint = {
    val complaint = new Complaint()
    complaint.id = id
    complaint.user = user
    complaint.asset = asset
    complaint.description = description
    complaint.status = status
    complaint
  }


  "ComplaintService" when {

    "createComplaint" should {

      "create complaint successfully with valid data" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.ASSIGNED)
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("1"),
          description = Some("Screen is flickering")
        )

        val savedComplaint = createSampleComplaint(
          user = user,
          asset = asset,
          description = "Screen is flickering"
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.findAssetByUserId(1L)).thenReturn(java.util.Arrays.asList(asset))
        when(complaintRepo.save(ArgumentMatchers.any[Complaint])).thenReturn(savedComplaint)

        val result = complaintService.createComplaint(dto)

        result.userId shouldBe 1L
        result.assetId shouldBe 1L
        result.status shouldBe ComplaintStatus.OPEN
        verify(complaintRepo).save(ArgumentMatchers.any[Complaint])
      }

      "throw IllegalArgumentException when userId is not given" in {
        val dto = ComplaintCreationDTO(
          userId = None,
          assetId = Some("1"),
          description = Some("Issue description")
        )

        the[IllegalArgumentException] thrownBy {
          complaintService.createComplaint(dto)
        } should have message "User ID is required"
      }

      "throw EntityNotFoundException when user is not found" in {
        val dto = ComplaintCreationDTO(
          userId = Some("999"),
          assetId = Some("1"),
          description = Some("Issue description")
        )

        when(userRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          complaintService.createComplaint(dto)
        } should have message "User not found"
      }

      "throw IllegalArgumentException when assetId is not given" in {
        val user = createSampleUser()
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = None,
          description = Some("Issue description")
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))

        the[IllegalArgumentException] thrownBy {
          complaintService.createComplaint(dto)
        } should have message "Asset ID is required"
      }

      "throw EntityNotFoundException when asset is not found" in {
        val user = createSampleUser()
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("999"),
          description = Some("Issue description")
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findById(999L)).thenReturn(Optional.empty())

        an[EntityNotFoundException] should be thrownBy {
          complaintService.createComplaint(dto)
        }
      }

      "throw SecurityException when asset does not belong to user" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.ASSIGNED)
        val otherAsset = createSampleAsset(id = 2L)
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("1"),
          description = Some("Issue description")
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.findAssetByUserId(1L)).thenReturn(java.util.Arrays.asList(otherAsset))

        the[SecurityException] thrownBy {
          complaintService.createComplaint(dto)
        } should have message "Asset does not belong to the user"
      }

      "throw SecurityException when user has no assets" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.ASSIGNED)
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("1"),
          description = Some("Issue description")
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.findAssetByUserId(1L)).thenReturn(java.util.Collections.emptyList())

        the[SecurityException] thrownBy {
          complaintService.createComplaint(dto)
        } should have message "Asset does not belong to the user"
      }

      "throw IllegalStateException when asset is not currently ASSIGNED" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.AVAILABLE)
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("1"),
          description = Some("Issue description")
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.findAssetByUserId(1L)).thenReturn(java.util.Arrays.asList(asset))

        the[IllegalStateException] thrownBy {
          complaintService.createComplaint(dto)
        } should have message "Asset is not currently assigned , cannot file complaint "
      }

      "throw IllegalStateException when asset status is MAINTENANCE" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.MAINTENANCE)
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("1"),
          description = Some("Issue description")
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.findAssetByUserId(1L)).thenReturn(java.util.Arrays.asList(asset))

        the[IllegalStateException] thrownBy {
          complaintService.createComplaint(dto)
        } should have message "Asset is not currently assigned , cannot file complaint "
      }

      "throw IllegalStateException when asset status is RETIRED" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.RETIRED)
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("1"),
          description = Some("Issue description")
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.findAssetByUserId(1L)).thenReturn(java.util.Arrays.asList(asset))

        the[IllegalStateException] thrownBy {
          complaintService.createComplaint(dto)
        } should have message "Asset is not currently assigned , cannot file complaint "
      }

      "create complaint with default description when description is not provided" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.ASSIGNED)
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("1"),
          description = None
        )

        val savedComplaint = createSampleComplaint(
          user = user,
          asset = asset,
          description = "Description not provided"
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.findAssetByUserId(1L)).thenReturn(java.util.Arrays.asList(asset))
        when(complaintRepo.save(ArgumentMatchers.any[Complaint])).thenReturn(savedComplaint)

        val result = complaintService.createComplaint(dto)

        result.description shouldBe "Description not provided"
      }

      "set complaint status as OPEN for new complaint" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.ASSIGNED)
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("1"),
          description = Some("Test issue")
        )

        val savedComplaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.OPEN
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(assetRepo.findAssetByUserId(1L)).thenReturn(java.util.Arrays.asList(asset))
        when(complaintRepo.save(ArgumentMatchers.any[Complaint])).thenReturn(savedComplaint)

        val result = complaintService.createComplaint(dto)

        result.status shouldBe ComplaintStatus.OPEN
      }
    }


    "createComplaintAdmin" should {

      "create complaint successfully for AVAILABLE asset" in {
        val user = createSampleUser(userType = UserType.ADMIN)
        val asset = createSampleAsset(status = AssetStatus.AVAILABLE)
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("1"),
          description = Some("Hardware issue found during inspection")
        )

        val savedComplaint = createSampleComplaint(
          user = user,
          asset = asset,
          description = "Hardware issue found during inspection"
        )

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(complaintRepo.save(ArgumentMatchers.any[Complaint])).thenReturn(savedComplaint)

        val result = complaintService.createComplaintAdmin(dto)

        result.status shouldBe ComplaintStatus.OPEN
        verify(complaintRepo).save(ArgumentMatchers.any[Complaint])
      }

      "throw IllegalArgumentException when assetId is not given" in {
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = None,
          description = Some("Issue description")
        )

        the[IllegalArgumentException] thrownBy {
          complaintService.createComplaintAdmin(dto)
        } should have message "Asset ID is required"
      }

      "throw EntityNotFoundException when asset is not found" in {
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("999"),
          description = Some("Issue description")
        )

        when(assetRepo.findById(999L)).thenReturn(Optional.empty())

        an[EntityNotFoundException] should be thrownBy {
          complaintService.createComplaintAdmin(dto)
        }
      }

      "throw IllegalArgumentException when userId is not given" in {
        val asset = createSampleAsset(status = AssetStatus.AVAILABLE)
        val dto = ComplaintCreationDTO(
          userId = None,
          assetId = Some("1"),
          description = Some("Issue description")
        )

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))

        the[IllegalArgumentException] thrownBy {
          complaintService.createComplaintAdmin(dto)
        } should have message "User ID is required"
      }

      "throw EntityNotFoundException when user is not found" in {
        val asset = createSampleAsset(status = AssetStatus.AVAILABLE)
        val dto = ComplaintCreationDTO(
          userId = Some("999"),
          assetId = Some("1"),
          description = Some("Issue description")
        )

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(userRepo.findById(999L)).thenReturn(Optional.empty())

        an[EntityNotFoundException] should be thrownBy {
          complaintService.createComplaintAdmin(dto)
        }
      }

      "throw IllegalStateException when asset status is ASSIGNED" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.ASSIGNED)
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("1"),
          description = Some("Issue description")
        )

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))

        the[IllegalStateException] thrownBy {
          complaintService.createComplaintAdmin(dto)
        } should have message "Admin cannot file complaint for an asset that is not available"
      }

      "throw IllegalStateException when asset status is MAINTENANCE" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.MAINTENANCE)
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("1"),
          description = Some("Issue description")
        )

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))

        the[IllegalStateException] thrownBy {
          complaintService.createComplaintAdmin(dto)
        } should have message "Admin cannot file complaint for an asset that is not available"
      }

      "throw IllegalStateException when asset status is RETIRED" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.RETIRED)
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("1"),
          description = Some("Issue description")
        )

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))

        the[IllegalStateException] thrownBy {
          complaintService.createComplaintAdmin(dto)
        } should have message "Admin cannot file complaint for an asset that is not available"
      }

      "create complaint with default description when not provided" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.AVAILABLE)
        val dto = ComplaintCreationDTO(
          userId = Some("1"),
          assetId = Some("1"),
          description = None
        )

        val savedComplaint = createSampleComplaint(
          user = user,
          asset = asset,
          description = "Description not provided"
        )

        when(assetRepo.findById(1L)).thenReturn(Optional.of(asset))
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(complaintRepo.save(ArgumentMatchers.any[Complaint])).thenReturn(savedComplaint)

        val result = complaintService.createComplaintAdmin(dto)

        result.description shouldBe "Description not provided"
      }
    }

    "processComplaint" should {

      "process complaint successfully and change status to IN_PROGRESS" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.ASSIGNED)
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.OPEN
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))
        when(complaintRepo.countByAssetIdAndStatus(1L, ComplaintStatus.IN_PROGRESS)).thenReturn(0L)
        when(complaintRepo.save(ArgumentMatchers.any[Complaint])).thenReturn(complaint)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        val result = complaintService.processComplaint(1L)

        verify(complaintRepo).save(ArgumentMatchers.any[Complaint])
      }

      "throw EntityNotFoundException when complaint is not found" in {
        when(complaintRepo.findById(999L)).thenReturn(Optional.empty())

        an[EntityNotFoundException] should be thrownBy {
          complaintService.processComplaint(999L)
        }
      }

      "throw IllegalStateException when complaint is not OPEN" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.IN_PROGRESS
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))

        the[IllegalStateException] thrownBy {
          complaintService.processComplaint(1L)
        } should have message "Only open complaints can be processed"
      }

      "throw IllegalStateException when complaint is already RESOLVED" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.RESOLVED
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))

        the[IllegalStateException] thrownBy {
          complaintService.processComplaint(1L)
        } should have message "Only open complaints can be processed"
      }

      "throw IllegalStateException when complaint is WITHDRAWN" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.WITHDRAWN
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))

        the[IllegalStateException] thrownBy {
          complaintService.processComplaint(1L)
        } should have message "Only open complaints can be processed"
      }

      "change asset status to MAINTENANCE when first complaint is processed" in {
        val user = createSampleUser()
        val asset = createSampleAsset( status = AssetStatus.ASSIGNED)
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.OPEN
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))
        when(complaintRepo.countByAssetIdAndStatus(1L, ComplaintStatus.IN_PROGRESS)).thenReturn(1L)
        when(complaintRepo.save(ArgumentMatchers.any[Complaint])).thenReturn(complaint)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        complaintService.processComplaint(1L)

        verify(assetRepo).save(ArgumentMatchers.any[Asset])
      }

      "not change asset status when multiple complaints are already in progress" in {
        val user = createSampleUser()
        val asset = createSampleAsset( status = AssetStatus.MAINTENANCE)
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.OPEN
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))
        when(complaintRepo.countByAssetIdAndStatus(1L, ComplaintStatus.IN_PROGRESS)).thenReturn(2L)
        when(complaintRepo.save(ArgumentMatchers.any[Complaint])).thenReturn(complaint)

        complaintService.processComplaint(1L)

        verify(assetRepo, never()).save(ArgumentMatchers.any[Asset])
      }
    }

    "resolveComplaint" should {

      "resolve complaint successfully and change status to RESOLVED" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.MAINTENANCE)
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.IN_PROGRESS
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))
        when(complaintRepo.save(ArgumentMatchers.any[Complaint])).thenReturn(complaint)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        val result = complaintService.resolveComplaint(1L)

        verify(complaintRepo).save(ArgumentMatchers.any[Complaint])
      }

      "throw EntityNotFoundException when complaint is not found" in {
        when(complaintRepo.findById(999L)).thenReturn(Optional.empty())

        an[EntityNotFoundException] should be thrownBy {
          complaintService.resolveComplaint(999L)
        }
      }

      "throw IllegalStateException when complaint is not IN_PROGRESS" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.OPEN
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))

        the[IllegalStateException] thrownBy {
          complaintService.resolveComplaint(1L)
        } should have message "Only complaints in progress can be resolved "
      }

      "throw IllegalStateException when complaint is already RESOLVED" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.RESOLVED
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))

        the[IllegalStateException] thrownBy {
          complaintService.resolveComplaint(1L)
        } should have message "Only complaints in progress can be resolved "
      }

      "throw IllegalStateException when complaint is WITHDRAWN" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.WITHDRAWN
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))

        the[IllegalStateException] thrownBy {
          complaintService.resolveComplaint(1L)
        } should have message "Only complaints in progress can be resolved "
      }

      "change asset status to ASSIGNED after resolving complaint" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status = AssetStatus.MAINTENANCE)
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.IN_PROGRESS
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))
        when(complaintRepo.save(ArgumentMatchers.any[Complaint])).thenReturn(complaint)
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)

        complaintService.resolveComplaint(1L)

        verify(assetRepo).save(ArgumentMatchers.any[Asset])
      }
    }

    "getAllComplaints" should {

      "return all complaints when no status filter" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaints = java.util.Arrays.asList(
          createSampleComplaint(id = 1L, user = user, asset = asset, status = ComplaintStatus.OPEN),
          createSampleComplaint(id = 2L, user = user, asset = asset, status = ComplaintStatus.IN_PROGRESS),
          createSampleComplaint(id = 3L, user = user, asset = asset, status = ComplaintStatus.RESOLVED)
        )

        when(complaintRepo.findAll()).thenReturn(complaints)

        val result = complaintService.getAllComplaints(null)

        result should have size 3
        verify(complaintRepo).findAll()
      }

      "filter by OPEN status" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaints = java.util.Arrays.asList(
          createSampleComplaint( user = user, asset = asset, status = ComplaintStatus.OPEN),
          createSampleComplaint(id = 2L, user = user, asset = asset, status = ComplaintStatus.OPEN)
        )

        when(complaintRepo.findByStatus(ComplaintStatus.OPEN)).thenReturn(complaints)

        val result = complaintService.getAllComplaints(ComplaintStatus.OPEN)

        result should have size 2
        result.foreach(_.status shouldBe ComplaintStatus.OPEN)
        verify(complaintRepo).findByStatus(ComplaintStatus.OPEN)
      }

      "filter by IN_PROGRESS status" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaints = java.util.Arrays.asList(
          createSampleComplaint( user = user, asset = asset, status = ComplaintStatus.IN_PROGRESS)
        )

        when(complaintRepo.findByStatus(ComplaintStatus.IN_PROGRESS)).thenReturn(complaints)

        val result = complaintService.getAllComplaints(ComplaintStatus.IN_PROGRESS)

        result should have size 1
        result.head.status shouldBe ComplaintStatus.IN_PROGRESS
      }

      "filter by RESOLVED status" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaints = java.util.Arrays.asList(
          createSampleComplaint( user = user, asset = asset, status = ComplaintStatus.RESOLVED)
        )

        when(complaintRepo.findByStatus(ComplaintStatus.RESOLVED)).thenReturn(complaints)

        val result = complaintService.getAllComplaints(ComplaintStatus.RESOLVED)

        result should have size 1
        result.head.status shouldBe ComplaintStatus.RESOLVED
      }

      "filter by WITHDRAWN status" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaints = java.util.Arrays.asList(
          createSampleComplaint( user = user, asset = asset, status = ComplaintStatus.WITHDRAWN)
        )

        when(complaintRepo.findByStatus(ComplaintStatus.WITHDRAWN)).thenReturn(complaints)

        val result = complaintService.getAllComplaints(ComplaintStatus.WITHDRAWN)

        result should have size 1
      }

      "return empty list when no complaints exist" in {
        when(complaintRepo.findAll()).thenReturn(java.util.Collections.emptyList())

        val result = complaintService.getAllComplaints(null)

        result shouldBe empty
      }

      "return empty list when no complaints match status filter" in {
        when(complaintRepo.findByStatus(ComplaintStatus.OPEN))
          .thenReturn(java.util.Collections.emptyList())

        val result = complaintService.getAllComplaints(ComplaintStatus.OPEN)

        result shouldBe empty
      }
    }

    "getComplaintStats" should {

      "return correct counts for all statuses" in {
        when(complaintRepo.count()).thenReturn(25L)
        when(complaintRepo.countByStatus(ComplaintStatus.OPEN)).thenReturn(10L)
        when(complaintRepo.countByStatus(ComplaintStatus.IN_PROGRESS)).thenReturn(8L)
        when(complaintRepo.countByStatus(ComplaintStatus.RESOLVED)).thenReturn(7L)

        val result = complaintService.getComplaintStats

        result.totalComplaints shouldBe 25L
        result.open shouldBe 10L
        result.inProgress shouldBe 8L
        result.resolved shouldBe 7L
      }

      "return zero counts when no complaints exist" in {
        when(complaintRepo.count()).thenReturn(0L)
        when(complaintRepo.countByStatus(ComplaintStatus.OPEN)).thenReturn(0L)
        when(complaintRepo.countByStatus(ComplaintStatus.IN_PROGRESS)).thenReturn(0L)
        when(complaintRepo.countByStatus(ComplaintStatus.RESOLVED)).thenReturn(0L)

        val result = complaintService.getComplaintStats

        result.totalComplaints shouldBe 0L
        result.open shouldBe 0L
        result.inProgress shouldBe 0L
        result.resolved shouldBe 0L
      }

      "return correct count when all complaints are OPEN" in {
        when(complaintRepo.count()).thenReturn(5L)
        when(complaintRepo.countByStatus(ComplaintStatus.OPEN)).thenReturn(5L)
        when(complaintRepo.countByStatus(ComplaintStatus.IN_PROGRESS)).thenReturn(0L)
        when(complaintRepo.countByStatus(ComplaintStatus.RESOLVED)).thenReturn(0L)

        val result = complaintService.getComplaintStats

        result.totalComplaints shouldBe 5L
        result.open shouldBe 5L
        result.inProgress shouldBe 0L
        result.resolved shouldBe 0L
      }

      "return correct count when all complaints are RESOLVED" in {
        when(complaintRepo.count()).thenReturn(10L)
        when(complaintRepo.countByStatus(ComplaintStatus.OPEN)).thenReturn(0L)
        when(complaintRepo.countByStatus(ComplaintStatus.IN_PROGRESS)).thenReturn(0L)
        when(complaintRepo.countByStatus(ComplaintStatus.RESOLVED)).thenReturn(10L)

        val result = complaintService.getComplaintStats

        result.totalComplaints shouldBe 10L
        result.open shouldBe 0L
        result.inProgress shouldBe 0L
        result.resolved shouldBe 10L
      }
    }

    "getAllComplaintsByUserId" should {

      "return all complaints for a user" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaints = java.util.Arrays.asList(
          createSampleComplaint( user = user, asset = asset, status = ComplaintStatus.OPEN),
          createSampleComplaint(id = 2L, user = user, asset = asset, status = ComplaintStatus.RESOLVED)
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(complaintRepo.findByUser(user)).thenReturn(complaints)

        val result = complaintService.getAllComplaintsByUserId(1L)

        result should have size 2
        result.foreach(_.userId shouldBe 1L)
      }

      "throw EntityNotFoundException when user is not found" in {
        when(userRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          complaintService.getAllComplaintsByUserId(999L)
        } should have message "User not found"
      }

      "return empty list when user has no complaints" in {
        val user = createSampleUser()

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(complaintRepo.findByUser(user)).thenReturn(java.util.Collections.emptyList())

        val result = complaintService.getAllComplaintsByUserId(1L)

        result shouldBe empty
      }

      "return complaints with different statuses for the same user" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaints = java.util.Arrays.asList(
          createSampleComplaint(user = user, asset = asset, status = ComplaintStatus.OPEN),
          createSampleComplaint(id = 2L, user = user, asset = asset, status = ComplaintStatus.IN_PROGRESS),
          createSampleComplaint(id = 3L, user = user, asset = asset, status = ComplaintStatus.RESOLVED),
          createSampleComplaint(id = 4L, user = user, asset = asset, status = ComplaintStatus.WITHDRAWN)
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(complaintRepo.findByUser(user)).thenReturn(complaints)

        val result = complaintService.getAllComplaintsByUserId(1L)

        result should have size 4
      }
    }

    "getComplaintById" should {

      "return complaint when found" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaint = createSampleComplaint(
          id = 1L,
          user = user,
          asset = asset,
          description = "Screen flickering",
          status = ComplaintStatus.OPEN
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))

        val result = complaintService.getComplaintById(1L)

        result.id shouldBe 1L
        result.userId shouldBe user.id
        result.assetId shouldBe asset.id
        result.description shouldBe "Screen flickering"
        result.status shouldBe ComplaintStatus.OPEN
      }

      "throw EntityNotFoundException when complaint is not found" in {
        when(complaintRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          complaintService.getComplaintById(999L)
        } should have message "Complaint not found"
      }

      "return correct data for IN_PROGRESS complaint" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.IN_PROGRESS
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))

        val result = complaintService.getComplaintById(1L)

        result.status shouldBe ComplaintStatus.IN_PROGRESS
      }

      "return correct data for RESOLVED complaint" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.RESOLVED
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))

        val result = complaintService.getComplaintById(1L)

        result.status shouldBe ComplaintStatus.RESOLVED
      }

      "return correct data for WITHDRAWN complaint" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaint = createSampleComplaint(
          user = user,
          asset = asset,
          status = ComplaintStatus.WITHDRAWN
        )

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))

        val result = complaintService.getComplaintById(1L)

        result.status shouldBe ComplaintStatus.WITHDRAWN
      }
    }

    "canUserAccessComplaint" should {

      "return true when user owns the complaint" in {
        val user = createSampleUser()
        val asset = createSampleAsset()
        val complaint = createSampleComplaint(user = user, asset = asset)

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))

        val result = complaintService.canUserAccessComplaint(1L, 1L)

        result shouldBe true
      }

      "return false when user does not own the complaint" in {
        val owner = createSampleUser()
        val asset = createSampleAsset()
        val complaint = createSampleComplaint(user = owner, asset = asset)

        when(complaintRepo.findById(1L)).thenReturn(Optional.of(complaint))

        val result = complaintService.canUserAccessComplaint(1L, 2L)

        result shouldBe false
      }

      "throw EntityNotFoundException when complaint is not found" in {
        when(complaintRepo.findById(999L)).thenReturn(Optional.empty())

        an[EntityNotFoundException] should be thrownBy {
          complaintService.canUserAccessComplaint(999L, 1L)
        }
      }

      "return true when checking with correct userId for different complaint" in {
        val user = createSampleUser(id = 5L)
        val asset = createSampleAsset()
        val complaint = createSampleComplaint(user = user, asset = asset)

        when(complaintRepo.findById(10L)).thenReturn(Optional.of(complaint))

        val result = complaintService.canUserAccessComplaint(10L, 5L)

        result shouldBe true
      }
    }
  }
}
