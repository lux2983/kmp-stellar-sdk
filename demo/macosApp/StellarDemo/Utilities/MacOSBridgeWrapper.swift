import SwiftUI
import shared

/// Centralized wrapper for MacOSBridge to enable dependency injection via @EnvironmentObject.
///
/// This class wraps the stateless Kotlin MacOSBridge class and makes it available throughout
/// the SwiftUI view hierarchy using the @EnvironmentObject pattern. This eliminates the need
/// for each view to create its own bridge instance.
///
/// MacOSBridge is stateless and has no observable properties, so this wrapper simply provides
/// a single shared instance. All bridge methods are suspend functions that return results
/// directly without modifying internal state.
///
/// Usage:
/// ```swift
/// // In StellarDemoApp.swift:
/// @main
/// struct StellarDemoApp: App {
///     @StateObject private var bridgeWrapper = MacOSBridgeWrapper()
///     var body: some Scene {
///         WindowGroup {
///             NavigationStack {
///                 MainScreen()
///             }
///             .environmentObject(bridgeWrapper)
///         }
///     }
/// }
///
/// // In view files:
/// struct KeyGenerationScreen: View {
///     @EnvironmentObject var bridgeWrapper: MacOSBridgeWrapper
///
///     var body: some View {
///         // Access bridge via bridgeWrapper.bridge
///         let keypair = try await bridgeWrapper.bridge.generateKeypair()
///     }
/// }
/// ```
class MacOSBridgeWrapper: ObservableObject {

    /// The wrapped MacOSBridge instance.
    ///
    /// This provides access to all Kotlin business logic functions:
    /// - generateKeypair(): Generate random Stellar keypairs
    /// - fundAccount(accountId:): Fund testnet accounts via Friendbot
    /// - fetchAccountDetails(accountId:): Fetch account data from Horizon
    /// - trustAsset(...): Establish trustlines to assets
    /// - sendPayment(...): Send XLM or issued assets
    /// - fetchContractDetails(contractId:): Fetch Soroban contract metadata
    /// - deployContract(...): Deploy Soroban smart contracts
    /// - loadTokenContract(contractId:): Load and validate token contracts
    /// - invokeTokenFunction(...): Invoke token contract functions
    ///
    /// All methods are suspend functions that should be called from async contexts (Task blocks).
    let bridge = MacOSBridge()

    /// Initialize the bridge wrapper.
    ///
    /// Creates a single MacOSBridge instance that will be shared across all views
    /// via SwiftUI's environment object system.
    init() {
        // MacOSBridge is stateless, so no additional initialization needed
    }
}
