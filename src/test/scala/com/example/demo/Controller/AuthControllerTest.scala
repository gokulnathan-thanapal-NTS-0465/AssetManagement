package com.example.demo.Controller

import com.example.demo.DTO.{LoginRequestDTO, LoginResponseDTO}
import com.example.demo.Model.Enums.UserType
import com.example.demo.Service.AuthService
import com.example.demo.Util.GlobalExceptionHandler
import jakarta.persistence.EntityNotFoundException
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
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

class AuthControllerTest extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  var authService: AuthService = uninitialized
  var mockMvc: MockMvc = uninitialized

  override def beforeEach(): Unit = {
    authService = mock[AuthService]
    mockMvc = MockMvcBuilders
      .standaloneSetup(new AuthController(authService))
      .setControllerAdvice(new GlobalExceptionHandler())
      .build()
  }


  "POST /api/auth/login" should {

    "return 200 with valid credentials" in {
      val response = LoginResponseDTO(
        token = "jwt-token-abc123",
        username = "admin001",
        userType = UserType.ADMIN,
        userId = 1L
      )
      when(authService.login(any[LoginRequestDTO])).thenReturn(response)

      mockMvc.perform(
          post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"username":"admin001","password":"admin01"}""")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.token").value("jwt-token-abc123"))
        .andExpect(jsonPath("$.username").value("admin001"))
        .andExpect(jsonPath("$.userType").value("ADMIN"))
        .andExpect(jsonPath("$.userId").value(1))
    }

    "return 404 when user not found" in {
      when(authService.login(any[LoginRequestDTO]))
        .thenThrow(new EntityNotFoundException("User not found"))

      mockMvc.perform(
          post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"username":"unknown1","password":"pass1234"}""")
        )
        .andExpect(status().isNotFound)
        .andExpect(jsonPath("$.message").value("User not found"))
    }

    "return 403 when invalid credentials" in {
      when(authService.login(any[LoginRequestDTO]))
        .thenThrow(new SecurityException("Invalid credentials"))

      mockMvc.perform(
          post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"username":"admin001","password":"wrongpas1"}""")
        )
        .andExpect(status().isForbidden)
        .andExpect(jsonPath("$.message").value("Invalid credentials"))
    }

    "return 400 when account is deactivated" in {
      when(authService.login(any[LoginRequestDTO]))
        .thenThrow(new IllegalStateException("User account is deactivated"))

      mockMvc.perform(
          post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"username":"deactive1","password":"pass1234"}""")
        )
        .andExpect(status().isBadRequest)
        .andExpect(jsonPath("$.message").value("User account is deactivated"))
    }


    "return 200 with EMPLOYEE user type" in {
      val response = LoginResponseDTO(
        token = "jwt-token-emp456",
        username = "employee1",
        userType = UserType.EMPLOYEE,
        userId = 10L
      )
      when(authService.login(any[LoginRequestDTO])).thenReturn(response)

      mockMvc.perform(
          post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"username":"employee1","password":"emppass01"}""")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.userType").value("EMPLOYEE"))
        .andExpect(jsonPath("$.userId").value(10))
    }

    "return 200 with TECH user type" in {
      val response = LoginResponseDTO(
        token = "jwt-token-tech789",
        username = "techuser1",
        userType = UserType.TECH,
        userId = 20L
      )
      when(authService.login(any[LoginRequestDTO])).thenReturn(response)

      mockMvc.perform(
          post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"username":"techuser1","password":"techpas01"}""")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.userType").value("TECH"))
    }
  }


  "POST /api/auth/logout" should {

    "return 200 with success message" in {
      mockMvc.perform(post("/api/auth/logout"))
        .andExpect(status().isOk)
        .andExpect(content().string("Logout successful"))
    }
  }
}
