package io.censystems.marketkit.models

data class Auditor(
    val name: String,
    val reports: List<AuditReport>
)
