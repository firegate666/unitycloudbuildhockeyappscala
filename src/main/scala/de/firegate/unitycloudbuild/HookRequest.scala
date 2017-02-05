package de.firegate.unitycloudbuild

final case class ApiEndpoints(
  api_self: ApiEndpoint,
  dashboard_url: ApiEndpoint,
  dashboard_project: ApiEndpoint,
  dashboard_summary: ApiEndpoint,
  dashboard_log: ApiEndpoint
)

final case class ApiEndpoint(method: String, href: String)

final case class HookRequest(
  projectName: String,
  buildTargetName: String,
  projectGuid: String,
  orgForeignKey: String,
  buildNumber: Int,
  buildStatus: String,
  startedBy: String,
  platform: String,
  scmType: String,
  links: ApiEndpoints
)
