import SwiftUI
import shared

// MARK: - Invoke Auth Contract Screen (matches Compose InvokeAuthContractScreen)

struct InvokeAuthContractScreen: View {
    @ObservedObject var toastManager: ToastManager
    @State private var contractId = ""
    @State private var userAccountId = ""
    @State private var userSecretKey = ""
    @State private var showUserSecret = false
    @State private var useSameAccount = true
    @State private var sourceAccountId = ""
    @State private var sourceSecretKey = ""
    @State private var showSourceSecret = false
    @State private var value = "1"
    @State private var isInvoking = false
    @State private var invocationResult: InvokeAuthContractResult?
    @State private var errorMessage: String?
    @State private var validationErrors: [String: String] = [:]

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                VStack(spacing: 16) {
                    infoCard
                    contractDetailsCard
                    userAccountCard
                    sameAccountToggle

                    // Source account card (conditional, shown when toggle is OFF)
                    if !useSameAccount {
                        sourceAccountCard
                    }

                    valueInputCard
                    invokeButton
                    resultView
                    placeholderView
                }
                .padding(16)
            }
            .background(Material3Colors.surface)
            .navigationToolbar(title: "Invoke Auth Contract")
            .onChange(of: invocationResult) { newValue in
                if newValue != nil {
                    withAnimation {
                        proxy.scrollTo("resultCard", anchor: .bottom)
                    }
                }
            }
            .onChange(of: errorMessage) { newValue in
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
        InfoCard(title: "Deploy the auth contract first!", color: .secondary) {
            Text("This demo showcases dynamic authorization handling with needsNonInvokerSigningBy(). The SDK automatically detects whether same-invoker (automatic authorization) or different-invoker (manual authorization) pattern applies, and conditionally calls signAuthEntries() only when needed.")
                .font(.system(size: 13))
                .foregroundStyle(Material3Colors.onSecondaryContainer)
        }
    }

    private var contractDetailsCard: some View {
        InfoCard(title: "Contract Details", color: .default) {
            StellarTextField(
                label: "Contract ID",
                placeholder: "C...",
                text: $contractId,
                error: validationErrors["contractId"],
                helpText: "Deploy the auth contract first using 'Deploy Smart Contract'",
                useMonospacedFont: true
            )
            .onChange(of: contractId) { _ in
                validationErrors.removeValue(forKey: "contractId")
                invocationResult = nil
                errorMessage = nil
            }
        }
    }

    private var userAccountCard: some View {
        InfoCard(title: "User Account", color: .default) {
            VStack(spacing: 12) {
                StellarTextField(
                    label: "User Account ID",
                    placeholder: "G...",
                    text: $userAccountId,
                    error: validationErrors["userAccount"],
                    helpText: "The account that owns the counter (will be incremented)",
                    useMonospacedFont: true
                )
                .onChange(of: userAccountId) { _ in
                    validationErrors.removeValue(forKey: "userAccount")
                    invocationResult = nil
                    errorMessage = nil
                }

                StellarTextField(
                    label: "User Secret Key",
                    placeholder: "S...",
                    text: $userSecretKey,
                    error: validationErrors["userSecretKey"],
                    helpText: "Used to authorize the increment operation",
                    isSecure: true,
                    showVisibilityToggle: true,
                    isVisible: $showUserSecret,
                    useMonospacedFont: true
                )
                .onChange(of: userSecretKey) { _ in
                    validationErrors.removeValue(forKey: "userSecretKey")
                    invocationResult = nil
                    errorMessage = nil
                }
            }
        }
    }

    private var sameAccountToggle: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Use Same Account")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundStyle(Material3Colors.onSurface)

                    Text(useSameAccount ?
                         "Same-invoker: User submits their own transaction (automatic authorization)" :
                         "Different-invoker: Source account submits on behalf of user (manual authorization required)")
                        .font(.system(size: 13))
                        .foregroundStyle(Material3Colors.onSurfaceVariant)
                }

                Spacer()

                Toggle("", isOn: $useSameAccount)
                    .labelsHidden()
                    .toggleStyle(.switch)
                    .tint(Material3Colors.primary)
                    .onChange(of: useSameAccount) { newValue in
                        if newValue {
                            // When switching to same account, clear source fields
                            sourceAccountId = ""
                            sourceSecretKey = ""
                            validationErrors.removeValue(forKey: "sourceAccount")
                            validationErrors.removeValue(forKey: "sourceSecretKey")
                        }
                        invocationResult = nil
                        errorMessage = nil
                    }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Material3Colors.primaryContainer.opacity(0.3))
        .cornerRadius(12)
    }

    private var sourceAccountCard: some View {
        InfoCard(title: "Source Account (Transaction Submitter)", color: .default) {
            VStack(spacing: 12) {
                StellarTextField(
                    label: "Source Account ID",
                    placeholder: "G...",
                    text: $sourceAccountId,
                    error: validationErrors["sourceAccount"],
                    helpText: "Different account that will submit the transaction",
                    useMonospacedFont: true
                )
                .onChange(of: sourceAccountId) { _ in
                    validationErrors.removeValue(forKey: "sourceAccount")
                    invocationResult = nil
                    errorMessage = nil
                }

                StellarTextField(
                    label: "Source Secret Key",
                    placeholder: "S...",
                    text: $sourceSecretKey,
                    error: validationErrors["sourceSecretKey"],
                    helpText: "Used to sign and submit the transaction",
                    isSecure: true,
                    showVisibilityToggle: true,
                    isVisible: $showSourceSecret,
                    useMonospacedFont: true
                )
                .onChange(of: sourceSecretKey) { _ in
                    validationErrors.removeValue(forKey: "sourceSecretKey")
                    invocationResult = nil
                    errorMessage = nil
                }
            }
        }
    }

    private var valueInputCard: some View {
        InfoCard(title: "Increment Amount", color: .default) {
            StellarTextField(
                label: "Value",
                placeholder: "1",
                text: $value,
                error: validationErrors["value"],
                helpText: "Amount to increment the counter (positive integer)",
                useMonospacedFont: true
            )
            .onChange(of: value) { _ in
                validationErrors.removeValue(forKey: "value")
                invocationResult = nil
                errorMessage = nil
            }
        }
    }

    private var invokeButton: some View {
        LoadingButton(
            action: invokeContract,
            isLoading: isInvoking,
            isEnabled: !isInvoking && isFormValid,
            icon: "checkmark.shield.fill",
            text: "Invoke Contract",
            loadingText: "Invoking..."
        )
    }

    private var isFormValid: Bool {
        if contractId.isEmpty || userAccountId.isEmpty || userSecretKey.isEmpty || value.isEmpty {
            return false
        }

        if !useSameAccount {
            if sourceAccountId.isEmpty || sourceSecretKey.isEmpty {
                return false
            }
        }

        return true
    }

    @ViewBuilder
    private var resultView: some View {
        if let result = invocationResult {
            VStack(spacing: 16) {
                if let success = result as? InvokeAuthContractResult.Success {
                    successCard(success)
                } else if let failure = result as? InvokeAuthContractResult.Failure {
                    errorCard(failure.message)
                    troubleshootingCard
                }
            }
            .id("resultCard")
        } else if let error = errorMessage {
            VStack(spacing: 16) {
                errorCard(error)
                troubleshootingCard
            }
            .id("resultCard")
        }
    }

    private func successCard(_ result: InvokeAuthContractResult.Success) -> some View {
        VStack(spacing: 16) {
            // Success header card
            InfoCard(color: .success) {
                HStack(spacing: 8) {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 24))
                        .foregroundStyle(Material3Colors.onSuccessContainer)

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Contract Invocation Successful")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(Material3Colors.onSuccessContainer)

                        Text("The auth contract was successfully invoked")
                            .font(.system(size: 14))
                            .foregroundStyle(Material3Colors.onSuccessContainer)
                    }
                }
            }

            // Transaction Hash Card (Prominent)
            InfoCard(title: "Transaction Hash", color: .primary) {
                VStack(alignment: .leading, spacing: 12) {
                    Divider()

                    HStack {
                        Text("Hash: ")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundStyle(Material3Colors.onPrimaryContainer)
                        Text(result.transactionHash)
                            .font(.system(size: 11, design: .monospaced))
                            .foregroundStyle(Material3Colors.onPrimaryContainer)
                            .lineLimit(1)
                        Button(action: {
                            ClipboardHelper.copy(result.transactionHash)
                        }) {
                            Image(systemName: "doc.on.doc")
                                .font(.system(size: 12))
                                .foregroundStyle(Material3Colors.onPrimaryContainer)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }

            // Counter Value Card
            InfoCard(title: "Counter Value", color: .surfaceVariant) {
                VStack(alignment: .leading, spacing: 12) {
                    Divider()

                    Text("\(result.counterValue)")
                        .font(.system(size: 48, weight: .bold, design: .default))
                        .foregroundStyle(Material3Colors.onSurfaceVariant)
                        .padding(16)
                        .frame(maxWidth: .infinity, alignment: .center)
                        .background(Material3Colors.onSurfaceVariant.opacity(0.1))
                        .cornerRadius(8)
                }
            }

            // Authorization Details Card
            InfoCard(title: "Authorization Details", color: .surfaceVariant) {
                VStack(alignment: .leading, spacing: 12) {
                    Divider()

                    // Detected scenario
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Detected Scenario")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundStyle(Material3Colors.onSurfaceVariant.opacity(0.7))

                        Text(result.scenario)
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundStyle(Material3Colors.onSurfaceVariant)
                            .padding(12)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Material3Colors.onSurfaceVariant.opacity(0.1))
                            .cornerRadius(8)
                    }

                    // Who needed to sign
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Who Needed to Sign")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundStyle(Material3Colors.onSurfaceVariant.opacity(0.7))

                        if result.whoNeedsToSign.isEmpty {
                            Text("None (automatic authorization)")
                                .font(.system(size: 14))
                                .foregroundStyle(Material3Colors.onSurfaceVariant)
                                .padding(12)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(Material3Colors.onSurfaceVariant.opacity(0.1))
                                .cornerRadius(8)
                        } else {
                            VStack(alignment: .leading, spacing: 4) {
                                ForEach(Array(result.whoNeedsToSign), id: \.self) { accountId in
                                    Text("• \(accountId)")
                                        .font(.system(size: 13, design: .monospaced))
                                        .foregroundStyle(Material3Colors.onSurfaceVariant)
                                }
                            }
                            .padding(12)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Material3Colors.onSurfaceVariant.opacity(0.1))
                            .cornerRadius(8)
                        }
                    }
                }
            }

            // What's Next? Card
            InfoCard(title: "What's Next?", color: .secondary) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("• The SDK dynamically detected the authorization pattern")
                        .font(.system(size: 13))
                        .foregroundStyle(Material3Colors.onSecondaryContainer)

                    Text("• Handled signing appropriately using needsNonInvokerSigningBy()")
                        .font(.system(size: 13))
                        .foregroundStyle(Material3Colors.onSecondaryContainer)

                    Text("• Conditional signAuthEntries() was applied only when needed")
                        .font(.system(size: 13))
                        .foregroundStyle(Material3Colors.onSecondaryContainer)

                    Text("• View the transaction on Stellar Expert or other block explorers")
                        .font(.system(size: 13))
                        .foregroundStyle(Material3Colors.onSecondaryContainer)
                }
                .padding(.leading, 8)
            }
        }
    }

    private func errorCard(_ error: String) -> some View {
        InfoCard(color: .error) {
            HStack(spacing: 8) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .foregroundStyle(Material3Colors.onErrorContainer)
                VStack(alignment: .leading, spacing: 8) {
                    Text("Invocation Failed")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(Material3Colors.onErrorContainer)

                    Text(error)
                        .font(.system(size: 14))
                        .foregroundStyle(Material3Colors.onErrorContainer)
                }
            }
        }
    }

    private var troubleshootingCard: some View {
        InfoCard(title: "Troubleshooting", color: .secondary) {
            VStack(alignment: .leading, spacing: 4) {
                Text("• Ensure the contract ID is correct and the auth contract is deployed on testnet")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Verify all accounts have sufficient XLM balance for fees")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Check that secret keys match their corresponding account IDs")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Make sure you deployed the auth contract (not another contract)")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• For different-invoker scenario, both user and source accounts must be funded")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Check your internet connection and Soroban RPC availability")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)
            }
            .padding(.leading, 8)
        }
    }

    @ViewBuilder
    private var placeholderView: some View {
        if invocationResult == nil && errorMessage == nil && !isInvoking && contractId.isEmpty {
            VStack(spacing: 16) {
                Image(systemName: "checkmark.shield")
                    .font(.system(size: 64))
                    .foregroundStyle(Material3Colors.onSurfaceVariant.opacity(0.3))

                Text("Enter contract details to invoke with dynamic authorization")
                    .font(.system(size: 14))
                    .foregroundStyle(Material3Colors.onSurfaceVariant)
                    .multilineTextAlignment(.center)
            }
            .padding(.vertical, 32)
        }
    }

    // MARK: - Actions

    private func validateInputs() -> [String: String] {
        var errors: [String: String] = [:]

        // Use FormValidation for Stellar protocol fields
        if let error = FormValidation.validateContractIdField(contractId) {
            errors["contractId"] = error
        }

        if let error = FormValidation.validateAccountIdField(userAccountId) {
            errors["userAccount"] = error
        }

        if let error = FormValidation.validateSecretSeedField(userSecretKey) {
            errors["userSecretKey"] = error
        }

        // Validate source account (if different-invoker scenario)
        if !useSameAccount {
            if let error = FormValidation.validateAccountIdField(sourceAccountId) {
                errors["sourceAccount"] = error
            }

            if let error = FormValidation.validateSecretSeedField(sourceSecretKey) {
                errors["sourceSecretKey"] = error
            }
        }

        // Domain-specific validation for value (stays inline)
        if value.isEmpty {
            errors["value"] = "Value is required"
        } else if let intValue = Int(value), intValue < 0 {
            errors["value"] = "Value must be non-negative"
        } else if Int(value) == nil {
            errors["value"] = "Value must be a valid integer"
        }

        return errors
    }

    private func invokeContract() {
        let errors = validateInputs()
        if !errors.isEmpty {
            validationErrors = errors
            return
        }

        isInvoking = true
        invocationResult = nil
        errorMessage = nil
        validationErrors = [:]

        Task {
            do {
                // Determine source account and keypair based on toggle
                let finalSourceAccountId: String
                let finalSourceSecretKey: String

                if useSameAccount {
                    // Same-invoker scenario: user is the source
                    finalSourceAccountId = userAccountId
                    finalSourceSecretKey = userSecretKey
                } else {
                    // Different-invoker scenario: separate source account
                    finalSourceAccountId = sourceAccountId
                    finalSourceSecretKey = sourceSecretKey
                }

                // Create keypairs
                let userKeyPair = try await KeyPair.companion.fromSecretSeed(seed: userSecretKey)
                let sourceKeyPair = try await KeyPair.companion.fromSecretSeed(seed: finalSourceSecretKey)

                // Call the Kotlin function from the shared module
                let result = try await InvokeAuthContractKt.invokeAuthContract(
                    contractId: contractId,
                    userAccountId: userAccountId,
                    userKeyPair: userKeyPair,
                    sourceAccountId: finalSourceAccountId,
                    sourceKeyPair: sourceKeyPair,
                    value: Int32(value) ?? 1
                )

                // Update UI on main thread
                await MainActor.run {
                    if let success = result as? InvokeAuthContractResult.Success {
                        invocationResult = success
                        errorMessage = nil
                    } else if let failure = result as? InvokeAuthContractResult.Failure {
                        errorMessage = failure.message
                        invocationResult = nil
                    }
                    isInvoking = false
                }
            } catch let error as NSError {
                // Handle errors on main thread
                await MainActor.run {
                    invocationResult = nil
                    // Extract error message from Kotlin exception
                    errorMessage = error.localizedDescription
                    isInvoking = false
                }
            }
        }
    }
}
