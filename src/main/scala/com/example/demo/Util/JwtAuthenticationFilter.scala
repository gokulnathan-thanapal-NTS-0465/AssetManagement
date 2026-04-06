package com.example.demo.Util

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import jakarta.servlet.{FilterChain, ServletException}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import com.example.demo.Model.Enums.UserType

import java.io.IOException
import scala.jdk.CollectionConverters.*

@Component
class JwtAuthenticationFilter @Autowired()(jwtUtil: JwtUtil) extends OncePerRequestFilter {

  @throws[ServletException]
  @throws[IOException]
  override def doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain): Unit = {

    val requestPath = request.getRequestURI
    
    val authHeader = request.getHeader("Authorization")

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      val token = authHeader.substring(7)
        if (jwtUtil.validateToken(token)) {
          val username = jwtUtil.extractUsername(token)
          val userType = jwtUtil.extractUserType(token)
          val userId = jwtUtil.extractUserId(token)

          val authorities = getAuthoritiesForUserType(userType)

          val authentication = new UsernamePasswordAuthenticationToken(
            username,
            null,
            authorities.asJava
          )

          authentication.setDetails(Map("userId" -> userId))

          SecurityContextHolder.getContext.setAuthentication(authentication)
        }
    }

    chain.doFilter(request, response)
  }

  private def getAuthoritiesForUserType(userType: UserType): List[SimpleGrantedAuthority] = {
    userType match {
      case UserType.ADMIN => List(
        new SimpleGrantedAuthority("ROLE_ADMIN")
      )
      case UserType.TECH => List(
        new SimpleGrantedAuthority("ROLE_TECH")
      )
      case UserType.EMPLOYEE => List(
        new SimpleGrantedAuthority("ROLE_EMPLOYEE")
      )
    }
  }
}
