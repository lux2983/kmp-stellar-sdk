import SwiftUI
import shared

// MARK: - Fund Account Screen (matches Compose FundAccountScreen)

struct FundAccountScreen: View {
    @ObservedObject var toastManager: ToastManager
    @State private var accountId = ""
    @State private var generatedKeypair: KeyPair?
    @State private var secretSeedVisible = false
    @State private var isGenerating = false
    @State private var isFunding = false
    @State private var fundingResult: AccountFundingResult?
    @State private var validationError: String?

    @EnvironmentObject var bridgeWrapper: MacOSBridgeWrapper

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                VStack(spacing: 16) {
                    infoCard
                    inputField
                    actionButtons
                    resultView
                    placeholderView
                }
                .padding(16)
            }
            .background(Material3Colors.surface)
            .navigationToolbar(title: "Fund Testnet Account")
            .onChange(of: fundingResult) { newValue in
                if newValue != nil {
                    withAnimation {
                        proxy.scrollTo("resultCard", anchor: .bottom)
                    }
                }
            }
        }
    }

    // MARK: - View Components

    private var infoCard: some View {
        InfoCard(title: "Friendbot: fund a testnet network account", color: .secondary) {
            Text("The friendbot is a horizon API endpoint that will fund an account with 10,000 lumens on the testnet network.")
                .font(.system(size: 13))
                .foregroundStyle(Material3Colors.onSecondaryContainer)
        }
    }

    private var inputField: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Public Key Field with StellarTextField
            StellarTextField(
                label: "Public Key",
                placeholder: "G...",
                text: $accountId,
                error: validationError,
                showCopyButton: !accountId.isEmpty,
                onCopy: {
                    ClipboardHelper.copy(accountId)
                    toastManager.show("Public key copied to clipboard")
                }
            )
            .onChange(of: accountId) { _ in
                validationError = nil
                fundingResult = nil
            }

            // Secret Seed Field (only shown when a keypair was generated)
            if let keypair = generatedKeypair {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Secret Seed (Private Key)")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(Material3Colors.onTertiaryContainer)

                    HStack(spacing: 0) {
                        if secretSeedVisible {
                            TextField("", text: .constant(keypair.getSecretSeedAsString() ?? ""))
                                .textFieldStyle(.plain)
                                .font(.system(.body, design: .monospaced))
                                .padding(12)
                                .disabled(true)
                        } else {
                            TextField("", text: .constant(String(repeating: "•", count: 56)))
                                .textFieldStyle(.plain)
                                .font(.system(.body, design: .monospaced))
                                .padding(12)
                                .disabled(true)
                        }

                        // Visibility toggle button
                        Button(action: {
                            secretSeedVisible.toggle()
                        }) {
                            Image(systemName: secretSeedVisible ? "eye.slash.fill" : "eye.fill")
                                .font(.system(size: 14))
                                .foregroundStyle(Material3Colors.onTertiaryContainer)
                                .frame(width: 44, height: 44)
                        }
                        .buttonStyle(.plain)
                        .help(secretSeedVisible ? "Hide secret" : "Show secret")

                        // Copy button
                        Button(action: {
                            if let secretSeed = keypair.getSecretSeedAsString() {
                                ClipboardHelper.copy(secretSeed)
                                toastManager.show("Secret seed copied to clipboard")
                            }
                        }) {
                            Image(systemName: "doc.on.doc")
                                .font(.system(size: 14))
                                .foregroundStyle(Material3Colors.onTertiaryContainer)
                                .frame(width: 44, height: 44)
                        }
                        .buttonStyle(.plain)
                        .help("Copy to clipboard")
                    }
                    .background(Material3Colors.tertiaryContainer)
                    .overlay(
                        RoundedRectangle(cornerRadius: 4)
                            .stroke(Material3Colors.onTertiaryContainer.opacity(0.3), lineWidth: 1)
                    )
                }

                // Security warning
                TransparentInfoCard(backgroundColor: Material3Colors.errorContainer.opacity(0.3)) {
                    HStack(spacing: 8) {
                        Image(systemName: "exclamationmark.triangle.fill")
                            .font(.system(size: 16))
                            .foregroundStyle(Material3Colors.onErrorContainer)
                        Text("WARNING:")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundStyle(Material3Colors.onErrorContainer)
                    }

                    Text("Keep your secret seed safe! Never share it with anyone. Anyone with access to your secret seed can control your account.")
                        .font(.system(size: 13))
                        .foregroundStyle(Material3Colors.onErrorContainer)
                        .padding(.top, 4)
                }
            }
        }
    }

    private var actionButtons: some View {
        HStack(spacing: 8) {
            LoadingButton(
                action: generateAndFill,
                isLoading: isGenerating,
                isEnabled: !isGenerating && !isFunding,
                icon: "arrow.clockwise",
                text: "Generate & Fill",
                loadingText: "Generating...",
                style: .outlined
            )

            LoadingButton(
                action: fundAccount,
                isLoading: isFunding,
                isEnabled: !isGenerating && !isFunding && !accountId.isEmpty,
                icon: "dollarsign.circle",
                text: "Get lumens",
                loadingText: "Funding..."
            )
        }
    }

    @ViewBuilder
    private var resultView: some View {
        if let result = fundingResult {
            if let success = result as? AccountFundingResult.Success {
                successCard(success)
                    .id("resultCard")
            } else if let error = result as? AccountFundingResult.Error {
                errorCard(error)
                    .id("resultCard")
                troubleshootingCard
            }
        }
    }

    private func successCard(_ success: AccountFundingResult.Success) -> some View {
        InfoCard(title: "Success", color: .success) {
            Text("Successfully funded \(shortenAccountId(success.accountId)) on testnet")
                .font(.system(size: 14))
                .foregroundStyle(Material3Colors.onSuccessContainer)

            Text(success.message)
                .font(.system(size: 13))
                .foregroundStyle(Material3Colors.onSuccessContainer)
                .padding(.top, 4)
        }
    }

    private func errorCard(_ error: AccountFundingResult.Error) -> some View {
        InfoCard(title: "Error", color: .error) {
            Text(error.message)
                .font(.system(size: 14))
                .foregroundStyle(Material3Colors.onErrorContainer)

            if let exception = error.exception {
                Text("Technical details: \(exception.message ?? "Unknown error")")
                    .font(.system(size: 13, design: .monospaced))
                    .foregroundStyle(Material3Colors.onErrorContainer)
                    .padding(.top, 4)
            }
        }
    }

    private var troubleshootingCard: some View {
        InfoCard(title: "Troubleshooting", color: .secondary) {
            VStack(alignment: .leading, spacing: 4) {
                Text("• Check that the account ID is valid (starts with 'G' and is 56 characters)")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• If the account was already funded, it cannot be funded again")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Verify you have an internet connection")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Try generating a new keypair if the issue persists")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)
            }
        }
    }

    @ViewBuilder
    private var placeholderView: some View {
        if fundingResult == nil && !isFunding && accountId.isEmpty {
            Spacer()
                .frame(height: 16)

            Text("Enter a public key or generate a new keypair to fund the account with testnet XLM")
                .font(.system(size: 14))
                .foregroundStyle(Material3Colors.onSurfaceVariant)
                .multilineTextAlignment(.center)
        }
    }

    // MARK: - Actions

    private func generateAndFill() {
        isGenerating = true

        Task {
            do {
                let keypair = try await bridgeWrapper.bridge.generateKeypair()
                await MainActor.run {
                    generatedKeypair = keypair
                    accountId = keypair.getAccountId()
                    secretSeedVisible = false
                    validationError = nil
                    fundingResult = nil
                    isGenerating = false
                    toastManager.show("New keypair generated and filled")
                }
            } catch {
                await MainActor.run {
                    isGenerating = false
                    toastManager.show("Failed to generate keypair: \(error.localizedDescription)")
                }
            }
        }
    }

    private func fundAccount() {
        // Validate before funding using FormValidation utility
        if let error = FormValidation.validateAccountIdField(accountId) {
            validationError = error
            toastManager.show(error)
            return
        }

        isFunding = true
        fundingResult = nil

        Task {
            do {
                let result = try await bridgeWrapper.bridge.fundAccount(accountId: accountId)
                await MainActor.run {
                    fundingResult = result
                    isFunding = false
                }
            } catch {
                await MainActor.run {
                    fundingResult = AccountFundingResult.Error(
                        message: "Failed to fund account: \(error.localizedDescription)",
                        exception: error as? KotlinThrowable
                    )
                    isFunding = false
                }
            }
        }
    }

    private func shortenAccountId(_ id: String) -> String {
        if id.count > 12 {
            return "\(id.prefix(4))...\(id.suffix(4))"
        }
        return id
    }
}

// MARK: - Account Details Screen (matches Compose AccountDetailsScreen)

