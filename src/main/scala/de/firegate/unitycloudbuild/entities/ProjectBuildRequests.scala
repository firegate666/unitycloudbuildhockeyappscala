package de.firegate.unitycloudbuild.entities

case class ProjectBuildRequestArtifactFile(
  filename: String,
  size: Int,
  href: String
)

case class ProjectBuildRequestArtifact(
  key: String,
  name: String,
  primary: Boolean,
  show_download: Boolean,
  files: List[ProjectBuildRequestArtifactFile]
)

case class ProjectBuildRequestDownloadPrimaryMeta(
  `type`: String
)

case class ProjectBuildRequestDownloadPrimary(
  method: String,
  href: String,
  meta: ProjectBuildRequestDownloadPrimaryMeta
)

case class ProjectBuildRequestLinks(
  self: ApiEndpoint,
  log: ApiEndpoint,
  auditlog: ApiEndpoint,
  canceled: Option[ApiEndpoint] = None,
  artifacts: List[ProjectBuildRequestArtifact],
  create_share: Option[ApiEndpoint] = None,
  revoke_share: Option[ApiEndpoint] = None,
  icon: Option[ApiEndpoint] = None,
  download_primary : Option[ProjectBuildRequestDownloadPrimary] = None
)

case class ProjectBuildRequestProjectVersion(
  name: String,
  filename: String,
  projectName: String,
  platform: String,
  size: Int,
  created: String,
  lastMod: String,
  bundleId: String,
  udids: List[String]
)


case class ProjectBuildCanceledRequest(
  build: Int,
  buildtargetid: String,
  buildTargetName: String,
  buildStatus: String,
  canceledBy: String,
  platform: String,
  workspaceSize: Int,
  created: String,
  finished: String,
  checkoutStartTime: String,
  checkoutTimeInSeconds: Int,
  buildStartTime: String,
  totalTimeInSeconds: Float,
  lastBuiltRevision: String,
  changeset: List[String],
  favorited: Boolean,
  deleted: Boolean,
  cooldownDate: String,
  auditChanges: Int,
  projectVersion: Null, // ??
  projectName: String,
  projectId: String,
  orgId: String,
  links: ProjectBuildRequestLinks
)

case class ProjectBuildQueuedRequest(
  build: Int,
  buildtargetid: String,
  buildTargetName: String,
  buildStatus: String,
  platform: String,
  created: String,
  changeset: List[String],
  favorited: Boolean,
  deleted: Boolean,
  queuedReason: String,
  cooldownDate: String,
  auditChanges: Int,
  projectVersion: Option[ProjectBuildRequestProjectVersion] = None, // ??
  projectName: String,
  projectId: String,
  orgId: String,
  links: ProjectBuildRequestLinks
)

case class ProjectBuildStartedRequest(
  build: Int,
  buildtargetid: String,
  buildTargetName: String,
  buildStatus: String,
  platform: String,
  created: String,
  checkoutStartTime: String,
  changeset: List[String],
  favorited: Boolean,
  deleted: Boolean,
  cooldownDate: String,
  auditChanges: Int,
  projectVersion: Null, // ??
  projectName: String,
  projectId: String,
  orgId: String,
  links: ProjectBuildRequestLinks
)

case class ProjectBuildSuccessRequest(
  build: Int,
  buildtargetid: String,
  buildTargetName: String,
  buildStatus: String,
  platform: String,
  workspaceSize: Int,
  created: String,
  finished: String,
  checkoutStartTime: String,
  checkoutTimeInSeconds: Int,
  buildStartTime: String,
  buildTimeInSeconds: Float,
  publishStartTime: String,
  publishTimeInSeconds: Float,
  totalTimeInSeconds: Float,
  lastBuiltRevision: String,
  changeset: List[String],
  favorited: Boolean,
  deleted: Boolean,
  cooldownDate: String,
  auditChanges: Int,
  projectVersion: ProjectBuildRequestProjectVersion,
  projectName: String,
  projectId: String,
  orgId: String,
  links: ProjectBuildRequestLinks
)
