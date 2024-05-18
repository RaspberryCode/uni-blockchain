plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "uni-blockchain"

include("common-node", "full-node", "miner-node", "user-api-node")
