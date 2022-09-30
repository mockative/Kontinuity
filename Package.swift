// swift-tools-version:5.3
import PackageDescription

let package = Package(
    name: "Kontinuity",
    platforms: [.iOS(.v13), .macOS(.v10_15), .tvOS(.v13), .watchOS(.v6)],
    products: [
        .library(
            name: "KontinuityCore",
            targets: ["KontinuityCore"]
        ),
        .library(
            name: "KontinuityCombine",
            targets: ["KontinuityCombine"]
        ),
        .library(
            name: "KontinuityAsync",
            targets: ["KontinuityAsync"]
        )
    ],
    targets: [
        .target(
            name: "KontinuityCore",
            path: "KontinuityCore"
        ),
        .target(
            name: "KontinuityCombine",
            dependencies: ["KontinuityCore"],
            path: "KontinuityCombine"
        ),
        .testTarget(
            name: "KontinuityCombineTests",
            dependencies: ["KontinuityCombine"],
            path: "KontinuityCombineTests"
        ),
        .target(
            name: "KontinuityAsync",
            dependencies: ["KontinuityCore"],
            path: "KontinuityAsync"
        ),
        .testTarget(
            name: "KontinuityAsyncTests",
            dependencies: ["KontinuityAsync"],
            path: "KontinuityAsyncTests"
        )
    ]
)
