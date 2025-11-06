import SwiftUI
import shared

// MARK: - Deploy Contract Screen (matches Compose DeployContractScreen)

struct DeployContractView: View {
    @ObservedObject var toastManager: ToastManager
    @State private var selectedContract: ContractMetadata?
    @State private var sourceAccountId = ""
    @State private var secretKey = ""
    @State private var showSecret = false
    @State private var constructorArgValues: [String: String] = [:]
    @State private var isDeploying = false
    @State private var deploymentResult: DeployContractResult?
    @State private var validationErrors: [String: String] = [:]

    @EnvironmentObject var bridgeWrapper: MacOSBridgeWrapper
    // Access the AVAILABLE_CONTRACTS list from the shared module
    private var availableContracts: [ContractMetadata] {
        DeployContractKt.AVAILABLE_CONTRACTS.compactMap { $0 as? ContractMetadata }
    }

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView {
                VStack(spacing: 16) {
                    infoCard
                    contractSelectionCard
                    sourceAccountCard

                    // Constructor parameters card (conditional)
                    if let contract = selectedContract, contract.hasConstructor {
                        constructorParamsCard(for: contract)
                    }

                    deployButton
                    resultView
                    placeholderView
                }
                .padding(16)
            }
            .background(Material3Colors.surface)
            .navigationToolbar(title: "Deploy a Smart Contract")
            .onChange(of: deploymentResult) { newValue in
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
        InfoCard(title: "ContractClient.deploy(): One-step contract deployment", color: .secondary) {
            Text("This demo showcases the SDK's high-level deployment API that handles WASM upload, contract deployment, and constructor invocation in a single call.")
                .font(.system(size: 13))
                .foregroundStyle(Material3Colors.onSecondaryContainer)
        }
    }

    private var contractSelectionCard: some View {
        InfoCard(title: "1. Select Contract", color: .default) {
            VStack(spacing: 12) {
                // Contract picker
                Menu {
                    ForEach(availableContracts.indices, id: \.self) { index in
                        let contract = availableContracts[index]
                        Button(action: {
                            selectContract(contract)
                        }) {
                            VStack(alignment: .leading) {
                                Text(contract.name)
                                Text(contract.description_)
                                    .font(.caption)
                            }
                        }
                    }
                } label: {
                    HStack {
                        Text(selectedContract?.name ?? "Select a contract")
                            .foregroundStyle(selectedContract == nil ? Material3Colors.onSurfaceVariant : Material3Colors.onSurface)
                        Spacer()
                        Image(systemName: "chevron.down")
                            .foregroundStyle(Material3Colors.onSurfaceVariant)
                    }
                    .padding(12)
                    .background(Material3Colors.surface)
                    .cornerRadius(4)
                    .overlay(
                        RoundedRectangle(cornerRadius: 4)
                            .stroke(Material3Colors.onSurfaceVariant.opacity(0.3), lineWidth: 1)
                    )
                }
                .buttonStyle(.plain)

                // Selected contract description
                if let contract = selectedContract {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(contract.description_)
                            .font(.system(size: 14))
                            .foregroundStyle(Material3Colors.onSurface)

                        if contract.hasConstructor {
                            Spacer().frame(height: 4)
                            Text("Constructor required: \(contract.constructorParams.count) parameter(s)")
                                .font(.system(size: 13, weight: .medium))
                                .foregroundStyle(Material3Colors.onSurfaceVariant)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(12)
                    .background(Material3Colors.primaryContainer.opacity(0.3))
                    .cornerRadius(8)
                }
            }
        }
    }

    private var sourceAccountCard: some View {
        InfoCard(title: "2. Source Account", color: .default) {
            VStack(spacing: 12) {
                StellarTextField(
                    label: "Source Account ID",
                    placeholder: "G...",
                    text: $sourceAccountId,
                    error: validationErrors["sourceAccount"]
                )
                .onChange(of: sourceAccountId) { _ in
                    validationErrors.removeValue(forKey: "sourceAccount")
                    deploymentResult = nil
                }

                StellarTextField(
                    label: "Secret Key",
                    placeholder: "S...",
                    text: $secretKey,
                    error: validationErrors["secretKey"],
                    isSecure: true,
                    showVisibilityToggle: true,
                    isVisible: $showSecret
                )
                .onChange(of: secretKey) { _ in
                    validationErrors.removeValue(forKey: "secretKey")
                    deploymentResult = nil
                }
            }
        }
    }

    private func constructorParamsCard(for contract: ContractMetadata) -> some View {
        InfoCard(title: "3. Constructor Parameters", color: .default) {
            VStack(spacing: 12) {
                ForEach(0..<contract.constructorParams.count, id: \.self) { index in
                    if let param = contract.constructorParams[index] as? ConstructorParam {
                        constructorParamField(param: param)
                    }
                }
            }
        }
    }

    private func constructorParamField(param: ConstructorParam) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            StellarTextField(
                label: param.name,
                placeholder: param.placeholder,
                text: Binding(
                    get: { constructorArgValues[param.name] ?? "" },
                    set: { newValue in
                        constructorArgValues[param.name] = newValue
                        validationErrors.removeValue(forKey: "constructor_\(param.name)")
                        deploymentResult = nil
                    }
                ),
                error: validationErrors["constructor_\(param.name)"],
                helpText: validationErrors["constructor_\(param.name)"] == nil ? param.description_ : nil
            )
        }
    }

    private var deployButton: some View {
        LoadingButton(
            action: deployContract,
            isLoading: isDeploying,
            isEnabled: isFormValid && !isDeploying,
            icon: "arrow.up.doc.fill",
            text: "Deploy Contract",
            loadingText: "Deploying..."
        )
    }

    @ViewBuilder
    private var resultView: some View {
        if let result = deploymentResult {
            switch result {
            case let success as DeployContractResult.Success:
                successCard(success)
                    .id("resultCard")
            case let error as DeployContractResult.Error:
                errorCard(error)
                    .id("resultCard")
                troubleshootingCard
            default:
                EmptyView()
            }
        }
    }

    private func successCard(_ success: DeployContractResult.Success) -> some View {
        VStack(spacing: 16) {
            // Success header card
            InfoCard(color: .success) {
                HStack(spacing: 8) {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: 24))
                        .foregroundStyle(Material3Colors.onSuccessContainer)

                    VStack(alignment: .leading, spacing: 4) {
                        Text("Deployment Successful")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(Material3Colors.onSuccessContainer)

                        Text("Your smart contract has been deployed to the testnet")
                            .font(.system(size: 14))
                            .foregroundStyle(Material3Colors.onSuccessContainer)
                    }
                }
            }

            // Contract Details Card
            InfoCard(title: "Contract Details", color: .surfaceVariant) {
                VStack(alignment: .leading, spacing: 12) {
                    Divider()

                    DeployContractCopyableRow(label: "Contract ID", value: success.contractId)

                    if let wasmId = success.wasmId {
                        DeployContractCopyableRow(label: "WASM ID", value: wasmId)
                    }
                }
            }

            // What's Next? Card
            InfoCard(title: "What's Next?", color: .secondary) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("• You can now use this contract ID to interact with your deployed contract")
                        .font(.system(size: 13))
                        .foregroundStyle(Material3Colors.onSecondaryContainer)

                    Text("• Use ContractClient.fromNetwork() to create a client instance")
                        .font(.system(size: 13))
                        .foregroundStyle(Material3Colors.onSecondaryContainer)

                    Text("• Try the 'Invoke Hello World Contract' or 'Invoke Auth Contract' demos")
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

    private func errorCard(_ error: DeployContractResult.Error) -> some View {
        InfoCard(title: "Deployment Failed", color: .error) {
            VStack(alignment: .leading, spacing: 8) {
                Text(error.message)
                    .font(.system(size: 14))
                    .foregroundStyle(Material3Colors.onErrorContainer)

                if let exception = error.exception {
                    Text("Technical details: \(exception.message ?? "Unknown error")")
                        .font(.system(size: 13, design: .monospaced))
                        .foregroundStyle(Material3Colors.onErrorContainer)
                }
            }
        }
    }

    private var troubleshootingCard: some View {
        InfoCard(title: "Troubleshooting", color: .secondary) {
            VStack(alignment: .leading, spacing: 4) {
                Text("• Verify the source account has sufficient XLM balance (at least 100 XLM recommended)")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Ensure the source account exists on testnet (use 'Fund Testnet Account' first)")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Check that the secret key matches the source account ID")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onSecondaryContainer)

                Text("• Verify constructor arguments match the expected types")
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
        if selectedContract == nil && deploymentResult == nil && !isDeploying {
            Spacer()
                .frame(height: 16)

            Image(systemName: "arrow.up.doc")
                .font(.system(size: 64))
                .foregroundStyle(Material3Colors.onSurfaceVariant.opacity(0.3))

            Text("Select a demo contract to begin deployment")
                .font(.system(size: 14))
                .foregroundStyle(Material3Colors.onSurfaceVariant)
                .multilineTextAlignment(.center)
        }
    }

    // MARK: - Computed Properties

    private var isFormValid: Bool {
        guard let contract = selectedContract else { return false }

        if sourceAccountId.isEmpty || secretKey.isEmpty {
            return false
        }

        if contract.hasConstructor {
            for i in 0..<contract.constructorParams.count {
                if let param = contract.constructorParams[i] as? ConstructorParam {
                    if (constructorArgValues[param.name] ?? "").isEmpty {
                        return false
                    }
                }
            }
        }

        return true
    }

    // MARK: - Actions

    private func selectContract(_ contract: ContractMetadata) {
        selectedContract = contract

        // Reset constructor args when changing contract
        constructorArgValues = [:]
        if contract.hasConstructor {
            for i in 0..<contract.constructorParams.count {
                if let param = contract.constructorParams[i] as? ConstructorParam {
                    constructorArgValues[param.name] = ""
                }
            }
        }
    }

    private func deployContract() {
        let errors = validateInputs()
        if !errors.isEmpty {
            validationErrors = errors
            return
        }

        guard let contract = selectedContract else {
            toastManager.show("Please select a contract to deploy")
            return
        }

        isDeploying = true
        deploymentResult = nil
        validationErrors = [:]

        Task {
            do {
                // Build constructor arguments map with proper types
                var constructorArgs: [String: Any] = [:]
                if contract.hasConstructor {
                    for i in 0..<contract.constructorParams.count {
                        if let param = contract.constructorParams[i] as? ConstructorParam {
                            let value = constructorArgValues[param.name] ?? ""

                            // Convert based on type
                            switch param.type {
                            case .address, .string:
                                constructorArgs[param.name] = value
                            case .u32:
                                if let intValue = Int32(value) {
                                    constructorArgs[param.name] = intValue
                                }
                            default:
                                constructorArgs[param.name] = value
                            }
                        }
                    }
                }

                // Call the shared business logic
                let result = try await bridgeWrapper.bridge.deployContract(
                    contractMetadata: contract,
                    constructorArgs: constructorArgs,
                    sourceAccountId: sourceAccountId,
                    secretKey: secretKey,
                )

                await MainActor.run {
                    deploymentResult = result
                    isDeploying = false
                }
            } catch {
                await MainActor.run {
                    deploymentResult = DeployContractResult.Error(
                        message: "Failed to deploy contract: \(error.localizedDescription)",
                        exception: error as? KotlinThrowable
                    )
                    isDeploying = false
                }
            }
        }
    }

    private func validateInputs() -> [String: String] {
        var errors: [String: String] = [:]

        if let error = FormValidation.validateAccountIdField(sourceAccountId) {
            errors["sourceAccount"] = error
        }

        if let error = FormValidation.validateSecretSeedField(secretKey) {
            errors["secretKey"] = error
        }

        // Validate constructor arguments
        if let contract = selectedContract, contract.hasConstructor {
            for i in 0..<contract.constructorParams.count {
                if let param = contract.constructorParams[i] as? ConstructorParam {
                    let value = constructorArgValues[param.name] ?? ""

                    if value.isEmpty {
                        errors["constructor_\(param.name)"] = "\(param.name) is required"
                    } else {
                        // Type-specific validation
                        switch param.type {
                        case .address:
                            if let error = FormValidation.validateAccountIdField(value) {
                                errors["constructor_\(param.name)"] = error
                            }
                        case .u32:
                            if Int32(value) == nil {
                                errors["constructor_\(param.name)"] = "Must be a valid number"
                            }
                        case .string:
                            break // No additional validation for strings
                        default:
                            break
                        }
                    }
                }
            }
        }

        return errors
    }

}

// MARK: - Deploy Contract Copyable Row

struct DeployContractCopyableRow: View {
    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.system(size: 12, weight: .medium))
                .foregroundStyle(Material3Colors.onSurfaceVariant.opacity(0.7))

            HStack(alignment: .top) {
                Text(value)
                    .font(.system(.body, design: .monospaced))
                    .foregroundStyle(Material3Colors.onSurfaceVariant)
                    .textSelection(.enabled)
                    .lineLimit(nil)
                    .fixedSize(horizontal: false, vertical: true)

                Spacer()

                Button(action: {
                    ClipboardHelper.copy(value)
                }) {
                    Image(systemName: "doc.on.doc")
                        .font(.system(size: 10))
                        .foregroundStyle(Material3Colors.primary)
                }
                .buttonStyle(.plain)
                .help("Copy to clipboard")
            }
        }
    }
}
