package de.firegate.unitycloudbuild.entities

final case class ApiEndpoints(
  api_self: ApiEndpoint,
  dashboard_url: ApiEndpoint,
  dashboard_project: ApiEndpoint,
  dashboard_summary: ApiEndpoint,
  dashboard_log: ApiEndpoint
)
