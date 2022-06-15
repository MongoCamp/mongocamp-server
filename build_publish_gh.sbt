publishTo := Some("GitHub Package Registry".at("https://maven.pkg.github.com/mongocamp/mongocamp-server/"))
credentials += Credentials("GitHub Package Registry", "maven.pkg.github.com", System.getenv("GITHUB_USER"), System.getenv("GITHUB_TOKEN"))
