import SwiftUI
import shared

// MARK: - Invoke Hello World Contract Screen (matches Compose InvokeHelloWorldContractScreen)

struct InvokeHelloWorldContractScreen: View {
    @ObservedObject var toastManager: ToastManager
    @State private var contractId = ""
    @State private var toParameter = ""
    @State private var submitterAccountId = ""
    @State private var secretKey = ""
    @State private var showSecret = false
    @State private var isInvoking = false
    @State private var invocationResult: InvokeHelloWorldResult?
    @State private var validationErrors: [String: String] = [:]

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                infoCard
                contractDetailsCard
                submitterAccountCard
                invokeButton
                resultView
                placeholderView
            }
            .padding(16)
        }
        .background(Material3Colors.surface)
        .navigationToolbar(title: "Invoke Hello World Contract")
    }

    // MARK: - View Components

    private var infoCard: some View {
        InfoCard(title: "ContractClient.invoke(): Beginner-friendly contract invocation", color: .secondary) {
            Text("This demo showcases the SDK's high-level contract invocation API with automatic type conversion. The invoke() method accepts Map-based arguments and handles XDR conversion, transaction building, signing, submission, and result parsing automatically.")
                .font(.system(size: 13))
                .foregroundStyle(Material3Colors.onSecondaryContainer)
        }
    }

    private var contractDetailsCard: some View {
        InfoCard(title: "Contract Details", color: .default) {
            VStack(spacing: 12) {
                StellarTextField(
                    label: "Contract ID",
                    placeholder: "C...",
                    text: $contractId,
                    error: validationErrors["contractId"],
                    helpText: "Deploy hello world contract first using 'Deploy a Smart Contract'",
                    useMonospacedFont: true
                )
                .onChange(of: contractId) { _ in
                    validationErrors.removeValue(forKey: "contractId")
                    invocationResult = nil
                }

                StellarTextField(
                    label: "Name (to parameter)",
                    placeholder: "Alice",
                    text: $toParameter,
                    error: validationErrors["toParameter"],
                    helpText: "The name to greet in the hello function"
                )
                .onChange(of: toParameter) { _ in
                    validationErrors.removeValue(forKey: "toParameter")
                    invocationResult = nil
                }
            }
        }
    }

    private var submitterAccountCard: some View {
        InfoCard(title: "Submitter Account", color: .default) {
            VStack(spacing: 12) {
                StellarTextField(
                    label: "Submitter Account ID",
                    placeholder: "G...",
                    text: $submitterAccountId,
                    error: validationErrors["submitterAccount"],
                    helpText: "Account that will sign and submit the transaction",
                    useMonospacedFont: true
                )
                .onChange(of: submitterAccountId) { _ in
                    validationErrors.removeValue(forKey: "submitterAccount")
                    invocationResult = nil
                }

                StellarTextField(
                    label: "Secret Key",
                    placeholder: "S...",
                    text: $secretKey,
                    error: validationErrors["secretKey"],
                    isSecure: true,
                    showVisibilityToggle: true,
                    isVisible: $showSecret,
                    useMonospacedFont: true
                )
                .onChange(of: secretKey) { _ in
                    validationErrors.removeValue(forKey: "secretKey")
                    invocationResult = nil
                }
            }
        }
    }

    private var invokeButton: some View {
        LoadingButton(
            action: invokeContract,
            isLoading: isInvoking,
            isEnabled: !isInvoking && isFormValid,
            icon: "play.fill",
            text: "Invoke Contract",
            loadingText: "Invoking..."
        )
    }

    private var isFormValid: Bool {
        !contractId.isEmpty &&
        !toParameter.isEmpty &&
        !submitterAccountId.isEmpty &&
        !secretKey.isEmpty
    }

    @ViewBuilder
    private var resultView: some View {
        if let result = invocationResult {
            switch result {
            case let success as InvokeHelloWorldResult.Success:
                successCard(success)
            case let error as InvokeHelloWorldResult.Error:
                errorCard(error)
                troubleshootingCard
            default:
                EmptyView()
            }
        }
    }

    private func successCard(_ success: InvokeHelloWorldResult.Success) -> some View {
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

                        Text("The hello function was successfully invoked")
                            .font(.system(size: 14))
                            .foregroundStyle(Material3Colors.onSuccessContainer)
                    }
                }
            }

            // Greeting Response Card
            InfoCard(title: "Greeting Response", color: .surfaceVariant) {
                VStack(alignment: .leading, spacing: 12) {
                    Divider()

                    Text(success.greeting)
                        .font(.system(size: 20, weight: .medium, design: .default))
                        .foregroundStyle(Material3Colors.onSurfaceVariant)
                        .padding(16)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Material3Colors.onSurfaceVariant.opacity(0.1))
                        .cornerRadius(8)
                }
            }

            // What's Next? Card
            InfoCard(title: "What's Next?", color: .secondary) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("• The contract function was successfully invoked using ContractClient.invoke()")
                        .font(.system(size: 13))
                        .foregroundStyle(Material3Colors.onSecondaryContainer)

                    Text("• Automatic type conversion from Map arguments to Soroban XDR types")
                        .font(.system(size: 13))
                        .foregroundStyle(Material3Colors.onSecondaryContainer)

                    Text("• Try invoking with different names to see various greetings")
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

    private func errorCard(_ error: InvokeHelloWorldResult.Error) -> some View {
        InfoCard(color: .error) {
            HStack(spacing: 8) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .foregroundStyle(Material3Colors.onErrorContainer)
                VStack(alignment: .leading, spacing: 8) {
                    Text("Invocation Failed")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(Material3Colors.onErrorContainer)

                    Text(error.message)
                        .font(.system(size: 14))
                        .foregroundStyle(Material3Colors.onErrorContainer)

                    if let exception = error.exception {
                        Text("Technical details: \(exception.message ?? "Unknown error")")
                            .font(.system(size: 12, design: .monospaced))
                            .foregroundStyle(Material3Colors.onErrorContainer)
                    }
                }
            }
        }
    }

    private var troubleshootingCard: some View {
        InfoCard(title: "Troubleshooting", color: .secondary) {
            VStack(alignment: .leading, spacing: 4) {
                Text("• Ensure the contract ID is correct and the contract is deployed on testnet")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Verify the submitter account has sufficient XLM balance for fees")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Check that the secret key matches the submitter account ID")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Make sure you deployed the Hello World contract first (not another contract)")
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
        if invocationResult == nil && !isInvoking && contractId.isEmpty {
            VStack(spacing: 16) {
                Image(systemName: "play.circle")
                    .font(.system(size: 64))
                    .foregroundStyle(Material3Colors.onSurfaceVariant.opacity(0.3))

                Text("Enter contract details to invoke the hello function")
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

        // Domain-specific validation for "to" parameter (stays inline)
        if toParameter.isEmpty {
            errors["toParameter"] = "Name parameter is required"
        }

        if let error = FormValidation.validateAccountIdField(submitterAccountId) {
            errors["submitterAccount"] = error
        }

        if let error = FormValidation.validateSecretSeedField(secretKey) {
            errors["secretKey"] = error
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
        validationErrors = [:]

        Task {
            do {
                // Call the Kotlin function from the shared module
                let result = try await InvokeHelloWorldContractKt.invokeHelloWorldContract(
                    contractId: contractId,
                    to: toParameter,
                    submitterAccountId: submitterAccountId,
                    secretKey: secretKey,
                )

                await MainActor.run {
                    invocationResult = result
                    isInvoking = false
                }
            } catch {
                await MainActor.run {
                    invocationResult = InvokeHelloWorldResult.Error(
                        message: "Failed to invoke contract: \(error.localizedDescription)",
                        exception: error as? KotlinThrowable
                    )
                    isInvoking = false
                }
            }
        }
    }
}
