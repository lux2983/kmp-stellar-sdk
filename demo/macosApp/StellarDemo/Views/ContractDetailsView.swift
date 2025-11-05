import SwiftUI
import shared

struct ContractDetailsScreen: View {
    @ObservedObject var toastManager: ToastManager
    @State private var contractId = "CBNCMQU5VCEVFASCPT4CCQX2LGYJK6YZ7LOIZLRXDEVJYQB7K6UTQNWW"
    @State private var isFetching = false
    @State private var detailsResult: ContractDetailsResult?
    @State private var validationError: String?

    @EnvironmentObject var bridgeWrapper: MacOSBridgeWrapper

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                infoCard
                exampleCard
                inputField
                fetchButton
                resultView
                placeholderView
            }
            .padding(16)
        }
        .background(Material3Colors.surface)
        .navigationToolbar(title: "Fetch Smart Contract Details")
    }

    // MARK: - View Components

    private var infoCard: some View {
        InfoCard(title: "Soroban RPC: fetch and parse smart contract details", color: .secondary) {
            Text("Enter a contract ID to fetch its WASM bytecode from the network and parse the contract specification including metadata and function definitions.")
                .font(.system(size: 13))
                .foregroundStyle(Material3Colors.onSecondaryContainer)
        }
    }

    private var exampleCard: some View {
        InfoCard(title: "Example Testnet Contract", color: .primary) {
            VStack(alignment: .leading, spacing: 4) {
                Text("The contract ID field is pre-filled with a testnet contract ID.")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onPrimaryContainer)

                Text("You can use it as-is or replace it with your own contract ID.")
                    .font(.system(size: 13))
                    .foregroundStyle(Material3Colors.onPrimaryContainer)
            }
        }
    }

    private var inputField: some View {
        StellarTextField(
            label: "Contract ID",
            placeholder: "C...",
            text: $contractId,
            error: validationError,
            useMonospacedFont: true,
            backgroundColor: .white
        )
        .onChange(of: contractId) { _ in
            validationError = nil
            detailsResult = nil
        }
    }

    private var fetchButton: some View {
        LoadingButton(
            action: fetchDetails,
            isLoading: isFetching,
            isEnabled: !isFetching && !contractId.isEmpty,
            icon: "magnifyingglass",
            text: "Fetch Details",
            loadingText: "Fetching..."
        )
    }

    @ViewBuilder
    private var resultView: some View {
        if let result = detailsResult {
            switch result {
            case let success as ContractDetailsResult.Success:
                ContractInfoView(contractInfo: success.contractInfo)
            case let error as ContractDetailsResult.Error:
                ContractErrorView(error: error)
            default:
                EmptyView()
            }
        }
    }

    @ViewBuilder
    private var placeholderView: some View {
        if detailsResult == nil && !isFetching && contractId.isEmpty {
            VStack(spacing: 16) {
                Image(systemName: "curlybraces")
                    .font(.system(size: 64))
                    .foregroundStyle(Material3Colors.onSurfaceVariant.opacity(0.3))

                Text("Enter a contract ID to view its parsed specification")
                    .font(.system(size: 14))
                    .foregroundStyle(Material3Colors.onSurfaceVariant)
                    .multilineTextAlignment(.center)
            }
            .padding(.vertical, 32)
        }
    }

    // MARK: - Actions

    private func fetchDetails() {
        if let error = FormValidation.validateContractIdField(contractId) {
            validationError = error
            return
        }

        isFetching = true
        detailsResult = nil
        validationError = nil

        Task {
            do {
                let result = try await bridgeWrapper.bridge.fetchContractDetails(contractId: contractId)
                await MainActor.run {
                    detailsResult = result
                    isFetching = false
                }
            } catch {
                await MainActor.run {
                    detailsResult = ContractDetailsResult.Error(
                        message: "Failed to fetch contract details: \(error.localizedDescription)",
                        exception: error as? KotlinThrowable
                    )
                    isFetching = false
                }
            }
        }
    }
}
