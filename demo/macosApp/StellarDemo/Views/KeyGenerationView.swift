import SwiftUI
import shared

struct KeyGenerationScreen: View {
    @EnvironmentObject var bridgeWrapper: MacOSBridgeWrapper
    @ObservedObject var toastManager: ToastManager
    @State private var keypairData: KeyPair?
    @State private var isGenerating = false
    @State private var showSecret = false

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                VStack(spacing: 16) {
                    InfoCard(title: "Stellar Keypair Generation", color: .secondary) {
                        Text("Generate a cryptographically secure Ed25519 keypair for Stellar network operations. The keypair consists of a public key (account ID starting with 'G') and a secret seed (starting with 'S').")
                            .font(.system(size: 13))
                            .foregroundStyle(Material3Colors.onSecondaryContainer)
                    }

                    LoadingButton(
                        action: generateKeypair,
                        isLoading: isGenerating,
                        isEnabled: !isGenerating,
                        icon: "arrow.clockwise",
                        text: keypairData == nil ? "Generate Keypair" : "Generate New Keypair",
                        loadingText: "Generating..."
                    )

                    if let data = keypairData {
                        KeyDisplayCard(
                            title: "Public Key (Account ID)",
                            value: data.getAccountId(),
                            description: "This is your public address. Share this to receive payments.",
                            backgroundColor: Color.white,
                            textColor: Material3Colors.onSurface,
                            descriptionColor: Material3Colors.onSurfaceVariant,
                            iconColor: Material3Colors.primary,
                            onCopy: {
                                ClipboardHelper.copy(data.getAccountId())
                                toastManager.show("Public key copied to clipboard")
                            }
                        )

                        SecretKeyDisplayCard(
                            title: "Secret Seed",
                            keypair: data,
                            description: "NEVER share this! Anyone with this seed can access your account.",
                            isVisible: $showSecret,
                            onCopy: {
                                ClipboardHelper.copy(data.getSecretSeedAsString() ?? "")
                                toastManager.show("Secret seed copied to clipboard")
                            }
                        )

                        InfoCard(title: "Security Warning", color: .error) {
                            Text("Keep your secret seed safe! Store it in a secure password manager or write it down and keep it in a safe place. Anyone who has access to your secret seed can access and control your account.")
                                .font(.system(size: 13))
                                .foregroundStyle(Material3Colors.onErrorContainer)
                        }
                        .id("resultCard")

                    } else if !isGenerating {
                        Spacer()
                            .frame(height: 32)

                        Text("Tap the button above to generate a new Stellar keypair")
                            .font(.system(size: 14))
                            .foregroundStyle(Material3Colors.onSurfaceVariant)
                            .multilineTextAlignment(.center)
                    }
                }
                .padding(16)
            }
            .background(Material3Colors.surface)
            .navigationToolbar(title: "Key Generation")
            .onChange(of: keypairData) { newValue in
                if newValue != nil {
                    withAnimation {
                        // Smart auto-scroll: reveal result without scrolling too far
                        proxy.scrollTo("resultCard", anchor: .top)
                    }
                }
            }
        }
    }

    private func generateKeypair() {
        isGenerating = true

        Task {
            do {
                let data = try await bridgeWrapper.bridge.generateKeypair()
                await MainActor.run {
                    keypairData = data
                    showSecret = false
                    isGenerating = false
                    toastManager.show("New keypair generated successfully")
                }
            } catch {
                await MainActor.run {
                    isGenerating = false
                    toastManager.show("Error: \(error.localizedDescription)")
                }
            }
        }
    }
}
