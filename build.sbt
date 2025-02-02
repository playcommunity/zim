import de.heikoseeberger.sbtheader.HeaderPlugin

Global / onLoad := {
  val GREEN = "\u001b[32m"
  val RESET = "\u001b[0m"
  println(s"""$GREEN
             |$GREEN                                 ____
             |$GREEN                ,--,           ,'  , `.
             |$GREEN        ,----,,--.'|        ,-+-,.' _ |
             |$GREEN      .'   .`||  |,      ,-+-. ;   , ||
             |$GREEN   .'   .'  .'`--'_     ,--.'|'   |  ||
             |$GREEN ,---, '   ./ ,' ,'|   |   |  ,', |  |,
             |$GREEN ;   | .'  /  '  | |   |   | /  | |--'
             |$GREEN `---' /  ;--,|  | :   |   : |  | ,
             |$GREEN   /  /  / .`|'  : |__ |   : |  |/
             |$GREEN ./__;     .' |  | '.'||   | |`-'
             |$GREEN ;   |  .'    ;  :    ;|   ;/
             |$GREEN `---'        |  ,   / '---'
             |$GREEN               ---`-'
             |$RESET        v.${version.value}
             |""".stripMargin)
  (Global / onLoad).value
}

ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("public"),
  Resolver.sonatypeRepo("snapshots")
)

lazy val assemblySettings = Seq(
  ThisBuild / assemblyMergeStrategy := {
    case "application.conf"                              => MergeStrategy.concat
    case x if x.endsWith(".txt") || x.endsWith(".proto") => MergeStrategy.first
    case x if x.endsWith("module-info.class")            => MergeStrategy.first
    case x if x.endsWith(".properties")                  => MergeStrategy.deduplicate
    case x =>
      val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)

lazy val configurationNoPublish: Project => Project =
  _.settings(Information.value)
    .settings(ProjectSetting.value)
    .settings(ProjectSetting.noPublish)
    .settings(commands ++= Commands.value)
    .settings(assemblySettings)

lazy val zim = (project in file("."))
  .settings(name := "zim")
  .aggregate(`zim-server`, `zim-domain`, `zim-cache`, `zim-tapir`, `zim-auth`)
  .configure(configurationNoPublish)

lazy val `zim-server` = (project in file("modules/zim-server"))
  .settings(libraryDependencies ++= Dependencies.serverDeps)
  .settings(assembly / mainClass := Some("org.bitlap.zim.server.ZimServer"))
  .configure(configurationNoPublish)
  .enablePlugins(ScalafmtPlugin, HeaderPlugin)
  .dependsOn(`zim-domain`, `zim-cache`, `zim-tapir`, `zim-auth`)

lazy val `zim-domain` = (project in file("modules/zim-domain"))
  .settings(BuildInfoSettings.value)
  .settings(libraryDependencies ++= Dependencies.domainDeps)
  .configure(configurationNoPublish)
  .enablePlugins(GitVersioning, BuildInfoPlugin, ScalafmtPlugin, HeaderPlugin)

lazy val `zim-cache` = (project in file("modules/zim-cache"))
  .settings(libraryDependencies ++= Dependencies.cacheDeps)
  .configure(configurationNoPublish)
  .enablePlugins(ScalafmtPlugin, HeaderPlugin)

lazy val `zim-tapir` = (project in file("modules/zim-tapir"))
  .settings(libraryDependencies ++= Dependencies.tapirApiDeps)
  .configure(configurationNoPublish)
  .enablePlugins(ScalafmtPlugin, HeaderPlugin)
  .dependsOn(`zim-domain`)

lazy val `zim-auth` = (project in file("modules/zim-auth"))
  .settings(libraryDependencies ++= Dependencies.domainDeps)
  .configure(configurationNoPublish)
  .enablePlugins(ScalafmtPlugin, HeaderPlugin)
  .dependsOn(`zim-domain`)