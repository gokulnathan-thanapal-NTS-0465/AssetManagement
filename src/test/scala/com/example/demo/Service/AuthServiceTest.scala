package com.example.demo.Service

import com.example.demo.DTO.{LoginRequestDTO, LoginResponseDTO}
import com.example.demo.Model.User
import com.example.demo.Model.Enums.UserType
import com.example.demo.Repo.UserRepository
import com.example.demo.Util.JwtUtil
import jakarta.persistence.EntityNotFoundException
import org.mockito.Mockito.{verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.{SecurityContext, SecurityContextHolder}
import org.springframework.security.crypto.password.PasswordEncoder

import java.time.LocalDate
import java.util.Optional
import scala.compiletime.uninitialized

class AuthServiceTest extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var userRepository: UserRepository = uninitialized
  var passwordEncoder: PasswordEncoder = uninitialized
  var jwtUtil: JwtUtil = uninitialized
  var assetRequestService: AssetRequestService = uninitialized
  var assetAssignmentService: AssetAssignmentService = uninitialized
  var complaintService: ComplaintService = uninitialized

  var authService: AuthService = uninitialized

  override def beforeEach(): Unit = {
    userRepository = mock[UserRepository]
    passwordEncoder = mock[PasswordEncoder]
    jwtUtil = mock[JwtUtil]
    assetRequestService = mock[AssetRequestService]
    assetAssignmentService = mock[AssetAssignmentService]
    complaintService = mock[ComplaintService]

    authService = new AuthService(
      userRepository,
      passwordEncoder,
      jwtUtil,
      assetRequestService,
      assetAssignmentService,
      complaintService
    )
  }

  override def afterEach(): Unit = {
    SecurityContextHolder.clearContext()
  }


  def createSampleUser(
                        id: Long = 1L,
                        username: String = "john.doe",
                        passwordHash: String = "encodedPassword123",
                        userType: UserType = UserType.EMPLOYEE,
                        deactivated: Boolean = false
                      ): User = {
    val user = new User()
    user.id = id
    user.username = username
    user.passwordHash = passwordHash
    user.userType = userType
    user.creditBalance = 100
    user.employeeId = "EM0001"
    user.department = "IT"
    user.joinedDate = LocalDate.now()
    user.deactivated = deactivated
    user
  }

  def setupSecurityContext(userId: Long): Unit = {
    val authentication = mock[Authentication]
    val securityContext = mock[SecurityContext]

    when(authentication.getDetails).thenReturn(Map("userId" -> userId))
    when(securityContext.getAuthentication).thenReturn(authentication)
    SecurityContextHolder.setContext(securityContext)
  }

  def setupSecurityContextWithoutDetails(): Unit = {
    val authentication = mock[Authentication]
    val securityContext = mock[SecurityContext]

    when(authentication.getDetails).thenReturn(null)
    when(securityContext.getAuthentication).thenReturn(authentication)
    SecurityContextHolder.setContext(securityContext)
  }

  def setupEmptySecurityContext(): Unit = {
    val securityContext = mock[SecurityContext]
    when(securityContext.getAuthentication).thenReturn(null)
    SecurityContextHolder.setContext(securityContext)
  }


  "AuthService" when {

    "login" should {

      "login successfully with valid credentials" in {
        val user = createSampleUser( passwordHash = "encodedPass")
        val loginRequest = LoginRequestDTO(
          username = "john.doe",
          password = "password123"
        )

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user))
        when(passwordEncoder.matches("password123", "encodedPass")).thenReturn(true)
        when(jwtUtil.generateToken("john.doe", UserType.EMPLOYEE, 1L)).thenReturn("jwt.token.here")

        val result = authService.login(loginRequest)

        result.token shouldBe "jwt.token.here"
        result.username shouldBe "john.doe"
        result.userType shouldBe UserType.EMPLOYEE
        result.userId shouldBe 1L
        verify(jwtUtil).generateToken("john.doe", UserType.EMPLOYEE, 1L)
      }

      "login successfully for ADMIN user" in {
        val adminUser = createSampleUser(id = 2L, username = "admin", userType = UserType.ADMIN)
        val loginRequest = LoginRequestDTO(
          username = "admin",
          password = "adminPass"
        )

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser))
        when(passwordEncoder.matches("adminPass", "encodedPassword123")).thenReturn(true)
        when(jwtUtil.generateToken("admin", UserType.ADMIN, 2L)).thenReturn("admin.jwt.token")

        val result = authService.login(loginRequest)

        result.userType shouldBe UserType.ADMIN
        result.username shouldBe "admin"
      }

      "login successfully for TECH user" in {
        val techUser = createSampleUser(id = 3L, username = "tech.support", userType = UserType.TECH)
        val loginRequest = LoginRequestDTO(
          username = "tech.support",
          password = "techPass"
        )

        when(userRepository.findByUsername("tech.support")).thenReturn(Optional.of(techUser))
        when(passwordEncoder.matches("techPass", "encodedPassword123")).thenReturn(true)
        when(jwtUtil.generateToken("tech.support", UserType.TECH, 3L)).thenReturn("tech.jwt.token")

        val result = authService.login(loginRequest)

        result.userType shouldBe UserType.TECH
      }

      "throw IllegalArgumentException when username is not provided" in {
        val loginRequest = LoginRequestDTO(
          username = "",
          password = "password123"
        )

        the[IllegalArgumentException] thrownBy {
          authService.login(loginRequest)
        } should have message "Username is required"
      }

      "throw EntityNotFoundException when user is not found" in {
        val loginRequest = LoginRequestDTO(
          username = "nonexistent",
          password = "password123"
        )

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty())

        the[EntityNotFoundException] thrownBy {
          authService.login(loginRequest)
        } should have message "User not found"
      }

      "throw IllegalStateException when user account is deactivated" in {
        val deactivatedUser = createSampleUser(username = "deactivated.user", deactivated = true)
        val loginRequest = LoginRequestDTO(
          username = "deactivated.user",
          password = "password123"
        )

        when(userRepository.findByUsername("deactivated.user")).thenReturn(Optional.of(deactivatedUser))

        the[IllegalStateException] thrownBy {
          authService.login(loginRequest)
        } should have message "User account is deactivated"
      }

      "throw IllegalArgumentException when password is not provided" in {
        val user = createSampleUser()
        val loginRequest = LoginRequestDTO(
          username = "john.doe",
          password = ""
        )

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user))

        the[IllegalArgumentException] thrownBy {
          authService.login(loginRequest)
        } should have message "Password required"
      }

      "throw SecurityException when password is incorrect" in {
        val user = createSampleUser(passwordHash = "correctEncodedPassword")
        val loginRequest = LoginRequestDTO(
          username = "john.doe",
          password = "wrongPassword"
        )

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user))
        when(passwordEncoder.matches("wrongPassword", "correctEncodedPassword")).thenReturn(false)

        the[SecurityException] thrownBy {
          authService.login(loginRequest)
        } should have message "Invalid credentials"
      }

      "return correct userId in response" in {
        val user = createSampleUser(id = 42L)
        val loginRequest = LoginRequestDTO(
          username = "john.doe",
          password = "password123"
        )

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user))
        when(passwordEncoder.matches("password123", "encodedPassword123")).thenReturn(true)
        when(jwtUtil.generateToken("john.doe", UserType.EMPLOYEE, 42L)).thenReturn("token")

        val result = authService.login(loginRequest)

        result.userId shouldBe 42L
      }
    }

    "isCurrentUser" should {

      "return true when userId matches authenticated user" in {
        setupSecurityContext(userId = 1L)

        val result = authService.isCurrentUser(1L)

        result shouldBe true
      }

      "return false when userId does not match authenticated user" in {
        setupSecurityContext(userId = 1L)

        val result = authService.isCurrentUser(2L)

        result shouldBe false
      }

      "return false when authentication is null" in {
        setupEmptySecurityContext()

        val result = authService.isCurrentUser(1L)

        result shouldBe false
      }

      "return false when authentication details is null" in {
        setupSecurityContextWithoutDetails()

        val result = authService.isCurrentUser(1L)

        result shouldBe false
      }

      "return false when details is not a Map" in {
        val authentication = mock[Authentication]
        val securityContext = mock[SecurityContext]

        when(authentication.getDetails).thenReturn("not a map")  // String instead of Map
        when(securityContext.getAuthentication).thenReturn(authentication)
        SecurityContextHolder.setContext(securityContext)

        val result = authService.isCurrentUser(1L)

        result shouldBe false
      }
    }

    "canAccessRequest" should {

      "return true when user can access the request" in {
        setupSecurityContext(userId = 1L)
        when(assetRequestService.canUserAccessRequest(10L, 1L)).thenReturn(true)

        val result = authService.canAccessRequest(10L)

        result shouldBe true
        verify(assetRequestService).canUserAccessRequest(10L, 1L)
      }

      "return false when user cannot access the request" in {
        setupSecurityContext(userId = 1L)
        when(assetRequestService.canUserAccessRequest(10L, 1L)).thenReturn(false)

        val result = authService.canAccessRequest(10L)

        result shouldBe false
      }

      "return false when no user is authenticated" in {
        setupEmptySecurityContext()

        val result = authService.canAccessRequest(10L)

        result shouldBe false
      }

      "return false when authentication details is null" in {
        setupSecurityContextWithoutDetails()

        val result = authService.canAccessRequest(10L)

        result shouldBe false
      }
    }

    "canAccessReturn" should {

      "return true when user can access the return" in {
        setupSecurityContext(userId = 1L)
        when(assetAssignmentService.canAccessReturn(5L, 1L)).thenReturn(true)

        val result = authService.canAccessReturn(5L)

        result shouldBe true
        verify(assetAssignmentService).canAccessReturn(5L, 1L)
      }

      "return false when user cannot access the return" in {
        setupSecurityContext(userId = 1L)
        when(assetAssignmentService.canAccessReturn(5L, 1L)).thenReturn(false)

        val result = authService.canAccessReturn(5L)

        result shouldBe false
      }

      "return false when no user is authenticated" in {
        setupEmptySecurityContext()

        val result = authService.canAccessReturn(5L)

        result shouldBe false
      }

      "return false when authentication details is null" in {
        setupSecurityContextWithoutDetails()

        val result = authService.canAccessReturn(5L)

        result shouldBe false
      }
    }

    "canAccessComplaint" should {

      "return true when user can access the complaint" in {
        setupSecurityContext(userId = 1L)
        when(complaintService.canUserAccessComplaint(7L, 1L)).thenReturn(true)

        val result = authService.canAccessComplaint(7L)

        result shouldBe true
        verify(complaintService).canUserAccessComplaint(7L, 1L)
      }

      "return false when user cannot access the complaint" in {
        setupSecurityContext(userId = 1L)
        when(complaintService.canUserAccessComplaint(7L, 1L)).thenReturn(false)

        val result = authService.canAccessComplaint(7L)

        result shouldBe false
      }

      "return false when no user is authenticated" in {
        setupEmptySecurityContext()

        val result = authService.canAccessComplaint(7L)

        result shouldBe false
      }

      "return false when authentication details is null" in {
        setupSecurityContextWithoutDetails()

        val result = authService.canAccessComplaint(7L)

        result shouldBe false
      }
    }

    "canAccessAssignment" should {

      "return true when user can access the assignment" in {
        setupSecurityContext(userId = 1L)
        when(assetAssignmentService.canAccessAssetAssignment(3L, 1L)).thenReturn(true)

        val result = authService.canAccessAssignment(3L)

        result shouldBe true
        verify(assetAssignmentService).canAccessAssetAssignment(3L, 1L)
      }

      "return false when user cannot access the assignment" in {
        setupSecurityContext(userId = 1L)
        when(assetAssignmentService.canAccessAssetAssignment(3L, 1L)).thenReturn(false)

        val result = authService.canAccessAssignment(3L)

        result shouldBe false
      }

      "return false when no user is authenticated" in {
        setupEmptySecurityContext()

        val result = authService.canAccessAssignment(3L)

        result shouldBe false
      }

      "return false when authentication details is null" in {
        setupSecurityContextWithoutDetails()

        val result = authService.canAccessAssignment(3L)

        result shouldBe false
      }
    }
  }
}
