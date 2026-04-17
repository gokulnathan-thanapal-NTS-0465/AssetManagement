package com.example.demo.Service

import com.example.demo.DTO.{UserCreateDTO, UserCredentialDTO, UserDepartmentDTO}
import com.example.demo.Model.Enums.{AssetStatus, ComplaintStatus, RequestStatus, UserType}
import com.example.demo.Model.{Asset, AssetAssignment, AssetRequest, Complaint, User}
import com.example.demo.Repo.{AssetAssignmentRepository, AssetRepository, AssetRequestRepository, ComplaintRepository, UserRepository}
import jakarta.persistence.EntityNotFoundException
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.springframework.security.crypto.password.PasswordEncoder

import java.time.{LocalDate, LocalDateTime}
import java.util
import java.util.Optional
import scala.compiletime.uninitialized

class UserServiceTest extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var userRepo: UserRepository = uninitialized
  var passwordEncoder: PasswordEncoder = uninitialized
  var assetAssignmentRepo: AssetAssignmentRepository = uninitialized
  var complaintRepo: ComplaintRepository = uninitialized
  var assetRequestRepo: AssetRequestRepository = uninitialized
  var assetRepo: AssetRepository = uninitialized

  var userService: UserService = uninitialized


  override def beforeEach(): Unit = {
    userRepo = mock[UserRepository]
    passwordEncoder = mock[PasswordEncoder]
    assetAssignmentRepo = mock[AssetAssignmentRepository]
    complaintRepo = mock[ComplaintRepository]
    assetRequestRepo = mock[AssetRequestRepository]
    assetRepo = mock[AssetRepository]

    userService = new UserService(
      userRepo,
      passwordEncoder,
      assetAssignmentRepo,
      complaintRepo,
      assetRequestRepo,
      assetRepo
    )
  }

  def createSampleComplaint(
                             id: Long = 1L,
                             user: User,
                             status: ComplaintStatus = ComplaintStatus.OPEN
                           ): Complaint = {
    val complaint = new Complaint()
    complaint.id = id
    complaint.user = user
    complaint.asset = createSampleAsset()
    complaint.description = "Test complaint"
    complaint.status = status
    complaint
  }

  def createSampleAssetAssignment(
                                   id: Long = 1L,
                                   user: User,
                                   asset: Asset
                                 ): AssetAssignment = {
    val assignment = new AssetAssignment()
    assignment.id = id
    assignment.user = user
    assignment.asset = asset
    assignment.assignedAt = LocalDateTime.now().minusDays(10)
    assignment.returnedAt = null
    assignment
  }
  def createSampleAssetRequest(
                                id: Long = 1L,
                                user: User,
                                status: RequestStatus = RequestStatus.PENDING
                              ): AssetRequest = {
    val request = new AssetRequest()
    request.id = id
    request.user = user
    request.reason = "Need for work"
    request.status = status
    request
  }

  def createSampleAsset(
                         id: Long = 1L,
                         status: AssetStatus = AssetStatus.ASSIGNED
                       ): Asset = {
    val asset = new Asset()
    asset.id = id
    asset.serialNumber = "LAP-001"
    asset.modelName = "Dell XPS"
    asset.status = status
    asset.credit = 10
    asset
  }

  def createSampleUser(
                        id: Long = 1L,
                        username: String = "gokul",
                        userType: UserType = UserType.EMPLOYEE,
                        deactivated: Boolean = false

                      ): User = {
    val user = new User()
    user.id = id
    user.username = username
    user.passwordHash = "encodedPassword123"
    user.userType = userType
    user.creditBalance = 50
    user.employeeId = "EM0001"
    user.department = "IT"
    user.joinedDate = LocalDate.now()
    user.deactivated = deactivated
    user

  }

  "UserService" when {
    "createUser" should {
      "create user successfully with valid data" in {
        val dto = UserCreateDTO(
          username = "gokul",
          passwordHash = "password123",
          userType = UserType.EMPLOYEE,
          department = "IT",
          creditBalance = Some(100)
        )

        val savedUser = createSampleUser()

        when(userRepo.findAllByUsername("gokul")).thenReturn(java.util.Collections.emptyList())
        when(userRepo.countByUserType(UserType.EMPLOYEE)).thenReturn(0L)
        when(passwordEncoder.encode("password123")).thenReturn("encoded123")
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(savedUser)

        val result = userService.createUser(dto)

        result.username shouldBe "gokul"
        verify(userRepo).save(ArgumentMatchers.any[User])
      }


      "throw IllegalStateException when username exist" in {
        val dto = UserCreateDTO(
          username = "gokul",
          passwordHash = "pass",
          department = "IT",
          userType=UserType.EMPLOYEE
        )

        when(userRepo.findAllByUsername("gokul")).thenReturn(java.util.Arrays.asList(createSampleUser()))

        the[IllegalStateException] thrownBy {
          userService.createUser(dto)
        } should have message "Username already exists"
      }

      "throw IllegalArgumentException when password missing" in {
        val dto = UserCreateDTO(
          username = "gokul",
          passwordHash =null,
          department = "IT",
          userType=UserType.EMPLOYEE
        )

        when(userRepo.findAllByUsername("gokul")).thenReturn(java.util.Collections.emptyList())

        the[IllegalArgumentException] thrownBy {
          userService.createUser(dto)
        } should have message "Password required"
      }
      "throw IllegalArgumentException when username missing" in {
        val dto = UserCreateDTO(
          username = null,
          passwordHash = "gokul123",
          department = "IT",
          userType = UserType.EMPLOYEE
        )

        when(userRepo.findAllByUsername("gokul")).thenReturn(java.util.Collections.emptyList())

        the[IllegalArgumentException] thrownBy {
          userService.createUser(dto)
        } should have message "Username is required"
      }


    }

    "getUserById" should {

      "return user when found" in {
        val user = createSampleUser(1L)
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))

        val result = userService.getUserById(1L)

        result.id shouldBe 1L
        result.username shouldBe "gokul"
      }

      "throw EntityNotFoundException when user not found" in {
        when(userRepo.findById(1L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          userService.getUserById(1L)
        } should have message "User not found"
      }
    }

    "updateUserCredentialById" should {
      "update username successfully" in {
        val existingUser = createSampleUser(username = "oldname")
        val updateUser = createSampleUser(username = "newname")
        val dto = UserCredentialDTO(
          username = Some("newname"),
          passwordHash = None
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser))
        when(userRepo.findAllByUsername("newname")).thenReturn(java.util.Collections.emptyList())
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(updateUser)

        val result = userService.updateUserCredentialById(1L, dto)

        result.username shouldBe "newname"
        verify(userRepo).save(ArgumentMatchers.any[User])
      }

      "update password successfully" in {
        val existingUser = createSampleUser()
        val dto = UserCredentialDTO(
          username = None,
          passwordHash = Some("newpass")
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser))
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNewPassword")
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(existingUser)

        userService.updateUserCredentialById(1L, dto)

        verify(passwordEncoder).encode("newpass")
        verify(userRepo).save(ArgumentMatchers.any[User])
      }

      "update both username and password " in {
       val existingUser = createSampleUser(username = "oldname")
        val updateUser = createSampleUser(username = "newname")
        val dto = UserCredentialDTO(
          username = Some("newname"),
          passwordHash = Some("newpass")
        )

        when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser))
        when(userRepo.findAllByUsername("newname")).thenReturn(java.util.Collections.emptyList())
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNewPassword")
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(updateUser)

        val result = userService.updateUserCredentialById(1L, dto)

        result.username shouldBe "newname"
        verify(passwordEncoder).encode("newpass")
        verify(userRepo).save(ArgumentMatchers.any[User])
      }

      "throw EntityNotFoundException when user not found" in {
        val dto = UserCredentialDTO(username = Some("newname"))
        when(userRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          userService.updateUserCredentialById(999L, dto)
        } should have message "User not found"
      }

      "throw IllegalStateException when user is deactivated" in {
        val deactivatedUser = createSampleUser(deactivated = true)
        val dto = UserCredentialDTO(username = Some("newname"))
        when(userRepo.findById(1L)).thenReturn(Optional.of(deactivatedUser))

        the[IllegalStateException] thrownBy {
          userService.updateUserCredentialById(1L, dto)
        } should have message "Cannot update credentials for a deactivated user"
      }

      "throw IllegalStateException when new username already exists" in {
        val existingUser = createSampleUser(id = 1L, username = "user1")
        val anotherUser = createSampleUser(id = 2L, username = "user2")
        val dto = UserCredentialDTO(username = Some("user2"))

        when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser))
        when(userRepo.findAllByUsername("user2")).thenReturn(java.util.Arrays.asList(anotherUser))

        the[IllegalStateException] thrownBy {
          userService.updateUserCredentialById(1L, dto)
        } should have message "Username already exists"
      }

      "throw IllegalArgumentException when new username is same as current" in {
        val existingUser = createSampleUser(id = 1L, username = "sameuser")
        val dto = UserCredentialDTO(username = Some("sameuser"))

        when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser))

        the[IllegalArgumentException] thrownBy {
          userService.updateUserCredentialById(1L, dto)
        } should have message "New username cannot be the same as the current username"
      }
    }
    "updateUserDepartmentById" should {
      "update department successfully" in {
        val existingUser = createSampleUser()
        val dto = UserDepartmentDTO(department = Some("HR"))

        when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser))
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(existingUser)

        val result = userService.updateUserDepartmentById(1L, dto)

        verify(userRepo).save(ArgumentMatchers.any[User])

      }
      "throw EntityNotFoundException when user not found " in {
        val dto = UserDepartmentDTO(department = Some("HR"))
        when(userRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          userService.updateUserDepartmentById(999L, dto)
        } should have message "User not found"
      }

      "throw IllegalStateException when user is deactivated" in {
        val deactivatedUser = createSampleUser(deactivated = true)
        val dto = UserDepartmentDTO(department = Some("HR"))
        when(userRepo.findById(1L)).thenReturn(Optional.of(deactivatedUser))

        the[IllegalStateException] thrownBy {
          userService.updateUserDepartmentById(1L, dto)
        } should have message "Cannot update department for a deactivated user"
      }

      "throw IllegalArgumentException when new department is same as current" in {
        val existingUser = createSampleUser()
        val dto = UserDepartmentDTO(department = Some("IT"))

        when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser))

        the[IllegalArgumentException] thrownBy {
          userService.updateUserDepartmentById(1L, dto)
        } should have message "New department cannot be the same as the current department"
      }

      "not update when department is None" in {
        val existingUser = createSampleUser()
        val dto = UserDepartmentDTO(department = None)

        when(userRepo.findById(1L)).thenReturn(Optional.of(existingUser))
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(existingUser)

        userService.updateUserDepartmentById(1L, dto)

        verify(userRepo).save(ArgumentMatchers.any[User])
      }

    }

    "updateCreditBalance" should {
      "update credit balance successfully" in {
        val user = createSampleUser()
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(user)

        val result = userService.updateCreditBalance(1L, 100)

        verify(userRepo).save(ArgumentMatchers.any[User])
      }

      "throw IllegalArgumentException when new balance is negative" in {
        the[IllegalArgumentException] thrownBy {
          userService.updateCreditBalance(1L, -10)
        } should have message "Credit balance cannot be negative"
      }

      "throw IllegalStateException when user is deactivated" in {
        val user = createSampleUser(deactivated = true)
        when(userRepo.findById(1L)).thenReturn(Optional.of(user))

        the[IllegalStateException] thrownBy {
          userService.updateCreditBalance(1L, 100)
        } should have message "Cannot update credit balance for a deactivated user"
      }

      " throw EntityNotFoundException when user is not found " in {
        val userId = 999L
        when(userRepo.findById(userId)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          userService.updateCreditBalance(999L, 100)
        } should have message "User not found"
      }
    }

    "deactivateUserById" should {
      "deactivate active user successfully" in {
        val user = createSampleUser()

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetAssignmentRepo.findAllByUserIdAndReturnedAtIsNull(1L))
          .thenReturn(java.util.Collections.emptyList())
        when(complaintRepo.findByUserId(1L)).thenReturn(java.util.Collections.emptyList())
        when(assetRequestRepo.findAllByUserId(1L)).thenReturn(java.util.Collections.emptyList())
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(user)

        userService.deactivateUserById(1L)

        verify(userRepo).save(ArgumentMatchers.any[User])
      }

      "skip already deactivated user" in {
        val deactivatedUser = createSampleUser(deactivated = true)
        when(userRepo.findById(1L)).thenReturn(Optional.of(deactivatedUser))
        userService.deactivateUserById(1L)
        verify(userRepo, never()).save(ArgumentMatchers.any[User])
      }

      "throw EntityNotFoundException when user not found" in {
        when(userRepo.findById(999L)).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          userService.deactivateUserById(999L)
        } should have message "User not found"
      }

      "return assigned assets when deactivating user" in {
        val user = createSampleUser()
        val asset = createSampleAsset(status=AssetStatus.ASSIGNED)
        val assignment= createSampleAssetAssignment(user = user,asset = asset)

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetAssignmentRepo.findAllByUserIdAndReturnedAtIsNull(1L)).thenReturn(util.Arrays.asList(assignment))
        when(complaintRepo.findByUserId(1L)).thenReturn(java.util.Collections.emptyList())
        when (assetRequestRepo.findAllByUserId(1L)).thenReturn(java.util.Collections.emptyList())
        when(assetRepo.save(ArgumentMatchers.any[Asset])).thenReturn(asset)
        when(assetAssignmentRepo.save(ArgumentMatchers.any[AssetAssignment])).thenReturn(assignment)
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(user)

        userService.deactivateUserById(1L)

        verify(assetRepo).save(ArgumentMatchers.any[Asset])
        verify(assetAssignmentRepo).save(ArgumentMatchers.any[AssetAssignment])
      }

      "withdraw open complaints when deactivating user" in {
        val user = createSampleUser()
        val openComplaint = createSampleComplaint(user = user, status = ComplaintStatus.OPEN)
        val inProgressComplaint = createSampleComplaint(id = 2L, user = user, status = ComplaintStatus.IN_PROGRESS)

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetAssignmentRepo.findAllByUserIdAndReturnedAtIsNull(1L))
          .thenReturn(java.util.Collections.emptyList())
        when(complaintRepo.findByUserId(1L))
          .thenReturn(java.util.Arrays.asList(openComplaint, inProgressComplaint))
        when(assetRequestRepo.findAllByUserId(1L)).thenReturn(java.util.Collections.emptyList())
        when(complaintRepo.save(ArgumentMatchers.any[Complaint])).thenReturn(openComplaint)
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(user)

        userService.deactivateUserById(1L)

        verify(complaintRepo, times(2)).save(ArgumentMatchers.any[Complaint])
      }

      "cancel pending requests when deactivating user" in {
        val user = createSampleUser()
        val pendingRequest = createSampleAssetRequest(user = user, status = RequestStatus.PENDING)

        when(userRepo.findById(1L)).thenReturn(Optional.of(user))
        when(assetAssignmentRepo.findAllByUserIdAndReturnedAtIsNull(1L))
          .thenReturn(java.util.Collections.emptyList())
        when(complaintRepo.findByUserId(1L)).thenReturn(java.util.Collections.emptyList())
        when(assetRequestRepo.findAllByUserId(1L))
          .thenReturn(java.util.Arrays.asList(pendingRequest))
        when(assetRequestRepo.save(ArgumentMatchers.any[AssetRequest])).thenReturn(pendingRequest)
        when(userRepo.save(ArgumentMatchers.any[User])).thenReturn(user)

        userService.deactivateUserById(1L)

        verify(assetRequestRepo).save(ArgumentMatchers.any[AssetRequest])
      }
    }

    "getAllUsers" should {

      "return all users when no filters provided" in {
        val users = util.Arrays.asList(
          createSampleUser(id=1L,username="user1"),
          createSampleUser(id=2L,username="user2"),
          createSampleUser(id=3L,username="user3")
        )
        when(userRepo.findAll()).thenReturn(users)

        val result = userService.getAllUsers(None,None)

        result should have size 3
      }

      "filter by userType only (EMPLOYEE) " in {
        val employees = java.util.Arrays.asList(
          createSampleUser(id=1L,userType = UserType.EMPLOYEE),
          createSampleUser(id=2L,userType = UserType.EMPLOYEE)
        )
        when(userRepo.findAllByUserType(UserType.EMPLOYEE)).thenReturn(employees)
        val result=userService.getAllUsers(Some(UserType.EMPLOYEE),None)

        result should have size 2
        result.foreach(_.userType shouldBe UserType.EMPLOYEE)
      }

      "filter by userType only (TECH)" in {
        val employees = java.util.Arrays.asList(
          createSampleUser(id = 1L, userType = UserType.TECH),
          createSampleUser(id = 2L, userType = UserType.TECH)
        )
        when(userRepo.findAllByUserType(UserType.TECH)).thenReturn(employees)
        val result = userService.getAllUsers(Some(UserType.TECH), None)

        result should have size 2
        result.foreach(_.userType shouldBe UserType.TECH)
      }

      "filter by deactivated = true only" in {
        val deactivatedUsers = util.Arrays.asList(
          createSampleUser(id=1L,deactivated = true),
          createSampleUser(id=2L,deactivated = true)
        )
        when(userRepo.findAllByDeactivatedIsTrue()).thenReturn(deactivatedUsers)
        val result = userService.getAllUsers(None, Some(true))
        result should have size 2
      }

      "filter by userType and deactivated = true" in {
        val deactivatedAdmins = java.util.Arrays.asList(
          createSampleUser(userType = UserType.ADMIN, deactivated = true),
          createSampleUser(userType = UserType.ADMIN, deactivated = true)
        )
        when (userRepo.findAllByUserTypeAndDeactivatedIsTrue(UserType.ADMIN, true)).thenReturn(deactivatedAdmins)
        val result = userService.getAllUsers(Some(UserType.ADMIN), Some(true))

        result should have size 2
      }

      "return empty list when no users match filters" in {
        when(userRepo.findAllByUserType(UserType.TECH)).thenReturn(java.util.Collections.emptyList())

        val result =userService.getAllUsers(Some(UserType.TECH), None)

        result shouldBe empty
      }
    }
  }


}
