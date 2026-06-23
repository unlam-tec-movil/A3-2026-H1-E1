package ar.edu.unlam.mobile.scaffolding.application.port.out.local.db

interface HasStoredClinicsUseCase {
    suspend operator fun invoke(): Boolean
}
