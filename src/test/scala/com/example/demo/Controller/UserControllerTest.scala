package com.example.demo.Controller

import com.example.demo.DTO.{UserCreateDTO, UserCredentialDTO, UserDepartmentDTO, UserResponseDTO}
import com.example.demo.Model.Enums.UserType
import com.example.demo.Service.UserService
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

import java.time.LocalDate
import scala.compiletime.uninitialized

class UserControllerTest extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var userService: UserService = uninitialized
  var mockMvc: MockMvc = uninitialized

  override def beforeEach(): Unit = {
    userService = mock[UserService]

    mockMvc = MockMvcBuilders
      .standaloneSetup(new UserController(userService))
      .setControllerAdvice(new GlobalExceptionHandler())
      .build()
  }

  private def sampleUserResponse(): UserResponseDTO = {
    val dto = new UserResponseDTO
    dto.id = 1L
    dto.username = "testuser1"
    dto.userType = UserType.EMPLOYEE
    dto.employeeId = "EM0001"
    dto.creditBalance = 100
    dto.department = "engineering1"
    dto.joinedDate = LocalDate.now()
    dto
  }


  "POST /api/user/create" should {

    "return 201 with valid data" in {
      val response = sampleUserResponse()
      when(userService.createUser(any[UserCreateDTO])).thenReturn(response)

      mockMvc.perform(
          post("/api/user/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"username":"testuser1","userType":"EMPLOYEE","department":"engineering1","passwordHash":"password1"}""")
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.username").value("testuser1"))
        .andExpect(jsonPath("$.userType").value("EMPLOYEE"))
        .andExpect(jsonPath("$.employeeId").value("EM0001"))
    }

    "return 400 when service throws IllegalStateException for duplicate username" in {
      when(userService.createUser(any[UserCreateDTO])).thenThrow(new IllegalStateException("Username already exists"))

      mockMvc.perform(
          post("/api/user/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"username":"testuser1","userType":"EMPLOYEE","department":"engineering1","passwordHash":"password1"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Username already exists"))
    }
  }


  "GET /api/user/{userId}" should {

    "return 200 with valid user" in {
      val response = sampleUserResponse()
      when(userService.getUserById(1L)).thenReturn(response)

      mockMvc.perform(get("/api/user/1"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.username").value("testuser1"))
    }

    "return 404 when user not found" in {
      when(userService.getUserById(999L)).thenThrow(new EntityNotFoundException("User not found"))

      mockMvc.perform(get("/api/user/999"))
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("User not found"))
    }
  }


  "DELETE /api/user/{userId}" should {

    "return 200 and deactivate user" in {
      mockMvc.perform(delete("/api/user/1"))
        .andExpect(status().isOk)
        .andExpect(content().string("User deactivated Successfully"))

      verify(userService).deactivateUserById(1L)
    }

    "return 404 when user not found" in {
      when(userService.deactivateUserById(999L)).thenThrow(new EntityNotFoundException("User not found"))

      mockMvc.perform(delete("/api/user/999"))
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("User not found"))
    }
  }


  "PATCH /api/user/{userId}/credentials" should {

    "return 200 with valid credential update" in {
      val response = sampleUserResponse()
      response.username = "newuser01"
      when(userService.updateUserCredentialById(eqTo(1L), any[UserCredentialDTO])).thenReturn(response)

      mockMvc.perform(
          patch("/api/user/1/credentials")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"username":"newuser01","passwordHash":"newpass01"}""")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.username").value("newuser01"))
    }

    "return 404 when user not found" in {
      when(userService.updateUserCredentialById(eqTo(999L), any[UserCredentialDTO]))
        .thenThrow(new EntityNotFoundException("User not found"))

      mockMvc.perform(
          patch("/api/user/999/credentials")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"username":"newuser01"}""")
        )
        .andExpect(status().isNotFound)
    }

    "return 400 when user is deactivated" in {
      when(userService.updateUserCredentialById(eqTo(1L), any[UserCredentialDTO]))
        .thenThrow(new IllegalStateException("Cannot update credentials for a deactivated user"))

      mockMvc.perform(
          patch("/api/user/1/credentials")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"username":"newuser01"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Cannot update credentials for a deactivated user"))
    }

    "return 400 when duplicate username" in {
      when(userService.updateUserCredentialById(eqTo(1L), any[UserCredentialDTO]))
        .thenThrow(new IllegalStateException("Username already exists"))

      mockMvc.perform(
          patch("/api/user/1/credentials")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"username":"existing1"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Username already exists"))
    }

    "return 400 when same username as current" in {
      when(userService.updateUserCredentialById(eqTo(1L), any[UserCredentialDTO]))
        .thenThrow(new IllegalArgumentException("New username cannot be the same as the current username"))

      mockMvc.perform(
          patch("/api/user/1/credentials")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"username":"testuser1"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("New username cannot be the same as the current username"))
    }
  }


  "PATCH /api/user/{userId}/department" should {

    "return 200 for valid department update" in {
      val response = sampleUserResponse()
      response.department = "marketing01"
      when(userService.updateUserDepartmentById(eqTo(1L), any[UserDepartmentDTO])).thenReturn(response)

      mockMvc.perform(
          patch("/api/user/1/department")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"department":"marketing01"}""")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.department").value("marketing01"))
    }

    "return 404 when user not found" in {
      when(userService.updateUserDepartmentById(eqTo(999L), any[UserDepartmentDTO]))
        .thenThrow(new EntityNotFoundException("User not found"))

      mockMvc.perform(
          patch("/api/user/999/department")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"department":"marketing01"}""")
        )
        .andExpect(status().isNotFound)
    }

    "return 400 when same department as current" in {
      when(userService.updateUserDepartmentById(eqTo(1L), any[UserDepartmentDTO]))
        .thenThrow(new IllegalArgumentException("New department cannot be the same as the current department"))

      mockMvc.perform(
          patch("/api/user/1/department")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"department":"engineering1"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("New department cannot be the same as the current department"))
    }

    "return 400 when user is deactivated" in {
      when(userService.updateUserDepartmentById(eqTo(1L), any[UserDepartmentDTO]))
        .thenThrow(new IllegalStateException("Cannot update department for a deactivated user"))

      mockMvc.perform(
          patch("/api/user/1/department")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"department":"marketing01"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Cannot update department for a deactivated user"))
    }
  }


  "GET /api/user/all" should {

    "return 200 with no filters" in {
      val users = List(sampleUserResponse())
      when(userService.getAllUsers(None, None)).thenReturn(users)

      mockMvc.perform(get("/api/user/all"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$[0].id").value(1))
    }

    "return 200 with userType filter" in {
      val users = List(sampleUserResponse())
      when(userService.getAllUsers(Some(UserType.EMPLOYEE), None)).thenReturn(users)

      mockMvc.perform(
          get("/api/user/all").param("userType", "EMPLOYEE")
        )
        .andExpect(status().isOk)
    }

    "return 200 with deactivated filter" in {
      when(userService.getAllUsers(None, Some(false))).thenReturn(List(sampleUserResponse()))

      mockMvc.perform(
          get("/api/user/all").param("deactivated", "false")
        )
        .andExpect(status().isOk)
    }

    "return 200 with both filters" in {
      when(userService.getAllUsers(Some(UserType.EMPLOYEE), Some(true))).thenReturn(List.empty)

      mockMvc.perform(
          get("/api/user/all")
            .param("userType", "EMPLOYEE")
            .param("deactivated", "true")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isEmpty)
    }

    "return empty list when no users exist" in {
      when(userService.getAllUsers(None, None)).thenReturn(List.empty)

      mockMvc.perform(get("/api/user/all"))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$").isEmpty)
    }
  }


  "PATCH /api/user/credit/{userId}" should {

    "return 200 with valid balance" in {
      val response = sampleUserResponse()
      response.creditBalance = 200
      when(userService.updateCreditBalance(1L, 200)).thenReturn(response)

      mockMvc.perform(
          patch("/api/user/credit/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("200")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.creditBalance").value(200))
    }

    "return 400 for negative balance" in {
      when(userService.updateCreditBalance(1L, -5))
        .thenThrow(new IllegalArgumentException("Credit balance cannot be negative"))

      mockMvc.perform(
          patch("/api/user/credit/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("-5")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Credit balance cannot be negative"))
    }

    "return 404 when user not found" in {
      when(userService.updateCreditBalance(999L, 100))
        .thenThrow(new EntityNotFoundException("User not found"))

      mockMvc.perform(
          patch("/api/user/credit/999")
            .contentType(MediaType.APPLICATION_JSON)
            .content("100")
        )
        .andExpect(status().isNotFound)
    }

    "return 400 when user is deactivated" in {
      when(userService.updateCreditBalance(1L, 100))
        .thenThrow(new IllegalStateException("Cannot update credit balance for a deactivated user"))

      mockMvc.perform(
          patch("/api/user/credit/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("100")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("Cannot update credit balance for a deactivated user"))
    }
  }
}
