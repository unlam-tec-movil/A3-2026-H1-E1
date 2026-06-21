package ar.edu.unlam.mobile.scaffolding.application.service.local.db

import ar.edu.unlam.mobile.scaffolding.domain.model.Clinic
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun List<Clinic>.getNearestClinics(
    userLat: Double,
    userLng: Double,
    count: Int = 10,
): List<Clinic> =
    this
        .map { clinic ->
            clinic to haversineDistance(userLat, userLng, clinic.lat, clinic.lng)
        }.sortedBy { (_, distance) -> distance }
        .take(count)
        .map { (clinic, _) -> clinic }

private fun haversineDistance(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double,
): Double {
    @Suppress("ktlint:standard:property-naming")
    val R = 6371 // Earth's radius in kilometers
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a =
        sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2).pow(2)
    val c = 2 * asin(sqrt(a))
    return R * c
}
