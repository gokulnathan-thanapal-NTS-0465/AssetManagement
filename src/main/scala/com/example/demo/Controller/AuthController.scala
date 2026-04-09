package com.example.demo.Controller

import com.example.demo.DTO.{LoginRequestDTO, LoginResponseDTO}
import com.example.demo.Service.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value=Array("/api/auth"))
@CrossOrigin(origins = Array("*"))
class AuthController @Autowired()(authService: AuthService) {

  @PostMapping(value=Array("/login"))
  def login(@RequestBody loginRequest: LoginRequestDTO): ResponseEntity[LoginResponseDTO] = {
    val response: LoginResponseDTO = authService.login(loginRequest)
    new ResponseEntity[LoginResponseDTO](response,HttpStatus.OK)
  }

  @PostMapping(value=Array("/logout"))
  def logout(): ResponseEntity[String] = {
    ResponseEntity.ok("Logout successful")
  }
}
