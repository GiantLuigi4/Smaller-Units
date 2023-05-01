plugins {
    id("xyz.deftu.gradle.multiversion-root")
}

preprocess {
    var fabric11902 = createNode("1.19.2-fabric", 1_19_02, "yarn")
    var forge11902 = createNode("1.19.2-forge", 1_19_02, "srg")

    forge11902.link(fabric11902)
}
