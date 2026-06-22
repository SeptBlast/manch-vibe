// swift-tools-version: 5.10
// Add this file to your Xcode project via File → Add Package Dependencies,
// or reference it as a local package for CI.
//
// Minimum deployment: iOS 16.0

import PackageDescription

let package = Package(
    name: "Solace",
    platforms: [.iOS(.v16)],
    dependencies: [
        .package(
            url: "https://github.com/firebase/firebase-ios-sdk.git",
            from: "11.0.0"
        ),
        .package(
            url: "https://github.com/google/GoogleSignIn-iOS.git",
            from: "7.1.0"
        ),
    ],
    targets: [
        .target(
            name: "Solace",
            dependencies: [
                .product(name: "FirebaseAuth",         package: "firebase-ios-sdk"),
                .product(name: "FirebaseFirestore",    package: "firebase-ios-sdk"),
                .product(name: "FirebaseStorage",      package: "firebase-ios-sdk"),
                .product(name: "FirebaseRemoteConfig", package: "firebase-ios-sdk"),
                .product(name: "FirebaseAnalytics",    package: "firebase-ios-sdk"),
                .product(name: "FirebaseCrashlytics",  package: "firebase-ios-sdk"),
                .product(name: "GoogleSignIn",         package: "GoogleSignIn-iOS"),
                .product(name: "GoogleSignInSwift",    package: "GoogleSignIn-iOS"),
            ]
        ),
    ]
)
