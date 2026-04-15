package com.example.demo.Util

import com.example.demo.Model.Enums.UserType
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterEach
import org.springframework.test.util.ReflectionTestUtils

class JwtUtilTest extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  var jwtUtil: JwtUtil = _

  override def beforeEach(): Unit = {
    jwtUtil = new JwtUtil()
    ReflectionTestUtils.setField(jwtUtil, "secret", "myVeryLongSecretKeyForJWTTokenGeneration")
    ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L)
  }

  "JwtUtil" should {

    "generate a valid JWT token" in {
      val token = jwtUtil.generateToken("testuser", UserType.EMPLOYEE, 1L)

      token should not be empty
      token.split("\\.").length shouldBe 3
    }

    "validate a correct token" in {
      val token = jwtUtil.generateToken("testuser", UserType.ADMIN, 1L)

      jwtUtil.validateToken(token) shouldBe true
    }

    "reject an invalid token" in {
      jwtUtil.validateToken("invalid.token.here") shouldBe false
    }

    "extract username from token" in {
      val token = jwtUtil.generateToken("john.doe", UserType.EMPLOYEE, 1L)

      jwtUtil.extractUsername(token) shouldBe "john.doe"
    }

    "extract userType from token" in {
      val token = jwtUtil.generateToken("admin", UserType.ADMIN, 1L)

      jwtUtil.extractUserType(token) shouldBe UserType.ADMIN
    }

    "extract userId from token" in {
      val token = jwtUtil.generateToken("user", UserType.EMPLOYEE, 42L)

      jwtUtil.extractUserId(token) shouldBe 42L
    }
  }
}
