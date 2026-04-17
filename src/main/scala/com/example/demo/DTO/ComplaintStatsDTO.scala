package com.example.demo.DTO

import scala.compiletime.uninitialized

class ComplaintStatsDTO{
                            var totalComplaints:Long =uninitialized
                            var open: Long = uninitialized
                            var resolved: Long = uninitialized
                            var inProgress :Long = uninitialized
}