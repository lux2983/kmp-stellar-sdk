import SwiftUI
import shared

struct InvokeTokenContractScreen: View {
    @ObservedObject var toastManager: ToastManager
    @State private var contractId = ""
    @State private var isLoadingContract = false
    @State private var loadedContract: InvokeTokenResult.ContractLoaded?
    @State private var loadError: String?
    @State private var validationErrors: [String: String] = [:]

    @State private var selectedFunction: ContractFunctionInfo?
    @State private var functionArguments: [String: String] = [:]
    @State private var sourceAccountId = ""
    @State private var signerSeeds: [String] = [""]
    @State private var isInvoking = false
    @State private var invocationResult: InvokeTokenResult?

    @EnvironmentObject var bridgeWrapper: MacOSBridgeWrapper

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                infoCard
                contractLoadingCard

                if let error = loadError {
                    errorCard(message: error)
                }

                if let contract = loadedContract {
                    contractInfoCard(contract: contract)
                    functionSelectionCard(contract: contract)

                    if let function = selectedFunction {
                        if !function.isReadOnly {
                            expectedSignersWarning(function: function)
                            sourceAccountAndSignersCard
                        }
                        invokeButton(function: function)
                    }
                }

                if let result = invocationResult {
                    resultCard(result: result)
                }

                placeholderView
            }
            .padding(16)
        }
        .background(Material3Colors.surface)
        .navigationToolbar(title: "Invoke Token Contract")
    }

    // MARK: - View Components

    private var infoCard: some View {
        InfoCard(title: "Token Contract Interaction", color: .secondary) {
            Text("Load a Stellar token contract (SEP-41 compliant) and interact with its functions. The demo validates the token interface, parses function signatures, and dynamically generates UI for parameter input.")
                .font(.system(size: 13))
                .foregroundStyle(Material3Colors.onSecondaryContainer)
        }
    }

    private var contractLoadingCard: some View {
        InfoCard(title: "Step 1: Load Contract", color: .primary) {
            VStack(alignment: .leading, spacing: 12) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Contract ID")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(Material3Colors.onSurfaceVariant)

                    TextField("C...", text: $contractId)
                        .textFieldStyle(.plain)
                        .font(.system(.body, design: .monospaced))
                        .padding(12)
                        .background(Color.white)
                        .overlay(
                            RoundedRectangle(cornerRadius: 4)
                                .stroke(validationErrors["contractId"] != nil ? Material3Colors.onErrorContainer : Material3Colors.onSurfaceVariant.opacity(0.3), lineWidth: 1)
                        )
                        .onChange(of: contractId) { _ in
                            validationErrors.removeValue(forKey: "contractId")
                            loadError = nil
                        }

                    if let error = validationErrors["contractId"] {
                        Text(error)
                            .font(.system(size: 12))
                            .foregroundStyle(Material3Colors.onErrorContainer)
                    }
                }

                LoadingButton(
                    action: loadContract,
                    isLoading: isLoadingContract,
                    isEnabled: !isLoadingContract && !contractId.isEmpty,
                    icon: "arrow.down.doc.fill",
                    text: "Load Contract",
                    loadingText: "Loading..."
                )
            }
        }
    }

    private func contractInfoCard(contract: InvokeTokenResult.ContractLoaded) -> some View {
        InfoCard(title: "Token Contract Loaded", color: .tertiary) {
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Name")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundStyle(Material3Colors.primary.opacity(0.7))
                        Text(contract.contractName)
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(Material3Colors.primary)
                    }

                    Spacer()

                    VStack(alignment: .trailing, spacing: 4) {
                        Text("Symbol")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundStyle(Material3Colors.primary.opacity(0.7))
                        Text(contract.contractSymbol)
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundStyle(Material3Colors.primary)
                    }
                }

                Divider()

                HStack {
                    if contract.currentLedger > 0 {
                        HStack(spacing: 4) {
                            Text("Current Ledger: \(contract.currentLedger)")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundStyle(Material3Colors.primary.opacity(0.8))

                            Button(action: {
                                ClipboardHelper.copy(String(contract.currentLedger))
                                toastManager.show("Current ledger copied to clipboard")
                            }) {
                                Image(systemName: "doc.on.doc")
                                    .font(.system(size: 12))
                                    .foregroundStyle(Material3Colors.primary)
                            }
                            .buttonStyle(.plain)
                        }
                    }

                    Spacer()

                    Text("\(contract.functions.count) functions")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundStyle(Material3Colors.primary.opacity(0.8))
                }
            }
        }
    }

    private func functionSelectionCard(contract: InvokeTokenResult.ContractLoaded) -> some View {
        InfoCard(title: "Step 2: Select Function", color: .primary) {
            VStack(alignment: .leading, spacing: 12) {
                Picker("Function", selection: $selectedFunction) {
                    Text("Select a function...").tag(nil as ContractFunctionInfo?)
                    ForEach(contract.functions, id: \.name) { function in
                        Text("\(function.name) \(function.isReadOnly ? "(read)" : "(write)")")
                            .tag(function as ContractFunctionInfo?)
                    }
                }
                .pickerStyle(.menu)
                .padding(8)
                .background(Color.white)
                .cornerRadius(8)
                .onChange(of: selectedFunction) { newFunction in
                    if let function = newFunction {
                        functionArguments = Dictionary(uniqueKeysWithValues: function.parameters.map { ($0.name, "") })
                        invocationResult = nil
                        sourceAccountId = ""
                        signerSeeds = [""]
                    }
                }

                if let function = selectedFunction {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(function.description_)
                            .font(.system(size: 12))
                            .foregroundStyle(Material3Colors.onPrimaryContainer)

                        if !function.parameters.isEmpty {
                            Text("Parameters:")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundStyle(Material3Colors.onPrimaryContainer)

                            ForEach(function.parameters, id: \.name) { param in
                                parameterField(param: param)
                            }
                        }
                    }
                }
            }
        }
    }

    private func parameterField(param: ParameterInfo) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("\(param.name) (\(param.typeName))")
                .font(.system(size: 11, weight: .medium))
                .foregroundStyle(Material3Colors.onSurfaceVariant)

            TextField("Enter \(param.name)", text: Binding(
                get: { functionArguments[param.name] ?? "" },
                set: { functionArguments[param.name] = $0 }
            ))
            .textFieldStyle(.plain)
            .font(.system(.body, design: .monospaced))
            .padding(8)
            .background(Color.white)
            .overlay(
                RoundedRectangle(cornerRadius: 4)
                    .stroke(validationErrors["arg_\(param.name)"] != nil ? Material3Colors.onErrorContainer : Material3Colors.onSurfaceVariant.opacity(0.3), lineWidth: 1)
            )

            if let error = validationErrors["arg_\(param.name)"] {
                Text(error)
                    .font(.system(size: 11))
                    .foregroundStyle(Material3Colors.onErrorContainer)
            }
        }
    }

    private func expectedSignersWarning(function: ContractFunctionInfo) -> some View {
        let expectedSigners = getExpectedSigners(functionName: function.name, args: functionArguments)

        return Group {
            if !expectedSigners.isEmpty {
                InfoCard(title: "Expected Signers", color: .warning) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("This function typically requires signatures from:")
                            .font(.system(size: 12))
                            .foregroundStyle(Material3Colors.onSecondaryContainer)

                        ForEach(expectedSigners, id: \.0) { paramName, accountId in
                            HStack {
                                Text("• \(paramName):")
                                    .font(.system(size: 11, weight: .semibold))
                                    .foregroundStyle(Material3Colors.onSecondaryContainer)
                                Text(accountId ?? "Not provided")
                                    .font(.system(size: 11, design: .monospaced))
                                    .foregroundStyle(Material3Colors.onSecondaryContainer)
                            }
                        }
                    }
                }
            }
        }
    }

    private var sourceAccountAndSignersCard: some View {
        InfoCard(title: "Step 3: Source Account & Signers", color: .primary) {
            VStack(alignment: .leading, spacing: 12) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Source Account ID")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(Material3Colors.onSurfaceVariant)

                    TextField("G...", text: $sourceAccountId)
                        .textFieldStyle(.plain)
                        .font(.system(.body, design: .monospaced))
                        .padding(12)
                        .background(Color.white)
                        .overlay(
                            RoundedRectangle(cornerRadius: 4)
                                .stroke(validationErrors["sourceAccount"] != nil ? Material3Colors.onErrorContainer : Material3Colors.onSurfaceVariant.opacity(0.3), lineWidth: 1)
                        )

                    if let error = validationErrors["sourceAccount"] {
                        Text(error)
                            .font(.system(size: 12))
                            .foregroundStyle(Material3Colors.onErrorContainer)
                    }
                }

                Text("Secret Seeds (Signers)")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundStyle(Material3Colors.onSurfaceVariant)

                ForEach(0..<signerSeeds.count, id: \.self) { index in
                    signerSeedField(index: index)
                }

                HStack {
                    Button(action: {
                        signerSeeds.append("")
                    }) {
                        HStack(spacing: 4) {
                            Image(systemName: "plus.circle")
                            Text("Add Signer")
                        }
                        .font(.system(size: 12))
                    }
                    .buttonStyle(.plain)

                    if signerSeeds.count > 1 {
                        Button(action: {
                            if !signerSeeds.isEmpty {
                                signerSeeds.removeLast()
                            }
                        }) {
                            HStack(spacing: 4) {
                                Image(systemName: "minus.circle")
                                Text("Remove Signer")
                            }
                            .font(.system(size: 12))
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }

    private func signerSeedField(index: Int) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Signer \(index + 1)")
                .font(.system(size: 11, weight: .medium))
                .foregroundStyle(Material3Colors.onSurfaceVariant)

            SecureField("S...", text: Binding(
                get: { signerSeeds[index] },
                set: { signerSeeds[index] = $0 }
            ))
            .textFieldStyle(.plain)
            .font(.system(.body, design: .monospaced))
            .padding(8)
            .background(Color.white)
            .overlay(
                RoundedRectangle(cornerRadius: 4)
                    .stroke(validationErrors["secretSeed_\(index)"] != nil ? Material3Colors.onErrorContainer : Material3Colors.onSurfaceVariant.opacity(0.3), lineWidth: 1)
            )

            if let error = validationErrors["secretSeed_\(index)"] {
                Text(error)
                    .font(.system(size: 11))
                    .foregroundStyle(Material3Colors.onErrorContainer)
            }
        }
    }

    private func invokeButton(function: ContractFunctionInfo) -> some View {
        LoadingButton(
            action: invokeFunction,
            isLoading: isInvoking,
            isEnabled: !isInvoking && (function.isReadOnly ? true : !sourceAccountId.isEmpty && !signerSeeds.allSatisfy { $0.isEmpty }),
            icon: "play.circle.fill",
            text: function.isReadOnly ? "Simulate Read Call" : "Invoke Function",
            loadingText: function.isReadOnly ? "Simulating..." : "Invoking...",
            backgroundColor: function.isReadOnly ? Material3Colors.onSuccessContainer : nil
        )
    }

    private func resultCard(result: InvokeTokenResult) -> some View {
        Group {
            if let success = result as? InvokeTokenResult.InvocationSuccess {
                successCard(success: success)
            } else if let needsSigners = result as? InvokeTokenResult.NeedsAdditionalSigners {
                needsSignersCard(result: needsSigners)
            } else if let error = result as? InvokeTokenResult.Error {
                errorCard(message: error.message)
            }
        }
    }

    private func successCard(success: InvokeTokenResult.InvocationSuccess) -> some View {
        InfoCard(title: "Function: \(success.functionName)", color: .success) {
            VStack(alignment: .leading, spacing: 12) {
                Text("Result")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundStyle(Material3Colors.onSuccessContainer.opacity(0.7))

                Text(success.result == "null" ? "ok" : success.result)
                    .font(.system(size: 13, design: .monospaced))
                    .foregroundStyle(Material3Colors.onSuccessContainer)
                    .padding(12)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.white)
                    .cornerRadius(8)

                Divider()

                Text(success.isReadOnly ?
                     "Read-only call completed successfully. No transaction was submitted." :
                        "Write call completed successfully. Transaction was submitted and confirmed.")
                    .font(.system(size: 12))
                    .foregroundStyle(Material3Colors.onSuccessContainer.opacity(0.8))

                if let hash = success.transactionHash, !success.isReadOnly {
                    Divider()

                    Text("Transaction Hash")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundStyle(Material3Colors.onSuccessContainer.opacity(0.7))

                    HStack {
                        Text(hash)
                            .font(.system(size: 11, design: .monospaced))
                            .foregroundStyle(Material3Colors.onSuccessContainer.opacity(0.8))
                            .lineLimit(1)

                        Button(action: {
                            ClipboardHelper.copy(hash)
                            toastManager.show("Transaction hash copied to clipboard")
                        }) {
                            Image(systemName: "doc.on.doc")
                                .font(.system(size: 12))
                                .foregroundStyle(Material3Colors.onSuccessContainer)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }

    private func needsSignersCard(result: InvokeTokenResult.NeedsAdditionalSigners) -> some View {
        InfoCard(title: "Additional Signatures Required", color: .error) {
            VStack(alignment: .leading, spacing: 12) {
                Text(result.message)
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onErrorContainer)

                Text("The following accounts must sign:")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundStyle(Material3Colors.onErrorContainer)

                ForEach(result.requiredSigners, id: \.self) { signer in
                    Text("• \(signer)")
                        .font(.system(size: 11, design: .monospaced))
                        .foregroundStyle(Material3Colors.onErrorContainer)
                        .padding(.leading, 8)
                }

                Text("Please enter the secret seeds for these accounts above and invoke again.")
                    .font(.system(size: 12))
                    .italic()
                    .foregroundStyle(Material3Colors.onErrorContainer)
            }
        }
    }

    private func errorCard(message: String) -> some View {
        InfoCard(title: "Error", color: .error) {
            HStack(spacing: 8) {
                Image(systemName: "exclamationmark.triangle.fill")
                    .font(.system(size: 16))
                    .foregroundStyle(Material3Colors.onErrorContainer)

                Text(message)
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onErrorContainer)
            }
        }
    }

    @ViewBuilder
    private var placeholderView: some View {
        if loadedContract == nil && !isLoadingContract && contractId.isEmpty {
            VStack(spacing: 16) {
                Image(systemName: "cube.transparent")
                    .font(.system(size: 64))
                    .foregroundStyle(Material3Colors.onSurfaceVariant.opacity(0.3))

                Text("Enter a contract ID to load a token contract and interact with its functions")
                    .font(.system(size: 14))
                    .foregroundStyle(Material3Colors.onSurfaceVariant)
                    .multilineTextAlignment(.center)
            }
            .padding(.vertical, 32)
        }
    }

    // MARK: - Actions

    private func loadContract() {
        guard !contractId.isEmpty else { return }

        if contractId.count != 56 || !contractId.hasPrefix("C") {
            validationErrors["contractId"] = "Contract ID must start with 'C' and be 56 characters"
            return
        }

        isLoadingContract = true
        loadError = nil
        loadedContract = nil
        selectedFunction = nil
        validationErrors = [:]

        Task {
            do {
                let result = try await bridgeWrapper.bridge.loadTokenContract(contractId: contractId)
                await MainActor.run {
                    if let loaded = result as? InvokeTokenResult.ContractLoaded {
                        loadedContract = loaded
                    } else if let error = result as? InvokeTokenResult.Error {
                        loadError = error.message
                    }
                    isLoadingContract = false
                }
            } catch {
                await MainActor.run {
                    loadError = "Failed to load contract: \(error.localizedDescription)"
                    isLoadingContract = false
                }
            }
        }
    }

    private func invokeFunction() {
        guard let contract = loadedContract, let function = selectedFunction else { return }

        validationErrors = [:]

        // Validate arguments
        for param in function.parameters {
            if functionArguments[param.name]?.isEmpty ?? true {
                validationErrors["arg_\(param.name)"] = "Required"
            }
        }

        // Validate source account and seeds for write functions
        if !function.isReadOnly {
            if sourceAccountId.isEmpty {
                validationErrors["sourceAccount"] = "Required"
            } else if !sourceAccountId.hasPrefix("G") || sourceAccountId.count != 56 {
                validationErrors["sourceAccount"] = "Invalid account ID"
            }

            if signerSeeds.allSatisfy({ $0.isEmpty }) {
                validationErrors["signers"] = "At least one signer required"
            }
        }

        if !validationErrors.isEmpty {
            return
        }

        isInvoking = true
        invocationResult = nil

        Task {
            do {
                let result = try await bridgeWrapper.bridge.invokeTokenFunction(
                    contractLoaded: contract,
                    functionName: function.name,
                    arguments: functionArguments,
                    sourceAccountId: function.isReadOnly ? nil : sourceAccountId,
                    signerSeeds: signerSeeds
                )
                await MainActor.run {
                    invocationResult = result
                    isInvoking = false
                }
            } catch {
                await MainActor.run {
                    invocationResult = InvokeTokenResult.Error(
                        message: "Failed to invoke function: \(error.localizedDescription)",
                        exception: error as? KotlinThrowable
                    )
                    isInvoking = false
                }
            }
        }
    }


    private func getExpectedSigners(functionName: String, args: [String: String]) -> [(String, String?)] {
        // Call the Kotlin function
        let kotlinResult = InvokeTokenContractKt.getExpectedSigners(functionName: functionName, args: args)

        // Convert NSArray to Swift array
        var result: [(String, String?)] = []
        for item in kotlinResult {
            if let pair = item as? KotlinPair {
                let paramName = pair.first as? String ?? ""
                let accountId = pair.second as? String
                result.append((paramName, accountId))
            }
        }
        return result
    }
}
