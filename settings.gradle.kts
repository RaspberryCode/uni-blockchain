plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "uni-blockchain"

include("blockchain-full-node", "blockchain-miner-node", "user-api")
