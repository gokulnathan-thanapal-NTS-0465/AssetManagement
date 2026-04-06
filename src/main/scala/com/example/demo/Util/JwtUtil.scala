package com.example.demo.Util

import io.jsonwebtoken.{Claims, Jwts, SignatureAlgorithm}
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import com.example.demo.Model.Enums.UserType

import java.util.{Date, HashMap => JHashMap}
import javax.crypto.SecretKey

@Component
class JwtUtil {

  @Value("${jwt.secret}")
  private var secret: String = _

  @Value("${jwt.expiration}")
  private var expiration: Long = _

  private def getSigningKey: SecretKey = {
    Keys.hmacShaKeyFor(secret.getBytes("UTF-8"))
  }

  def generateToken(username: String, userType: UserType, userId: Long): String = {

    val claims = new JHashMap[String, Object]()
    claims.put("userType", userType.toString)
    claims.put("userId", userId.toString)
    val token: String = Jwts.builder()
      .claims(claims)
      .subject(username)
      .issuedAt(new Date(System.currentTimeMillis()))
      .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
      .signWith(getSigningKey)
      .compact()
    token

  }

  def extractUsername(token: String): String = {
    extractAllClaims(token).getSubject
  }

  def extractUserType(token: String): UserType = {
    val claims = extractAllClaims(token)
    UserType.valueOf(claims.get("userType", classOf[String]))
  }

  def extractUserId(token: String): Long = {
    val claims = extractAllClaims(token)
    claims.get("userId", classOf[String]).toLong
  }

  def isTokenExpired(token: String): Boolean = {
    extractAllClaims(token).getExpiration.before(new Date())
  }

  def validateToken(token: String): Boolean = {
    try {
      !isTokenExpired(token)
    } catch {
      case _: Exception => false
    }
  }

  private def extractAllClaims(token: String): Claims = {
    Jwts.parser().verifyWith(getSigningKey).build().parseSignedClaims(token).getPayload
  }
}
