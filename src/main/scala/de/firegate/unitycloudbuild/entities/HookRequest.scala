package de.firegate.unitycloudbuild.entities

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
